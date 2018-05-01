package com.unilol.comp4521.unilol

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso

/**
 * Created by arvind on 1/5/18.
 */

data class Post(
        val id: String = "",
        val title: String = "",
        val upvotes: Int = 0,
        val url : String = "",
        val user_id : String = ""
)

public class PostAdapter(private val posts: ArrayList<Post>) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {
    class ViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        val title = view.findViewById(R.id.post_title) as TextView
        val imageView = view.findViewById(R.id.thumbnail) as ImageView
        val upvoteText = view.findViewById(R.id.count) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.post_layout, parent, false) as View
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = posts[position].title
        Picasso.get().load(posts[position].url).into(holder.imageView)
        holder.upvoteText.text = "${posts[position].upvotes} upvotes"
    }

    override fun getItemCount(): Int = posts.size
}