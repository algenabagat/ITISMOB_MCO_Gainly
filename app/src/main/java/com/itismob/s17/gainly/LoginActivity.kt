package com.itismob.s17.gainly

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        val loginBtn = findViewById<Button>(R.id.login2Btn)
        val goBackBtn = findViewById<Button>(R.id.gobackBtn)

        loginBtn.setOnClickListener {
            // Navigate to main page after login
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        goBackBtn.setOnClickListener {
            // Go back to welcome screen
            finish()
        }
    }
}