package com.itismob.s17.gainly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView

class WorkoutAdapter(
    private var workouts: List<Workout> = ArrayList(),
    private val onExerciseClick: (Exercise) -> Unit = {},
    private val onStartWorkout: (Int) -> Unit = {}, // Now takes position instead of Workout
    private val onFavoriteToggle: (Int, Boolean) -> Unit =  { _, _ -> } // Now takes position instead of Workout } // Now takes position instead of Workout
) : RecyclerView.Adapter<WorkoutViewHolder>() {

    fun updateWorkouts(newWorkouts: List<Workout>) {
        workouts = newWorkouts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = workouts[position]

        // workout info
        holder.workoutNameTv.text = workout.name
        holder.workoutDescriptionTv.text = workout.description
        holder.exerciseCountTv.text = "${workout.exerciseCount} exercises"

        updateFavoriteButtonIcon(holder.favoriteBtn, workout.isFavorite)

        // Pass position to favorite toggle
        holder.favoriteBtn.setOnClickListener {
            val newFavoriteState = !workout.isFavorite
            onFavoriteToggle(position, newFavoriteState) // Pass position instead of workout
            updateFavoriteButtonIcon(holder.favoriteBtn, newFavoriteState)
        }

        // clear views
        holder.exercisesLayout.removeAllViews()

        // add ALL exercises (no limit)
        workout.exercises.forEach { exercise ->
            val exerciseView = TextView(holder.itemView.context).apply {
                text = "• ${exercise.name} (${exercise.defaultSets}x${exercise.defaultReps})" // Updated to use defaultSets/defaultReps
                setTextColor(holder.itemView.context.getColor(android.R.color.black))
                textSize = 12f
                setOnClickListener {
                    onExerciseClick(exercise)
                }
            }
            holder.exercisesLayout.addView(exerciseView)
        }

        // Pass position to start workout
        holder.startBtn.setOnClickListener {
            onStartWorkout(position) // Pass position instead of workout
        }

        holder.optionsBtn.setOnClickListener { view ->
            showOptionsMenu(view, workout, holder, position)
        }
    }

    private fun updateFavoriteButtonIcon(favoriteBtn: ImageButton, isFavorite: Boolean) {
        if (isFavorite) {
            favoriteBtn.setImageResource(R.drawable.favorite)
        } else {
            favoriteBtn.setImageResource(R.drawable.not_favorite)
        }
    }

    private fun showOptionsMenu(view: View, workout: Workout, holder: WorkoutViewHolder, position: Int) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.workout_option, popup.menu)

        // popup menu logic for edit and delete not needed yet
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit -> {
                    // Can use position to edit: onEditWorkout(position)
                    true
                }
                R.id.delete -> {
                    // Can use position to delete: onDeleteWorkout(position)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun getItemCount(): Int = workouts.size
}
