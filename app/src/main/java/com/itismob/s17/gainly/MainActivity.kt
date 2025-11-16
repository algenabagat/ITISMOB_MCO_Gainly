package com.itismob.s17.gainly

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : BaseActivity() {

    private lateinit var workoutAdapter: WorkoutAdapter
    private lateinit var selectedExercisesAdapter: SelectedExercisesAdapter
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_page)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

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
        val exercisesRecyclerView = dialog.findViewById<RecyclerView>(R.id.workoutListRecycler)

        val selectedExercises = mutableListOf<Exercise>()
// This line now correctly creates an instance of the adapter you just made.
        val selectedExercisesAdapter = SelectedExercisesAdapter(selectedExercises) { exercise ->
            // This lambda is the 'onRemoveClick' callback.
            // It gets triggered when the remove button in the adapter is clicked.
            selectedExercises.remove(exercise)
            selectedExercisesAdapter.updateExercises(selectedExercises)
        }
        exercisesRecyclerView.layoutManager = LinearLayoutManager(this)
        exercisesRecyclerView.adapter = selectedExercisesAdapter

        addExerciseBtn.setOnClickListener {
            showExerciseSelectionDialog { exercise ->
                if (!selectedExercises.any { it.id == exercise.id }) {
                    selectedExercises.add(exercise)
                    selectedExercisesAdapter.updateExercises(selectedExercises)
                } else {
                    Toast.makeText(this, "${exercise.name} is already in the list.", Toast.LENGTH_SHORT).show()
                }
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

            val newWorkout = Workout(
                id = Workout.generateId(),
                name = name,
                description = description,
                exercises = selectedExercises,
                isFavorite = false
            )

            // 1. Save to Firebase
            saveWorkoutToFirestore(newWorkout)

            // 2. Add to local data manager for immediate UI update
            WorkoutDataManager.addWorkout(newWorkout)

            // 3. Notify adapter
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
        // Here, we create a sample list of exercises. In a real app, you would fetch this
        // from a master list in Firestore or a local database.
        val allExercises = createMasterExerciseList()
        val exerciseNames = allExercises.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select an Exercise")
            .setItems(exerciseNames) { _, which ->
                onExerciseSelected(allExercises[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveWorkoutToFirestore(workout: Workout) {
        firestore.collection("workouts").document(workout.id)
            .set(workout)
            .addOnSuccessListener {
                Log.d("Firestore", "Workout ${workout.id} successfully written!")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error writing document", e)
                Toast.makeText(this, "Failed to save workout to cloud.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchWorkoutsFromFirestore() {
        firestore.collection("workouts")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    // No workouts in Firestore, so load local sample data as a fallback
                    loadSampleData()
                    return@addOnSuccessListener
                }

                val fetchedWorkouts = result.toObjects(Workout::class.java)
                WorkoutDataManager.workouts.clear()
                WorkoutDataManager.workouts.addAll(fetchedWorkouts)

                // Update adapter with fetched data
                workoutAdapter.updateWorkouts(WorkoutDataManager.workouts)
                Log.d("Firestore", "Successfully fetched ${fetchedWorkouts.size} workouts.")
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
                Toast.makeText(this, "Failed to load workouts from cloud.", Toast.LENGTH_SHORT).show()
                // Load local sample data as a fallback
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
            firestore.collection("workouts").document(workout.id)
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
                )
            ),
            Workout(
                id = "sample_upper_body",
                name = "Upper Body",
                description = "Chest and back focus",
                exercises = listOf(
                    Exercise(id = "ex_bench", name = "Bench Press", description = "...", targetMuscle = "Chest, Triceps", defaultSets = 4, defaultReps = 8),
                    Exercise(id = "ex_pullups", name = "Pull-ups", description = "...", targetMuscle = "Back, Biceps", defaultSets = 3, defaultReps = 6)
                )
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
