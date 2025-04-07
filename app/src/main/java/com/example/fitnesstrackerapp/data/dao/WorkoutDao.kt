package com.example.fitnesstrackerapp.data.dao

import com.example.fitnesstrackerapp.data.entities.Workout
import androidx.room.*

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout)

    @Query("SELECT * FROM workouts WHERE user_id = :userId")
    suspend fun getWorkoutsByUser(userId: Int): List<Workout>

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)

}
