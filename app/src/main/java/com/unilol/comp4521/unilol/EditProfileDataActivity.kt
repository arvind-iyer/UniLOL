package com.unilol.comp4521.unilol

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_edit_profile_data.*

class EditProfileDataActivity : AppCompatActivity() {

    private var mode: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile_data)

//        imagePath = intent.extras?.getString("imagePath") ?: ""
        mode = intent.extras?.getInt("mode") ?: 0
        updateButton.setOnClickListener { updateData() }
        updateUI()
    }

    private fun updateUI() {
        when (mode) {
            EDIT_FULLNAME -> {
                textInputLayout.hint = "Your Full Name"
            }
            EDIT_SCHOOL -> {
                textInputLayout.hint = "Your School"
            }
            EDIT_STATUS -> {
                textInputLayout.hint = "Your Current Status"
            }
            else -> {
                Log.e(tag, "section $mode not implemented")
            }
        }
    }

    private fun updateData() {
        val data = inputTextField.text.toString()
        val userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val db = FirebaseFirestore.getInstance()
        val ref = db.collection("users").document(userId)

        when (mode) {
            EDIT_FULLNAME -> {
                if (data.isBlank()) {
                    Toast.makeText(this, "Full name shouldn't be empty!",
                            Toast.LENGTH_SHORT).show()
                    return
                }
                ref.update("fullName", data).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Full name successfully updated",
                                Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Update failed",
                                Toast.LENGTH_SHORT).show()
                    }
                }
            }
            EDIT_SCHOOL -> {
                ref.update("school", data).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "School successfully updated",
                                Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Update failed",
                                Toast.LENGTH_SHORT).show()
                    }
                }
            }
            EDIT_STATUS -> {
                ref.update("status", data).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Status successfully updated",
                                Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Update failed",
                                Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else -> {
                Log.e(tag, "section $mode not implemented")
            }
        }
    }

    companion object {
        const val tag = "EditProfileDataActivity"
        const val EDIT_FULLNAME = 0
        const val EDIT_SCHOOL = 1
        const val EDIT_STATUS = 2
    }
}
