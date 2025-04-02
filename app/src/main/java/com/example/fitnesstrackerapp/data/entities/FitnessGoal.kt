package com.example.fitnesstrackerapp.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "fitness_goals",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class FitnessGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val user_id: Int,
    val goal_type: String,
    val target_value: Float,
    val deadline: Long,
    val current_value: Float = 0.0f,
    val status: String = "In Progress"
)
