package com.unilol.comp4521.unilol

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.unilol.comp4521.unilol.interfaces.Post
import com.unilol.comp4521.unilol.interfaces.Profile
import com.unilol.comp4521.unilol.interfaces.ProfilePostsAdapter
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    private val tag = "ProfileActivity"

    private val mDB = FirebaseFirestore.getInstance()
    private lateinit var adapter: ProfilePostsAdapter

    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val incomingIntent = intent
        try {
            userId = incomingIntent.getStringExtra("@string/user_id")
        }
        catch(e: IllegalStateException){
            userId = ""
        }

        adapter = ProfilePostsAdapter(this, ArrayList<Post>(), { post: Post -> postItemClicked(post) })
        postsGridView.adapter = adapter


        if (userId == FirebaseAuth.getInstance().currentUser?.uid ?: "" || userId == "") {
            editProfile.setOnClickListener {
                startActivity(Intent(this, ProfileSettingsActivity::class.java))
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
            }
        }
        else{
            editProfile.visibility = View.INVISIBLE
        }
    }

    override fun onResume() {
        super.onResume()

        refreshProfileData()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // When user presses back button, go back to previous MainActivity
        return when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this, MainActivity::class.java))
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun refreshProfileData() {
        // Toolbar and actionbar stuff --> places an actionbar with a back button
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        adapter.posts.clear()

        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val db = FirebaseFirestore.getInstance()

        val requestProfile = db.collection("users").document(userId)
        requestProfile.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val profile = task.result.toObject(Profile::class.java)!!
                val postIds = task.result["posts"] as ArrayList<String>
                actionbar!!.title = profile.username
                profileFullName.text = profile.fullName
                profileEmail.text = profile.email
                profileSchool.text = profile.school
                profileStatus.text = profile.status
                numOfPosts.text = postIds.size.toString()
                Picasso.get().load(profile.profilePictureUrl).into(profile_image)
                postIds.forEach { postId ->
                    val requestPost = db.collection("posts").document(postId)
                    requestPost.get().addOnCompleteListener { task2 ->
                        if (task2.isSuccessful) {
                            val post = task2.result.toObject(Post::class.java)!!
                            adapter.posts.add(post)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }

    private fun postItemClicked(post : Post) {
        val requestUsername = mDB.collection("users").document(post.user_id)
        requestUsername.get().addOnCompleteListener({task ->
            if(task.isSuccessful) {
                val userObj = task.result.data
                val intent = Intent(this, DetailedMemeActivity::class.java)
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
                Log.d(tag, "Error while collecting username! ${task.exception}")
            }
        })
    }
}
