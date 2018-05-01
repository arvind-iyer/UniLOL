package com.unilol.comp4521.unilol

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.android.synthetic.main.activity_login.*
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.FacebookCallback
import com.facebook.CallbackManager
import com.facebook.AccessToken
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.firestore.FirebaseFirestore


/**
* Created by Budi Ryan on 02-Mar-18.
*/
class LoginActivity : AppCompatActivity(){
    val TAG = "LoginActivity"

    // Initialize Facebook Login button
    lateinit var mCallbackManager: CallbackManager

    // Firebase Auth
    lateinit var mAuth: FirebaseAuth

    public override fun onStart() {
        super.onStart()
        mAuth = FirebaseAuth.getInstance()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            Toast.makeText(this, "Successfully signed up, welcome " +
                    "${currentUser.displayName} !", Toast.LENGTH_SHORT).show()
            // Don't directly switch to mainActivity for demo purpose
//            startActivity(Intent(this, MainActivity::class.java).putExtra("currentUser", currentUser))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mCallbackManager = CallbackManager.Factory.create()
        facebook_login_button.setReadPermissions("email", "public_profile")
        facebook_login_button.registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
                // ...
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
                // ...
            }
        })

        btn_login.setOnClickListener(View.OnClickListener {
            view -> loginEmailPassword()
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
        })

        link_forgot_password.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
        })

        link_signup.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
        })


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun loginEmailPassword () {
        val email = login_email.text.toString()
        val password = login_password.text.toString()
        mAuth = FirebaseAuth.getInstance()
        if (!email.isEmpty() && !password.isEmpty()) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener ( this, {task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    Toast.makeText(this, "Successfully Logged in, welcome ${user!!.email}!", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, MainActivity::class.java).putExtra("currentUser", user))
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
                } else {
                    val e = task.exception as FirebaseAuthException
                    Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                }
            })

        }else {
            Toast.makeText(this, "Please fill up the credentials!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:" + token)
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signin with facebook success!")
                        val currentUser = mAuth.currentUser
                        val displayName = currentUser!!.displayName
                        val fullName = displayName
                        val userObj = HashMap<String, String>()
                        userObj.put("firstName", fullName!!)

                        firestore.collection("users")
                                .document(mAuth.currentUser?.uid!!)
                                .set(userObj as Map<String, String>)

                        Toast.makeText(this@LoginActivity, "Succesfully logged in using Facebook, Welcome ${displayName} !",
                                Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "sign in with facebook failure", task.getException())
                        Toast.makeText(this@LoginActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

}