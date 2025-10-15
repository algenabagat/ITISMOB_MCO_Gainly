package com.itismob.s17.gainly

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import android.widget.Toast

class FinishWorkoutSummaryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.finish_workout_summary)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        val cameraBtn = findViewById<Button>(R.id.cameraBtn)
        val shareBtn = findViewById<ImageButton>(R.id.shareBtn)
        val finishWorkoutBtn = findViewById<Button>(R.id.finishWorkoutBtn)

        // camera logic/api placeholder
        cameraBtn.setOnClickListener {
            Toast.makeText(this, "Open Camera!", Toast.LENGTH_SHORT).show()
        }

        shareBtn.setOnClickListener {
            Toast.makeText(this, "Photo Shared!", Toast.LENGTH_SHORT).show()
        }

        finishWorkoutBtn.setOnClickListener {
            // back to mainactivity page
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}