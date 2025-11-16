package com.itismob.s17.gainly

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : BaseActivity() {

    private lateinit var workoutAdapter: WorkoutAdapter
    // REMOVED: private val workoutList = ArrayList<Workout>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_page)

        // Load data FIRST
        loadSampleData()

        // Then setup UI that depends on the data
        setupRecyclerView()
        setupClickListeners()
        scrollToTop()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        // FIX: Pass the manager's list directly to the adapter
        workoutAdapter = WorkoutAdapter(
            workouts = WorkoutDataManager.workouts,
            onExerciseClick = { exercise ->
                showExerciseDetailDialog(exercise)
            },
            onStartWorkout = { position ->
                // FIX: Get the workout from the single source of truth
                val workout = WorkoutDataManager.workouts[position]
                startWorkout(workout)
            },
            onFavoriteToggle = { position, isFavorite ->
                // FIX: Get the workout from the single source of truth
                val workout = WorkoutDataManager.workouts[position]
                toggleFavorite(workout, isFavorite)
            }
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

    // Function to start the workout tracking
    private fun startWorkout(workout: Workout) {
        val workoutPosition = WorkoutDataManager.workouts.indexOf(workout)
        if (workoutPosition != -1) {
            val intent = Intent(this, WorkoutTrackingActivity::class.java)
            intent.putExtra("workout_position", workoutPosition)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Workout not found!", Toast.LENGTH_SHORT).show()
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

    private fun showExerciseDetailDialog(exercise: Exercise) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.exercise_detail_dialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val exerciseNameTv = dialog.findViewById<TextView>(R.id.workoutTv)
        val exerciseDescriptionTv = dialog.findViewById<TextView>(R.id.exerciseDescriptionTv)
        val targetMuscleTv = dialog.findViewById<TextView>(R.id.targetMuscleTv)
        val closeBtn = dialog.findViewById<ImageButton>(R.id.closeBtn)

        if (exerciseNameTv != null) {
            exerciseNameTv.text = exercise.name
        }

        if (exerciseDescriptionTv != null) {
            exerciseDescriptionTv.text = exercise.description
        }

        if (targetMuscleTv != null) {
            targetMuscleTv.text = exercise.targetMuscle
        }

        closeBtn?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun toggleFavorite(workout: Workout, isFavorite: Boolean) {
        val position = WorkoutDataManager.workouts.indexOf(workout)
        if (position != -1) {
            val updatedWorkout = workout.copy(isFavorite = isFavorite)
            WorkoutDataManager.workouts[position] = updatedWorkout

            // The adapter is already pointing to this list, so just notify it
            workoutAdapter.updateWorkouts(WorkoutDataManager.workouts)

            val message = if (isFavorite) "Added to favorites" else "Removed from favorites"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSampleData() {
        if (WorkoutDataManager.workouts.isEmpty()) {
            WorkoutDataManager.workouts.addAll(
                listOf(
                    Workout(
                        id = Workout.generateId(),
                        name = "Leg Day",
                        description = "Complete lower body workout",
                        exercises = listOf(
                            Exercise(
                                id = Exercise.generateId(),
                                name = "Squats",
                                description = "Stand with feet shoulder-width apart...",
                                targetMuscle = "Quadriceps, Glutes, Hamstrings",
                                defaultSets = 4,
                                defaultReps = 12
                            ),
                            Exercise(
                                id = Exercise.generateId(),
                                name = "Romanian Deadlift",
                                description = "Hold a barbell or dumbbells...",
                                targetMuscle = "Hamstrings, Glutes",
                                defaultSets = 3,
                                defaultReps = 10
                            )
                        )
                    ),
                    Workout(
                        id = Workout.generateId(),
                        name = "Upper Body",
                        description = "Chest and back focus",
                        exercises = listOf(
                            Exercise(
                                id = Exercise.generateId(),
                                name = "Bench Press",
                                description = "Lie on a flat bench...",
                                targetMuscle = "Chest, Triceps, Shoulders",
                                defaultSets = 4,
                                defaultReps = 8
                            ),
                            Exercise(
                                id = Exercise.generateId(),
                                name = "Pull-ups",
                                description = "Hang from a bar with palms facing away...",
                                targetMuscle = "Back, Biceps",
                                defaultSets = 3,
                                defaultReps = 6
                            )
                        )
                    )
                )
            )
        }
        // No longer need to call workoutAdapter.updateWorkouts here if setupRecyclerView is called after
    }

    private fun scrollToTop() {
        val scrollView = findViewById<ScrollView>(R.id.workoutSv)
        scrollView?.post {
            scrollView.scrollTo(0, 0)
        }
    }
}
