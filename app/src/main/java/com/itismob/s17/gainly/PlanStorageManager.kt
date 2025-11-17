package com.itismob.s17.gainly

import android.content.Context
import androidx.compose.ui.input.key.type
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PlanStorageManager {

    private const val PREFS_NAME = "GainlyPlanPrefs"
    private const val PLANS_KEY = "user_plans"
    private val gson = Gson()

    // Saves the entire list of plans to SharedPreferences
    fun savePlans(context: Context, plans: List<Plan>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = gson.toJson(plans)
        prefs.edit().putString(PLANS_KEY, jsonString).apply()
    }

    // Loads the list of plans from SharedPreferences
    fun loadPlans(context: Context): ArrayList<Plan> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(PLANS_KEY, null)

        return if (jsonString != null) {
            val type = object : TypeToken<ArrayList<Plan>>() {}.type
            gson.fromJson(jsonString, type)
        } else {
            ArrayList() // Return an empty list if no plans are saved yet
        }
    }
}