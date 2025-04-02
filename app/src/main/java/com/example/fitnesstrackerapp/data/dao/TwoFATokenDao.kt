package com.example.fitnesstrackerapp.data.dao

import com.example.fitnesstrackerapp.data.entities.TwoFAToken
import androidx.room.*

@Dao
interface TwoFATokenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToken(token: TwoFAToken)

    @Query("SELECT * FROM two_fa_tokens WHERE user_id = :userId AND is_verified = 0")
    suspend fun getPending2FATokens(userId: Int): List<TwoFAToken>
}
