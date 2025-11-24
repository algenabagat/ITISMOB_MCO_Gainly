package com.itismob.s17.gainly

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object PlanStorageManager {

    private const val PREFS_NAME = "GainlyPlanPrefs"
    private const val PLANS_KEY = "user_plans"

    private fun getPlansKey(context: Context): String {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: "unknown_user"
        return "${PLANS_KEY}$userId"
    }

    fun savePlans(context: Context, plans: List<Plan>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val plansKey = getPlansKey(context)
        val jsonArray = JSONArray()

        plans.forEach { plan ->
            val planObject = JSONObject().apply {
                put("id", plan.id)
                put("year", plan.year)
                put("month", plan.month)
                put("day", plan.day)
                put("hour", plan.hour)
                put("minute", plan.minute)
                put("workoutId", plan.workout.id)
            }
            jsonArray.put(planObject)
        }

        prefs.edit().putString(plansKey, jsonArray.toString()).apply()
    }

    fun loadPlans(context: Context): ArrayList<Plan> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val plansKey = getPlansKey(context)
        val jsonString = prefs.getString(plansKey, null)
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
                            id = planObject.getString("id"),
                            year = planObject.getInt("year"),
                            month = planObject.getInt("month"),
                            day = planObject.getInt("day"),
                            hour = planObject.getInt("hour"),
                            minute = planObject.getInt("minute"),
                            workout = workout
                        )
                        plans.add(plan)
                    } else {
                        android.util.Log.w("PlanStorageManager", "Workout with ID $workoutId not found in WorkoutDataManager")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return plans
    }

    fun generatePlanId(): String {
        return UUID.randomUUID().toString()
    }
}