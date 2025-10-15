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
            workouts = workoutList,
            onExerciseClick = { exercise ->
                showExerciseDetailDialog(exercise)
            },
            onStartWorkout = { workout ->
                startWorkout(workout)
            },
            onFavoriteToggle = { workout, isFavorite ->
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

        // Get references to the dialog views - use the correct IDs from your XML
        val exerciseNameTv = dialog.findViewById<TextView>(R.id.workoutTv) // This was the issue!
        // val exerciseImageIv = dialog.findViewById<ImageView>(R.id.exerciseImageIv)
        val exerciseDescriptionTv = dialog.findViewById<TextView>(R.id.exerciseDescriptionTv)
        val targetMuscleTv = dialog.findViewById<TextView>(R.id.targetMuscleTv)
        val closeBtn = dialog.findViewById<ImageButton>(R.id.closeBtn)

        // Check if views are not null before using them
        if (exerciseNameTv != null) {
            exerciseNameTv.text = exercise.name
        }

        if (exerciseDescriptionTv != null) {
            exerciseDescriptionTv.text = exercise.description
        }

        if (targetMuscleTv != null) {
            targetMuscleTv.text = exercise.targetMuscle
        }

        // Set the exercise image (using the resource ID from the Exercise object)
        // exerciseImageIv.setImageResource(exercise.imageResId)

        closeBtn?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun startWorkout(workout: Workout) {
        val intent = Intent(this, WorkoutTrackingActivity::class.java)
        intent.putExtra("workout", workout)
        startActivity(intent)
    }

    private fun toggleFavorite(workout: Workout, isFavorite: Boolean) {
        val index = workoutList.indexOfFirst { it.name == workout.name }
        if (index != -1) {
            val updatedWorkout = workout.copy(isFavorite = isFavorite)
            workoutList[index] = updatedWorkout
            workoutAdapter.updateWorkouts(workoutList)

            val message = if (isFavorite) "Added to favorites" else "Removed from favorites"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun addSampleWorkouts() {
        // sample workout data with detailed exercises
        workoutList.add(
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

        workoutList.add(
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

        workoutAdapter.updateWorkouts(workoutList)
    }

    private fun scrollToTop() {
        val scrollView = findViewById<ScrollView>(R.id.workoutSv)
        scrollView?.post {
            scrollView.scrollTo(0, 0)
        }
    }


}