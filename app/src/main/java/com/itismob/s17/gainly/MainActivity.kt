package com.itismob.s17.gainly

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : BaseActivity() {

    private lateinit var workoutAdapter: WorkoutAdapter
    private lateinit var firestore: FirebaseFirestore

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
        fetchWorkoutsFromFirestore() // Fetch data from Firestore on start
        scrollToTop()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        workoutAdapter = WorkoutAdapter(
            workouts = WorkoutDataManager.workouts,
            onExerciseClick = { exercise ->
                showExerciseDetailDialog(exercise)
            },
            onStartWorkout = { position ->
                val workout = WorkoutDataManager.workouts[position]
                startWorkout(workout)
            },
            onFavoriteToggle = { position, isFavorite ->
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

        dialog.show()
    }

    private fun showExerciseSelectionDialog(onExerciseSelected: (Exercise) -> Unit) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_select_exercise)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val spinner = dialog.findViewById<android.widget.Spinner>(R.id.muscleGroupSpinner)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.allExercisesRecyclerView)

        val allExercises = createMasterExerciseList()

        // Setup Adapter for the RecyclerView
        val allExercisesAdapter = AllExercisesAdapter(
            allExercises = allExercises,
            onExerciseClick = { selectedExercise ->
                // This part stays the same: selects the exercise
                onExerciseSelected(selectedExercise)
                dialog.dismiss()
            },
            onInfoClick = { exerciseToShow ->
                // This is the new part: shows the detail dialog
                showExerciseDetailDialog(exerciseToShow)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = allExercisesAdapter

        // Setup Spinner
        val muscleGroups = listOf("All") + allExercises.map { it.targetMuscle }.distinct().sorted()
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, muscleGroups)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedMuscleGroup = muscleGroups[position]
                allExercisesAdapter.filterByMuscleGroup(selectedMuscleGroup)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
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
        val exerciseDescriptionTv = dialog.findViewById<TextView>(R.id.exerciseDescriptionTv)
        val targetMuscleTv = dialog.findViewById<TextView>(R.id.targetMuscleTv)
        val closeBtn = dialog.findViewById<ImageButton>(R.id.closeBtn)

        exerciseNameTv?.text = exercise.name
        exerciseDescriptionTv?.text = exercise.description
        targetMuscleTv?.text = exercise.targetMuscle

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

    private fun loadSampleData() {
        if (WorkoutDataManager.workouts.isEmpty()) {
            val sampleWorkouts = createSampleWorkouts()
            WorkoutDataManager.workouts.addAll(sampleWorkouts)
            // Save sample workouts to Firestore so they persist
            sampleWorkouts.forEach { saveWorkoutToFirestore(it) }
        }
        workoutAdapter.updateWorkouts(WorkoutDataManager.workouts)
    }

    private fun createSampleWorkouts(): List<Workout> {
        return listOf(
            Workout(
                id = "sample_leg_day",
                name = "Leg Day",
                description = "Complete lower body workout",
                exercises = listOf(
                    Exercise(id = "ex_squats", name = "Squats", description = "...", targetMuscle = "Quadriceps, Glutes", defaultSets = 4, defaultReps = 12),
                    Exercise(id = "ex_rdl", name = "Romanian Deadlift", description = "...", targetMuscle = "Hamstrings, Glutes", defaultSets = 3, defaultReps = 10)
                ),
                createdBy = "Gainly"
            ),
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
    }

    private fun createMasterExerciseList(): List<Exercise> {
        // In a real app, this would be a comprehensive list fetched from Firestore
        return listOf(
            Exercise(id = "ex_squats", name = "Squats", description = "...", targetMuscle = "Quadriceps, Glutes", defaultSets = 4, defaultReps = 12),
            Exercise(id = "ex_rdl", name = "Romanian Deadlift", description = "...", targetMuscle = "Hamstrings, Glutes", defaultSets = 3, defaultReps = 10),
            Exercise(id = "ex_bench", name = "Bench Press", description = "...", targetMuscle = "Chest, Triceps", defaultSets = 4, defaultReps = 8),
            Exercise(id = "ex_pullups", name = "Pull-ups", description = "...", targetMuscle = "Back, Biceps", defaultSets = 3, defaultReps = 6),
            Exercise(id = "ex_overhead_press", name = "Overhead Press", description = "...", targetMuscle = "Shoulders, Triceps", defaultSets = 4, defaultReps = 8),
            Exercise(id = "ex_barbell_row", name = "Barbell Row", description = "...", targetMuscle = "Back, Biceps", defaultSets = 4, defaultReps = 8),
            Exercise(id = "ex_lat_pulldown", name = "Lat Pulldown", description = "...", targetMuscle = "Back", defaultSets = 3, defaultReps = 12),
            Exercise(id = "ex_leg_press", name = "Leg Press", description = "...", targetMuscle = "Quadriceps, Glutes", defaultSets = 4, defaultReps = 15)
        )
    }


    private fun scrollToTop() {
        val scrollView = findViewById<ScrollView>(R.id.workoutSv)
        scrollView?.post {
            scrollView.scrollTo(0, 0)
        }
    }
}
