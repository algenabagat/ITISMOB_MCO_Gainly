package com.itismob.s17.gainly

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ScrollView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.itismob.s17.gainly.ui.theme.GainlyTheme

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_page)
        setupClickListeners()
        scrollToTop()
    }

    private fun setupClickListeners() {
        val newWorkoutBtn = findViewById<Button>(R.id.newWorkoutBtn)
        newWorkoutBtn.setOnClickListener {
            showNewWorkoutDialog()
        }
    }

    private fun showNewWorkoutDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.new_workout_popup)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val closeBtn = dialog.findViewById<ImageButton>(R.id.closeBtn)

        closeBtn.setOnClickListener {
            dialog.dismiss() // closes the dialog
        }

        dialog.show()
    }


    private fun scrollToTop() {
        val scrollView = findViewById<ScrollView>(R.id.workoutSv)
        scrollView?.post {
            scrollView.scrollTo(0, 0)
        }
    }
}

