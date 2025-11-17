package com.itismob.s17.gainly

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.io.path.Path

class PlanActivity : BaseActivity(), DatePickerFragment.OnDateSelectedListener, TimePickerFragment.OnTimeSelectedListener{

    private lateinit var planAdapter: PlanAdapter
    private val planList = ArrayList<Plan>()
    private lateinit var notificationHelper: NotificationHelper

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
        addSamplePlans()
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
        val workoutPosition = WorkoutDataManager.workouts.indexOf(plan.workout)
        if (workoutPosition != -1) {
            val intent = Intent(this, WorkoutTrackingActivity::class.java)
            intent.putExtra("workout_position", workoutPosition)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Workout not found!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addSamplePlans() {
        planList.add(
            Plan(
                year = 2025,
                month = 11,
                day = 25,
                hour = 21,
                minute = 0,
                workout =
                    Workout(
                        id = "sample_leg_day",
                        name = "Leg Day",
                        description = "Complete lower body workout",
                        exercises = listOf(
                            Exercise(id = "ex_squats", name = "Squats", description = "...", targetMuscle = "Quadriceps, Glutes", defaultSets = 4, defaultReps = 12),
                            Exercise(id = "ex_rdl", name = "Romanian Deadlift", description = "...", targetMuscle = "Hamstrings, Glutes", defaultSets = 3, defaultReps = 10)
                        ),
                        createdBy = "Gainly"
                    )
            )
        )

        planList.add(
            Plan(
                year = 2025,
                month = 11,
                day = 27,
                hour = 15,
                minute = 0,
                workout =
                    Workout(
                        id = "sample_upper_body",
                        name = "Upper Body",
                        description = "Chest and back focus",
                        exercises = listOf(
                            Exercise(id = "ex_bench", name = "Bench Press", description = "...", targetMuscle = "Chest, Triceps", defaultSets = 4, defaultReps = 8),
                            Exercise(id = "ex_pullups", name = "Pull-ups", description = "...", targetMuscle = "Back, Biceps", defaultSets = 3, defaultReps = 6)
                        ),
                        createdBy = "Gainly"
                    )
            )
        )

        planList.add(
            Plan(
                year = 2025,
                month = 11,
                day = 16,
                hour = 16,
                minute = 20,
                workout =
                    Workout(
                        id = "sample_leg_day",
                        name = "Leg Day",
                        description = "Complete lower body workout",
                        exercises = listOf(
                            Exercise(id = "ex_squats", name = "Squats", description = "...", targetMuscle = "Quadriceps, Glutes", defaultSets = 4, defaultReps = 12),
                            Exercise(id = "ex_rdl", name = "Romanian Deadlift", description = "...", targetMuscle = "Hamstrings, Glutes", defaultSets = 3, defaultReps = 10)
                        ),
                        createdBy = "Gainly"
                    )
            )
        )

        planAdapter.updatePlans(planList)
    }


}