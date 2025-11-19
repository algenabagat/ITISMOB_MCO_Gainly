package com.itismob.s17.gainly

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FinishWorkoutSummaryActivity : AppCompatActivity() {

    private lateinit var capturedImageView: ImageView
    private lateinit var exercisesSummaryContainer: LinearLayout
    private lateinit var workoutSummaryTv: TextView
    private lateinit var desc3Txv: TextView
    private var latestTmpUri: Uri? = null
    private var currentSession: WorkoutSession? = null
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val takeImageResult = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            latestTmpUri?.let { uri ->
                capturedImageView.setImageURI(uri)
                Toast.makeText(this, "Image captured!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Image capture cancelled.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.finish_workout_summary)

        initViews()
        loadWorkoutSession()
        setupClickListeners()
    }

    private fun initViews() {
        capturedImageView = findViewById(R.id.capturedImageView)
        exercisesSummaryContainer = findViewById(R.id.exercisesSummaryContainer)
        workoutSummaryTv = findViewById(R.id.workoutSummary)
        desc3Txv = findViewById(R.id.desc3Txv)
    }

    private fun loadWorkoutSession() {
        val sessionId = intent.getStringExtra("session_id")
        currentSession = WorkoutDataManager.workoutSessions.find { it.id == sessionId }

        if (currentSession != null) {
            displayWorkoutSummary()
        } else {
            Toast.makeText(this, "Workout session not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun displayWorkoutSummary() {
        currentSession?.let { session ->
            // Calculate ACTUAL completed workout stats
            val completedSets = session.exerciseSessions.sumOf { ex ->
                ex.sets.count { it.completed }
            }
            val totalSets = session.exerciseSessions.sumOf { it.sets.size }
            val completedReps = session.exerciseSessions.sumOf { ex ->
                ex.sets.filter { it.completed }.sumOf { it.reps }
            }
            val completedWeight = session.exerciseSessions.sumOf { ex ->
                ex.sets.filter { it.completed }.sumOf { it.getVolume() }
            }

            // Display workout stats - show ACTUAL completed values
            val totalTime = formatTime(session.duration)

            // Create workout stats view
            val statsView = layoutInflater.inflate(R.layout.item_workout_stats, exercisesSummaryContainer, false)

            statsView.findViewById<TextView>(R.id.totalTime).text = "Total Time: $totalTime"
            statsView.findViewById<TextView>(R.id.totalSets).text = "Completed Sets: $completedSets/$totalSets"
            statsView.findViewById<TextView>(R.id.totalReps).text = "Completed Reps: $completedReps"
            statsView.findViewById<TextView>(R.id.totalWeight).text = "Total Weight: ${String.format("%.1f", completedWeight)} kg"

            val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
            statsView.findViewById<TextView>(R.id.completedDate).text = "Completed: ${dateFormat.format(Date(session.date))}"

            exercisesSummaryContainer.addView(statsView)

            // Display exercises summary - only show completed exercises/sets
            session.exerciseSessions.forEach { exerciseSession ->
                // Only show exercises that have at least one completed set
                val exerciseCompletedSets = exerciseSession.sets.count { it.completed }
                if (exerciseCompletedSets > 0) {
                    addExerciseSummaryView(exerciseSession)
                }
            }

            // Update motivational text based on actual performance
            updateMotivationalText(completedSets, totalSets)
        }
    }


    private fun addExerciseSummaryView(exerciseSession: ExerciseSession) {
        val exerciseView = layoutInflater.inflate(R.layout.item_exercise_summary, exercisesSummaryContainer, false)

        exerciseView.findViewById<TextView>(R.id.exerciseName).text = exerciseSession.exerciseName

        // Calculate ACTUAL completed stats for this exercise
        val completedSets = exerciseSession.sets.count { it.completed }
        val totalSets = exerciseSession.sets.size
        val completedReps = exerciseSession.sets.filter { it.completed }.sumOf { it.reps }
        val completedWeight = exerciseSession.sets.filter { it.completed }.sumOf { it.getVolume() }

        val exerciseStats = "$completedSets/$totalSets sets • $completedReps reps • ${String.format("%.1f", completedWeight)} kg"
        exerciseView.findViewById<TextView>(R.id.exerciseStats).text = exerciseStats

        val setsContainer = exerciseView.findViewById<LinearLayout>(R.id.setsContainer)

        // Add individual set details - only show completed sets
        exerciseSession.sets.forEach { set ->
            if (set.completed) {
                addSetSummaryView(setsContainer, set)
            }
        }

        exercisesSummaryContainer.addView(exerciseView)
    }

    private fun addSetSummaryView(setsContainer: LinearLayout, setRecord: SetRecord) {
        val setView = layoutInflater.inflate(R.layout.item_set_summary, setsContainer, false)

        setView.findViewById<TextView>(R.id.setNumber).text = "Set ${setRecord.setNumber}"

        val setDetails = "${setRecord.reps} reps • ${String.format("%.1f", setRecord.weight)} kg"
        setView.findViewById<TextView>(R.id.setDetails).text = setDetails

        setsContainer.addView(setView)
    }

    private fun updateMotivationalText(completedSets: Int, totalSets: Int) {
        val completionRate = if (totalSets > 0) completedSets.toDouble() / totalSets else 0.0

        when {
            completionRate == 1.0 -> {
                desc3Txv.text = "Perfect workout! You completed all sets!"
            }
            completionRate >= 0.8 -> {
                desc3Txv.text = "Great job! You're making excellent progress!"
            }
            completionRate >= 0.5 -> {
                desc3Txv.text = "Good work! Keep pushing forward!"
            }
            else -> {
                desc3Txv.text = "Every workout counts! Keep it up!"
            }
        }
    }

    private fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    private fun setupClickListeners() {
        val cameraBtn = findViewById<Button>(R.id.cameraBtn)
        val shareBtn = findViewById<ImageButton>(R.id.shareBtn)
        val finishWorkoutBtn = findViewById<Button>(R.id.finishWorkoutBtn)

        cameraBtn.setOnClickListener {
            takeImage()
        }

        shareBtn.setOnClickListener {
            latestTmpUri?.let { uri ->
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, generateShareText())
                }
                startActivity(Intent.createChooser(shareIntent, "Share your workout photo"))
            } ?: run {
                // Share workout summary without image
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, generateShareText())
                }
                startActivity(Intent.createChooser(shareIntent, "Share your workout"))
            }
        }

        finishWorkoutBtn.setOnClickListener {
            saveWorkoutData()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun generateShareText(): String {
        currentSession?.let { session ->
            val completedSets = session.exerciseSessions.sumOf { ex ->
                ex.sets.count { it.completed }
            }
            val totalSets = session.exerciseSessions.sumOf { it.sets.size }
            val completedWeight = session.exerciseSessions.sumOf { ex ->
                ex.sets.filter { it.completed }.sumOf { it.getVolume() }
            }
            val totalTime = formatTime(session.duration)

            return "Just completed ${session.workoutName} on Gainly! " +
                    "$totalTime • $completedSets/$totalSets sets • ${String.format("%.1f", completedWeight)} kg total weight. " +
                    "Check out Gainly for your fitness journey!"
        }
        return "Check out my workout on Gainly!"
    }

    private fun saveWorkoutData() {
        currentSession?.let { session ->
            // Save locally
            saveWorkoutLocally(session)

            // Save to Firebase
            saveWorkoutToFirebase(session)
        }
    }

    private fun saveWorkoutLocally(session: WorkoutSession) {
        try {
            val sharedPref = getSharedPreferences("workout_data", MODE_PRIVATE)
            val editor = sharedPref.edit()

            // Convert session to JSON
            val sessionJson = convertSessionToJson(session)
            editor.putString(session.id, sessionJson.toString())

            // Save session IDs list
            val savedSessions = sharedPref.getStringSet("saved_sessions", mutableSetOf()) ?: mutableSetOf()
            savedSessions.add(session.id)
            editor.putStringSet("saved_sessions", savedSessions)

            editor.apply()
            Toast.makeText(this, "Workout saved locally", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving locally: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveWorkoutToFirebase(session: WorkoutSession) {
        val currentUser = auth.currentUser

        val sessionData = convertSessionToFirebaseMap(session)

        db.collection("user-workout-data")
            .document(currentUser?.email ?: "unknown")
            .collection("workout-session")
            .document(session.id)
            .set(sessionData)
            .addOnSuccessListener {
                Toast.makeText(this, "Session saved to Firebase", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save to Firebase: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun convertSessionToJson(session: WorkoutSession): JSONObject {
        return JSONObject().apply {
            put("id", session.id)
            put("workoutId", session.workoutId)
            put("workoutName", session.workoutName)
            put("date", session.date)
            put("duration", session.duration)
            put("completed", session.completed)
            put("totalVolume", session.totalVolume)
            put("notes", session.notes)

            val exercisesArray = JSONArray()
            session.exerciseSessions.forEach { exercise ->
                val exerciseObj = JSONObject().apply {
                    put("exerciseId", exercise.exerciseId)
                    put("exerciseName", exercise.exerciseName)
                    put("targetMuscle", exercise.targetMuscle)
                    put("targetReps", exercise.targetReps)
                    put("targetWeight", exercise.targetWeight)
                    put("completed", exercise.completed)
                    put("totalVolume", exercise.getTotalVolume())

                    val setsArray = JSONArray()
                    exercise.sets.forEach { set ->
                        val setObj = JSONObject().apply {
                            put("setNumber", set.setNumber)
                            put("weight", set.weight)
                            put("reps", set.reps)
                            put("completed", set.completed)
                            put("timestamp", set.timestamp)
                            put("volume", set.getVolume())
                        }
                        setsArray.put(setObj)
                    }
                    put("sets", setsArray)
                }
                exercisesArray.put(exerciseObj)
            }
            put("exerciseSessions", exercisesArray)
        }
    }

    private fun convertSessionToFirebaseMap(session: WorkoutSession): Map<String, Any> {
        val currentUser = auth.currentUser
        val userEmail = currentUser?.email ?: "unknown"
        val userId = currentUser?.uid ?: "unknown"

        // Calculate ACTUAL completed workout stats (only counting completed sets)
        val completedSets = session.exerciseSessions.sumOf { ex ->
            ex.sets.count { it.completed }
        }
        val totalSets = session.exerciseSessions.sumOf { it.sets.size }
        val completedReps = session.exerciseSessions.sumOf { ex ->
            ex.sets.filter { it.completed }.sumOf { it.reps }
        }
        val completedWeight = session.exerciseSessions.sumOf { ex ->
            ex.sets.filter { it.completed }.sumOf { it.getVolume() }
        }

        // Calculate exercise-specific completed stats
        val exerciseSessionsData = session.exerciseSessions.map { exercise ->
            val exerciseCompletedSets = exercise.sets.count { it.completed }
            val exerciseTotalSets = exercise.sets.size
            val exerciseCompletedReps = exercise.sets.filter { it.completed }.sumOf { it.reps }
            val exerciseCompletedVolume = exercise.sets.filter { it.completed }.sumOf { it.getVolume() }

            mapOf(
                "exerciseId" to exercise.exerciseId,
                "exerciseName" to exercise.exerciseName,
                "targetMuscle" to exercise.targetMuscle,
                "targetReps" to exercise.targetReps,
                "targetWeight" to exercise.targetWeight,
                "completed" to exercise.completed,

                // ACTUAL completed values for this exercise
                "completedSets" to exerciseCompletedSets,
                "totalSets" to exerciseTotalSets,
                "completedReps" to exerciseCompletedReps,
                "completedVolume" to exerciseCompletedVolume,

                // Legacy field (total volume of ALL sets - keep for backward compatibility)
                "totalVolume" to exercise.getTotalVolume(),

                "sets" to exercise.sets.map { set ->
                    mapOf(
                        "setNumber" to set.setNumber,
                        "weight" to set.weight,
                        "reps" to set.reps,
                        "completed" to set.completed,
                        "timestamp" to set.timestamp,
                        "volume" to set.getVolume()
                    )
                }
            )
        }

        return mapOf(
            "id" to session.id,
            "userId" to userId,
            "userEmail" to userEmail,
            "workoutId" to session.workoutId,
            "workoutName" to session.workoutName,
            "date" to session.date,
            "duration" to session.duration,
            "completed" to session.completed,

            // ACTUAL completed workout totals
            "completedVolume" to completedWeight,
            "completedSets" to completedSets,
            "completedReps" to completedReps,

            // Total counts (all sets, including uncompleted)
            "totalSets" to totalSets,
            "totalVolume" to session.totalVolume, // This includes uncompleted sets

            "notes" to session.notes,
            "timestamp" to System.currentTimeMillis(),
            "exerciseCount" to session.exerciseSessions.size,
            "exerciseSessions" to exerciseSessionsData
        )
    }

    private fun takeImage() {
        getTmpFileUri().let { uri ->
            latestTmpUri = uri
            takeImageResult.launch(uri)
        }
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(applicationContext, "${BuildConfig.APPLICATION_ID}.provider", tmpFile)
    }
}