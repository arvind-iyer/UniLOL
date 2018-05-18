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
import com.like.LikeButton
import com.like.OnLikeListener
import com.squareup.picasso.Picasso
import com.unilol.comp4521.unilol.R
import com.unilol.comp4521.unilol.toast
import java.util.*

/**
 * Created by arvind on 1/5/18.
 */

data class Post(
        val id: String = "",
        var title: String = "",
        var upvotes: Int = 0,
        val url : String = "",
        val user_id : String = "",
        val timestamp: Date = Date()
)

public class PostAdapter(private val posts: ArrayList<Post>, val clickListener: (Post) -> Unit):
        RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById(R.id.post_title) as TextView
        val imageView = itemView.findViewById(R.id.thumbnail) as ImageView
        val upvoteText = itemView.findViewById(R.id.count) as TextView
        val likeButton = itemView.findViewById<LikeButton>(R.id.upvote_button)
        val dislikeButton = itemView.findViewById<LikeButton>(R.id.downvote_button)
        private val currentUser = FirebaseAuth.getInstance().currentUser!!
        private val userRef = FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("id", currentUser.uid)


        fun bind(post: Post, clickListener: (Post) -> Unit) {
            // Bind the click listener to title and image
            var user = User()
            userRef.get()
                    .addOnCompleteListener {
                        if(it.isSuccessful) {
                            it.result.forEach { u ->
                                user = u.toObject(User::class.java)
                            }
                        }
                    }
            if (user.id.equals("")) {
                return
            }

            itemView.setOnClickListener {
                clickListener(post)
            }
            imageView.setOnClickListener{
                clickListener(post)
            }

            likeButton.setOnLikeListener(object: OnLikeListener {
                override fun liked(p0: LikeButton?) {
                    dislikeButton.isLiked = false
                    "Liked post ${post.id}".toast(itemView.context)
                    user.votes.posts[post.id] = 1
                    post.upvotes += 1
                    Log.d("like/", "${user.votes.posts}")
                    Log.d("like/", "${post.upvotes}")
                }

                override fun unLiked(p0: LikeButton?) {
                    "Unliked post ${post.id}".toast(itemView.context)
                    user.votes.posts.remove(post.id)
                }
            })

            dislikeButton.setOnLikeListener(object: OnLikeListener {
                override fun liked(p0: LikeButton?) {
                    likeButton.isLiked = false
                    "Disliked post ${post.id}".toast(itemView.context)
                    user.votes.posts[post.id] = -1
                }

                override fun unLiked(p0: LikeButton?) {
                    "Undisliked post ${post.id}".toast(itemView.context)
                    user.votes.posts.remove(post.id)
                }
            })
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
        (holder as ViewHolder).bind(posts[position], clickListener)
    }

    override fun getItemCount(): Int = posts.size
}