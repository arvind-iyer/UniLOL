package com.unilol.comp4521.unilol

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.NonNull
import android.util.Base64
import android.view.View
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.storage.FileDownloadTask
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.*


class MainActivity : AppCompatActivity() {
    private val mStorage = FirebaseStorage.getInstance().getReference()
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        post_new_meme.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, MakeMemeActivity::class.java)
            startActivityForResult(intent, Activity.RESULT_CANCELED)
        })
    }

    fun uploadMeme(view: View) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
            && data != null && data.data != null ) {
            val uri = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                image_holder.setImageBitmap(bitmap)
                val memeRef = mStorage.child("images")
                val memeUUID = UUID.randomUUID().toString() + ".jpg"
                btn_upload.isClickable = true
                btn_upload.setOnClickListener {
                    memeRef.child(memeUUID)
                            .putFile(uri)
                            .addOnSuccessListener { taskSnapshot ->
                                val downloadUrl = taskSnapshot.downloadUrl
                                //TODO: Add database entry with this link

                            }
                            .addOnFailureListener { exception: Exception ->
                                exception.printStackTrace()
                            }
                }

            } catch (e : IOException) {
                e.printStackTrace()
            }
        }
    }

    fun downloadMeme(view: View) {

        val memeRef = mStorage.child("images")
        val localFile = File.createTempFile("images", "jpg")
        memeRef.getFile(localFile)
                .addOnSuccessListener(OnSuccessListener<FileDownloadTask.TaskSnapshot> {
                    // Successfully downloaded data to local file
                    // ...
                    val mbp = BitmapFactory.decodeFile(localFile.absolutePath)
                    image_holder.setImageBitmap(mbp)
                }).addOnFailureListener(OnFailureListener {
            // Handle failed download
            // ...
        })
    }
}
