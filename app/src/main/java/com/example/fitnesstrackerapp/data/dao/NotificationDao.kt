package com.example.fitnesstrackerapp.data.dao
import com.example.fitnesstrackerapp.data.entities.Notification
import androidx.room.*

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    @Query("SELECT * FROM notifications WHERE user_id = :userId AND is_sent = 0")
    suspend fun getPendingNotifications(userId: Int): List<Notification>
}
