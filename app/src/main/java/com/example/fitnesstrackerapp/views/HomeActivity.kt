package com.example.fitnesstrackerapp.views

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
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
import com.example.fitnesstrackerapp.data.entities.Notification
import com.example.fitnesstrackerapp.data.entities.Workout
import com.example.fitnesstrackerapp.databinding.ActivityHomeBinding
import com.example.fitnesstrackerapp.utils.NotificationHelper
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var workoutDao: WorkoutDao
    private lateinit var notificationHelper: NotificationHelper
    private var currentUserId: Int = -1

    private val defaultWorkouts = listOf(
        Workout(0, 1, "Running", 30, 300),
        Workout(0, 1, "Cycling", 45, 400),
        Workout(0, 1, "Swimming", 60, 500)
    )

    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        notificationHelper = NotificationHelper(this)

        currentUserId = SessionManager.getInstance(this).currentUserId ?: run {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val db = AppDatabase.getDatabase(this)
        workoutDao = db.workoutDao()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }

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
                updateCalorieSummary()
            }
        }
    }

    private fun updateCalorieSummary() {
        CoroutineScope(Dispatchers.IO).launch {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = calendar.timeInMillis
            val endOfDay = startOfDay + 24 * 60 * 60 * 1000

            val workouts = workoutDao.getWorkoutsByUserAndDateRange(currentUserId, startOfDay, endOfDay)
            val totalCalories = workouts.sumOf { it.calories_burned }
            val workoutCount = workouts.size

            withContext(Dispatchers.Main) {
                binding.tvTotalCalories.text = "$totalCalories kcal"
                binding.tvWorkoutCount.text = workoutCount.toString()
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
        val switchNotification = dialogView.findViewById<SwitchMaterial>(R.id.switchNotification)
        val timePickerLayout = dialogView.findViewById<TextInputLayout>(R.id.timePickerLayout)
        val etNotificationTime = dialogView.findViewById<TextInputEditText>(R.id.etNotificationTime)

        var selectedTimeInMillis: Long = 0

        switchNotification.setOnCheckedChangeListener { _, isChecked ->
            timePickerLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        etNotificationTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val selectedCalendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hourOfDay)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                    }
                    selectedTimeInMillis = selectedCalendar.timeInMillis

                    val amPm = if (hourOfDay < 12) "AM" else "PM"
                    val displayHour = if (hourOfDay > 12) hourOfDay - 12 else hourOfDay
                    etNotificationTime.setText("$displayHour:$minute $amPm")
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        }

        AlertDialog.Builder(this)
            .setTitle("Add New Workout")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val workoutType = etWorkoutType.text.toString().trim()
                val duration = etDuration.text.toString().toIntOrNull()
                val calories = etCalories.text.toString().toIntOrNull()

                if (workoutType.isNotEmpty() && duration != null && calories != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val existingWorkout = workoutDao.getWorkoutByTypeAndUser(workoutType, currentUserId)

                            if (existingWorkout != null) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@HomeActivity, "You already have a workout with this name", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                val newWorkout = Workout(
                                    id = 0,
                                    user_id = currentUserId,
                                    workout_type = workoutType,
                                    duration = duration,
                                    calories_burned = calories,
                                    date = System.currentTimeMillis()
                                )

                                val workoutId = workoutDao.insertWorkout(newWorkout).toInt()
                                val workoutWithId = newWorkout.copy(id = workoutId)

                                if (switchNotification.isChecked && selectedTimeInMillis > 0) {
                                    withContext(Dispatchers.Main) {
                                        notificationHelper.scheduleWorkoutReminder(workoutWithId, selectedTimeInMillis)
                                    }

                                    val notification = Notification(
                                        user_id = currentUserId,
                                        notification_type = "workout_reminder",
                                        message = "Reminder for ${workoutType} workout",
                                        scheduled_time = selectedTimeInMillis
                                    )
                                    workoutDao.insertNotification(notification)
                                }

                                loadWorkoutsFromDatabase()
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@HomeActivity, "Error adding workout: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
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
        val switchNotification = dialogView.findViewById<SwitchMaterial>(R.id.switchNotification)
        val timePickerLayout = dialogView.findViewById<TextInputLayout>(R.id.timePickerLayout)
        val etNotificationTime = dialogView.findViewById<TextInputEditText>(R.id.etNotificationTime)

        etWorkoutType.setText(workout.workout_type)
        etDuration.setText(workout.duration.toString())
        etCalories.setText(workout.calories_burned.toString())

        var selectedTimeInMillis: Long = 0

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val existingNotification = workoutDao.getLatestNotificationForUser(currentUserId)

                withContext(Dispatchers.Main) {
                    existingNotification?.let { notification ->
                        selectedTimeInMillis = notification.scheduled_time
                        val calendar = Calendar.getInstance().apply { timeInMillis = selectedTimeInMillis }
                        val hour = calendar.get(Calendar.HOUR_OF_DAY)
                        val minute = calendar.get(Calendar.MINUTE)
                        val amPm = if (hour < 12) "AM" else "PM"
                        val displayHour = if (hour > 12) hour - 12 else hour
                        etNotificationTime.setText("$displayHour:$minute $amPm")
                        switchNotification.isChecked = true
                    } ?: run {
                        timePickerLayout.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeActivity, "Error loading notification: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        switchNotification.setOnCheckedChangeListener { _, isChecked ->
            timePickerLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        etNotificationTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val selectedCalendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hourOfDay)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                    }
                    selectedTimeInMillis = selectedCalendar.timeInMillis

                    val amPm = if (hourOfDay < 12) "AM" else "PM"
                    val displayHour = if (hourOfDay > 12) hourOfDay - 12 else hourOfDay
                    etNotificationTime.setText("$displayHour:$minute $amPm")
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Workout")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedType = etWorkoutType.text.toString().trim()
                val updatedDuration = etDuration.text.toString().toIntOrNull()
                val updatedCalories = etCalories.text.toString().toIntOrNull()

                if (updatedType.isNotEmpty() && updatedDuration != null && updatedCalories != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val existingWorkout = workoutDao.getWorkoutsByUser(currentUserId).find {
                            it.workout_type.equals(updatedType, ignoreCase = true) && it.id != workout.id
                        }

                        withContext(Dispatchers.Main) {
                            if (existingWorkout != null) {
                                Toast.makeText(this@HomeActivity, "Workout type '$updatedType' already exists!", Toast.LENGTH_SHORT).show()
                            } else {
                                val updatedWorkout = workout.copy(
                                    workout_type = updatedType,
                                    duration = updatedDuration,
                                    calories_burned = updatedCalories
                                )

                                try {
                                    workoutDao.updateWorkout(updatedWorkout)

                                    if (switchNotification.isChecked && selectedTimeInMillis > 0) {
                                        notificationHelper.scheduleWorkoutReminder(updatedWorkout, selectedTimeInMillis)

                                        val notification = Notification(
                                            user_id = currentUserId,
                                            notification_type = "workout_reminder",
                                            message = "Reminder for ${updatedWorkout.workout_type} workout",
                                            scheduled_time = selectedTimeInMillis
                                        )

                                        val existing = workoutDao.getLatestNotificationForUser(currentUserId)
                                        if (existing != null) {
                                            workoutDao.updateNotification(notification.copy(id = existing.id))
                                        } else {
                                            workoutDao.insertNotification(notification)
                                        }
                                    }

                                    loadWorkoutsFromDatabase()
                                } catch (e: Exception) {
                                    Toast.makeText(this@HomeActivity, "Error saving workout: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
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
                    withContext(Dispatchers.Main) {
                        loadWorkoutsFromDatabase()
                    }
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