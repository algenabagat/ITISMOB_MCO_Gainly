package com.itismob.s17.gainly

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        val registerBtn = findViewById<Button>(R.id.register2Btn)
        val goBackBtn = findViewById<Button>(R.id.gobackBtn)

        registerBtn.setOnClickListener {
            // goes to login after register
            startActivity(Intent(this, LoginActivity::class.java))
        }

        goBackBtn.setOnClickListener {
            // back to welcome screen
            finish()
        }
    }
}