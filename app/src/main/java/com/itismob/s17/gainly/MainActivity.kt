package com.itismob.s17.gainly

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.ImageView
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : BaseActivity() {

    private lateinit var workoutAdapter: WorkoutAdapter
    private lateinit var firestore: FirebaseFirestore

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var noWorkoutsHint: TextView
    private lateinit var recyclerView: RecyclerView

    companion object {
        private const val PREFS_NAME = "WorkoutPrefs"
        private const val KEY_USER_WORKOUTS = "user_workouts"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_page)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        noWorkoutsHint = findViewById(R.id.noWorkoutsHint)
        recyclerView = findViewById(R.id.recyclerView)

        setupRecyclerView()
        setupClickListeners()
        loadWorkoutsFromLocalStorage()
        scrollToTop()
        updateNoWorkoutsHint()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        workoutAdapter = WorkoutAdapter(
            workouts = WorkoutDataManager.workouts,
            onExerciseClick = { exercise ->
                showExerciseDetailDialog(exercise)
            },
            onStartWorkout = { workout ->
                // val workout = WorkoutDataManager.workouts[position]
                startWorkout(workout)
            },
            onFavoriteToggle = { workout, isFavorite ->
                // val workout = WorkoutDataManager.workouts[position]
                toggleFavorite(workout, isFavorite)
            },
            onEditWorkout = { workout ->
                // val workout = WorkoutDataManager.workouts[position]
                // showEditWorkoutDialog(workout, position)
                val position = WorkoutDataManager.workouts.indexOf(workout)
                if (position != -1) {
                    showEditWorkoutDialog(workout, position)
                }
            },
            onDeleteWorkout = { workout ->
                // val workout = WorkoutDataManager.workouts[position]
                // deleteWorkout(workout, position)
                val position = WorkoutDataManager.workouts.indexOf(workout)
                if (position != -1) {
                    deleteWorkout(workout, position)
                }
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = workoutAdapter
    }

    private fun updateNoWorkoutsHint() {
        if (WorkoutDataManager.workouts.isEmpty()) {
            noWorkoutsHint.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            noWorkoutsHint.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun setupClickListeners() {
        val newWorkoutBtn = findViewById<Button>(R.id.newWorkoutBtn)
        newWorkoutBtn.setOnClickListener {
            showNewWorkoutDialog()
        }
    }

    private fun loadWorkoutsFromLocalStorage() {
        val workoutsJson = sharedPreferences.getString(KEY_USER_WORKOUTS, null)

        if (workoutsJson.isNullOrEmpty()) {
            loadSampleWorkouts()
        } else {
            // Load from SharedPreferences
            val savedWorkouts = parseWorkoutsFromJson(workoutsJson)
            WorkoutDataManager.workouts.clear()
            WorkoutDataManager.workouts.addAll(savedWorkouts)
            workoutAdapter.updateWorkouts(WorkoutDataManager.workouts)
        }
        updateNoWorkoutsHint()
    }

    private fun saveWorkoutsToLocalStorage() {
        val workoutsJson = convertWorkoutsToJson(WorkoutDataManager.workouts)
        sharedPreferences.edit().putString(KEY_USER_WORKOUTS, workoutsJson).apply()
    }

    private fun convertWorkoutsToJson(workouts: List<Workout>): String {
        val jsonArray = JSONArray()
        workouts.forEach { workout ->
            val workoutJson = JSONObject().apply {
                put("id", workout.id)
                put("name", workout.name)
                put("description", workout.description)
                put("isFavorite", workout.isFavorite)
                put("createdBy", workout.createdBy)

                val exercisesArray = JSONArray()
                workout.exercises.forEach { exercise ->
                    val exerciseJson = JSONObject().apply {
                        put("id", exercise.id)
                        put("name", exercise.name)
                        put("description", exercise.description)
                        put("targetMuscle", exercise.targetMuscle)
                        put("category", exercise.category)
                        put("defaultSets", exercise.defaultSets)
                        put("defaultReps", exercise.defaultReps)
                        put("imageResId", exercise.imageResId)
                    }
                    exercisesArray.put(exerciseJson)
                }
                put("exercises", exercisesArray)
            }
            jsonArray.put(workoutJson)
        }
        return jsonArray.toString()
    }

    private fun parseWorkoutsFromJson(jsonString: String): List<Workout> {
        val workouts = mutableListOf<Workout>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val workoutJson = jsonArray.getJSONObject(i)
                val exercises = mutableListOf<Exercise>()

                val exercisesArray = workoutJson.getJSONArray("exercises")
                for (j in 0 until exercisesArray.length()) {
                    val exerciseJson = exercisesArray.getJSONObject(j)
                    val exercise = Exercise(
                        id = exerciseJson.getString("id"),
                        name = exerciseJson.getString("name"),
                        description = exerciseJson.getString("description"),
                        targetMuscle = exerciseJson.getString("targetMuscle"),
                        category = exerciseJson.getString("category"),
                        defaultSets = exerciseJson.getInt("defaultSets"),
                        defaultReps = exerciseJson.getInt("defaultReps"),
                        imageResId = exerciseJson.getInt("imageResId")
                    )
                    exercises.add(exercise)
                }

                val workout = Workout(
                    id = workoutJson.getString("id"),
                    name = workoutJson.getString("name"),
                    description = workoutJson.getString("description"),
                    exercises = exercises,
                    isFavorite = workoutJson.getBoolean("isFavorite"),
                    createdBy = workoutJson.getString("createdBy")
                )
                workouts.add(workout)
            }
        } catch (e: Exception) {
            Log.e("LocalStorage", "Error parsing workouts from JSON: ${e.message}")
        }
        return workouts
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

            showUploadOptionDialog(newWorkout, dialog)
        }

        closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showUploadOptionDialog(workout: Workout, parentDialog: Dialog) {
        AlertDialog.Builder(this)
            .setTitle("Upload to Cloud?")
            .setMessage("Do you want to upload this workout to the cloud so you can access it from other devices?")
            .setPositiveButton("Upload to Cloud") { dialog, which ->
                // Save locally and upload to cloud
                addWorkoutLocally(workout)
                saveWorkoutToFirestore(workout)
                parentDialog.dismiss()
                Toast.makeText(this, "Workout '${workout.name}' created and uploaded!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Save Locally Only") { dialog, which ->
                // Save only locally
                addWorkoutLocally(workout)
                parentDialog.dismiss()
                Toast.makeText(this, "Workout '${workout.name}' saved locally!", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun addWorkoutLocally(workout: Workout) {
        WorkoutDataManager.addWorkout(workout)
        saveWorkoutsToLocalStorage()
        workoutAdapter.notifyItemInserted(0)
        updateNoWorkoutsHint()
        findViewById<RecyclerView>(R.id.recyclerView).scrollToPosition(0)
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
        val muscleGroups = listOf("All") + allExercises.map { it.category }.distinct().sorted()
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
                    loadSampleWorkouts()
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
                loadSampleWorkouts()
            }
    }

    private fun showExerciseDetailDialog(exercise: Exercise) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.exercise_detail_dialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val exerciseNameTv = dialog.findViewById<TextView>(R.id.workoutTv)
        val exerciseImageIv = dialog.findViewById<ImageView>(R.id.exerciseImageIv) // Make sure this ID exists in your layout
        val exerciseDescriptionTv = dialog.findViewById<TextView>(R.id.exerciseDescriptionTv)
        val targetMuscleTv = dialog.findViewById<TextView>(R.id.targetMuscleTv)
        val closeBtn = dialog.findViewById<ImageButton>(R.id.closeBtn)

        exerciseNameTv?.text = exercise.name
        exerciseDescriptionTv?.text = exercise.description
        targetMuscleTv?.text = exercise.targetMuscle

        exerciseImageIv?.setImageResource(exercise.imageResId)

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
            saveWorkoutsToLocalStorage() // Save favorite status locally

            workoutAdapter.updateWorkouts(WorkoutDataManager.workouts)
            val message = if (isFavorite) "Added to favorites" else "Removed from favorites"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEditWorkoutDialog(workout: Workout, position: Int) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.new_workout_popup)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val closeBtn = dialog.findViewById<ImageButton>(R.id.closeBtn)
        val createWorkoutBtn = dialog.findViewById<Button>(R.id.createWorkoutBtn)
        val addExerciseBtn = dialog.findViewById<Button>(R.id.addExerciseBtn)
        val workoutNameEditText = dialog.findViewById<EditText>(R.id.workoutNameEtx)
        val descriptionEditText = dialog.findViewById<EditText>(R.id.descriptionEtx)
        val titleTextView = dialog.findViewById<TextView>(R.id.newWorkoutTxv)
        val exercisesContainer = dialog.findViewById<LinearLayout>(R.id.exercisesContainer)

        titleTextView.text = "Edit Workout"
        createWorkoutBtn.text = "Update Workout"

        // Pre-fill the fields with existing workout data
        workoutNameEditText.setText(workout.name)
        descriptionEditText.setText(workout.description)

        val selectedExercises = workout.exercises.toMutableList()

        // Clear and re-populate exercises container
        exercisesContainer.removeAllViews()
        workout.exercises.forEach { exercise ->
            val exerciseConfigView = LayoutInflater.from(this)
                .inflate(R.layout.pre_config_exercise_item, exercisesContainer, false)

            val exerciseNameTv = exerciseConfigView.findViewById<TextView>(R.id.exerciseNameTv)
            val setsEtx = exerciseConfigView.findViewById<EditText>(R.id.defaultSetsEtx)
            val repsEtx = exerciseConfigView.findViewById<EditText>(R.id.defaultRepsEtx)
            val removeBtn = exerciseConfigView.findViewById<ImageButton>(R.id.removeExerciseBtn)

            exerciseNameTv.text = exercise.name
            setsEtx.setText(exercise.defaultSets.toString())
            repsEtx.setText(exercise.defaultReps.toString())

            // Tag the view with the exercise object
            exerciseConfigView.tag = exercise

            removeBtn.setOnClickListener {
                selectedExercises.remove(exercise)
                exercisesContainer.removeView(exerciseConfigView)
            }

            exercisesContainer.addView(exerciseConfigView)
        }

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
                val setsEtx = exerciseConfigView.findViewById<EditText>(R.id.defaultSetsEtx)
                val repsEtx = exerciseConfigView.findViewById<EditText>(R.id.defaultRepsEtx)
                val removeBtn = exerciseConfigView.findViewById<ImageButton>(R.id.removeExerciseBtn)

                exerciseNameTv.text = exercise.name
                setsEtx.setText(exercise.defaultSets.toString())
                repsEtx.setText(exercise.defaultReps.toString())

                // Tag the view with the exercise object
                exerciseConfigView.tag = exercise

                removeBtn.setOnClickListener {
                    selectedExercises.remove(exercise)
                    exercisesContainer.removeView(exerciseConfigView)
                }

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

                val sets = setsEtx.text.toString().toIntOrNull() ?: 3
                val reps = repsEtx.text.toString().toIntOrNull() ?: 10

                configuredExercises.add(exercise.copy(defaultSets = sets, defaultReps = reps))
            }

            val currentUser = auth.currentUser
            val userEmail = currentUser?.email ?: "unknown"

            val updatedWorkout = Workout(
                id = workout.id, // Keep the same ID
                name = name,
                description = description,
                exercises = configuredExercises,
                isFavorite = workout.isFavorite, // Keep the same favorite status
                createdBy = userEmail
            )

            val isUserWorkout = workout.createdBy != "Gainly"

            if (isUserWorkout) {
                // Check if the workout exists in the cloud
                checkIfExistsInCloud(workout.id) { existsInCloud ->
                    if (existsInCloud) {
                        // Show option dialog for workouts that exist in cloud
                        showEditOptionDialog(updatedWorkout, position, dialog)
                    } else {
                        // Update only locally for user workouts not in cloud
                        updateWorkoutLocally(updatedWorkout, position)
                        dialog.dismiss()
                        Toast.makeText(this, "Workout '$name' updated locally!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // For Gainly workouts, just update locally
                updateWorkoutLocally(updatedWorkout, position)
                dialog.dismiss()
                Toast.makeText(this, "Workout '$name' updated locally!", Toast.LENGTH_SHORT).show()
            }
        }

        closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showEditOptionDialog(workout: Workout, position: Int, parentDialog: Dialog) {
        AlertDialog.Builder(this)
            .setTitle("Update Workout")
            .setMessage("Do you want to update this workout in the cloud as well?")
            .setPositiveButton("Update Locally & Cloud") { dialog, which ->
                // Update both locally and in cloud
                updateWorkoutLocally(workout, position)
                updateWorkoutInFirestore(workout)
                parentDialog.dismiss()
                Toast.makeText(this, "Workout '${workout.name}' updated everywhere!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Update Locally Only") { dialog, which ->
                // Update only locally
                updateWorkoutLocally(workout, position)
                parentDialog.dismiss()
                Toast.makeText(this, "Workout '${workout.name}' updated locally!", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun updateWorkoutLocally(workout: Workout, position: Int) {
        WorkoutDataManager.workouts[position] = workout
        saveWorkoutsToLocalStorage()
        workoutAdapter.notifyItemChanged(position)
    }

    private fun updateWorkoutInFirestore(workout: Workout) {
        firestore.collection("user-saved-workouts").document(workout.id)
            .set(workout)
            .addOnSuccessListener {
                Log.d("Firestore", "Workout ${workout.id} successfully updated!")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error updating workout: ${e.message}")
                Toast.makeText(this, "Failed to update workout in cloud.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteWorkout(workout: Workout, position: Int) {
        val isUserWorkout = workout.createdBy != "Gainly"
        // Check if this workout exists in the database (user-created workout)
        if (isUserWorkout) {
            // Check if the workout exists in the cloud
            checkIfExistsInCloud(workout.id) { existsInCloud ->
                if (existsInCloud) {
                    // Show option dialog for workouts that exist in cloud
                    showDeleteOptionDialog(workout, position)
                } else {
                    // Delete only locally for user workouts not in cloud
                    deleteWorkoutLocally(workout, position)
                    Toast.makeText(this, "Workout '${workout.name}' deleted locally!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // For Gainly workouts, just delete locally
            deleteWorkoutLocally(workout, position)
            Toast.makeText(this, "Workout '${workout.name}' deleted locally!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteOptionDialog(workout: Workout, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Workout")
            .setMessage("Do you want to delete this workout from the cloud as well?")
            .setPositiveButton("Delete Everywhere") { dialog, which ->
                // Delete from both local and cloud
                deleteWorkoutFromFirestore(workout, position)
            }
            .setNegativeButton("Delete Locally Only") { dialog, which ->
                // Delete only locally
                deleteWorkoutLocally(workout, position)
                Toast.makeText(this, "Workout '${workout.name}' deleted locally!", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun deleteWorkoutLocally(workout: Workout, position: Int) {
        WorkoutDataManager.workouts.removeAt(position)
        saveWorkoutsToLocalStorage()
        workoutAdapter.notifyItemRemoved(position)
        updateNoWorkoutsHint()
    }

    private fun deleteWorkoutFromFirestore(workout: Workout, position: Int) {
        // 1. Delete from Firebase
        firestore.collection("user-saved-workouts").document(workout.id)
            .delete()
            .addOnSuccessListener {
                Log.d("Firestore", "Workout ${workout.id} successfully deleted from cloud!")

                // 2. Delete from local data manager
                deleteWorkoutLocally(workout, position)

                Toast.makeText(this, "Workout '${workout.name}' deleted everywhere!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error deleting workout from cloud: ${e.message}")
                Toast.makeText(this, "Failed to delete workout from cloud.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkIfExistsInCloud(workoutId: String, onComplete: (Boolean) -> Unit) {
        firestore.collection("user-saved-workouts").document(workoutId)
            .get()
            .addOnSuccessListener { document ->
                onComplete(document.exists())
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    private fun loadSampleWorkouts() {
        if (WorkoutDataManager.workouts.isEmpty()) {
            val sampleWorkouts = createSampleWorkouts()
            WorkoutDataManager.workouts.addAll(sampleWorkouts)
            saveWorkoutsToLocalStorage()
            workoutAdapter.updateWorkouts(WorkoutDataManager.workouts)
        }
    }

    private fun createSampleWorkouts(): List<Workout> {
        return listOf(
            Workout(
                id = "leg_day",
                name = "Leg Day",
                description = "Complete lower body workout",
                exercises = listOf(
                    createMasterExerciseList().find { it.id == "barbell_back_squat" }!!.copy(defaultSets = 4, defaultReps = 8),
                    createMasterExerciseList().find { it.id == "romanian_deadlift" }!!.copy(defaultSets = 3, defaultReps = 10)
                ),
                createdBy = "Gainly"
            ),
            Workout(
                id = "upper_body",
                name = "Upper Body",
                description = "Chest and back focus",
                exercises = listOf(
                    createMasterExerciseList().find { it.id == "dumbbell_fly" }!!.copy(defaultSets = 3, defaultReps = 12),
                    createMasterExerciseList().find { it.id == "deadlift" }!!.copy(defaultSets = 4, defaultReps = 5)
                ),
                createdBy = "Gainly"
            )
        )
    }

    private fun createMasterExerciseList(): List<Exercise> {
        return listOf(
            // CHEST EXERCISES
            Exercise(
                id = "barbell_bench_press",
                name = "Barbell Bench Press",
                description = "The cornerstone of chest development. Lying on a flat bench, you lower a barbell to your mid-chest and press it back up.\n\nHow-to: Lie on a flat bench with feet firmly on the floor. Grip the bar slightly wider than shoulder-width. Lower the bar to your mid-chest with control, keeping your elbows at a 45-75 degree angle from your body. Press the bar back up to the starting position.",
                targetMuscle = "Chest, Triceps, Shoulders",
                category = "Chest",
                defaultSets = 4,
                defaultReps = 8,
                imageResId = R.drawable.barbell_bench_press
            ),
            Exercise(
                id = "incline_dumbbell_press",
                name = "Incline Dumbbell Press",
                description = "Targets the upper pectorals, helping to build a full, balanced chest.\n\nHow-to: Set a bench to a 30-45 degree incline. Sit back with a dumbbell in each hand at your chest. Press the dumbbells up until your arms are extended but not locked. Lower them back down with control.",
                targetMuscle = "Upper Chest, Shoulders",
                category = "Chest",
                defaultSets = 4,
                defaultReps = 10,
                imageResId = R.drawable.incline_dumbell_press
            ),
            Exercise(
                id = "dumbbell_fly",
                name = "Dumbbell Fly",
                description = "An isolation movement that focuses on the stretch and contraction of the chest muscles.\n\nHow-to: Lie on a flat or incline bench, holding dumbbells above your chest with a slight bend in your elbows. With a controlled motion, lower the dumbbells out to your sides in a wide arc until you feel a deep stretch in your chest. Squeeze your chest to bring the dumbbells back to the starting position.",
                targetMuscle = "Chest",
                category = "Chest",
                defaultSets = 3,
                defaultReps = 12,
                imageResId = R.drawable.dumbbell_fly
            ),
            Exercise(
                id = "push_up",
                name = "Push-Up",
                description = "A fundamental bodyweight exercise that builds chest, shoulder, and tricep strength.\n\nHow-to: Start in a high plank position with hands slightly wider than shoulders. Keep your body in a straight line from head to heels. Lower your body until your chest nearly touches the floor, then push back up to the start.",
                targetMuscle = "Chest, Shoulders, Triceps",
                category = "Chest",
                defaultSets = 3,
                defaultReps = 15,
                imageResId = R.drawable.push_up
            ),
            Exercise(
                id = "cable_crossover",
                name = "Cable Crossover",
                description = "Provides constant tension on the chest, excellent for building muscle definition and a peak contraction.\n\nHow-to: Set two cable pulleys to a high position. Grab a handle in each hand and step forward, leaning slightly. With a slight bend in your elbows, pull the cables down and across your body in a wide arc, squeezing your chest hard at the bottom.",
                targetMuscle = "Chest",
                category = "Chest",
                defaultSets = 3,
                defaultReps = 12,
                imageResId = R.drawable.cable_crossover
            ),

            // BACK EXERCISES
            Exercise(
                id = "deadlift",
                name = "Deadlift",
                description = "A foundational compound lift that builds total-body strength and a powerful back.\n\nHow-to: Stand with feet hip-width, a barbell over your mid-foot. Hinge at your hips and bend knees to grip the bar. Keep your back straight, chest up, and drive through your heels to stand up tall, pulling the bar along your shins. Reverse the movement with control.",
                targetMuscle = "Back, Hamstrings, Glutes",
                category = "Back",
                defaultSets = 4,
                defaultReps = 5,
                imageResId = R.drawable.deadlift
            ),
            Exercise(
                id = "pull_up",
                name = "Pull-Up",
                description = "The ultimate exercise for building wide lats.\n\nHow-to: Grab a bar with an overhand grip, wider than your shoulders. Hang with arms extended. Pull your chest towards the bar by driving your elbows down, squeezing your back. Lower yourself with control.",
                targetMuscle = "Back, Biceps",
                category = "Back",
                defaultSets = 3,
                defaultReps = 8,
                imageResId = R.drawable.pull_up
            ),
            Exercise(
                id = "bent_over_barbell_row",
                name = "Bent-Over Barbell Row",
                description = "A mass-builder for the entire back, focusing on the mid-back and lats.\n\nHow-to: Stand with feet shoulder-width, holding a barbell. Hinge at your hips until your torso is nearly parallel to the floor, knees slightly bent. Pull the bar towards your lower chest/upper stomach, squeezing your back. Lower the bar with control.",
                targetMuscle = "Back, Biceps",
                category = "Back",
                defaultSets = 4,
                defaultReps = 8,
                imageResId = R.drawable.bent_over_barbell_row
            ),
            Exercise(
                id = "seated_cable_row",
                name = "Seated Cable Row",
                description = "Excellent for building thickness in the mid-back and rhomboids.\n\nHow-to: Sit at a cable row machine with feet braced. Grab the handle (V-grip is common). With a straight back, pull the handle towards your stomach, driving your elbows back and squeezing your shoulder blades. Extend your arms to feel a stretch in your back.",
                targetMuscle = "Back, Biceps",
                category = "Back",
                defaultSets = 3,
                defaultReps = 10,
                imageResId = R.drawable.seated_cable_row
            ),
            Exercise(
                id = "face_pull",
                name = "Face Pull",
                description = "A crucial exercise for shoulder health and building rear delts/upper back.\n\nHow-to: Set a cable pulley at chest height with a rope attachment. Grab the ropes and step back. Pull the ropes directly towards your face, splitting the rope apart so your hands end up by your ears. Squeeze your rear delts and upper back.",
                targetMuscle = "Rear Delts, Upper Back",
                category = "Back",
                defaultSets = 3,
                defaultReps = 15,
                imageResId = R.drawable.face_pull
            ),

            // SHOULDER EXERCISES
            Exercise(
                id = "overhead_press",
                name = "Overhead Press",
                description = "The primary compound movement for building strong, rounded shoulders.\n\nHow-to: Stand or sit with a barbell at your upper chest or dumbbells at your shoulders. Press the weight directly overhead until your arms are fully extended. Keep your core tight. Lower the weight with control.",
                targetMuscle = "Shoulders, Triceps",
                category = "Shoulders",
                defaultSets = 4,
                defaultReps = 8,
                imageResId = R.drawable.overhead_press
            ),
            Exercise(
                id = "dumbbell_lateral_raise",
                name = "Dumbbell Lateral Raise",
                description = "Isolates the medial (side) delt, which is key for shoulder width.\n\nHow-to: Stand holding a dumbbell in each hand at your sides. With a slight bend in your elbows, raise the dumbbells out to your sides until they are at shoulder height. Lower them with control. Avoid using momentum.",
                targetMuscle = "Shoulders",
                category = "Shoulders",
                defaultSets = 3,
                defaultReps = 12,
                imageResId = R.drawable.dumbbell_lateral_raise
            ),
            Exercise(
                id = "dumbbell_front_raise",
                name = "Dumbbell Front Raise",
                description = "Targets the anterior (front) delt.\n\nHow-to: Stand holding a dumbbell in each hand in front of your thighs. Keeping your arms straight, raise one dumbbell directly in front of you to shoulder height. Lower it with control and alternate arms.",
                targetMuscle = "Front Shoulders",
                category = "Shoulders",
                defaultSets = 3,
                defaultReps = 12,
                imageResId = R.drawable.dumbbell_front_raise
            ),
            Exercise(
                id = "bent_over_dumbbell_reverse_fly",
                name = "Bent-Over Dumbbell Reverse Fly",
                description = "Targets the posterior (rear) delt, crucial for balanced shoulder development and posture.\n\nHow-to: Hinge at your hips until your torso is nearly parallel to the floor, holding light dumbbells. With a slight bend in your elbows, raise the dumbbells out to your sides, squeezing your rear delts. Lower with control.",
                targetMuscle = "Rear Shoulders",
                category = "Shoulders",
                defaultSets = 3,
                defaultReps = 12,
                imageResId = R.drawable.bent_over_dumbbell_reverse_fly
            ),
            Exercise(
                id = "cable_upright_row",
                name = "Cable Upright Row",
                description = "Works the side delts and traps. Use a controlled motion to be shoulder-friendly.\n\nHow-to: Stand facing a cable machine with a straight bar attachment set low. Grip the bar with hands close together. Pull the bar straight up along your body, leading with your elbows, until the bar reaches chest level. Lower with control.",
                targetMuscle = "Rear Shoulders",
                category = "Shoulders",
                defaultSets = 3,
                defaultReps = 10,
                imageResId = R.drawable.cable_upright_row
            ),

            // LEG EXERCISES
            Exercise(
                id = "barbell_back_squat",
                name = "Barbell Back Squat",
                description = "The king of lower-body exercises. Builds overall leg strength and size.\n\nHow-to: Rest a barbell across your upper back. Stand with feet shoulder-width apart. Keeping your chest up and back straight, lower your hips as if sitting in a chair until your thighs are at least parallel to the floor. Drive through your heels to return to the start.",
                targetMuscle = "Quadriceps, Glutes, Hamstrings",
                category = "Legs",
                defaultSets = 4,
                defaultReps = 8,
                imageResId = R.drawable.barbell_back_squat
            ),
            Exercise(
                id = "romanian_deadlift",
                name = "Romanian Deadlift",
                description = "Unlocks the hamstrings and glutes through a hip-hinging movement.\n\nHow-to: Hold a barbell or dumbbells in front of your thighs. With a slight bend in your knees, hinge at your hips, pushing them back. Keep your back straight as you lower the weight down your shins until you feel a deep stretch in your hamstrings. Squeeze your glutes to return to the start.",
                targetMuscle = "Hamstrings, Glutes",
                category = "Legs",
                defaultSets = 3,
                defaultReps = 10,
                imageResId = R.drawable.romanian_deadlift
            ),
            Exercise(
                id = "bulgarian_split_squat",
                name = "Bulgarian Split Squat",
                description = "A single-leg exercise that builds incredible leg strength and stability.\n\nHow-to: Stand a few feet in front of a bench. Rest the top of one foot on the bench behind you. Lower your body down until your front thigh is parallel to the floor, keeping your knee behind your toe. Drive through your front foot to return to the start.",
                targetMuscle = "Quadriceps, Glutes",
                category = "Legs",
                defaultSets = 3,
                defaultReps = 10,
                imageResId = R.drawable.bulgarian_split_squat
            ),
            Exercise(
                id = "leg_press",
                name = "Leg Press",
                description = "A machine-based compound movement that allows you to safely load the quads and glutes with heavy weight.\n\nHow-to: Sit in the machine and place your feet shoulder-width on the platform. Lower the safety bars and press the platform away until your legs are extended (do not lock knees). Lower the weight with control until your knees are at a 90-degree angle.",
                targetMuscle = "Quadriceps, Glutes",
                category = "Legs",
                defaultSets = 4,
                defaultReps = 12,
                imageResId = R.drawable.leg_press
            ),
            Exercise(
                id = "lying_leg_curl",
                name = "Lying Leg Curl",
                description = "Isolates the hamstrings.\n\nHow-to: Lie face down on the machine, with the pad resting on the back of your ankles. Curl your heels towards your glutes, squeezing your hamstrings. Lower the weight with control.",
                targetMuscle = "Hamstrings",
                category = "Legs",
                defaultSets = 3,
                defaultReps = 12,
                imageResId = R.drawable.lying_leg_curl
            ),
            Exercise(
                id = "walking_lunges",
                name = "Walking Lunges",
                description = "A dynamic leg exercise that improves coordination and unilateral strength.\n\nHow-to: Stand holding dumbbells at your sides. Take a large step forward and lower your back knee until it almost touches the floor. Both knees should be at 90-degree angles. Push off your back foot to bring it forward into the next lunge.",
                targetMuscle = "Quadriceps, Glutes",
                category = "Legs",
                defaultSets = 3,
                defaultReps = 10,
                imageResId = R.drawable.walking_lunges
            ),

            // ARM EXERCISES
            Exercise(
                id = "barbell_bicep_curl",
                name = "Barbell Bicep Curl",
                description = "A classic mass-builder for the biceps.\n\nHow-to: Stand holding a barbell with an underhand grip, hands shoulder-width. Keeping your elbows pinned to your sides, curl the bar up towards your shoulders. Squeeze your biceps at the top, then lower with control.",
                targetMuscle = "Biceps",
                category = "Arms",
                defaultSets = 3,
                defaultReps = 10,
                imageResId = R.drawable.barbell_bicep_curl
            ),
            Exercise(
                id = "skull_crusher",
                name = "Skull Crusher",
                description = "A highly effective isolation movement for the triceps.\n\nHow-to: Lie on a flat bench holding an EZ-bar or dumbbells above your chest. Hinge at the elbows to lower the weight towards the top of your head. Keep your upper arms stationary. Extend your elbows to press the weight back to the start.",
                targetMuscle = "Triceps",
                category = "Arms",
                defaultSets = 3,
                defaultReps = 10,
                imageResId = R.drawable.skull_crusher
            ),
            Exercise(
                id = "hammer_curl",
                name = "Hammer Curl",
                description = "Targets the biceps brachialis and brachioradialis, adding thickness to the upper arm.\n\nHow-to: Hold dumbbells at your sides with a neutral (palms-facing) grip. Curl the dumbbells up, keeping your palms facing each other. Squeeze at the top and lower with control.",
                targetMuscle = "Biceps, Forearms",
                category = "Arms",
                defaultSets = 3,
                defaultReps = 12,
                imageResId = R.drawable.hammer_curl
            ),
            Exercise(
                id = "tricep_pushdown",
                name = "Tricep Pushdown",
                description = "The go-to exercise for tricep definition and the horseshoe shape.\n\nHow-to: Stand facing a high-pulley cable machine with a rope or bar attachment. Grip the attachment and push it down until your elbows are fully extended, squeezing your triceps. Control the weight back up.",
                targetMuscle = "Triceps",
                category = "Arms",
                defaultSets = 3,
                defaultReps = 12,
                imageResId = R.drawable.tricep_pushdown
            ),
            Exercise(
                id = "close_grip_bench_press",
                name = "Close-Grip Bench Press",
                description = "A compound tricep movement that allows you to move significant weight.\n\nHow-to: Perform a bench press with your hands placed shoulder-width apart or closer. This places more emphasis on the triceps.",
                targetMuscle = "Triceps, Chest",
                category = "Arms",
                defaultSets = 4,
                defaultReps = 8,
                imageResId = R.drawable.close_grip_bench_press
            ),

            // CORE EXERCISES
            Exercise(
                id = "plank",
                name = "Plank",
                description = "A fundamental isometric exercise for core stability.\n\nHow-to: Lie on your stomach and prop yourself up on your forearms and toes. Keep your body in a straight line from head to heels, engaging your core and glutes. Do not let your hips sag.",
                targetMuscle = "Core, Abs",
                category = "Core",
                defaultSets = 3,
                defaultReps = 1, // Hold for time instead of reps
                imageResId = R.drawable.plank
            ),
            Exercise(
                id = "hanging_leg_raise",
                name = "Hanging Leg Raise",
                description = "An advanced movement that targets the entire abdominal wall, especially the lower abs.\n\nHow-to: Hang from a pull-up bar with a firm grip. Keeping your legs relatively straight, use your lower abs to raise your legs until they are parallel to the floor. Lower them with control.",
                targetMuscle = "Abs, Core",
                category = "Core",
                defaultSets = 3,
                defaultReps = 12,
                imageResId = R.drawable.hanging_leg_raise
            ),
            Exercise(
                id = "russian_twist",
                name = "Russian Twist",
                description = "Targets the obliques and rotational core strength.\n\nHow-to: Sit on the floor with knees bent, lean back to engage your core. Hold a weight or medicine ball with both hands. Rotate your torso to one side, tap the weight on the floor, then rotate to the other side.",
                targetMuscle = "Obliques, Core",
                category = "Core",
                defaultSets = 3,
                defaultReps = 15,
                imageResId = R.drawable.russian_twist
            ),
            Exercise(
                id = "cable_crunch",
                name = "Cable Crunch",
                description = "Effectively loads the rectus abdominis (the 'six-pack' muscle) for growth.\n\nHow-to: Kneel below a high-pulley cable with a rope attachment. Grab the ropes and position them by your head. Flex your spine, bringing your elbows down towards your knees. Squeeze your abs hard and return with control.",
                targetMuscle = "Abs",
                category = "Core",
                defaultSets = 3,
                defaultReps = 15,
                imageResId = R.drawable.cable_crunch
            ),
            Exercise(
                id = "dead_bug",
                name = "Dead Bug",
                description = "An excellent exercise for core stability and preventing lower back arching.\n\nHow-to: Lie on your back with arms extended towards the ceiling and legs raised with knees bent at 90 degrees. Slowly lower your right arm and left leg towards the floor simultaneously, without letting your back arch. Return to the start and alternate sides.",
                targetMuscle = "Core, Abs",
                category = "Core",
                defaultSets = 3,
                defaultReps = 10,
                imageResId = R.drawable.dead_bug
            )
        )
    }


    private fun scrollToTop() {
        val scrollView = findViewById<ScrollView>(R.id.workoutSv)
        scrollView?.post {
            scrollView.scrollTo(0, 0)
        }
    }
}
