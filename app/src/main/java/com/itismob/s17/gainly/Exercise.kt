package com.itismob.s17.gainly

import java.io.Serializable

data class Exercise(
    val id: String = "",
    val name: String,
    val description: String,
    val targetMuscle: String,
    val category: String = "",
    val defaultSets: Int = 3,
    val defaultReps: Int = 10,
    val imageResId: Int = R.drawable.gainly_logo,
    var lastWeight: Double = 0.0,
    var personalBest: Double = 0.0
) : Serializable {

    constructor() : this("", "", "", "", "", 3, 10)

    companion object {
        fun generateId(): String = "exercise_${System.currentTimeMillis()}"
    }
}
