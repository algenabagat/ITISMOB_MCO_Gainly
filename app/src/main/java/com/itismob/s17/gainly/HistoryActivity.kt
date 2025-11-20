package com.itismob.s17.gainly

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryActivity : BaseActivity() {

    private lateinit var totalWorkoutsValue: TextView
    private lateinit var totalWorkoutsSub: TextView
    private lateinit var totalSetsValue: TextView
    private lateinit var totalSetsSub: TextView
    private lateinit var totalTimeValue: TextView
    private lateinit var totalTimeSub: TextView
    private lateinit var recentWorkoutsContainer: LinearLayout
    private lateinit var noHistoryLayout: LinearLayout

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_layout)

        initViews()
        loadWorkoutHistory()
    }

    private fun initViews() {
        totalWorkoutsValue = findViewById(R.id.totalWorkoutsValue)
        totalWorkoutsSub = findViewById(R.id.totalWorkoutsSub)
        totalSetsValue = findViewById(R.id.totalSetsValue)
        totalSetsSub = findViewById(R.id.totalSetsSub)
        totalTimeValue = findViewById(R.id.totalTimeValue)
        totalTimeSub = findViewById(R.id.totalTimeSub)
        recentWorkoutsContainer = findViewById(R.id.recentWorkoutsContainer)

        // Just keep this single line
        noHistoryLayout = findViewById(R.id.noHistoryLayout)
    }


    private fun loadWorkoutHistory() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            showNoHistory()
            return
        }

        db.collection("user-workout-data")
            .document(currentUser.email ?: "unknown")
            .collection("workout-session")
            .get()
            .addOnSuccessListener { documents ->
                val sessions = mutableListOf<WorkoutSessionData>()
                var totalWorkouts = 0
                var totalSets = 0
                var totalTime = 0L
                var totalVolume = 0.0

                for (document in documents) {
                    val session = document.toObject(WorkoutSessionData::class.java)
                    sessions.add(session)

                    totalWorkouts++
                    totalSets += session.completedSets
                    totalTime += session.duration
                    totalVolume += session.completedVolume
                }

                // Sort sessions by date (newest first)
                sessions.sortByDescending { it.date }

                if (sessions.isEmpty()) {
                    showNoHistory()
                } else {
                    showWorkoutHistory(sessions)
                    updateStats(totalWorkouts, totalSets, totalTime, totalVolume, sessions)
                }
            }
            .addOnFailureListener { exception ->
                showNoHistory()
                // You might want to show a Toast here
            }
    }

    private fun showNoHistory() {
        noHistoryLayout.isVisible = true
        recentWorkoutsContainer.isVisible = false

        // Reset stats to 0
        totalWorkoutsValue.text = "0"
        totalWorkoutsSub.text = "0 this week"
        totalSetsValue.text = "0"
        totalSetsSub.text = "Avg 0 per workout"
        totalTimeValue.text = "0"
        totalTimeSub.text = "Avg 0/min per workout"
    }

    private fun showWorkoutHistory(sessions: List<WorkoutSessionData>) {
        noHistoryLayout.isVisible = false
        recentWorkoutsContainer.isVisible = true

        // Clear existing views
        recentWorkoutsContainer.removeAllViews()

        // Add each session to the container (similar to workout summary)
        sessions.forEach { session ->
            addSessionView(session)
        }
    }

    private fun addSessionView(session: WorkoutSessionData) {
        val sessionView = layoutInflater.inflate(R.layout.item_workout_stats, recentWorkoutsContainer, false)

        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        val totalTime = formatTime(session.duration)

        sessionView.findViewById<TextView>(R.id.totalTime).text = "Total Time: $totalTime"
        sessionView.findViewById<TextView>(R.id.totalSets).text = "Completed Sets: ${session.completedSets}/${session.totalSets}"
        sessionView.findViewById<TextView>(R.id.totalReps).text = "Completed Reps: ${session.completedReps}"
        sessionView.findViewById<TextView>(R.id.totalWeight).text = "Total Weight: ${String.format("%.1f", session.completedVolume)} kg"
        sessionView.findViewById<TextView>(R.id.completedDate).text = "Completed: ${dateFormat.format(Date(session.date))}"

        // Add workout name at the top
        val workoutNameView = TextView(this).apply {
            text = session.workoutName
            textSize = 18f
            setTextColor(resources.getColor(android.R.color.black))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        (sessionView as LinearLayout).addView(workoutNameView, 0)

        recentWorkoutsContainer.addView(sessionView)
    }

    private fun updateStats(totalWorkouts: Int, totalSets: Int, totalTime: Long, totalVolume: Double, sessions: List<WorkoutSessionData>) {
        // Update total workouts
        totalWorkoutsValue.text = totalWorkouts.toString()

        // Calculate workouts this week
        val currentWeek = getWeekNumber(Date())
        val workoutsThisWeek = sessions.count {
            getWeekNumber(Date(it.date)) == currentWeek
        }
        totalWorkoutsSub.text = "$workoutsThisWeek this week"

        // Update total sets
        totalSetsValue.text = totalSets.toString()
        val avgSetsPerWorkout = if (totalWorkouts > 0) totalSets / totalWorkouts else 0
        totalSetsSub.text = "Avg $avgSetsPerWorkout per workout"

        // Update total time
        val totalTimeFormatted = formatTime(totalTime)
        totalTimeValue.text = totalTimeFormatted

        val avgTimePerWorkout = if (totalWorkouts > 0) totalTime / totalWorkouts else 0
        val avgTimeFormatted = formatTime(avgTimePerWorkout)
        totalTimeSub.text = "Avg $avgTimeFormatted per workout"
    }

    private fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
        } else {
            String.format("%02d:%02d", minutes, remainingSeconds)
        }
    }

    private fun getWeekNumber(date: Date): Int {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        return calendar.get(java.util.Calendar.WEEK_OF_YEAR)
    }
}

// Data class to match Firebase structure
data class WorkoutSessionData(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val workoutId: String = "",
    val workoutName: String = "",
    val date: Long = 0,
    val duration: Long = 0,
    val completed: Boolean = false,
    val completedVolume: Double = 0.0,
    val completedSets: Int = 0,
    val completedReps: Int = 0,
    val totalSets: Int = 0,
    val totalVolume: Double = 0.0,
    val notes: String = "",
    val timestamp: Long = 0,
    val exerciseCount: Int = 0,
    val exerciseSessions: List<Map<String, Any>> = emptyList()
)