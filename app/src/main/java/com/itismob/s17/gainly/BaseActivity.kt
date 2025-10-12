package com.itismob.s17.gainly

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    override fun setContentView(layoutResID: Int) {
        // Only use header/footer for main page and plan layout
        if (layoutResID == R.layout.main_page || layoutResID == R.layout.plan_layout) {
            val fullView = layoutInflater.inflate(R.layout.layout_header_footer, null)
            val frame = fullView.findViewById<FrameLayout>(R.id.content_frame)
            layoutInflater.inflate(layoutResID, frame, true)
            super.setContentView(fullView)
            setupHeader()
//            setupBottomNav()
        } else {
            // For other activities, use the layout directly without header/footer
            super.setContentView(layoutResID)
        }
    }

    private fun setupHeader() {
        findViewById<View>(R.id.imageButton8)?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

//    private fun setupBottomNav() {
//        findViewById<View>(R.id.imageButton5)?.setOnClickListener {
//            startActivity(Intent(this, HistoryActivity::class.java))
//        }
//        findViewById<View>(R.id.imageButton6)?.setOnClickListener {
//            startActivity(Intent(this, WorkoutActivity::class.java))
//        }
//        findViewById<View>(R.id.imageButton)?.setOnClickListener {
//            startActivity(Intent(this, ExerciseActivity::class.java))
//        }
//    }
}