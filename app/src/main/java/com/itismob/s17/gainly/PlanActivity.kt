package com.itismob.s17.gainly

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton

class PlanActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plan_layout)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        val newPlanBtn = findViewById<Button>(R.id.newPlanBtn)
        val pickTimeBtn = findViewById<Button>(R.id.pickTime)
        val pickDateBtn = findViewById<Button>(R.id.pickDate)

        newPlanBtn.setOnClickListener {
            showCreatePlanDialog()
        }

//        pickTimeBtn.setOnClickListener {
//            TimePickerFragment().show(supportFragmentManager, "timePicker")
//        }
//
//        pickDateBtn.setOnClickListener {
//            val newFragment = DatePickerFragment()
//            newFragment.show(supportFragmentManager, "datePicker")
//        }
    }

    private fun showCreatePlanDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.new_plan_popup)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val closeBtn = dialog.findViewById<ImageButton>(R.id.closePlanBtn)
        val createPlanBtn = dialog.findViewById<Button>(R.id.createPlanBtn)


        closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        createPlanBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

}