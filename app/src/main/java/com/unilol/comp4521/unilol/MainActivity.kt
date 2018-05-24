/*
# COMP 4521 # IYER ARVIND   20218689    aiyer@connect.ust.hk
# COMP 4521 # RYAN BUDI     20214841    bryanaa@connect.ust.hk
# COMP 4521 # JI TONY WAI SUM 20217881  twsji@connect.ust.hk
*/
package com.unilol.comp4521.unilol

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
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
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.unilol.comp4521.unilol.interfaces.Comment
import com.unilol.comp4521.unilol.interfaces.Post
import com.unilol.comp4521.unilol.interfaces.PostAdapter
import com.unilol.comp4521.unilol.interfaces.searchPost
import kotlinx.android.synthetic.main.activity_main.*
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
        progress_loader.getIndeterminateDrawable().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
        mDrawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->

            when(menuItem.itemId){
                R.id.my_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("@string/user_id", mAuth.currentUser?.uid ?: "")
                    startActivity(intent)
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
                }
                R.id.logout-> {
                    mAuth.signOut()
                    "Signed out".toast(this, 2)
                    startActivity(Intent(this, LoginActivity::class.java))
                    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
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


    fun performSearch(query : String) {
        posts.clear()
        progress_loader.visibility = ProgressBar.VISIBLE
        Log.d("Query", "q: ${query}")
        mDB.collection("posts").orderBy("upvotes", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener({ task ->
                    if (task.isSuccessful) {
                        for(p in task.result){
                            val post = p.toObject(Post::class.java)
                            if(searchPost(post, query))
                                posts.add(post)
                                println("Matched ${post.id}")
                            }
                        }
                        viewAdapter.notifyDataSetChanged()
                        progress_loader.visibility = ProgressBar.INVISIBLE

                })
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val mSearch = menu?.findItem(R.id.search_bar) as MenuItem
        val mSearchView = mSearch.actionView as SearchView

        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String) : Boolean{
                performSearch(query)
                return false
            }

            override fun onQueryTextChange(newText: String) : Boolean{
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    posts.removeIf { post -> !searchPost(post, newText) }
                    viewAdapter.notifyDataSetChanged()
                }
                return true
            }
        })



        return super.onCreateOptionsMenu(menu)
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
        progress_loader.visibility = ProgressBar.VISIBLE
        mDB.collection("posts").orderBy("upvotes", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener({ task ->
                if( task.isSuccessful ) {
                    for(q in task.result){
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
                    for(post in posts){
                        mDB.collection("posts").document(post.id)
                                .collection("comments").get()
                        .addOnCompleteListener({ subtask ->
                            if (subtask.isSuccessful) {
                                post.comments = ArrayList<Comment>()
                                for(comment in subtask.result){
                                    post.comments?.add(comment.toObject(Comment::class.java))
                                }
                            }
                            viewAdapter.notifyDataSetChanged()
                        })
                    }
                    progress_loader.visibility = ProgressBar.INVISIBLE

                }
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
