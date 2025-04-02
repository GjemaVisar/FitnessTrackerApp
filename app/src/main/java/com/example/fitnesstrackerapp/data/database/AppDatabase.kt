package com.example.fitnesstrackerapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fitnesstrackerapp.data.dao.*
import com.example.fitnesstrackerapp.data.entities.*

@Database(
    entities = [User::class, Workout::class, FitnessGoal::class, ProgressTracking::class, Notification::class, TwoFAToken::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun fitnessGoalDao(): FitnessGoalDao
    abstract fun progressTrackingDao(): ProgressTrackingDao
    abstract fun notificationDao(): NotificationDao
    abstract fun twoFATokenDao(): TwoFATokenDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitness_app_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
