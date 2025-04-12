package com.example.fitnesstrackerapp.views

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackerapp.R
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.viewmodel.AuthViewModel
import com.example.fitnesstrackerapp.viewmodel.AuthViewModelFactory

class SignUpActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(AppDatabase.getDatabase(this).userDao())
        ).get(AuthViewModel::class.java)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val btnLogin = findViewById<TextView>(R.id.btnLogin)
        val etName = findViewById<EditText>(R.id.etName)

        authViewModel.emailError.observe(this, Observer { error ->
            error?.let { etEmail.error = it }
        })

        authViewModel.passwordError.observe(this, Observer { error ->
            error?.let { etPassword.error = it }
        })

        authViewModel.registerSuccess.observe(this, Observer { success ->
            if (success) {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        })

        btnSignUp.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Full name cannot be empty"
                return@setOnClickListener
            }

            authViewModel.validateRegister(email, password, confirmPassword, name)
        }

        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
