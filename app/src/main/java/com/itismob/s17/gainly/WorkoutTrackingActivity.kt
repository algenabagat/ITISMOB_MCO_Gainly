package com.itismob.s17.gainly

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import androidx.compose.ui.semantics.setText
import androidx.compose.ui.semantics.text
import androidx.core.graphics.blue
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class WorkoutTrackingActivity : AppCompatActivity() {

    private lateinit var timerTv: TextView
    private lateinit var workoutNameTv: TextView
    private lateinit var exercisesContainer: LinearLayout
    private lateinit var nextBtn: Button
    private lateinit var cancelWorkoutBtn: Button

    private var timer: CountDownTimer? = null
    private var secondsElapsed = 0L
    private lateinit var currentSession: WorkoutSession
    private var currentWorkout: Workout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.workout_tracking_activity)

        initViews()
        setupWorkout()
        startTimer()
    }

    private fun initViews() {
        timerTv = findViewById(R.id.timerTv)
        workoutNameTv = findViewById(R.id.workoutNameTv)
        exercisesContainer = findViewById(R.id.exercisesContainer)
        nextBtn = findViewById(R.id.nextBtn)
        cancelWorkoutBtn = findViewById(R.id.cancelWorkoutBtn)

        // Initially disable the next button until at least one set is completed
        nextBtn.isEnabled = false
        nextBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))

        nextBtn.setOnClickListener {
            completeWorkout()
        }

        cancelWorkoutBtn.setOnClickListener {
            Toast.makeText(this, "Workout cancelled!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupWorkout() {
        // Get position from intent and start workout session
        val workoutPosition = intent.getIntExtra("workout_position", -1)
        if (workoutPosition == -1) {
            Toast.makeText(this, "Error: Workout not found.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        currentSession = WorkoutDataManager.startWorkoutSession(workoutPosition)
        currentWorkout = WorkoutDataManager.getWorkout(workoutPosition)

        workoutNameTv.text = currentSession.workoutName

        // Redraw all exercises. This is a simple way to refresh the entire UI.
        redrawAllExerciseViews()
    }

    private fun redrawAllExerciseViews() {
        exercisesContainer.removeAllViews()
        currentSession.exerciseSessions.forEachIndexed { exerciseIndex, exerciseSession ->
            addExerciseView(exerciseSession, exerciseIndex)
        }
    }

    private fun addExerciseView(exerciseSession: ExerciseSession, exerciseIndex: Int) {
        val exerciseView = LayoutInflater.from(this)
            .inflate(R.layout.exercise_tracking_item, exercisesContainer, false)

        val exerciseNameTv = exerciseView.findViewById<TextView>(R.id.exerciseNameTv)
        val setsContainer = exerciseView.findViewById<LinearLayout>(R.id.setsContainer)
        val addSetBtn = exerciseView.findViewById<Button>(R.id.addSetBtn)

        exerciseNameTv.text = exerciseSession.exerciseName

        // Add views for existing sets
        exerciseSession.sets.forEachIndexed { setIndex, _ ->
            addSetView(setsContainer, exerciseIndex, setIndex)
        }

        addSetBtn.setOnClickListener {
            // Create a new set record
            val newSetRecord = SetRecord(
                setNumber = exerciseSession.sets.size + 1,
                weight = 0.0, // Or use previous weight as default
                reps = exerciseSession.targetReps,
                completed = false
            )

            // Get the current exercise session, add the new set to its list
            val updatedSets = exerciseSession.sets.toMutableList().apply { add(newSetRecord) }
            val updatedExerciseSession = exerciseSession.copy(sets = updatedSets)

            // Update the main session object
            currentSession.exerciseSessions[exerciseIndex] = updatedExerciseSession

            // Redraw the UI for this specific exercise to show the new set
            addSetView(setsContainer, exerciseIndex, updatedSets.size - 1)
        }

        exercisesContainer.addView(exerciseView)
    }

    private fun addSetView(
        setsContainer: LinearLayout,
        exerciseIndex: Int,
        setIndex: Int
    ) {
        val setView = LayoutInflater.from(this)
            .inflate(R.layout.set_item, setsContainer, false)

        val setNumberTv = setView.findViewById<TextView>(R.id.setNumberTv)
        val previousWeightTv = setView.findViewById<TextView>(R.id.previousWeightTv)
        val weightEditText = setView.findViewById<EditText>(R.id.weightEditText)
        val repsEditText = setView.findViewById<EditText>(R.id.repsEditText)
        val completedCheckbox = setView.findViewById<CheckBox>(R.id.completedCheckbox)

        // Get the CURRENT set record from the session (not a copy)
        val exerciseSession = currentSession.exerciseSessions[exerciseIndex]
        val setRecord = exerciseSession.sets[setIndex]

        setNumberTv.text = "${setRecord.setNumber}."
        previousWeightTv.text = "${exerciseSession.targetWeight.toInt()} lbs"

        // Set initial values from the ACTUAL set record
        weightEditText.setText(if (setRecord.weight > 0) setRecord.weight.toInt().toString() else "")
        repsEditText.setText(if (setRecord.reps > 0) setRecord.reps.toString() else exerciseSession.targetReps.toString())
        completedCheckbox.isChecked = setRecord.completed

        println("DEBUG: Initializing set ${setRecord.setNumber} - completed: ${setRecord.completed}")

        // --- Event Listeners to update the session state ---
        val updateSetRecord: () -> Unit = {
            val weight = weightEditText.text.toString().toDoubleOrNull() ?: 0.0
            val reps = repsEditText.text.toString().toIntOrNull() ?: 0
            val isChecked = completedCheckbox.isChecked

            println("DEBUG: Updating set ${setRecord.setNumber} - completed: $isChecked")

            // CRITICAL FIX: Directly update the set record in the current session
            // Since sets is a MutableList, we can update it directly
            currentSession.exerciseSessions[exerciseIndex].sets[setIndex].apply {
                this.weight = weight
                this.reps = reps
                this.completed = isChecked
            }

            // Verify the update
            val updatedSet = currentSession.exerciseSessions[exerciseIndex].sets[setIndex]
            println("DEBUG: Set ${updatedSet.setNumber} verified - completed: ${updatedSet.completed}")

            checkWorkoutCompletionState()
        }

        weightEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) updateSetRecord()
        }

        repsEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) updateSetRecord()
        }

        completedCheckbox.setOnCheckedChangeListener { _, isChecked ->
            // Auto-fill values when checkbox is checked
            if (isChecked) {
                if (weightEditText.text.isNullOrEmpty()) {
                    val defaultWeight = if (setRecord.weight > 0) setRecord.weight else exerciseSession.targetWeight
                    weightEditText.setText(defaultWeight.toInt().toString())
                }
                if (repsEditText.text.isNullOrEmpty() || repsEditText.text.toString() == "0") {
                    val defaultReps = if (setRecord.reps > 0) setRecord.reps else exerciseSession.targetReps
                    repsEditText.setText(defaultReps.toString())
                }
            }
            updateSetRecord()
        }

        // Also update when user manually types values
        weightEditText.setOnKeyListener { _, _, _ ->
            updateSetRecord()
            false
        }

        repsEditText.setOnKeyListener { _, _, _ ->
            updateSetRecord()
            false
        }

        setsContainer.addView(setView)
    }

    private fun checkWorkoutCompletionState() {
        val anySetCompleted = currentSession.exerciseSessions.any { ex -> ex.sets.any { it.completed } }
        nextBtn.isEnabled = anySetCompleted
        if (anySetCompleted) {
            nextBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
        } else {
            nextBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
        }
    }

    private fun completeWorkout() {
        // Mark session as completed
        currentSession.completed = true
        currentSession.duration = secondsElapsed

        saveWorkoutSession()

        // Calculate completion stats for the toast message
        val totalSets = currentSession.exerciseSessions.sumOf { it.sets.size }
        val completedSets = currentSession.exerciseSessions.sumOf { ex -> ex.sets.count { it.completed } }
        Toast.makeText(this, "Workout completed! $completedSets/$totalSets sets done.", Toast.LENGTH_SHORT).show()

        // Go to summary screen
        val intent = Intent(this, FinishWorkoutSummaryActivity::class.java)
        intent.putExtra("session_id", currentSession.id)
        startActivity(intent)
        finish()
    }

    private fun saveWorkoutSession() {
        // This function would contain logic to save to SQLite and Firebase.
        // For now, the session is updated in WorkoutDataManager.
        WorkoutDataManager.saveWorkoutSession(currentSession)

        // Update exercise personal bests if needed
        currentSession.exerciseSessions.forEach { exerciseSession ->
            val bestSet = exerciseSession.getBestSet()
            bestSet?.let { set ->
                if (set.weight > 0) {
                    // Here you would implement logic to update a global personal best record
                }
            }
        }

        val message = "Workout saved! Total volume: ${currentSession.totalVolume} lbs"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun startTimer() {
        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                secondsElapsed++
                updateTimerText()
            }
            override fun onFinish() {}
        }.start()
    }

    private fun updateTimerText() {
        val minutes = secondsElapsed / 60
        val seconds = secondsElapsed % 60
        timerTv.text = String.format("%d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
