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
        val intent = Intent(this, WorkoutTrackingActivity::class.java)
        intent.putExtra("workout", plan.workout)
        startActivity(intent)
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
                        name = "Leg Day",
                        description = "Complete lower body workout",
                        exercises = listOf(
                            Exercise(
                                name = "Squats",
                                description = "Stand with feet shoulder-width apart, lower your body as if sitting in a chair, then return to standing position.",
                                targetMuscle = "Quadriceps, Glutes, Hamstrings",
                                sets = 4,
                                reps = 12,
                                lastWeight = 60.0,
                                personalBest = 70.0
                            ),
                            Exercise(
                                name = "Romanian Deadlift",
                                description = "Hold a barbell or dumbbells, hinge at your hips while keeping your back straight, lower the weight, then return to standing.",
                                targetMuscle = "Hamstrings, Glutes",
                                sets = 3,
                                reps = 10,
                                lastWeight = 100.0,
                                personalBest = 120.0
                            ),
                            Exercise(
                                name = "Lunges",
                                description = "Step forward with one leg, lower your hips until both knees are bent at 90-degree angles, then return to starting position.",
                                targetMuscle = "Quadriceps, Glutes",
                                sets = 3,
                                reps = 10,
                                lastWeight = 100.0,
                                personalBest = 120.0
                            ),
                            Exercise(
                                name = "Calf Raises",
                                description = "Stand with feet hip-width apart, raise your heels off the ground, then lower them back down.",
                                targetMuscle = "Calves",
                                sets = 4,
                                reps = 15,
                                lastWeight = 100.0,
                                personalBest = 120.0
                            )
                        )
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
                        name = "Upper Body",
                        description = "Chest and back focus",
                        exercises = listOf(
                            Exercise(
                                name = "Bench Press",
                                description = "Lie on a flat bench, lower the barbell to your chest, then press it back up to starting position.",
                                targetMuscle = "Chest, Triceps, Shoulders",
                                sets = 4,
                                reps = 8,
                                lastWeight = 165.0,
                                personalBest = 215.0
                            ),
                            Exercise(
                                name = "Pull-ups",
                                description = "Hang from a bar with palms facing away, pull your body up until your chin is above the bar, then lower yourself down.",
                                targetMuscle = "Back, Biceps",
                                sets = 3,
                                reps = 6,
                                lastWeight = 40.0,
                                personalBest = 50.0
                            ),
                            Exercise(
                                name = "Shoulder Press",
                                description = "Sit or stand with dumbbells at shoulder height, press them overhead until arms are fully extended, then lower back down.",
                                targetMuscle = "Shoulders, Triceps",
                                sets = 3,
                                reps = 10,
                                lastWeight = 50.0,
                                personalBest = 60.0
                            )
                        )
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
                        name = "Leg Day",
                        description = "Complete lower body workout",
                        exercises = listOf(
                            Exercise(
                                name = "Squats",
                                description = "Stand with feet shoulder-width apart, lower your body as if sitting in a chair, then return to standing position.",
                                targetMuscle = "Quadriceps, Glutes, Hamstrings",
                                sets = 4,
                                reps = 12,
                                lastWeight = 60.0,
                                personalBest = 70.0
                            ),
                            Exercise(
                                name = "Romanian Deadlift",
                                description = "Hold a barbell or dumbbells, hinge at your hips while keeping your back straight, lower the weight, then return to standing.",
                                targetMuscle = "Hamstrings, Glutes",
                                sets = 3,
                                reps = 10,
                                lastWeight = 100.0,
                                personalBest = 120.0
                            ),
                            Exercise(
                                name = "Lunges",
                                description = "Step forward with one leg, lower your hips until both knees are bent at 90-degree angles, then return to starting position.",
                                targetMuscle = "Quadriceps, Glutes",
                                sets = 3,
                                reps = 10,
                                lastWeight = 100.0,
                                personalBest = 120.0
                            ),
                            Exercise(
                                name = "Calf Raises",
                                description = "Stand with feet hip-width apart, raise your heels off the ground, then lower them back down.",
                                targetMuscle = "Calves",
                                sets = 4,
                                reps = 15,
                                lastWeight = 100.0,
                                personalBest = 120.0
                            )
                        )
                    )
            )
        )

        planAdapter.updatePlans(planList)
    }


}