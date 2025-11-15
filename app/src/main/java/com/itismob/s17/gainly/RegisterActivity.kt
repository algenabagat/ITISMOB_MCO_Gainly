package com.itismob.s17.gainly

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        val registerBtn = findViewById<Button>(R.id.register2Btn)
        val goBackBtn = findViewById<Button>(R.id.gobackBtn)
        val emailEt = findViewById<EditText>(R.id.emailEt)
        val passwordEt = findViewById<EditText>(R.id.passwordEt)
        val usernameEt = findViewById<EditText>(R.id.nameEt)

        registerBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()
            val username = usernameEt.text.toString().trim()

            if (validateInputs(email, password, username)) {
                registerUser(email, password, username)
            }
        }

        goBackBtn.setOnClickListener {
            finish() // back to welcome screen
        }
    }

    private fun validateInputs(email: String, password: String, username: String): Boolean {
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun registerUser(email: String, password: String, username: String) {
        val registerBtn = findViewById<Button>(R.id.register2Btn)
        registerBtn.isEnabled = false
        registerBtn.text = "Creating Account..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                        // just tries to save to firestore
                        try {
                            saveUserToFirestore(user.uid, email, username)
                        } catch (e: Exception) {
                            android.util.Log.e("RegisterActivity", "Firestore save failed: ${e.message}")
                        }

                        Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_SHORT).show()
                        navigateToLogin()
                    }
                } else {
                    registerBtn.isEnabled = true
                    registerBtn.text = "Register"
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserToFirestore(uid: String, email: String, username: String) {
        val userData = hashMapOf(
            "email" to email,
            "username" to username,
            "createdAt" to com.google.firebase.Timestamp.now(),
            // !! could store other data here
        )

        db.collection("users").document(uid).set(userData)
            .addOnFailureListener { e ->
                android.util.Log.e("RegisterActivity", "Firestore save failed: ${e.message}")
            }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}