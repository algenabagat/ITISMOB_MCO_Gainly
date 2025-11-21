package com.itismob.s17.gainly

data class Plan(
    val id: String = PlanStorageManager.generatePlanId(),
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val workout: Workout
)