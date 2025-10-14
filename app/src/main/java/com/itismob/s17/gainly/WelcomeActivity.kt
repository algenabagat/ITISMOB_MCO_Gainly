package com.itismob.s17.gainly

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        val registerBtn = findViewById<Button>(R.id.registerBtn)
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val facebookBtn = findViewById<ImageButton>(R.id.facebookBtn)
        val instagramBtn = findViewById<ImageButton>(R.id.instagramBtn)


        registerBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        loginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        facebookBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        instagramBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}