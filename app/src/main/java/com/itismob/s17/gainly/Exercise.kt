package com.itismob.s17.gainly

import java.io.Serializable

data class Exercise(
    val name: String,
    val description: String,
    val imageResId: Int = R.drawable.gainly_logo,
    val targetMuscle: String,
    val category: String = "",
    val defaultSets: Int = 3,
    val defaultReps: Int = 10,
    val imageResId: Int = R.drawable.gainly_logo
) {

    constructor() : this("", "", "", "", "", 3, 10)
    // Helper to create consistent IDs
    companion object {
        fun generateId(): String = "exercise_${System.currentTimeMillis()}"
    }
}
