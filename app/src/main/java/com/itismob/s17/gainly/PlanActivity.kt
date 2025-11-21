package com.itismob.s17.gainly

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.layout.add
import androidx.compose.ui.semantics.text
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.io.path.Path

class PlanActivity : BaseActivity(), DatePickerFragment.OnDateSelectedListener, TimePickerFragment.OnTimeSelectedListener {

    private lateinit var planAdapter: PlanAdapter
    private val planList: ArrayList<Plan> by lazy {
        PlanStorageManager.loadPlans(this)
    }
    private lateinit var notificationHelper: NotificationHelper
    private var selectedWorkoutForPlan: Workout? = null
    private var dateButtonToUpdate: Button? = null
    private var timeButtonToUpdate: Button? = null
    private val NOTIFICATION_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plan_layout)
        notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()
        requestNotificationPermission()
        setupRecyclerView()
        setupClickListeners()
        planList.sortBy { it.year * 10000 + (it.month + 1) * 100 + it.day }
        planAdapter.updatePlans(planList)
    }

    private fun addPlan(plan: Plan) {
        planList.add(plan)
        planList.sortBy { it.year * 10000 + (it.month + 1) * 100 + it.day }
        PlanStorageManager.savePlans(this, planList)
        notificationHelper.scheduleNotification(plan)
        planAdapter.updatePlans(planList)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.futureWorkoutsRv)
        planAdapter = PlanAdapter(
            plans = planList,
            onStartPlan = { plan ->
                startPlan(plan)
            },
            onEditPlan = { plan ->
                editPlan(plan)
            },
            onDeletePlan = { plan ->
                deletePlan(plan)
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
        val selectedTime = String.format("%02d:%02d", hour, minute)
        timeButtonToUpdate?.text = selectedTime
    }

    private fun showCreatePlanDialog(planToEdit: Plan? = null) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.new_plan_popup)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val closeBtn = dialog.findViewById<ImageButton>(R.id.closePlanBtn)
        val createPlanBtn = dialog.findViewById<Button>(R.id.createPlanBtn)
        val pickTime = dialog.findViewById<Button>(R.id.pickTimeBtn)
        val pickDateBtn = dialog.findViewById<Button>(R.id.pickDateBtn)
        val pickWorkoutBtn = dialog.findViewById<Button>(R.id.addWorkoutBtn)

        dateButtonToUpdate = pickDateBtn
        timeButtonToUpdate = pickTime

        if (planToEdit != null) {
            selectedWorkoutForPlan = planToEdit.workout
            pickWorkoutBtn.text = planToEdit.workout.name
            pickDateBtn.text = "${planToEdit.month + 1}/${planToEdit.day}/${planToEdit.year}"
            pickTime.text = String.format("%02d:%02d", planToEdit.hour, planToEdit.minute)
            createPlanBtn.text = "Update Plan"
        } else {
            selectedWorkoutForPlan = null
            createPlanBtn.text = "Create Plan"
        }

        pickDateBtn.setOnClickListener {
            DatePickerFragment().show(supportFragmentManager, "datePicker")
        }

        pickTime.setOnClickListener {
            TimePickerFragment().show(supportFragmentManager, "timePicker")
        }

        pickWorkoutBtn.setOnClickListener {
            showWorkoutSelectionDialog { workout ->
                selectedWorkoutForPlan = workout
                pickWorkoutBtn.text = workout.name
            }
        }

        closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        createPlanBtn.setOnClickListener {
            val currentSelectedWorkout = selectedWorkoutForPlan
            if (dateButtonToUpdate?.text.isNullOrEmpty() || timeButtonToUpdate?.text.isNullOrEmpty()) {
                Toast.makeText(this, "Please select a date and time.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (currentSelectedWorkout == null) {
                Toast.makeText(this, "Please select a workout to schedule.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dateParts = dateButtonToUpdate?.text.toString().split("/")
            val timeParts = timeButtonToUpdate?.text.toString().split(":")

            val year = dateParts[2].toInt()
            val month = dateParts[0].toInt() - 1
            val day = dateParts[1].toInt()
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()

            val newPlan = if (planToEdit != null) {
                planToEdit.copy(
                    year = year,
                    month = month,
                    day = day,
                    hour = hour,
                    minute = minute,
                    workout = currentSelectedWorkout
                )
            } else {
                Plan(
                    year = year,
                    month = month,
                    day = day,
                    hour = hour,
                    minute = minute,
                    workout = currentSelectedWorkout
                )
            }

            if (planToEdit != null) {
                val index = planList.indexOfFirst { it.id == planToEdit.id }
                if (index != -1) {
                    notificationHelper.cancelNotification(planToEdit)

                    planList[index] = newPlan
                    planList.sortBy { it.year * 10000 + (it.month + 1) * 100 + it.day }
                    PlanStorageManager.savePlans(this, planList)

                    notificationHelper.scheduleNotification(newPlan)

                    planAdapter.updatePlans(planList)
                    Toast.makeText(this, "Plan for '${newPlan.workout.name}' updated!", Toast.LENGTH_SHORT).show()
                }
            } else {
                addPlan(newPlan)
            }

            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            dateButtonToUpdate = null
            timeButtonToUpdate = null
        }

        dialog.show()
    }

    private fun showWorkoutSelectionDialog(onWorkoutSelected: (Workout) -> Unit) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_select_workout)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.workoutsRecyclerView)

        val workouts = WorkoutDataManager.workouts

        if (workouts.isEmpty()) {
            Toast.makeText(this, "No workouts found. Create a workout first.", Toast.LENGTH_LONG).show()
            return
        }

        val adapter = WorkoutSelectionAdapter(workouts) { selectedWorkout ->
            onWorkoutSelected(selectedWorkout)
            dialog.dismiss()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        dialog.show()
    }

    private fun startPlan(plan: Plan) {
        val workoutPosition = WorkoutDataManager.workouts.indexOfFirst { it.id == plan.workout.id }

        if (workoutPosition != -1) {
            val intent = Intent(this, WorkoutTrackingActivity::class.java)
            intent.putExtra("workout_position", workoutPosition)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Could not find workout to start.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editPlan(plan: Plan) {
        showCreatePlanDialog(planToEdit = plan)
    }

    private fun deletePlan(plan: Plan) {
        planList.removeAll { it.id == plan.id }
        PlanStorageManager.savePlans(this, planList)
        notificationHelper.cancelNotification(plan)
        planAdapter.updatePlans(planList)
        Toast.makeText(this, "Plan for '${plan.workout.name}' deleted.", Toast.LENGTH_SHORT).show()
    }
}