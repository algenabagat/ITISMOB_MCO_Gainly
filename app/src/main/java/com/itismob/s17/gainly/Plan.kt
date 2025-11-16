package com.itismob.s17.gainly

data class Plan(
    val id: String = "",
    val year: Int,
    val month: Int, // 0-11 (Java Calendar style)
    val day: Int,
    val hour: Int,
    val minute: Int,
    val workoutId: String, // Reference to workout template
    val workoutName: String,
    val notes: String = "",
    val completed: Boolean = false
) {
    fun getFormattedDateTime(): String {
        return "$month/$day/$year at $hour:${minute.toString().padStart(2, '0')}"
    }

    fun getTimestamp(): Long {
        // Convert to timestamp for sorting/comparison
        val calendar = java.util.Calendar.getInstance()
        calendar.set(year, month, day, hour, minute)
        return calendar.timeInMillis
    }

    companion object {
        fun generateId(): String = "plan_${System.currentTimeMillis()}"
    }
}
