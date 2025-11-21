package com.itismob.s17.gainly

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object PlanStorageManager {

    private const val PREFS_NAME = "GainlyPlanPrefs"
    private const val PLANS_KEY = "user_plans"

    // Saves the entire list of plans to SharedPreferences using JSON
    fun savePlans(context: Context, plans: List<Plan>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()

        plans.forEach { plan ->
            val planObject = JSONObject().apply {
                put("year", plan.year)
                put("month", plan.month)
                put("day", plan.day)
                put("hour", plan.hour)
                put("minute", plan.minute)

                // Store workout ID only to avoid duplication
                put("workoutId", plan.workout.id)
            }
            jsonArray.put(planObject)
        }

        prefs.edit().putString(PLANS_KEY, jsonArray.toString()).apply()
    }

    // Loads the list of plans from SharedPreferences using JSON
    fun loadPlans(context: Context): ArrayList<Plan> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(PLANS_KEY, null)
        val plans = ArrayList<Plan>()

        if (jsonString != null) {
            try {
                val jsonArray = JSONArray(jsonString)

                for (i in 0 until jsonArray.length()) {
                    val planObject = jsonArray.getJSONObject(i)

                    // Look up workout by ID from WorkoutDataManager
                    val workoutId = planObject.getString("workoutId")
                    val workout = WorkoutDataManager.getWorkoutById(workoutId)

                    if (workout != null) {
                        val plan = Plan(
                            year = planObject.getInt("year"),
                            month = planObject.getInt("month"),
                            day = planObject.getInt("day"),
                            hour = planObject.getInt("hour"),
                            minute = planObject.getInt("minute"),
                            workout = workout
                        )
                        plans.add(plan)
                    } else {
                        // Log warning if workout not found
                        android.util.Log.w("PlanStorageManager", "Workout with ID $workoutId not found in WorkoutDataManager")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Return empty list if parsing fails
            }
        }

        return plans
    }

    // Delete a specific plan (by comparing all fields since there's no ID)
    fun deletePlan(context: Context, planToDelete: Plan) {
        val plans = loadPlans(context)
        val updatedPlans = plans.filter {
            it.year != planToDelete.year ||
                    it.month != planToDelete.month ||
                    it.day != planToDelete.day ||
                    it.hour != planToDelete.hour ||
                    it.minute != planToDelete.minute ||
                    it.workout.id != planToDelete.workout.id
        }
        savePlans(context, updatedPlans)
    }

    // Update a specific plan
    fun updatePlan(context: Context, oldPlan: Plan, newPlan: Plan) {
        val plans = loadPlans(context)
        val updatedPlans = plans.map {
            if (it.year == oldPlan.year &&
                it.month == oldPlan.month &&
                it.day == oldPlan.day &&
                it.hour == oldPlan.hour &&
                it.minute == oldPlan.minute &&
                it.workout.id == oldPlan.workout.id) {
                newPlan
            } else {
                it
            }
        }
        savePlans(context, updatedPlans)
    }

    // Helper function to check if a plan already exists
    fun planExists(context: Context, plan: Plan): Boolean {
        val plans = loadPlans(context)
        return plans.any {
            it.year == plan.year &&
                    it.month == plan.month &&
                    it.day == plan.day &&
                    it.hour == plan.hour &&
                    it.minute == plan.minute &&
                    it.workout.id == plan.workout.id
        }
    }

    // Get plans for a specific date
    fun getPlansForDate(context: Context, year: Int, month: Int, day: Int): List<Plan> {
        val plans = loadPlans(context)
        return plans.filter {
            it.year == year && it.month == month && it.day == day
        }
    }

    // Get upcoming plans (today and future)
    fun getUpcomingPlans(context: Context): List<Plan> {
        val plans = loadPlans(context)
        val currentTime = System.currentTimeMillis()

        return plans.filter { plan ->
            // Convert plan date to milliseconds for comparison
            val planTime = java.util.Calendar.getInstance().apply {
                set(plan.year, plan.month, plan.day, plan.hour, plan.minute)
            }.timeInMillis

            planTime >= currentTime
        }.sortedBy { plan ->
            plan.year * 100000000L + plan.month * 1000000L + plan.day * 10000L + plan.hour * 100L + plan.minute
        }
    }
}