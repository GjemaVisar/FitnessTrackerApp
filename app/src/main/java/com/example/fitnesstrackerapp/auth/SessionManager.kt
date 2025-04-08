package com.example.fitnesstrackerapp.auth

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        @Volatile private var instance: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context.applicationContext).also { instance = it }
            }
        }
    }

    var currentUserId: Int?
        get() = if (prefs.contains("user_id")) prefs.getInt("user_id", -1) else null
        set(value) {
            prefs.edit().apply {
                if (value != null) {
                    putInt("user_id", value)
                } else {
                    remove("user_id")
                }
                apply()
            }
        }

    fun saveUserEmail(email: String) {
        prefs.edit().putString("user_email", email).apply()
    }

    fun getCurrentUserEmail(): String? {
        return prefs.getString("user_email", null)
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
