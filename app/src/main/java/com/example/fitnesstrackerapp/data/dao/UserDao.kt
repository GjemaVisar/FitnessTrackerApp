package com.example.fitnesstrackerapp.data.dao

import com.example.fitnesstrackerapp.data.entities.User
import androidx.room.*

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?

    @Query("UPDATE users SET password = :newPassword WHERE id = :userId")
    suspend fun updatePassword(userId: Int, newPassword: String)

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET reset_token = :token, reset_token_expiry = :expiry WHERE email = :email")
    suspend fun setResetToken(email: String, token: String, expiry: Long)

    @Query("UPDATE users SET password = :newPassword, reset_token = NULL, reset_token_expiry = NULL WHERE email = :email AND reset_token = :token")
    suspend fun updatePasswordWithToken(email: String, token: String, newPassword: String): Int

    @Query("SELECT * FROM users WHERE email = :email AND reset_token = :token")
    suspend fun getUserWithResetToken(email: String, token: String): User?
}
