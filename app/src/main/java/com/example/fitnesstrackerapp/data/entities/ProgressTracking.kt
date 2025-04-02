package com.example.fitnesstrackerapp.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "progress_tracking",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["user_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = FitnessGoal::class, parentColumns = ["id"], childColumns = ["goal_id"], onDelete = ForeignKey.CASCADE)
    ]
)
data class ProgressTracking(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val user_id: Int,
    val goal_id: Int,
    val progress_date: Long = System.currentTimeMillis(),
    val progress_value: Float
)
