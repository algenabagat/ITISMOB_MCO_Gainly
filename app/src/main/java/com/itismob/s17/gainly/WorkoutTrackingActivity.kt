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
        val workoutPosition = intent.getIntExtra("workout_position", -1)
        if (workoutPosition == -1) {
            Toast.makeText(this, "Error: Workout not found.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        currentSession = WorkoutDataManager.startWorkoutSession(workoutPosition)
        currentWorkout = WorkoutDataManager.getWorkout(workoutPosition)

        workoutNameTv.text = currentSession.workoutName

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

        exerciseSession.sets.forEachIndexed { setIndex, _ ->
            addSetView(setsContainer, exerciseIndex, setIndex)
        }

        addSetBtn.setOnClickListener {
            val newSetRecord = SetRecord(
                setNumber = exerciseSession.sets.size + 1,
                weight = 0.0,
                reps = exerciseSession.targetReps,
                completed = false
            )

            val updatedSets = exerciseSession.sets.toMutableList().apply { add(newSetRecord) }
            val updatedExerciseSession = exerciseSession.copy(sets = updatedSets)

            currentSession.exerciseSessions[exerciseIndex] = updatedExerciseSession

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

        val exerciseSession = currentSession.exerciseSessions[exerciseIndex]
        val setRecord = exerciseSession.sets[setIndex]

        setNumberTv.text = "${setRecord.setNumber}."
        previousWeightTv.text = "${exerciseSession.targetWeight.toInt()} lbs"

        weightEditText.setText(if (setRecord.weight > 0) setRecord.weight.toInt().toString() else "")
        repsEditText.setText(if (setRecord.reps > 0) setRecord.reps.toString() else exerciseSession.targetReps.toString())
        completedCheckbox.isChecked = setRecord.completed

        println("DEBUG: Initializing set ${setRecord.setNumber} - completed: ${setRecord.completed}")

        val updateSetRecord: () -> Unit = {
            val weight = weightEditText.text.toString().toDoubleOrNull() ?: 0.0
            val reps = repsEditText.text.toString().toIntOrNull() ?: 0
            val isChecked = completedCheckbox.isChecked

            println("DEBUG: Updating set ${setRecord.setNumber} - completed: $isChecked")

            currentSession.exerciseSessions[exerciseIndex].sets[setIndex].apply {
                this.weight = weight
                this.reps = reps
                this.completed = isChecked
            }

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
        currentSession.completed = true
        currentSession.duration = secondsElapsed

        saveWorkoutSession()

        val totalSets = currentSession.exerciseSessions.sumOf { it.sets.size }
        val completedSets = currentSession.exerciseSessions.sumOf { ex -> ex.sets.count { it.completed } }
        Toast.makeText(this, "Workout completed! $completedSets/$totalSets sets done.", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, FinishWorkoutSummaryActivity::class.java)
        intent.putExtra("session_id", currentSession.id)
        startActivity(intent)
        finish()
    }

    private fun saveWorkoutSession() {
        WorkoutDataManager.saveWorkoutSession(currentSession)

        currentSession.exerciseSessions.forEach { exerciseSession ->
            val bestSet = exerciseSession.getBestSet()
            bestSet?.let { set ->
                if (set.weight > 0) {
                    // what to do here?
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
