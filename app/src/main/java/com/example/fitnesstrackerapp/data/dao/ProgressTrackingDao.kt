package com.example.fitnesstrackerapp.data.dao

import com.example.fitnesstrackerapp.data.entities.ProgressTracking
import androidx.room.*

@Dao
interface ProgressTrackingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ProgressTracking)

    @Query("SELECT * FROM progress_tracking WHERE goal_id = :goalId")
    suspend fun getProgressByGoal(goalId: Int): List<ProgressTracking>
}
