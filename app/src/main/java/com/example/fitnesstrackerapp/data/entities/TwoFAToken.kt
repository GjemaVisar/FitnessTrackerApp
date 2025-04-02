package com.example.fitnesstrackerapp.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "two_fa_tokens",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class TwoFAToken(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val user_id: Int,
    val token: String,
    val expiration_time: Long,
    val is_verified: Int = 0
)
