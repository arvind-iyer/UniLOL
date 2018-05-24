/*
# COMP 4521 # IYER ARVIND   20218689    aiyer@connect.ust.hk
# COMP 4521 # RYAN BUDI     20214841    bryanaa@connect.ust.hk
# COMP 4521 # JI TONY WAI SUM 20217881  twsji@connect.ust.hk
*/
package com.unilol.comp4521.unilol.interfaces

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.like.LikeButton
import com.like.OnLikeListener
import com.squareup.picasso.Picasso
import com.unilol.comp4521.unilol.R
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by arvind on 1/5/18.
 */

data class Post(
        var id: String = "",
        var title: String = "",
        var upvotes: Int = 0,
        val url : String = "",
        val user_id : String = "",
        val timestamp: Date = Date(),
        val description: String  = "",
        val tags: ArrayList<String> = ArrayList(),
        var comments: ArrayList<Comment>? = null
)

fun searchPost(post: Post, query: String) : Boolean {
    return (post.title.toLowerCase().contains(query.toLowerCase())
            || post.description.toLowerCase().contains(query.toLowerCase())
            || post.tags.contains(query.toLowerCase()))
}

class PostAdapter(private val posts: ArrayList<Post>, val clickListener: (Post) -> Unit):
        RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById(R.id.post_title) as TextView
        val commentCount = itemView.findViewById<TextView>(R.id.comment_count)
        val imageView = itemView.findViewById(R.id.thumbnail) as ImageView
        val upvoteText = itemView.findViewById(R.id.count) as TextView
        val likeButton = itemView.findViewById<LikeButton>(R.id.upvote_button)
        val dislikeButton = itemView.findViewById<LikeButton>(R.id.downvote_button)
        private val currentUser = FirebaseAuth.getInstance().currentUser!!

        private val likeListener = {user : User, post: Post, vote : Int-> object: OnLikeListener {
            fun updatePost() {
                val postRef = FirebaseFirestore.getInstance()
                        .collection("posts")
                        .document(post.id)
                postRef.set(post)
                upvoteText.text = "${post.upvotes} upvotes"

            }

            fun updateUser() {
                val userRef = FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.id)
                userRef.set(user)

            }

            override fun liked(p0: LikeButton?) {
                if(vote == 1) {
                    if(dislikeButton.isLiked) {
                        post.upvotes += vote
                        dislikeButton.isLiked = false
                    }
                }
                else {
                    if(likeButton.isLiked) {
                        post.upvotes += vote
                        likeButton.isLiked = false
                    }
                }
                user.votes.posts[post.id] = vote
                post.upvotes += vote
                Log.d("like/", "${user.votes.posts}")
                Log.d("like/", "${post.upvotes}")
                // update database
                updatePost()
                updateUser()
            }

            override fun unLiked(p0: LikeButton?) {
                user.votes.posts.remove(post.id)
                post.upvotes -= vote
                updatePost()
                updateUser()
            }
        }}

        fun bind(post: Post, clickListener: (Post) -> Unit) {
            // Bind the click listener to title and image
            var user = User()
            val userRef = FirebaseFirestore.getInstance()
                    .collection("users")
                    .whereEqualTo("id", currentUser.uid).get()
            userRef.addOnCompleteListener {
                if(it.isSuccessful) {
                    for (u in it.result.take(1)){
                        user = u.toObject(User::class.java)
                        user.id = u.id
                        Log.d("user/", u.id)
                    }
                }
                when(user.votes.posts[post.id]) {
                    1 -> likeButton.isLiked = true
                    -1 -> dislikeButton.isLiked = true
                    else -> likeButton.isLiked = false
                }

                likeButton.setOnLikeListener(likeListener(user,post,1))

                dislikeButton.setOnLikeListener(likeListener(user, post, -1))
            }

            itemView.setOnClickListener {
                clickListener(post)
            }
            imageView.setOnClickListener{
                clickListener(post)
            }

            title.isSelected = true

            if(post.comments?.size == 1) {
                commentCount.text = "1 comment"
            }
            else if(post.comments != null){
                commentCount.text = "${post.comments?.size} comments"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.post_layout, parent, false) as View
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = posts[position].title
        Picasso.get().load(posts[position].url).into(holder.imageView)
        holder.upvoteText.text = "${posts[position].upvotes} upvotes"

        holder.bind(posts[position], clickListener)
    }

    override fun getItemCount(): Int = posts.size
}
