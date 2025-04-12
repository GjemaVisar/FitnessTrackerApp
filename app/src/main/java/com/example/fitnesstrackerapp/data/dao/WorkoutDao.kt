package com.example.fitnesstrackerapp.data.dao

import com.example.fitnesstrackerapp.data.entities.Workout
import androidx.room.*
import com.example.fitnesstrackerapp.data.entities.Notification

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout): Long

    @Query("SELECT * FROM workouts WHERE user_id = :userId")
    suspend fun getWorkoutsByUser(userId: Int): List<Workout>

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)

    @Query("SELECT * FROM notifications WHERE user_id = :userId AND notification_type = 'workout_reminder' AND scheduled_time = (SELECT scheduled_time FROM notifications WHERE id = (SELECT MAX(id) FROM notifications WHERE user_id = :userId))")
    suspend fun getLatestNotificationForUser(userId: Int): Notification?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    @Update
    suspend fun updateNotification(notification: Notification)

    @Query("SELECT * FROM workouts WHERE workout_type = :workoutType AND user_id = :userId LIMIT 1")
    suspend fun getWorkoutByTypeAndUser(workoutType: String, userId: Int): Workout?

    @Query("SELECT * FROM workouts WHERE user_id = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getWorkoutsByUserAndDateRange(userId: Int, startDate: Long, endDate: Long): List<Workout>
}

