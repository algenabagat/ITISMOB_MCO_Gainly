package com.itismob.s17.gainly

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        val backBtn = findViewById<ImageButton>(R.id.backBtn)

        backBtn.setOnClickListener {
            finish()
        }
    }
}