package com.unilol.comp4521.unilol

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.unilol.comp4521.unilol.interfaces.Post
import com.unilol.comp4521.unilol.interfaces.Profile
import com.unilol.comp4521.unilol.interfaces.ProfilePostsAdapter
import kotlinx.android.synthetic.main.activity_detailed_meme.*
import kotlinx.android.synthetic.main.activity_profile.*
import org.ocpsoft.prettytime.PrettyTime

class ProfileActivity : AppCompatActivity() {

    private val tag = "ProfileActivity"

    private val mDB = FirebaseFirestore.getInstance()
    private lateinit var adapter: ProfilePostsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        adapter = ProfilePostsAdapter(this, ArrayList<Post>(), { post: Post -> postItemClicked(post) })
        postsGridView.adapter = adapter
        getProfileData()
    }

    private fun getProfileData() {
        adapter.posts.clear()
        val userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val db = FirebaseFirestore.getInstance()

        val requestProfile = db.collection("users").document(userId)
        requestProfile.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val profile = task.result.toObject(Profile::class.java)!!
                val postIds = task.result["posts"] as ArrayList<String>

                profileName.text = profile.fullName
                profileEmail.text = profile.email
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
