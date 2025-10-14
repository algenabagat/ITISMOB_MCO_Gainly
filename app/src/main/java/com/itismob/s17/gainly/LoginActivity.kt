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
            // automatic navigate to mainactivity page
            startActivity(Intent(this, MainActivity::class.java))
        }

        goBackBtn.setOnClickListener {
            // goes back to either register or welcome page
            finish()
        }
    }
}