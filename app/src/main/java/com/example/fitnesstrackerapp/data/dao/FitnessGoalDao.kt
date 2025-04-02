package com.example.fitnesstrackerapp.data.dao

import com.example.fitnesstrackerapp.data.entities.FitnessGoal
import androidx.room.*

@Dao
interface FitnessGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFitnessGoal(goal: FitnessGoal)

    @Query("SELECT * FROM fitness_goals WHERE user_id = :userId")
    suspend fun getFitnessGoalsByUser(userId: Int): List<FitnessGoal>
}
