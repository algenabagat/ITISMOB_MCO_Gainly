package com.itismob.s17.gainly

import java.io.Serializable

data class Plan(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val workout: Workout,
) : Serializable