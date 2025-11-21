package com.itismob.s17.gainly

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var userNameTxv: TextView
    private lateinit var userEmailTxv: TextView
    private lateinit var changeNameTx: EditText
    private lateinit var changeEmailTx: EditText
    private lateinit var changePass1Tx: EditText
    private lateinit var changePass2Tx: EditText
    private lateinit var saveBtn: Button
    private lateinit var logoutBtn: Button
    private var isFacebookUser: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initializeViews()
        setupClickListeners()
        loadUserData()
    }

    private fun initializeViews() {
        userNameTxv = findViewById(R.id.userNameTxv)
        userEmailTxv = findViewById(R.id.userEmailTxv)
        changeNameTx = findViewById(R.id.changeNameTx)
        changeEmailTx = findViewById(R.id.changeEmailTx)
        changePass1Tx = findViewById(R.id.changePass1Tx)
        changePass2Tx = findViewById(R.id.changePass2Tx)
        saveBtn = findViewById(R.id.saveBtn)
        logoutBtn = findViewById(R.id.logoutBtn)
    }

    private fun setupClickListeners() {
        val backBtn = findViewById<android.widget.ImageButton>(R.id.backBtn)

        backBtn.setOnClickListener {
            finish()
        }

        saveBtn.setOnClickListener {
            saveChanges()
        }

        logoutBtn.setOnClickListener {
            logout()
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            // checks if user is logged in using facebook
            isFacebookUser = user.providerData.any { it.providerId == "facebook.com" }

            userEmailTxv.text = user.email ?: "No email"

            // disable email and password fields if user is logged in using facebook
            if (isFacebookUser) {
                disableFBFields()
            }

            if (user.displayName.isNullOrEmpty()) {
                getUserFirestore(user.uid)
            } else {
                userNameTxv.text = user.displayName
                changeNameTx.setText(user.displayName)
            }

            changeEmailTx.setText(user.email)

        } ?: run {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun disableFBFields() {
        // disable email/password fields
        changeEmailTx.isEnabled = false

        // Disable password fields for Facebook users
        changePass1Tx.isEnabled = false
        changePass1Tx.hint = "Cannot change password"

        changePass2Tx.isEnabled = false
        changePass2Tx.hint = "Cannot change password"

        // save
        saveBtn.text = "UPDATE PROFILE"
    }

    private fun getUserFirestore(uid: String) {
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username")
                    if (!username.isNullOrEmpty()) {
                        userNameTxv.text = username
                        changeNameTx.setText(username)
                    } else {
                        val displayName = auth.currentUser?.displayName
                        userNameTxv.text = displayName ?: "User"
                        changeNameTx.setText(displayName ?: "")
                    }
                } else {
                    val displayName = auth.currentUser?.displayName
                    userNameTxv.text = displayName ?: "User"
                    changeNameTx.setText(displayName ?: "")
                }
            }
            .addOnFailureListener {
                val displayName = auth.currentUser?.displayName
                userNameTxv.text = displayName ?: "User"
                changeNameTx.setText(displayName ?: "")
            }
    }

    private fun saveChanges() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val newName = changeNameTx.text.toString().trim()
        val newEmail = changeEmailTx.text.toString().trim()
        val newPassword1 = changePass1Tx.text.toString().trim()
        val newPassword2 = changePass2Tx.text.toString().trim()

        if (newName.isEmpty()) {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isFacebookUser && newEmail.isEmpty()) {
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
            return
        }

        if (isFacebookUser) {
            updateFBProfile(currentUser, newName)
            return
        }

        // for email/password users, validate email and password
        if (newEmail.isEmpty()) {
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
            return
        }

        // check if passwords match
        if (newPassword1.isNotEmpty() || newPassword2.isNotEmpty()) {
            if (newPassword1 != newPassword2) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return
            }

            if (newPassword1.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return
            }
        }

        saveBtn.isEnabled = false
        saveBtn.text = "Saving..."

        updateUserProfile(currentUser, newName, newEmail, newPassword1)
    }

    private fun updateFBProfile(user: com.google.firebase.auth.FirebaseUser, newName: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { profileTask ->
                if (profileTask.isSuccessful) {
                    updateFirestoreData(user.uid, newName, user.email ?: "")
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    saveBtn.isEnabled = true
                    saveBtn.text = "UPDATE PROFILE"
                    Toast.makeText(this, "Profile update failed: ${profileTask.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun updateUserProfile(user: com.google.firebase.auth.FirebaseUser, newName: String, newEmail: String, newPassword: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { profileTask ->
                if (profileTask.isSuccessful) {
                    if (newEmail != user.email) {
                        user.updateEmail(newEmail)
                            .addOnCompleteListener { emailTask ->
                                if (emailTask.isSuccessful) {
                                    if (newPassword.isNotEmpty()) {
                                        updatePassword(user, newPassword, newName, newEmail)
                                    } else {
                                        updateFirestoreData(user.uid, newName, newEmail)
                                    }
                                } else {
                                    saveBtn.isEnabled = true
                                    saveBtn.text = "SAVE CHANGES"
                                    Toast.makeText(this, "Email update failed: ${emailTask.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        if (newPassword.isNotEmpty()) {
                            updatePassword(user, newPassword, newName, newEmail)
                        } else {
                            updateFirestoreData(user.uid, newName, newEmail)
                        }
                    }
                } else {
                    saveBtn.isEnabled = true
                    saveBtn.text = "SAVE CHANGES"
                    Toast.makeText(this, "Profile update failed: ${profileTask.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun updatePassword(user: com.google.firebase.auth.FirebaseUser, newPassword: String, newName: String, newEmail: String) {
        user.updatePassword(newPassword)
            .addOnCompleteListener { passwordTask ->
                if (passwordTask.isSuccessful) {
                    updateFirestoreData(user.uid, newName, newEmail)
                } else {
                    saveBtn.isEnabled = true
                    saveBtn.text = "SAVE CHANGES"
                    Toast.makeText(this, "Password update failed: ${passwordTask.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun updateFirestoreData(uid: String, newName: String, newEmail: String) {
        val userData = hashMapOf(
            "username" to newName,
            "email" to newEmail,
            "updatedAt" to com.google.firebase.Timestamp.now(),
            "loginMethod" to if (isFacebookUser) "facebook" else "email"
        )

        db.collection("users").document(uid)
            .set(userData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                saveBtn.isEnabled = true
                saveBtn.text = if (isFacebookUser) "UPDATE PROFILE" else "SAVE CHANGES"
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                userNameTxv.text = newName
                if (!isFacebookUser) {
                    userEmailTxv.text = newEmail
                }

                changePass1Tx.text.clear()
                changePass2Tx.text.clear()
            }
            .addOnFailureListener { e ->
                saveBtn.isEnabled = true
                saveBtn.text = if (isFacebookUser) "UPDATE PROFILE" else "SAVE CHANGES"

                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                userNameTxv.text = newName
                if (!isFacebookUser) {
                    userEmailTxv.text = newEmail
                }

                changePass1Tx.text.clear()
                changePass2Tx.text.clear()

                android.util.Log.e("ProfileActivity", "Firestore sync failed but Auth updated: ${e.message}")
            }
    }

    private fun logout() {
        // logout from firebase
        auth.signOut()

        // logout from fb
        if (isFacebookUser) {
            LoginManager.getInstance().logOut()
        }

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // back to WelcomeActivity
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}