package com.example.fitnesstrackerapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.fitnesstrackerapp.data.dao.*
import com.example.fitnesstrackerapp.data.entities.*

@Database(
    entities = [User::class, Workout::class, FitnessGoal::class, ProgressTracking::class, Notification::class, TwoFAToken::class],
    version = 2,
    exportSchema = true
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

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN reset_token TEXT")
                database.execSQL("ALTER TABLE users ADD COLUMN reset_token_expiry INTEGER")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitness_app_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}