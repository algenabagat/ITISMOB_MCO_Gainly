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
    private val onStartWorkout: (Workout) -> Unit = {},
    private val onFavoriteToggle: (Workout, Boolean) -> Unit = { _, _ -> },

    // not needed yet:
    // private val onEditWorkout: (Workout) -> Unit = {},
    // private val onDeleteWorkout: (Workout) -> Unit = {}
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

        // basically updates the fav button state
        holder.favoriteBtn.setOnClickListener {
            val newFavoriteState = !workout.isFavorite
            onFavoriteToggle(workout, newFavoriteState)
            updateFavoriteButtonIcon(holder.favoriteBtn, newFavoriteState)
        }

        // clear views
        holder.exercisesLayout.removeAllViews()

        // add ALL exercises (no limit)
        workout.exercises.forEach { exercise ->
            val exerciseView = TextView(holder.itemView.context).apply {
                text = "• ${exercise.name} (${exercise.sets}x${exercise.reps})"
                setTextColor(holder.itemView.context.getColor(android.R.color.black))
                textSize = 12f
                setOnClickListener {
                    onExerciseClick(exercise)
                }
            }
            holder.exercisesLayout.addView(exerciseView)
        }

        holder.startBtn.setOnClickListener {
            onStartWorkout(workout)
        }

        holder.optionsBtn.setOnClickListener { view ->
            showOptionsMenu(view, workout, holder)
        }
    }

    private fun updateFavoriteButtonIcon(favoriteBtn: ImageButton, isFavorite: Boolean) {
        if (isFavorite) {
            favoriteBtn.setImageResource(R.drawable.favorite)
        } else {
            favoriteBtn.setImageResource(R.drawable.not_favorite)
        }
    }

    private fun showOptionsMenu(view: View, workout: Workout, holder: WorkoutViewHolder) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.workout_option, popup.menu)

        // popup menu logic for edit and delete not needed yet
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit -> {
                    // onEditWorkout(workout)
                    true
                }
                R.id.delete -> {
                    // onDeleteWorkout(workout)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun getItemCount(): Int = workouts.size
}
