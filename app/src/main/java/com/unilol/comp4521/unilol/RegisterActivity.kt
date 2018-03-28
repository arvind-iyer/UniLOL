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

        btn_register.setOnClickListener(View.OnClickListener {
            view -> registerUser()
        })

        link_login.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(R.anim.push_left_out, R.anim.push_left_in)
        }
    }

    private fun registerUser () {
        val email = input_email.text.toString()
        val password = input_password.text.toString()
        if (validate()) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            val user = mAuth.currentUser.toString()
                            Toast.makeText(this, "Successfully signed up, you can login now", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
                        } else {
                            // If sign in fails, display a message to the user.
                            val e = task.exception as FirebaseAuthException
                            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                        }
                    }
        }
        else {
            Toast.makeText(this,"Login failed", Toast.LENGTH_LONG).show()
        }
    }

    private fun validate(): Boolean {
        var valid = true

        val email = input_email.text.toString()
        val password = input_password.text.toString()
        val confirmPassword = input_confirmPassword.text.toString()

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            input_email.setError("enter a valid email address")
            valid = false
        } else {
            input_email.setError(null)
        }

        if (password.isEmpty() || password.length < 4 || password.length > 10) {
            input_password.setError("between 4 and 10 alphanumeric characters")
            valid = false
        } else {
            input_password.setError(null)
        }

        if (confirmPassword.isEmpty() || confirmPassword.length < 4 || confirmPassword.length > 10 || confirmPassword != password) {
            input_confirmPassword.setError("Password Do not match")
            valid = false
        } else {
            input_confirmPassword.setError(null)
        }

        return valid;
    }
}