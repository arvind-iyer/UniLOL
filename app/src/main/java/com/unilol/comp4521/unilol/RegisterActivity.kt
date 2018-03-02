package com.unilol.comp4521.unilol

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*
import com.google.firebase.auth.FirebaseAuthException


/**
* Created by Budi Ryan on 02-Mar-18.
*/
class RegisterActivity : AppCompatActivity(){
    val mAuth = FirebaseAuth.getInstance()
    lateinit var mDatabase : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mDatabase = FirebaseDatabase.getInstance().getReference("Names")

        register_btn_submit.setOnClickListener(View.OnClickListener {
            view -> registerUser()
        })

    }

    private fun registerUser () {

        val email = register_email.text.toString()
        val password = register_password.text.toString()
        val name = register_username.text.toString()

        if (!email.isEmpty() && !password.isEmpty() && !name.isEmpty()) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            val user = mAuth.currentUser.toString()
                            Toast.makeText(this, "Successfully signed up, welcome $user!!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java).putExtra("username", user))
                        } else {
                            // If sign in fails, display a message to the user.
                            val e = task.exception as FirebaseAuthException
                            Toast.makeText(this, "Failed Registration: "
                                    + e.message, Toast.LENGTH_LONG).show()
                        }
                    }
        }
        else {
            Toast.makeText(this,"Please fill up the Credentials :|", Toast.LENGTH_LONG).show()
        }
    }
}