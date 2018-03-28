package com.unilol.comp4521.unilol

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.android.synthetic.main.activity_forgot_password.*

/**
 * Created by Budi Ryan on 23-Mar-18.
 */
class ForgotPasswordActivity: AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        mAuth = FirebaseAuth.getInstance()

        link_login.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
        }

        btn_submit.setOnClickListener {
            val email = input_email.text.toString()

            if (validate()) {
                mAuth!!.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this@ForgotPasswordActivity, "Success, check your email!", Toast.LENGTH_SHORT).show()
                            } else {
                                val e = task.exception as FirebaseAuthException
                                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                            }
                        }
            }
        }

    }

    private fun validate(): Boolean {
        var valid = true
        val email = input_email.text.toString()
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            input_email.setError("enter a valid email address")
            valid = false
        } else {
            input_email.setError(null)
        }
        return valid;
    }
}