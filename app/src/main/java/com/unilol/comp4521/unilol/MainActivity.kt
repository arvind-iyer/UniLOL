package com.unilol.comp4521.unilol

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.NonNull
import android.view.View
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.storage.FileDownloadTask
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {
    private val mStorage = FirebaseStorage.getInstance().getReference()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun downloadMeme(view: View) {

        val memeRef = mStorage.child("sad-person-looking-paper-bag-head-thumb-down-30565391.jpg")
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
