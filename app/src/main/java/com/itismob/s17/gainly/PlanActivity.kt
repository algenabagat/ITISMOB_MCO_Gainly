package com.itismob.s17.gainly

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton

class PlanActivity : BaseActivity(), DatePickerFragment.OnDateSelectedListener, TimePickerFragment.OnTimeSelectedListener{

    private var dateButtonToUpdate: Button? = null
    private var timeButtonToUpdate: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plan_layout)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        val newPlanBtn = findViewById<Button>(R.id.newPlanBtn)

        newPlanBtn.setOnClickListener {
            showCreatePlanDialog()
        }
    }

    override fun onDateSelected(year: Int, month: Int, day: Int) {
        val selectedDate = "${month + 1}/$day/$year"
        dateButtonToUpdate?.text = selectedDate
    }

    override fun onTimeSelected(hour: Int, minute: Int) {
        val selectedTime = "$hour:$minute"
        timeButtonToUpdate?.text = selectedTime
    }

    private fun showCreatePlanDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.new_plan_popup)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val closeBtn = dialog.findViewById<ImageButton>(R.id.closePlanBtn)
        val createPlanBtn = dialog.findViewById<Button>(R.id.createPlanBtn)
        val pickTime = dialog.findViewById<Button>(R.id.pickTimeBtn)
        val pickDateBtn = dialog.findViewById<Button>(R.id.pickDateBtn)

        dateButtonToUpdate = pickDateBtn
        timeButtonToUpdate = pickTime

        pickDateBtn.setOnClickListener {
            DatePickerFragment().show(supportFragmentManager, "datePicker")
        }

        pickTime.setOnClickListener {
            TimePickerFragment().show(supportFragmentManager, "timePicker")
        }


        closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        createPlanBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            dateButtonToUpdate = null
            timeButtonToUpdate = null
        }

        dialog.show()
    }

}