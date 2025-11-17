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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : BaseActivity() {

    private lateinit var workoutAdapter: WorkoutAdapter
    private val workoutList = ArrayList<Workout>()

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_page)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Setup UI and load data
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
        val addExerciseBtn = dialog.findViewById<Button>(R.id.addExerciseBtn)
        val workoutNameEditText = dialog.findViewById<EditText>(R.id.workoutNameEtx)
        val descriptionEditText = dialog.findViewById<EditText>(R.id.descriptionEtx)

        val exercisesContainer = dialog.findViewById<LinearLayout>(R.id.exercisesContainer)

        val selectedExercises = mutableListOf<Exercise>()

        addExerciseBtn.setOnClickListener {
            showExerciseSelectionDialog { exercise ->
                // Prevent adding the same exercise multiple times
                if (selectedExercises.any { it.id == exercise.id }) {
                    Toast.makeText(this, "${exercise.name} is already in the list.", Toast.LENGTH_SHORT).show()
                    return@showExerciseSelectionDialog
                }

                selectedExercises.add(exercise)

                // Inflate the pre_config_exercise_item layout
                val exerciseConfigView = LayoutInflater.from(this)
                    .inflate(R.layout.pre_config_exercise_item, exercisesContainer, false)

                val exerciseNameTv = exerciseConfigView.findViewById<TextView>(R.id.exerciseNameTv)
                val removeBtn = exerciseConfigView.findViewById<ImageButton>(R.id.removeExerciseBtn)

                exerciseNameTv.text = exercise.name

                // Tag the view with the exercise object so we can retrieve it later
                exerciseConfigView.tag = exercise

                removeBtn.setOnClickListener {
                    selectedExercises.remove(exercise)
                    exercisesContainer.removeView(exerciseConfigView)
                }

                // Add the new view to the container. This makes it appear in the UI.
                exercisesContainer.addView(exerciseConfigView)
            }
        }

        createWorkoutBtn.setOnClickListener {
            val name = workoutNameEditText.text.toString()
            val description = descriptionEditText.text.toString()

            if (name.isBlank()) {
                workoutNameEditText.error = "Workout name cannot be empty."
                return@setOnClickListener
            }
            if (selectedExercises.isEmpty()) {
                Toast.makeText(this, "Please add at least one exercise.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val configuredExercises = mutableListOf<Exercise>()
            for (i in 0 until exercisesContainer.childCount) {
                val view = exercisesContainer.getChildAt(i)
                val exercise = view.tag as Exercise

                val setsEtx = view.findViewById<EditText>(R.id.defaultSetsEtx)
                val repsEtx = view.findViewById<EditText>(R.id.defaultRepsEtx)

                val sets = setsEtx.text.toString().toIntOrNull() ?: 3 // Default to 3 if empty/invalid
                val reps = repsEtx.text.toString().toIntOrNull() ?: 10 // Default to 10 if empty/invalid

                // Create a copy of the exercise with the new default values
                configuredExercises.add(exercise.copy(defaultSets = sets, defaultReps = reps))
            }

            val currentUser = auth.currentUser
            val userEmail = currentUser?.email ?: "unknown"

            val newWorkout = Workout(
                id = Workout.generateId(),
                name = name,
                description = description,
                exercises = configuredExercises, // Use the list with user-defined sets/reps
                isFavorite = false,
                createdBy = "$userEmail"
            )

            // 1. Save to Firebase
            saveWorkoutToFirestore(newWorkout)

            // 2. Add to local data manager for immediate UI update
            WorkoutDataManager.addWorkout(newWorkout)

            // 3. Notify adapter to show the new item
            workoutAdapter.notifyItemInserted(0)
            findViewById<RecyclerView>(R.id.recyclerView).scrollToPosition(0)

            dialog.dismiss()
            Toast.makeText(this, "Workout '$name' created!", Toast.LENGTH_SHORT).show()
        }

        closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        createWorkoutBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveWorkoutToFirestore(workout: Workout) {
        firestore.collection("user-saved-workouts").document(workout.id)
            .set(workout)
            .addOnSuccessListener {
                Log.d("Firestore", "Workout ${workout.id} successfully written!")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error writing workout: ${e.message}")
                Log.e("Firestore", "Workout data: $workout")
                e.printStackTrace()
                Toast.makeText(this, "Failed to save workout to cloud: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun fetchWorkoutsFromFirestore() {
        val currentUser = auth.currentUser
        val userEmail = currentUser?.email

        firestore.collection("user-saved-workouts")
            .get()
            .addOnSuccessListener { result ->
                val userWorkouts = mutableListOf<Workout>()
                val gainlyWorkouts = mutableListOf<Workout>()

                for (document in result) {
                    try {
                        val workout = document.toObject(Workout::class.java)
                        when (workout.createdBy) {
                            userEmail -> userWorkouts.add(workout)
                            "Gainly" -> gainlyWorkouts.add(workout)
                        }
                    } catch (e: Exception) {
                        Log.e("Firestore", "Error parsing document ${document.id}: ${e.message}")
                    }
                }

                val allWorkouts = userWorkouts + gainlyWorkouts

                if (allWorkouts.isEmpty()) {
                    loadSampleData()
                } else {
                    WorkoutDataManager.workouts.clear()
                    WorkoutDataManager.workouts.addAll(allWorkouts)
                    workoutAdapter.updateWorkouts(WorkoutDataManager.workouts)
                    Log.d("Firestore", "Loaded ${userWorkouts.size} user workouts + ${gainlyWorkouts.size} Gainly workouts")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents: ", exception)
                Toast.makeText(this, "Failed to load workouts from cloud.", Toast.LENGTH_SHORT).show()
                loadSampleData()
            }
    }

    private fun showExerciseDetailDialog(exercise: Exercise) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.exercise_detail_dialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val exerciseNameTv = dialog.findViewById<TextView>(R.id.workoutTv)
        // val exerciseImageIv = dialog.findViewById<ImageView>(R.id.exerciseImageIv) // not being used yet
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

        // Set the exercise image WIP
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

            // Also update this change in Firestore
            firestore.collection("user-saved-workouts").document(workout.id)
                .update("favorite", isFavorite)
                .addOnFailureListener {
                    // Optionally revert the change and notify user
                }

            workoutAdapter.updateWorkouts(WorkoutDataManager.workouts)
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