package com.itismob.s17.gainly

object WorkoutDataManager {
    // In-memory storage (will be backed by SQLite/Firebase)
    val workouts = mutableListOf<Workout>()
    val workoutSessions = mutableListOf<WorkoutSession>()

    // Simple position-based access
    fun getWorkout(position: Int): Workout = workouts[position]

    fun getWorkoutById(id: String): Workout? = workouts.find { it.id == id }

    fun addWorkout(workout: Workout) {
        workouts.add(0, workout) // Add to beginning
    }

    // Start a new workout session from template
    fun startWorkoutSession(workoutPosition: Int): WorkoutSession {
        val workout = workouts[workoutPosition]

        val exerciseSessions = workout.exercises.map { exercise ->
            ExerciseSession(
                exerciseId = exercise.id,
                exerciseName = exercise.name,
                targetMuscle = exercise.targetMuscle,
                sets = List(exercise.defaultSets) { setIndex ->
                    SetRecord(
                        setNumber = setIndex + 1,
                        weight = 0.0,
                        reps = exercise.defaultReps
                    )
                }.toMutableList(), // Assuming sets are mutable
                targetReps = exercise.defaultReps,
                targetWeight = 0.0,
                completed = false
            )
        }.toMutableList()

        val session = WorkoutSession(
            id = WorkoutSession.generateId(),
            workoutId = workout.id,
            workoutName = workout.name,
            exerciseSessions = exerciseSessions,
            completed = false, // Add initial value for 'completed'
            duration = 0L // Add initial value for 'duration'
        )

        workoutSessions.add(session)
        return session
    }

    // Get session by position
    fun getWorkoutSession(position: Int): WorkoutSession = workoutSessions[position]

    // Simple search
    fun searchWorkouts(query: String): List<Workout> {
        return workouts.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
        }
    }

    // Get favorite workouts
    fun getFavoriteWorkouts(): List<Workout> {
        return workouts.filter { it.isFavorite }
    }

    fun saveWorkoutSession(session: WorkoutSession) {
        val existingSession = workoutSessions.find { it.id == session.id }
        if (existingSession != null) {
            // Update existing session
            val index = workoutSessions.indexOf(existingSession)
            workoutSessions[index] = session
        } else {
            // Add new session
            workoutSessions.add(session)
        }
    }

}
