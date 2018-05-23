package com.unilol.comp4521.unilol

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.unilol.comp4521.unilol.R.color.*
import com.unilol.comp4521.unilol.interfaces.Post
import com.unilol.comp4521.unilol.interfaces.User
import com.unilol.comp4521.unilol.interfaces.ProfilePostsAdapter
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    private val tag = "ProfileActivity"

    private val mDB = FirebaseFirestore.getInstance()
    private lateinit var adapter: ProfilePostsAdapter

    private lateinit var selfUserId: String
    private lateinit var followUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val incomingIntent = intent
        followUserId = try {
            incomingIntent.getStringExtra("@string/user_id")
        }
        catch(e: IllegalStateException){
            ""
        }
        selfUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        adapter = ProfilePostsAdapter(this, ArrayList<Post>(), { post: Post -> postItemClicked(post) })
        postsGridView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()

        refreshProfileData()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // When user presses back button, go back to previous MainActivity
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
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

        val db = FirebaseFirestore.getInstance()

        val requestProfile = db.collection("users").document(followUserId)
        val selfProfile = db.collection("users").document(selfUserId)
        requestProfile.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = task.result.toObject(User::class.java)!!
                actionbar!!.title = user.username
                profileFullName.text = user.fullName
                profileEmail.text = user.email
                profileSchool.text = user.school
                profileStatus.text = user.status
                numOfPosts.text = user.posts.size.toString()
                numOfFollowers.text = user.followers.size.toString()
                numOfFollowing.text = user.following.size.toString()
                Picasso.get().load(user.profilePictureUrl).into(profile_image)
                user.posts.forEach { postId ->
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

        if (selfUserId == followUserId) {
            // viewing self profile
            updateButtonStyle(RELATION_EDIT_PROFILE, {
                startActivity(Intent(this, ProfileSettingsActivity::class.java))
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
            })
        } else {
            // viewing others' profile
            selfProfile.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result.toObject(User::class.java)!!
                    if (user.following.contains(followUserId)) {
                        // following
                        if (user.followers.contains(followUserId)) {
                            // friends
                            updateButtonStyle(RELATION_FRIENDS, { unfollowUser() })
                        } else {
                            updateButtonStyle(RELATION_FOLLOWING, { unfollowUser() })
                        }
                    } else {
                        // not following
                        updateButtonStyle(RELATION_FOLLOW, { followUser() })
                    }
                }
            }
        }
    }

    private fun updateButtonStyle(style: Int, clickListener: () -> Unit) {
        when (style) {
            RELATION_FOLLOW -> {
                editProfile.text = "Follow"
                editProfile.setBackgroundColor(resources.getColor(colorPrimary))
            }
            RELATION_FOLLOWING -> {
                editProfile.text = "Following"
                editProfile.setBackgroundColor(resources.getColor(primary))
            }
            RELATION_FRIENDS -> {
                editProfile.text = "Friends"
                editProfile.setBackgroundColor(resources.getColor(primary))
            }
            RELATION_EDIT_PROFILE -> {
                editProfile.text = "Edit Profile"
                editProfile.setBackgroundColor(resources.getColor(colorPrimary))
            }
        }
        editProfile.setOnClickListener { clickListener() }
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

    private fun followUser() {
        if (selfUserId == followUserId) { return }
        val db = FirebaseFirestore.getInstance()
        val currentRef = db.collection("users").document(selfUserId)
        val followerRef = db.collection("users").document(followUserId)
        // Add to self's followings
        currentRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val self = task.result.toObject(User::class.java)!!
                val selfCurrentFollowing = self.following
                selfCurrentFollowing.add(followUserId)
                val followerNewFollower = ArrayList(selfCurrentFollowing.distinct())
                currentRef.update("following", followerNewFollower).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Followed User Successfully",
                                Toast.LENGTH_SHORT).show()
                        if (self.followers.contains(followUserId)) {
                            updateButtonStyle(RELATION_FRIENDS, { unfollowUser() })
                        } else {
                            updateButtonStyle(RELATION_FOLLOWING, { unfollowUser() })
                        }
                    } else {
                        Toast.makeText(this, "Followed User Failed",
                                Toast.LENGTH_SHORT).show()
                        updateButtonStyle(RELATION_FOLLOW, { followUser() })
                    }
                }
            }
        }

        // Add to follower's followers
        followerRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val follower = task.result.toObject(User::class.java)!!
                val followerCurrentFollowers = follower.followers
                followerCurrentFollowers.add(selfUserId)
                val followerNewFollower = ArrayList(followerCurrentFollowers.distinct())
                followerRef.update("followers", followerNewFollower).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        numOfFollowers.text = followerNewFollower.size.toString()
                    }
                }
            }
        }
    }

    private fun unfollowUser() {
        if (selfUserId == followUserId) { return }
        val db = FirebaseFirestore.getInstance()
        val currentRef = db.collection("users").document(selfUserId)
        val followerRef = db.collection("users").document(followUserId)
        currentRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val self = task.result.toObject(User::class.java)!!
                val selfCurrentFollowing = self.followers
                selfCurrentFollowing.remove(followUserId)
                currentRef.update("following", selfCurrentFollowing).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Unfollowed User Successfully",
                                Toast.LENGTH_SHORT).show()
                        updateButtonStyle(RELATION_FOLLOW, { followUser() })
                    } else {
                        Toast.makeText(this, "Unfollowed User Failed",
                                Toast.LENGTH_SHORT).show()
                        updateButtonStyle(RELATION_FOLLOWING, { unfollowUser() })
                    }
                }
            }
        }

        // Remove from follower's followers
        followerRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val follower = task.result.toObject(User::class.java)!!
                val followerCurrentFollowers = follower.followers
                followerCurrentFollowers.remove(selfUserId)
                followerRef.update("followers", followerCurrentFollowers).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        numOfFollowers.text = follower.followers.size.toString()
                    }
                }
            }
        }
    }

    companion object {
        private const val RELATION_FOLLOW = 0
        private const val RELATION_FOLLOWING = 1
        private const val RELATION_FRIENDS = 2
        private const val RELATION_EDIT_PROFILE = 3
    }
}
