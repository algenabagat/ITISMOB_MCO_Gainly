package com.itismob.s17.gainly

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult
import com.google.firebase.Firebase
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class WelcomeActivity : AppCompatActivity() {
    private lateinit var callbackManager: CallbackManager
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupClickListeners()

        // Initialize Facebook Login button
        callbackManager = CallbackManager.Factory.create()
        val loginButton = findViewById<com.facebook.login.widget.LoginButton>(R.id.login_button)
        auth = Firebase.auth

        loginButton.setReadPermissions("email", "public_profile")
        loginButton.registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    // Log.d(TAG, "facebook:onSuccess:$loginResult")
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {
                    // Log.d(TAG, "facebook:onCancel")
                }

                override fun onError(error: FacebookException) {
                    // Log.d(TAG, "facebook:onError", error)
                }
            },
        )
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        // Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    //Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    //Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI(null)
                }
            }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    fun updateUI(currentUser: FirebaseUser?) {
        if(currentUser!=null) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
//            Toast.makeText(
//                baseContext,
//                "Authentication failed.",
//                Toast.LENGTH_SHORT,
//            ).show()
        }
    }

    private fun setupClickListeners() {
        val registerBtn = findViewById<Button>(R.id.registerBtn)
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val facebookBtn = findViewById<ImageButton>(R.id.facebookBtn)
        val instagramBtn = findViewById<ImageButton>(R.id.instagramBtn)
        val loginButton = findViewById<com.facebook.login.widget.LoginButton>(R.id.login_button)


        registerBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        loginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        facebookBtn.setOnClickListener {
            loginButton.performClick();
            //startActivity(Intent(this, MainActivity::class.java))
        }

        instagramBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}