package com.itismob.s17.gainly

import java.io.Serializable

data class Exercise(
    val name: String,
    val description: String,
    val imageResId: Int = R.drawable.gainly_logo,
    val targetMuscle: String,
    var sets: Int = 3,
    var reps: Int = 10,
    var lastWeight: Double = 0.0, // Track last used weight
    var personalBest: Double = 0.0 // Track personal best weight
) : Serializable