package com.example.fitnesstrackerapp.data.entities
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "workouts",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val user_id: Int,
    val workout_type: String,
    val duration: Int,
    val calories_burned: Int,
    val date: Long = System.currentTimeMillis()
)
