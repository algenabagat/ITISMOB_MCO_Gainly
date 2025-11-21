package com.itismob.s17.gainly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AllExercisesAdapter(
    private var allExercises: List<Exercise>,
    private val onExerciseClick: (Exercise) -> Unit,
    private val onInfoClick: (Exercise) -> Unit
) : RecyclerView.Adapter<AllExercisesAdapter.ExerciseViewHolder>() {

    private var filteredExercises: List<Exercise> = allExercises

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTv: TextView = itemView.findViewById(R.id.exerciseNameTv)
        private val muscleTv: TextView = itemView.findViewById(R.id.muscleGroupTv)
        private val infoBtn: ImageButton = itemView.findViewById(R.id.exerciseInfoBtn)
        private val textContainer: LinearLayout = itemView.findViewById(R.id.exerciseTextContainer)

        fun bind(exercise: Exercise) {
            nameTv.text = exercise.name
            muscleTv.text = exercise.targetMuscle

            // selecting an exercise
            textContainer.setOnClickListener {
                onExerciseClick(exercise)
            }

            // clicking the info
            infoBtn.setOnClickListener {
                onInfoClick(exercise)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise_selectable, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(filteredExercises[position])
    }

    override fun getItemCount(): Int {
        return filteredExercises.size
    }

    fun filterByMuscleGroup(muscleGroup: String) {
        filteredExercises = if (muscleGroup == "All") {
            allExercises
        } else {
            allExercises.filter { it.category.contains(muscleGroup, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }
}
