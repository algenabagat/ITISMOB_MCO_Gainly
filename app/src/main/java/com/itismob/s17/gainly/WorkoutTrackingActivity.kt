package com.itismob.s17.gainly

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class WorkoutTrackingActivity : AppCompatActivity() {

    private lateinit var timerTv: TextView
    private lateinit var workoutNameTv: TextView
    private lateinit var exercisesContainer: LinearLayout
    private lateinit var nextBtn: Button

    private var timer: CountDownTimer? = null
    private var secondsElapsed = 0L
    private var currentWorkout: Workout? = null
    private val exerciseSessions = mutableListOf<ExerciseSession>()

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

        // Set button background color programmatically
        nextBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))

        nextBtn.setOnClickListener {
            saveWorkoutData()
            finish()
        }
    }

    private fun setupWorkout() {
        currentWorkout = intent.getSerializableExtra("workout") as? Workout
        workoutNameTv.text = currentWorkout?.name ?: "Workout"

        exercisesContainer.removeAllViews()
        currentWorkout?.exercises?.forEach { exercise ->
            addExerciseView(exercise)
        }
    }

    private fun addExerciseView(exercise: Exercise) {
        val exerciseView = LayoutInflater.from(this)
            .inflate(R.layout.exercise_tracking_item, exercisesContainer, false)

        val exerciseNameTv = exerciseView.findViewById<TextView>(R.id.exerciseNameTv)
        val setsContainer = exerciseView.findViewById<LinearLayout>(R.id.setsContainer)
        val addSetBtn = exerciseView.findViewById<Button>(R.id.addSetBtn)

        exerciseNameTv.text = exercise.name

        val sets = mutableListOf<ExerciseSet>()

        // Add initial sets
        repeat(exercise.sets) { setIndex ->
            val set = addSetView(setsContainer, exercise, setIndex + 1)
            sets.add(set)
        }

        // Store the exercise session
        exerciseSessions.add(ExerciseSession(exercise.name, sets))

        addSetBtn.setOnClickListener {
            val newSet = addSetView(setsContainer, exercise, setsContainer.childCount + 1)
            sets.add(newSet)
            // Update the exercise session with new sets list
            exerciseSessions.find { it.exerciseName == exercise.name }?.let { session ->
                exerciseSessions.remove(session)
                exerciseSessions.add(ExerciseSession(exercise.name, sets))
            }
        }

        exercisesContainer.addView(exerciseView)
    }

    private fun addSetView(setsContainer: LinearLayout, exercise: Exercise, setNumber: Int): ExerciseSet {
        val setView = LayoutInflater.from(this)
            .inflate(R.layout.set_item, setsContainer, false)

        val setNumberTv = setView.findViewById<TextView>(R.id.setNumberTv)
        val previousWeightTv = setView.findViewById<TextView>(R.id.previousWeightTv)
        val weightEditText = setView.findViewById<EditText>(R.id.weightEditText)
        val repsEditText = setView.findViewById<EditText>(R.id.repsEditText)
        val completedCheckbox = setView.findViewById<CheckBox>(R.id.completedCheckbox)

        setNumberTv.text = "$setNumber."

        // Show previous weight (use lastWeight from exercise)
        previousWeightTv.text = exercise.lastWeight.toInt().toString()

        // Set default reps to target reps
        repsEditText.setText(exercise.reps.toString())

        // Create ExerciseSet object with initial values
        val exerciseSet = ExerciseSet(
            setNumber = setNumber,
            weight = exercise.lastWeight, // Start with last weight
            reps = exercise.reps, // Start with target reps
            previousWeight = exercise.lastWeight
        )

        // Set initial values in UI
        weightEditText.setText(exercise.lastWeight.toInt().toString())

        // Set up listeners to update the ExerciseSet object in real-time
        weightEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val weight = weightEditText.text.toString().toDoubleOrNull() ?: 0.0
                exerciseSet.weight = weight
                // Auto-check when weight is entered
                if (weight > 0 && !completedCheckbox.isChecked) {
                    completedCheckbox.isChecked = true
                }
            }
        }

        repsEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val reps = repsEditText.text.toString().toIntOrNull() ?: 0
                exerciseSet.reps = reps
                // Auto-check when reps are entered
                if (reps > 0 && !completedCheckbox.isChecked) {
                    completedCheckbox.isChecked = true
                }
            }
        }

        completedCheckbox.setOnCheckedChangeListener { _, isChecked ->
            exerciseSet.completed = isChecked
            // If unchecked, clear the inputs
            if (!isChecked) {
                weightEditText.setText("")
                repsEditText.setText(exercise.reps.toString())
                exerciseSet.weight = 0.0
                exerciseSet.reps = exercise.reps
            }
        }

        setsContainer.addView(setView)
        return exerciseSet
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

    private fun saveWorkoutData() {
        currentWorkout?.let { workout ->
            // Update exercise personal bests and last weights
            workout.exercises.forEach { exercise ->
                val session = exerciseSessions.find { it.exerciseName == exercise.name }
                session?.let {
                    val maxWeight = it.getMaxWeight()
                    if (maxWeight > exercise.personalBest) {
                        exercise.personalBest = maxWeight
                    }
                    if (maxWeight > 0) {
                        exercise.lastWeight = maxWeight
                    }
                }
            }

            // Create workout session
            val workoutSession = WorkoutSession(
                workoutName = workout.name,
                exerciseSessions = exerciseSessions,
                duration = secondsElapsed,
                completed = true
            )

            // Save to storage
            saveSessionToStorage(workoutSession)
            updateWorkoutProgress(workout)

            Toast.makeText(this, "Workout completed! Progress saved.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveSessionToStorage(session: WorkoutSession) {
        // For now, just print to log - you can implement SharedPreferences or Room DB later
        println("Session saved: ${session.workoutName}")
        println("Duration: ${session.duration} seconds")
        println("Total volume: ${session.getTotalVolume()}")
        session.exerciseSessions.forEach { exerciseSession ->
            println("${exerciseSession.exerciseName}: ${exerciseSession.getMaxWeight()} lbs max")
        }
    }

    private fun updateWorkoutProgress(workout: Workout) {
        // Update your main workout list with new progress data
        // This would typically update your database or shared preferences
        println("Updated workout progress for: ${workout.name}")
        workout.exercises.forEach { exercise ->
            println("${exercise.name}: Last weight = ${exercise.lastWeight}, PB = ${exercise.personalBest}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}