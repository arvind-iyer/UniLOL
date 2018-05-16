package com.unilol.comp4521.unilol

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_detailed_meme.*

class DetailedMemeActivity: AppCompatActivity() {
    val TAG = "DetailedMemeActivity"

    private var postId: String? = null
    private var postURL: String? = null
    private var postTitle: String? = null
    private var postUserId: String? = null
    private var postUpvotes: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_meme)
        getMemeInformation()
        Toast.makeText(this, "Title: ${postTitle} UserId: ${postUserId} Upvote:${postUpvotes}!", Toast.LENGTH_LONG).show()

        setMemeInformation()
    }


    private fun getMemeInformation(){
        val incomingIntent = intent
        postId = incomingIntent.getStringExtra("@string/post_thumbnail")
        postURL = incomingIntent.getStringExtra("@string/post_url")
        postTitle = incomingIntent.getStringExtra("@string/post_title")
        postUserId = incomingIntent.getStringExtra("@string/post_user_id")
        postUpvotes = incomingIntent.getIntExtra("@int/post_upvotes", 0)
    }

    private fun setMemeInformation(){
        post_title.setText(postTitle)
        post_author.setText(postUserId)
        post_upvotes.setText("${postUpvotes.toString()} upvotes")
        Picasso.get().load(postURL).into(post_thumbnail)
    }
}