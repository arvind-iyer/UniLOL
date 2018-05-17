package com.unilol.comp4521.unilol

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_detailed_meme.*
import java.util.ArrayList
import android.util.Log
import android.view.View
import android.widget.*
import com.ceylonlabs.imageviewpopup.ImagePopup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.HashMap


class DetailedMemeActivity: AppCompatActivity() {
    val TAG = "DetailedMemeActivity"

    private var postId: String? = null
    private var postURL: String? = null
    private var postTitle: String? = null
    private var postUserId: String? = null
    private var postUpvotes: Int? = null

    private var comments: ArrayList<Comment>? = null
    private var mProgressBar: ProgressBar? = null
    private var progressText: TextView? = null
    private var mListView: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_meme)

        mProgressBar = findViewById(R.id.comments_loading_progressbar) as ProgressBar

        val btnReply = findViewById(R.id.btn_post_reply) as Button

        btnReply.setOnClickListener {
            postUserComment()
        }

        getMemeInformation()

        setMemeInformation()

        displayComments()
    }


    private fun getMemeInformation(){
        // Store all data coming from the intent from MainActivity
        val incomingIntent = intent
        postId = incomingIntent.getStringExtra("@string/post_id")
        postURL = incomingIntent.getStringExtra("@string/post_url")
        postTitle = incomingIntent.getStringExtra("@string/post_title")
        postUserId = incomingIntent.getStringExtra("@string/post_user_id")
        postUpvotes = incomingIntent.getIntExtra("@int/post_upvotes", 0)
    }

    private fun setMemeInformation(){
        // Set meme information above all the comments
        post_title.setText(postTitle)
        post_author.setText(postUserId)
        post_upvotes.setText("${postUpvotes.toString()} upvotes")
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
        val requestComments = db.collection("posts").document(postId!!).collection("comments")
        requestComments.get().addOnCompleteListener({ task ->
                    if( task.isSuccessful ) {
                        mListView = findViewById(R.id.comments_list_view) as ListView
                        for (document in task.result) {
                            val commentObj = document.data

                            // Create another request to extract the username
                            val requestUsername = db.collection("users").document(commentObj.getValue("user_id").toString())
                            requestUsername.get().addOnCompleteListener({task ->
                                if(task.isSuccessful) {
                                    val userObj = task.result.data
                                    comments!!.add(Comment(
                                            commentObj.getValue("message").toString(),
                                            userObj!!.getValue("username").toString(),
                                            commentObj.getValue("upvotes").toString().toInt(),
                                            commentObj.getValue("time").toString()
                                    ))
                                    val adapter = CommentsListAdapter(this@DetailedMemeActivity, R.layout.comment_layout, comments!!)

                                    mListView!!.setAdapter(adapter)
                                }

                                else{
                                    Log.d(TAG, "Error while collecting username! ${task.exception}")
                                }
                            })

                        }

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
            val commentObj = HashMap<String, Any>()
            commentObj.put("message", comment.text.toString())
            commentObj.put("time", FieldValue.serverTimestamp())
            commentObj.put("upvotes", 0)
            commentObj.put("user_id", mAuth.currentUser?.uid!!)

            db.collection("posts").document(postId!!).collection("comments")
                    .add(commentObj)
                    .addOnSuccessListener {
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