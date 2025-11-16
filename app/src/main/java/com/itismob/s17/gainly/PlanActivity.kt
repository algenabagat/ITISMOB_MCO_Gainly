package com.itismob.s17.gainly

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.io.path.Path

class PlanActivity : BaseActivity(), DatePickerFragment.OnDateSelectedListener, TimePickerFragment.OnTimeSelectedListener{

    private lateinit var planAdapter: PlanAdapter
    private val planList = ArrayList<Plan>()

    private var dateButtonToUpdate: Button? = null
    private var timeButtonToUpdate: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plan_layout)
        setupRecyclerView()
        setupClickListeners()
        addSamplePlans()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.futureWorkoutsRv)
        planAdapter = PlanAdapter(
            plans = planList,
            onStartPlan = {
                plan ->
                startPlan(plan)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = planAdapter
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

    private fun startPlan(plan: Plan) {
        val intent = Intent(this, WorkoutTrackingActivity::class.java)
        intent.putExtra("workout", plan.workoutId)
        startActivity(intent)
    }

    private fun addSamplePlans() {

        planAdapter.updatePlans(planList)
    }


}