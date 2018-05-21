package com.unilol.comp4521.unilol

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.unilol.comp4521.unilol.interfaces.Comment
import com.unilol.comp4521.unilol.interfaces.Post
import com.unilol.comp4521.unilol.interfaces.PostAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.*

fun Any.toast(context: Context, duration: Int = Toast.LENGTH_SHORT) : Toast {
    return Toast.makeText(context, this.toString(), duration).apply { show() }
}

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"

    private val mStorage = FirebaseStorage.getInstance().getReference()
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var recyclerView : RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager : RecyclerView.LayoutManager
    private val mDB = FirebaseFirestore.getInstance()
    private val mAuth = FirebaseAuth.getInstance()
    private val posts = ArrayList<Post>()

    private lateinit var mDrawerLayout: DrawerLayout

    private val mSwipeRefreshLayout: SwipeRefreshLayout by lazy {
        findViewById(R.id.swiperefresh) as SwipeRefreshLayout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Toolbar and actionbar stuff
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }
        mDrawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // set item as selected to persist highlight
            menuItem.isChecked = true

            when(menuItem.itemId){
                R.id.my_profile -> "GO TO MY PROFILE".toast(this, 1)
                R.id.logout-> {
                    mAuth.signOut()
                    "Signed out".toast(this, 2)
                    startActivity(Intent(this, LoginActivity::class.java))
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
                }
            }

            // close drawer when item is tapped
            mDrawerLayout.closeDrawers()
            // Add code here to update the UI based on the item selected
            // For example, swap UI fragments here
            true
        }

        // Swiperefrastesh
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary)
        mSwipeRefreshLayout.setOnRefreshListener(
                SwipeRefreshLayout.OnRefreshListener {
                   loadPosts()
                    mSwipeRefreshLayout.isRefreshing = false
                }
        )

        // Load all the memes upon Activity creation
        loadPosts()

        post_new_meme.setOnClickListener({
            val intent = Intent(this, MakeMemeActivity::class.java)
            startActivityForResult(intent, Activity.RESULT_CANCELED)
        })
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                mDrawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    fun loadPosts() {
        posts.clear()
        mDB.collection("posts").orderBy("upvotes", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener({ task ->
                if( task.isSuccessful ) {
                    task.result.forEach { q ->
                        println("Title: ${q.get("title")}")
                        val post = q.toObject(Post::class.java)
                        post.id = q.id
                        posts.add(post)

                    }


                    viewManager = LinearLayoutManager(this)
                    viewAdapter = PostAdapter(posts, { post: Post -> postItemClicked(post) })
                    recyclerView = memes_recycler.apply {
                        setHasFixedSize(true)
                        layoutManager = viewManager
                        adapter = viewAdapter
                    }
                    //Get comments
                    posts.forEach { post ->
                        mDB.collection("posts").document(post.id)
                                .collection("comments").get()
                        .addOnCompleteListener({ subtask ->
                            if (subtask.isSuccessful) {
                                post.comments = ArrayList<Comment>()
                                subtask.result.forEach {comment ->
                                    post.comments?.add(comment.toObject(Comment::class.java))
                                }
                            }
                            viewAdapter.notifyDataSetChanged()
                        })
                    }


                }
            })
    }
    fun uploadMeme(view: View) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        "Select a picture to upload".toast(view.context)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
            && data != null && data.data != null ) {
            val uri = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                image_holder.setImageBitmap(bitmap)
                val memeRef = mStorage.child("images")
                val memeUUID = UUID.randomUUID().toString() + ".jpg"
                btn_upload.isClickable = true
                btn_upload.setOnClickListener {
                    memeRef.child(memeUUID)
                            .putFile(uri)
                            .addOnSuccessListener { taskSnapshot ->
//                                val downloadUrl = taskSnapshot.downloadUrl
                                //TODO: Add database entry with this link

                            }
                            .addOnFailureListener { exception: Exception ->
                                exception.printStackTrace()
                            }
                }

            } catch (e : IOException) {
                e.printStackTrace()
            }
        }
    }


    fun downloadMeme(view: View) {

        val memeRef = mStorage.child("images")
        val localFile = File.createTempFile("images", "jpg")
        memeRef.getFile(localFile)
                .addOnSuccessListener({
                    // Successfully downloaded data to local file
                    // ...
                    val mbp = BitmapFactory.decodeFile(localFile.absolutePath)
                    image_holder.setImageBitmap(mbp)
                }).addOnFailureListener({
            // Handle failed download
            // ...
        })
    }

    private fun postItemClicked(post : Post) {
        // Activate the detailed meme view, and also view all comments regarding that post
        // Before switching activity request the username of the corresponding meme
        // Create another request to extract the username
        val requestUsername = mDB.collection("users").document(post.user_id)
        requestUsername.get().addOnCompleteListener({task ->
            if(task.isSuccessful) {
                val userObj = task.result.data
                val intent = Intent(this@MainActivity, DetailedMemeActivity::class.java)
                intent.putExtra("@string/post_id", post.id)
                intent.putExtra("@string/post_url", post.url)
                intent.putExtra("@stringArray/post_tags", post.tags)
                intent.putExtra("@int/post_upvotes", post.upvotes)
                intent.putExtra("@date/post_time", post.timestamp.time)
                intent.putExtra("@string/post_title", post.title)
                intent.putExtra("@string/post_description", post.description)
                intent.putExtra("@string/post_user_id", userObj!!.getValue("username").toString())
                startActivity(intent)
            }
            else{
                Log.d(TAG, "Error while collecting username! ${task.exception}")
            }
        })
    }
}
