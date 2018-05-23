package com.unilol.comp4521.unilol

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_post_meme.*
import java.io.File
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unilol.comp4521.unilol.interfaces.Post
import com.unilol.comp4521.unilol.interfaces.User
import java.util.*
import kotlin.collections.ArrayList

class PostMemeActivity : AppCompatActivity() {

    private val tag = "PostMemeActivity"

    private var imagePath  = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_meme)

        imagePath = intent.extras?.getString("imagePath") ?: ""
        Log.i("postMeme", "path: $imagePath")

        val imgFile = File(imagePath)

        if (!imgFile.exists()) { return }

        val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
        postMeme_imageView.setImageBitmap(myBitmap)
        uploadMemeButton.setOnClickListener {
            uploadMeme()
        }
    }

    private fun uploadMeme() {
        if (uploadMeme_title.text.isBlank()) {
            Toast.makeText(this, "Title shouldn't be empty!",
                    Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Posting...",
                Toast.LENGTH_SHORT).show()

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference

        val file = Uri.fromFile(File(imagePath))
        val uuid = UUID.randomUUID().toString()
        val memeRef = storageRef.child("images/$uuid")
        var uploadTask = memeRef.putFile(file)

        Log.i(tag, "file to upload: $file, $memeRef")
        uploadTask.addOnFailureListener({
            // Handle unsuccessful uploads
            Toast.makeText(this, "Upload Meme failed. $it.message",
                    Toast.LENGTH_SHORT).show()
        }).addOnSuccessListener({
            val memeURL = it.downloadUrl
            Log.i(tag, "memeURL: $memeURL")
            postMeme(memeURL.toString())
        })
    }


    private fun postMeme(memeURL: String) {
        val title = uploadMeme_title.text.toString().trim()
        val description = uploadMeme_description.text.toString().trim()
        val tags = ArrayList(uploadMeme_tags.text.toString().split(",").map { it.trim() })
        val id: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val db = FirebaseFirestore.getInstance()

        val newPost = Post(
                title = title,
                upvotes = 0,
                url = memeURL,
                user_id = id,
                timestamp = Date(),
                description = description,
                tags = tags
        )

        db.collection("posts").add(newPost)
            .addOnSuccessListener {
                addPostToUser(it.id)
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
            }.addOnFailureListener {
                Toast.makeText(this, "Post meme failed. $it.message",
                        Toast.LENGTH_SHORT).show()
            }
    }


    private fun addPostToUser(postId: String){
        val db = FirebaseFirestore.getInstance()
        val userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val requestUser = db.collection("users").document(userId)

        requestUser.get().addOnCompleteListener({task->
            if(task.isSuccessful){
                val user = task.result.toObject(User::class.java)
                val userPosts = user!!.posts
                userPosts!!.add(postId) // Add the new post

                // Update to database
                requestUser.update("posts", userPosts).addOnCompleteListener({task_inner->
                    if(task_inner.isSuccessful){
                        Toast.makeText(this, "Post meme succeeded!",
                                Toast.LENGTH_SHORT).show()
                    }
                    else{
                        task_inner.exception!!.toast(this, 2)
                    }
                })

            }
            else{
                Toast.makeText(this, "Fail getting information",
                        Toast.LENGTH_SHORT).show()
            }
        })
    }
}
