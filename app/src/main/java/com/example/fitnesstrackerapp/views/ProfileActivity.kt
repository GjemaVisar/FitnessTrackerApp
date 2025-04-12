package com.example.fitnesstrackerapp.views

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitnesstrackerapp.auth.SessionManager
import com.example.fitnesstrackerapp.databinding.ActivityProfileBinding
import com.example.fitnesstrackerapp.viewmodel.AuthViewModel
import com.google.android.material.button.MaterialButton

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private var currentUserId: Int = -1
    private lateinit var profileViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = intent.getIntExtra("CURRENT_USER_ID", -1)
        setupViews()

        val userEmail = SessionManager.getInstance(this).getCurrentUserEmail()
        binding.tvUserEmail.text = userEmail ?: "No email found"

        setupWindowInsets()
    }

    private fun setupViews() {
        binding.btnBackToHome.setOnClickListener {
            finish()
        }

        binding.btnLogout.setOnClickListener {
            SessionManager.getInstance(this).logout()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }


    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
