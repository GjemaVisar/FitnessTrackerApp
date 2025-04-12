package com.example.fitnesstrackerapp.views

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackerapp.R
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.databinding.ActivityResetPasswordBinding
import com.example.fitnesstrackerapp.utils.ValidationUtils
import com.example.fitnesstrackerapp.viewmodel.AuthViewModel
import com.example.fitnesstrackerapp.viewmodel.AuthViewModelFactory

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var authViewModel: AuthViewModel
    private lateinit var email: String
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(AppDatabase.getDatabase(this).userDao())
        )[AuthViewModel::class.java]

        email = intent.getStringExtra("email") ?: run {
            Toast.makeText(this, "Invalid reset link", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        token = intent.getStringExtra("token")
        binding.tokenLayout.visibility = View.VISIBLE
        token?.let { binding.etToken.setText(it) }

        binding.btnSubmit.setOnClickListener {
            val enteredToken = binding.etToken.text.toString().trim()
            val newPassword = binding.etNewPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (enteredToken.isEmpty()) {
                binding.etToken.error = "Reset token is required"
                return@setOnClickListener
            }

            if (newPassword.isEmpty()) {
                binding.etNewPassword.error = "New password is required"
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidPassword(newPassword)) {
                binding.etNewPassword.error = "Password must be 8+ chars with uppercase, number, and special char"
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                binding.etConfirmPassword.error = "Passwords don't match"
                return@setOnClickListener
            }

            binding.btnSubmit.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE

            authViewModel.resetPassword(
                email = email,
                token = enteredToken,
                newPassword = newPassword,
                confirmPassword = confirmPassword
            )

            authViewModel.passwordResetState.observe(this) { state ->
                binding.btnSubmit.isEnabled = true
                binding.progressBar.visibility = View.GONE

                when (state) {
                    is AuthViewModel.PasswordResetState.Loading -> {}
                    AuthViewModel.PasswordResetState.Success -> {
                        Toast.makeText(this, "Password reset successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    is AuthViewModel.PasswordResetState.Error -> {
                        Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.tvBackToLogin.setOnClickListener {
            finish()
        }
    }
}
