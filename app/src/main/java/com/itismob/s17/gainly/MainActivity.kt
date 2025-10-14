package com.itismob.s17.gainly

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : BaseActivity() {

    private lateinit var workoutAdapter: WorkoutAdapter
    private val workoutList = ArrayList<Workout>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_page)
        setupRecyclerView()
        setupClickListeners()
        scrollToTop()
        addSampleWorkouts() // sample data
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        workoutAdapter = WorkoutAdapter(
            workouts = workoutList
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = workoutAdapter
    }

    private fun setupClickListeners() {
        val newWorkoutBtn = findViewById<Button>(R.id.newWorkoutBtn)
        newWorkoutBtn.setOnClickListener {
            showNewWorkoutDialog()
        }
    }

    private fun showNewWorkoutDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.new_workout_popup)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val closeBtn = dialog.findViewById<ImageButton>(R.id.closeBtn)
        val createWorkoutBtn = dialog.findViewById<Button>(R.id.createWorkoutBtn)

        closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        createWorkoutBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addSampleWorkouts() {
        // sample workout data
        workoutList.add(
            Workout(
                name = "Leg Day",
                description = "Complete lower body workout",
                exercises = listOf("Squats", "Romanian Deadlift", "Lunges", "Calf Raises")
            )
        )

        workoutList.add(
            Workout(
                name = "Upper Body",
                description = "Chest and back focus",
                exercises = listOf("Bench Press", "Pull-ups", "Shoulder Press")
            )
        )

        workoutAdapter.updateWorkouts(workoutList)
    }

    private fun scrollToTop() {
        val scrollView = findViewById<ScrollView>(R.id.workoutSv)
        scrollView?.post {
            scrollView.scrollTo(0, 0)
        }
    }
}