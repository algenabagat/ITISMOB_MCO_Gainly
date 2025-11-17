package com.itismob.s17.gainly

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File

class FinishWorkoutSummaryActivity : AppCompatActivity() {

    private lateinit var capturedImageView: ImageView
    private var latestTmpUri: Uri? = null

    private val takeImageResult = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            latestTmpUri?.let { uri ->

                capturedImageView.setImageURI(uri)
                Toast.makeText(this, "Image captured!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Image capture cancelled.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.finish_workout_summary)

        capturedImageView = findViewById(R.id.capturedImageView)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        val cameraBtn = findViewById<Button>(R.id.cameraBtn)
        val shareBtn = findViewById<ImageButton>(R.id.shareBtn)
        val finishWorkoutBtn = findViewById<Button>(R.id.finishWorkoutBtn)

        cameraBtn.setOnClickListener {
            takeImage()
        }

        shareBtn.setOnClickListener {
            latestTmpUri?.let { uri ->
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, "Check out my workout on Gainly!")
                }
                startActivity(Intent.createChooser(shareIntent, "Share your workout photo"))
            } ?: run {
                Toast.makeText(this, "Take a picture first to share it!", Toast.LENGTH_SHORT).show()
            }
        }

        finishWorkoutBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun takeImage() {
        getTmpFileUri().let { uri ->
            latestTmpUri = uri
            takeImageResult.launch(uri)
        }
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(applicationContext, "${BuildConfig.APPLICATION_ID}.provider", tmpFile)
    }
}
