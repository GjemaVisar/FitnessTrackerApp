package com.example.fitnesstrackerapp.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackerapp.R
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.databinding.ActivityForgotPasswordBinding
import com.example.fitnesstrackerapp.utils.ValidationUtils
import com.example.fitnesstrackerapp.viewmodel.AuthViewModel
import com.example.fitnesstrackerapp.viewmodel.AuthViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(AppDatabase.getDatabase(this).userDao())
        ).get(AuthViewModel::class.java)

        intent.getStringExtra("email")?.let { email ->
            binding.etEmail.setText(email)
        }

        binding.btnSubmit.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()

            if (email.isEmpty()) {
                binding.etEmail.error = "Email is required"
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidEmail(email)) {
                binding.etEmail.error = "Invalid email format"
                return@setOnClickListener
            }

            binding.btnSubmit.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE

            binding.btnSubmit.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE

            authViewModel.requestPasswordReset(email)

            authViewModel.resetTokenState.observe(this) { state ->
                when (state) {
                    is AuthViewModel.ResetTokenState.Loading -> {
                        // Loading state already handled
                    }
                    is AuthViewModel.ResetTokenState.Success -> {
                        binding.btnSubmit.isEnabled = true
                        binding.progressBar.visibility = View.GONE

                        // Show token in a Toast (for testing)
                        Toast.makeText(
                            this,
                            "Reset token: ${state.token}",
                            Toast.LENGTH_LONG
                        ).show()

                        val intent = Intent(this, ResetPasswordActivity::class.java)
                        intent.putExtra("email", email)
                        intent.putExtra("token", state.token) // Pass the token
                        startActivity(intent)
                        finish()
                    }
                    is AuthViewModel.ResetTokenState.Error -> {
                        binding.btnSubmit.isEnabled = true
                        binding.progressBar.visibility = View.GONE
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