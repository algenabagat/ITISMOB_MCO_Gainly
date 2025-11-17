package com.itismob.s17.gainly

data class SetRecord(
    val setNumber: Int,
    var weight: Double,
    var reps: Int,
    var completed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getVolume(): Double = weight * reps
}
