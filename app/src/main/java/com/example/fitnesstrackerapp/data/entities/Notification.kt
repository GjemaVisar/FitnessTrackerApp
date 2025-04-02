package com.example.fitnesstrackerapp.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val user_id: Int,
    val notification_type: String,
    val message: String,
    val scheduled_time: Long,
    val is_sent: Int = 0
)
