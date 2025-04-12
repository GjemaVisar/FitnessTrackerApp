package com.example.fitnesstrackerapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val password: String,
    val is_2fa_enabled: Int = 0,
    val created_at: Long = System.currentTimeMillis(),
    var reset_token: String? = null,
    var reset_token_expiry: Long? = null
)
