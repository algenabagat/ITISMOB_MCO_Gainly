package com.itismob.s17.gainly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class PlanAdapter(
    private var plans: List<Plan> = ArrayList(),
    private val onWorkoutClick: (Workout) -> Unit = {},
    private val onStartPlan: (Plan) -> Unit = {},
    private val onEditPlan: (Plan) -> Unit = {},
    private val onDeletePlan: (Plan) -> Unit = {}
) : RecyclerView.Adapter<PlanViewHolder>() {

    fun updatePlans(newPlans: List<Plan>) {
        plans = newPlans
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plan, parent, false)
        return PlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val plan = plans[position]

        holder.planNameTv.text = plan.workout.name
        val formattedMonth = String.format(Locale.US, "%02d", plan.month + 1)
        val formattedDay = String.format(Locale.US, "%02d", plan.day)
        val formattedDate = "$formattedMonth/$formattedDay/${plan.year}"
        holder.dateTv.text = formattedDate
        val formattedMinute = String.format(Locale.US, "%02d", plan.minute)
        val formattedTime = plan.hour.toString() + ":" + formattedMinute
        holder.timeTv.text = formattedTime

        holder.startPlanBtn.setOnClickListener {
            onStartPlan(plan)
        }

        holder.optionsPlanBtn.setOnClickListener { view ->
            showOptionsMenu(view, plan, holder)
        }
    }

    private fun showOptionsMenu(view: View, plan: Plan, holder: PlanViewHolder) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.plan_option, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.editPlan -> {
                    onEditPlan(plan)
                    true
                }
                R.id.deletePlan -> {
                    onDeletePlan(plan)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun getItemCount(): Int = plans.size
}
