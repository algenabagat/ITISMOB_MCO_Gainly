package com.itismob.s17.gainly

import android.content.Intent
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    override fun setContentView(layoutResID: Int) {
        if (layoutResID == R.layout.main_page || layoutResID == R.layout.plan_layout || layoutResID == R.layout.history_layout) {
            val fullView = layoutInflater.inflate(R.layout.layout_header_footer, null)
            val frame = fullView.findViewById<FrameLayout>(R.id.content_frame)
            layoutInflater.inflate(layoutResID, frame, true)
            super.setContentView(fullView)
            setupHeader()
            setupBottomNav()
        } else {
            super.setContentView(layoutResID)
        }
    }

    private fun setupHeader() {
        findViewById<View>(R.id.profileBtn)?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupBottomNav() {
        findViewById<View>(R.id.planBtn)?.setOnClickListener {
            if (this !is PlanActivity) {
                startActivity(Intent(this, PlanActivity::class.java))
                overridePendingTransition(0, 0)
                finish()
            }
        }

        findViewById<View>(R.id.workoutBtn)?.setOnClickListener {
            if (this !is MainActivity) {
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(0, 0)
                finish()
            }
        }

        findViewById<View>(R.id.historyBtn)?.setOnClickListener {
            if (this !is HistoryActivity) {
                startActivity(Intent(this, HistoryActivity::class.java))
                overridePendingTransition(0, 0)
                finish()
            }
        }
    }
}