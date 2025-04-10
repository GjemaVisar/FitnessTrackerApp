package com.example.fitnesstrackerapp.views

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnesstrackerapp.R
import com.example.fitnesstrackerapp.adapters.WorkoutAdapter
import com.example.fitnesstrackerapp.auth.SessionManager
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.data.dao.WorkoutDao
import com.example.fitnesstrackerapp.data.entities.Workout
import com.example.fitnesstrackerapp.databinding.ActivityHomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var workoutDao: WorkoutDao
    private var currentUserId: Int = -1

    private val defaultWorkouts = listOf(
        Workout(0, 1, "Running", 30, 300),
        Workout(0, 1, "Cycling", 45, 400),
        Workout(0, 1, "Swimming", 60, 500)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = SessionManager.getInstance(this).currentUserId ?: run {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val db = AppDatabase.getDatabase(this)
        workoutDao = db.workoutDao()

        setupWorkoutsRecycler()
        setupAddWorkoutButton()
        setupProfileNavigation()
    }

    private fun setupWorkoutsRecycler() {
        binding.rvWorkouts.layoutManager = LinearLayoutManager(this)
        binding.rvWorkouts.adapter = WorkoutAdapter(emptyList(), { workout ->
            showEditWorkoutDialog(workout)
        }, { workout ->
            showDeleteConfirmationDialog(workout)
        })

        loadWorkoutsFromDatabase()
    }

    private fun loadWorkoutsFromDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            val workouts = workoutDao.getWorkoutsByUser(currentUserId)

            if (workouts.isEmpty()) {
                insertDefaultWorkouts()
            }

            val finalWorkouts = workoutDao.getWorkoutsByUser(currentUserId)
            withContext(Dispatchers.Main) {
                binding.rvWorkouts.adapter = WorkoutAdapter(finalWorkouts, { workout ->
                    showEditWorkoutDialog(workout)
                }, { workout ->
                    showDeleteConfirmationDialog(workout)
                })
            }
        }
    }

    private fun setupAddWorkoutButton() {
        binding.fabAddWorkout.setOnClickListener {
            showAddWorkoutDialog()
        }
    }

    private fun showAddWorkoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_workout, null)
        val etWorkoutType = dialogView.findViewById<EditText>(R.id.etWorkoutType)
        val etDuration = dialogView.findViewById<EditText>(R.id.etDuration)
        val etCalories = dialogView.findViewById<EditText>(R.id.etCalories)

        AlertDialog.Builder(this)
            .setTitle("Add New Workout")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val workoutType = etWorkoutType.text.toString().trim()
                val duration = etDuration.text.toString().toIntOrNull()
                val calories = etCalories.text.toString().toIntOrNull()

                if (workoutType.isNotEmpty() && duration != null && calories != null) {
                    val newWorkout = Workout(
                        id = 0,
                        user_id = currentUserId,
                        workout_type = workoutType,
                        duration = duration,
                        calories_burned = calories,
                        date = System.currentTimeMillis()
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        workoutDao.insertWorkout(newWorkout)
                        loadWorkoutsFromDatabase()
                    }
                } else {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun showEditWorkoutDialog(workout: Workout) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_workout, null)
        val etWorkoutType = dialogView.findViewById<EditText>(R.id.etWorkoutType)
        val etDuration = dialogView.findViewById<EditText>(R.id.etDuration)
        val etCalories = dialogView.findViewById<EditText>(R.id.etCalories)

        etWorkoutType.setText(workout.workout_type)
        etDuration.setText(workout.duration.toString())
        etCalories.setText(workout.calories_burned.toString())

        AlertDialog.Builder(this)
            .setTitle("Edit Workout")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedType = etWorkoutType.text.toString().trim()
                val updatedDuration = etDuration.text.toString().toIntOrNull()
                val updatedCalories = etCalories.text.toString().toIntOrNull()

                if (updatedType.isNotEmpty() && updatedDuration != null && updatedCalories != null) {
                    val updatedWorkout = workout.copy(
                        workout_type = updatedType,
                        duration = updatedDuration,
                        calories_burned = updatedCalories
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        workoutDao.updateWorkout(updatedWorkout)
                        loadWorkoutsFromDatabase()
                    }
                } else {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun showDeleteConfirmationDialog(workout: Workout) {
        AlertDialog.Builder(this)
            .setTitle("Delete Workout")
            .setMessage("Are you sure you want to delete this workout?")
            .setPositiveButton("Yes") { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    workoutDao.deleteWorkout(workout)
                    loadWorkoutsFromDatabase()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun insertDefaultWorkouts() {
        CoroutineScope(Dispatchers.IO).launch {
            val workouts = workoutDao.getWorkoutsByUser(currentUserId)
            if (workouts.isEmpty()) {
                defaultWorkouts.forEach { workout ->
                    workoutDao.insertWorkout(workout.copy(user_id = currentUserId))
                }
                loadWorkoutsFromDatabase()
            }
        }
    }

    private fun setupProfileNavigation() {
        binding.btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("CURRENT_USER_ID", currentUserId)
            startActivity(intent)
        }
    }
}
