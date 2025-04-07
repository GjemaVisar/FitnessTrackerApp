package com.example.fitnesstrackerapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstrackerapp.R
import com.example.fitnesstrackerapp.data.entities.Workout
import java.text.SimpleDateFormat
import java.util.*

class WorkoutAdapter(
    private val workouts: List<Workout>,
    private val onWorkoutClick: (Workout) -> Unit,
    private val onWorkoutLongClick: (Workout) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>()
 {

    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvWorkoutType: TextView = itemView.findViewById(R.id.tvWorkoutType)
        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        val tvCalories: TextView = itemView.findViewById(R.id.tvCalories)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout, parent, false)
        return WorkoutViewHolder(view)
    }

     override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
         val workout = workouts[position]
         holder.tvWorkoutType.text = workout.workout_type
         holder.tvDuration.text = "Duration: ${workout.duration} min"
         holder.tvCalories.text = "Calories: ${workout.calories_burned} kcal"

         val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
         holder.tvDate.text = dateFormat.format(Date(workout.date))

         holder.itemView.setOnClickListener {
             onWorkoutClick(workout)
         }

         holder.itemView.setOnLongClickListener {
             onWorkoutLongClick(workout)
             true
         }
     }


    override fun getItemCount() = workouts.size
}
