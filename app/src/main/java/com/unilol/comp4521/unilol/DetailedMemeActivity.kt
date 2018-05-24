package com.unilol.comp4521.unilol

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_detailed_meme.*
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.ceylonlabs.imageviewpopup.ImagePopup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*
import com.unilol.comp4521.unilol.interfaces.Comment
import com.unilol.comp4521.unilol.interfaces.CommentsListAdapter
import com.unilol.comp4521.unilol.interfaces.User
import org.ocpsoft.prettytime.PrettyTime
import kotlin.collections.ArrayList


class DetailedMemeActivity: AppCompatActivity() {
    val TAG = "DetailedMemeActivity"

    private var postId: String? = null
    private var postURL: String? = null
    private var postTitle: String? = null
    private var postDescription: String? = null
    private var postUserId: String? = null
    private var postUpvotes: Int? = null
    private var postTags: ArrayList<String>? = ArrayList()
    private var postTime: Date? = Date()

    private var comments: ArrayList<Comment>? = null
    private var mProgressBar: ProgressBar? = null
    private var mListView: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_meme)

        mProgressBar = findViewById(R.id.comments_loading_progressbar) as ProgressBar

        val btnReply = findViewById(R.id.btn_post_reply) as FloatingActionButton

        btnReply.setOnClickListener {
            postUserComment()
        }

        getMemeInformation()

        setMemeInformation()

        displayComments()
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


    private fun getMemeInformation(){
        // Store all data coming from the intent from MainActivity
        val incomingIntent = intent
        postId = incomingIntent.getStringExtra("@string/post_id")
        postURL = incomingIntent.getStringExtra("@string/post_url")
        postTitle = incomingIntent.getStringExtra("@string/post_title")
        postDescription = incomingIntent.getStringExtra("@string/post_description")
        postUserId = incomingIntent.getStringExtra("@string/post_user_id")
        postUpvotes = incomingIntent.getIntExtra("@int/post_upvotes", 0)
        postTags = incomingIntent.getStringArrayListExtra("@stringArray/post_tags")
        postTime!!.setTime(incomingIntent.getLongExtra("@date/post_time", -1));

        // Toolbar and actionbar stuff --> places an actionbar with a back button
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        actionbar!!.title = postTitle
    }

    private fun setMemeInformation(){
        // Set meme information above all the comments
        post_description.setText(postDescription)
        post_author.setText(postUserId)
        post_upvotes.setText("${postUpvotes.toString()} upvotes")
        post_tags.setText("Tag(s): ${postTags!!.joinToString()}")
        post_time.setText(PrettyTime().format(postTime))

        Picasso.get().load(postURL).into(post_thumbnail)
        val imagePopup = ImagePopup(this);
        imagePopup.setImageOnClickClose(true);  // Optional
        post_thumbnail.setOnClickListener({
            imagePopup.initiatePopup(post_thumbnail.drawable)
            imagePopup.viewPopup();
        })
    }

    private fun displayComments(){
        // Retrieve all comments from the selected post
        val db = FirebaseFirestore.getInstance()
        comments = ArrayList<Comment>()
        val requestComments = db.collection("posts").document(postId!!)
                .collection("comments")
                .orderBy("upvotes", Query.Direction.DESCENDING)
        requestComments.get().addOnCompleteListener({ task ->
                    if( task.isSuccessful ) {
                        mListView = findViewById(R.id.comments_list_view) as ListView
                        task.result.forEach{commentSnapshot ->
                            val comment = commentSnapshot.toObject(Comment::class.java)
                            comments!!.add(Comment(
                                            commentSnapshot.id,
                                            comment.message,
                                            comment.user_id,
                                            comment.upvotes,
                                            comment.time
                                            ))
                        }
                        val adapter = CommentsListAdapter(this@DetailedMemeActivity,
                                            R.layout.comment_layout, comments!!, postId!!)
                        mListView!!.setAdapter(adapter)
                        mProgressBar!!.setVisibility(View.GONE)
                    }
                    else{
                        Log.d(TAG, "Error while collecting comments! ${task.exception}")
                    }
                })

    }

    private fun postUserComment(){
        // Given the post ID, post the user comment
        val dialog = Dialog(this@DetailedMemeActivity)
        dialog.setTitle("dialog")
        dialog.setContentView(R.layout.comment_input_dialog)

        val width = (resources.displayMetrics.widthPixels * 0.95).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.5).toInt()

        dialog.window!!.setLayout(width, height)
        dialog.show()

        val btnPostComment = dialog.findViewById(R.id.btn_post_comment) as Button
        val comment = dialog.findViewById(R.id.dialog_comment) as EditText

        btnPostComment.setOnClickListener {
            val db = FirebaseFirestore.getInstance()
            val mAuth = FirebaseAuth.getInstance()

            val newComment = Comment(
                    id = "",
                    message = comment.text.toString(),
                    user_id = mAuth.currentUser?.uid!!,
                    upvotes = 0,
                    time = Date())

            db.collection("posts").document(postId!!).collection("comments")
                    .add(newComment)
                    .addOnSuccessListener {
                        val commentId = it.id
                        db.collection("posts").document(postId!!).collection("comments").document(commentId).update("id", commentId)
                        Toast.makeText(applicationContext, "Post comment success!", Toast.LENGTH_SHORT).show()
                        displayComments()
                        dialog.dismiss()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(applicationContext, "Error adding comment: ${e}", Toast.LENGTH_SHORT).show()
                    }
        }
    }
}