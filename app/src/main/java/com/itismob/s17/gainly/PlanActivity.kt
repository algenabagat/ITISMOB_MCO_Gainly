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
import androidx.compose.foundation.layout.add
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.io.path.Path

class PlanActivity : BaseActivity(), DatePickerFragment.OnDateSelectedListener, TimePickerFragment.OnTimeSelectedListener {

    private lateinit var planAdapter: PlanAdapter
    private val planList: ArrayList<Plan> by lazy {
        PlanStorageManager.loadPlans(this)
    }
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
        planAdapter.updatePlans(planList)
    }

    private fun addPlan(plan: Plan) {
        planList.add(plan)
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

        val selectedWorkout: Workout? = WorkoutDataManager.workouts.firstOrNull()

        dateButtonToUpdate = pickDateBtn
        timeButtonToUpdate = pickTime

        // ... (other listeners like pickDateBtn, pickTime, closeBtn remain the same)

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
            // Basic validation
            if (dateButtonToUpdate?.text.isNullOrEmpty() || timeButtonToUpdate?.text.isNullOrEmpty()) {
                Toast.makeText(this, "Please select a date and time.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedWorkout == null) {
                Toast.makeText(this, "No available workouts to schedule.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // You'll need to parse the date and time back from the buttons' text
            // This is a simplified example. A more robust solution would store these values directly.
            val dateParts = dateButtonToUpdate?.text.toString().split("/")
            val timeParts = timeButtonToUpdate?.text.toString().split(":")

            val year = dateParts[2].toInt()
            val month = dateParts[0].toInt() - 1 // month is 0-indexed
            val day = dateParts[1].toInt()
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()

            val newPlan = Plan(
                year = year,
                month = month,
                day = day,
                hour = hour,
                minute = minute,
                workout = selectedWorkout
            )

            addPlan(newPlan)
            dialog.dismiss()
            Toast.makeText(this, "Plan for '${newPlan.workout.name}' created!", Toast.LENGTH_SHORT).show()
        }

        dialog.setOnDismissListener {
            dateButtonToUpdate = null
            timeButtonToUpdate = null
        }

        dialog.show()
    }

    private fun startPlan(plan: Plan) {
        // Find the position of the plan's workout within the global list in WorkoutDataManager.
        val workoutPosition = WorkoutDataManager.workouts.indexOfFirst { it.id == plan.workout.id }

        // Check if the workout was actually found in the master list.
        if (workoutPosition != -1) {
            val intent = Intent(this, WorkoutTrackingActivity::class.java)
            // Pass the integer position with the key that WorkoutTrackingActivity expects.
            intent.putExtra("workout_position", workoutPosition)
            startActivity(intent)
        } else {
            // If the workout from the plan doesn't exist in the manager, show an error.
            Toast.makeText(this, "Could not find workout to start.", Toast.LENGTH_SHORT).show()
        }
    }


    /*private fun addSamplePlans() {
        val plan1 = Plan(
            year = 2025,
            month = 11, // December
            day = 25,
            hour = 21,
            minute = 0,
            workout =
                Workout(
                    id = "sample_leg_day",
                    name = "Leg Day",
                    description = "Complete lower body workout",
                    exercises = listOf(
                        Exercise(
                            id = "ex_squats",
                            name = "Squats",
                            description = "...",
                            targetMuscle = "Quadriceps, Glutes",
                            defaultSets = 4,
                            defaultReps = 12
                        ),
                        Exercise(
                            id = "ex_rdl",
                            name = "Romanian Deadlift",
                            description = "...",
                            targetMuscle = "Hamstrings, Glutes",
                            defaultSets = 3,
                            defaultReps = 10
                        )
                    ),
                    createdBy = "Gainly"
                )
        )
        addPlan(plan1)

        val plan2 = Plan(
            year = 2025,
            month = 11, // December
            day = 27,
            hour = 15,
            minute = 0,
            workout =
                Workout(
                    id = "sample_upper_body",
                    name = "Upper Body",
                    description = "Chest and back focus",
                    exercises = listOf(
                        Exercise(
                            id = "ex_bench",
                            name = "Bench Press",
                            description = "...",
                            targetMuscle = "Chest, Triceps",
                            defaultSets = 4,
                            defaultReps = 8
                        ),
                        Exercise(
                            id = "ex_pullups",
                            name = "Pull-ups",
                            description = "...",
                            targetMuscle = "Back, Biceps",
                            defaultSets = 3,
                            defaultReps = 6
                        )
                    ),
                    createdBy = "Gainly"
                )
        )
        addPlan(plan2)

        val plan3 = Plan(
            year = 2025,
            month = 10, // November (current month)
            day = 16, // Assuming today's date
            hour = 16,
            minute = 20,
            workout =
                Workout(
                    id = "sample_leg_day",
                    name = "Leg Day",
                    description = "Complete lower body workout",
                    exercises = listOf(
                        Exercise(
                            id = "ex_squats",
                            name = "Squats",
                            description = "...",
                            targetMuscle = "Quadriceps, Glutes",
                            defaultSets = 4,
                            defaultReps = 12
                        ),
                        Exercise(
                            id = "ex_rdl",
                            name = "Romanian Deadlift",
                            description = "...",
                            targetMuscle = "Hamstrings, Glutes",
                            defaultSets = 3,
                            defaultReps = 10
                        )
                    ),
                    createdBy = "Gainly"
                )
        )
        addPlan(plan3)
    }
     */
}