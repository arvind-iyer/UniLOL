package com.unilol.comp4521.unilol

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.android.synthetic.main.activity_login.*

/**
* Created by Budi Ryan on 02-Mar-18.
*/
class LoginActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_btn_login.setOnClickListener(View.OnClickListener {
            view -> login()
        })

        login_btn_register.setOnClickListener(View.OnClickListener {
            view -> register()
        })

    }

    private fun login () {
        val email = login_email.text.toString()
        val password = login_password.text.toString()
        val mAuth = FirebaseAuth.getInstance()

        if (!email.isEmpty() && !password.isEmpty()) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener ( this, {task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser.toString()
                    Toast.makeText(this, "Successfully Logged in, welcome $user!!", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, MainActivity::class.java).putExtra("username", user))
                } else {
                    val e = task.exception as FirebaseAuthException
                    Toast.makeText(this, "Failed Login: "
                            + e.message, Toast.LENGTH_LONG).show()
                }
            })

        }else {
            Toast.makeText(this, "Please fill up the Credentials :|", Toast.LENGTH_SHORT).show()
        }
    }

    private fun register () {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

}