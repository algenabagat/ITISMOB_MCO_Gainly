package com.itismob.s17.gainly

import android.os.Bundle
import android.widget.FrameLayout
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

open class BaseActivity : AppCompatActivity() {

    override fun setContentView(layoutResID: Int) {
        val fullView = layoutInflater.inflate(R.layout.layout_header_footer, null)
        val frame = fullView.findViewById<FrameLayout>(R.id.content_frame)
        layoutInflater.inflate(layoutResID, frame, true)
        super.setContentView(fullView)
        setupBottomNav()
    }

    private fun setupBottomNav() {
        /*
        findViewById<View>(R.id.nav_history).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        findViewById<View>(R.id.nav_workout).setOnClickListener {
            startActivity(Intent(this, WorkoutActivity::class.java))
        }
        findViewById<View>(R.id.nav_exercise).setOnClickListener {
            startActivity(Intent(this, ExerciseActivity::class.java))
        }
        */
    }
}

