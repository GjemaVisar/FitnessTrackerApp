package com.example.fitnesstrackerapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstrackerapp.R
import com.example.fitnesstrackerapp.data.entities.Workout
import java.text.SimpleDateFormat
import java.util.*

class WorkoutAdapter(
    private val workouts: List<Workout>,
    private val onEditClicked: (Workout) -> Unit,
    private val onDeleteClicked: (Workout) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    inner class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivWorkoutLogo: ImageView = itemView.findViewById(R.id.ivWorkoutLogo)
        val tvWorkoutType: TextView = itemView.findViewById(R.id.tvWorkoutType)
        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        val tvCalories: TextView = itemView.findViewById(R.id.tvCalories)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = workouts[position]

        // Set workout details
        holder.tvWorkoutType.text = workout.workout_type
        holder.tvDuration.text = "Duration: ${workout.duration} min"
        holder.tvCalories.text = "Calories: ${workout.calories_burned} kcal"

        // Set workout logo based on type
        when (workout.workout_type.toLowerCase(Locale.ROOT)) {
            "running" -> holder.ivWorkoutLogo.setImageResource(R.drawable.ic_running)
            "cycling" -> holder.ivWorkoutLogo.setImageResource(R.drawable.ic_cycling)
            "swimming" -> holder.ivWorkoutLogo.setImageResource(R.drawable.ic_swimming)
            else -> holder.ivWorkoutLogo.setImageResource(R.drawable.baseline_fitness_center_24)
        }

        // Set button click listeners
        holder.btnEdit.setOnClickListener { onEditClicked(workout) }
        holder.btnDelete.setOnClickListener { onDeleteClicked(workout) }
    }

    override fun getItemCount() = workouts.size
}