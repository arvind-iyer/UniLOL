package com.unilol.comp4521.unilol

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.annotation.NonNull
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_post_meme.*
import java.io.File
import com.google.firebase.storage.UploadTask
import android.widget.Toast
import com.google.android.gms.tasks.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class PostMemeActivity : AppCompatActivity() {

    private val tag = "PostMemeActivity"

    private var imagePath  = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_meme)

        imagePath = intent.extras?.getString("imagePath") ?: ""
        Log.i("postMeme", "path: $imagePath")

        val imgFile = File(imagePath)

        if (imgFile.exists()) {
            val myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath())
            postMeme_imageView.setImageBitmap(myBitmap)
        }

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
        val id: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        // TODO: add tags?
//        val tags = uploadMeme_tags.text.toString().trim()

        val db = FirebaseFirestore.getInstance()
        val mAuth = FirebaseAuth.getInstance()
        val postObj = HashMap<String, Any>()

        postObj.put("time", FieldValue.serverTimestamp())
        postObj.put("title", title)
        postObj.put("upvotes", 0)
        postObj.put("url", memeURL)
        postObj.put("user_id", id)

        db.collection("posts").add(postObj)
            .addOnSuccessListener {
                Toast.makeText(this, "Post meme succeeded!",
                        Toast.LENGTH_SHORT).show()
                this.finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Post meme failed. $it.message",
                        Toast.LENGTH_SHORT).show()
            }
    }
}
