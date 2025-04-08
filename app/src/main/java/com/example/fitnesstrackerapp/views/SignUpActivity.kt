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

    private val authViewModel: AuthViewModel by lazy {
        ViewModelProvider(
            this,
            AuthViewModelFactory(AppDatabase.Companion.getDatabase(this).userDao())
        ).get(AuthViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val btnLogin = findViewById<TextView>(R.id.btnLogin)

        authViewModel.emailError.observe(this, Observer { error ->
            etEmail.error = error
        })

        authViewModel.passwordError.observe(this, Observer { error ->
            etPassword.error = error
        })

        authViewModel.registerSuccess.observe(this, Observer { success ->
            if (success) {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                Toast.makeText(this, "Registration failed! Email may already be used.", Toast.LENGTH_SHORT).show()
            }
        })

        btnSignUp.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            authViewModel.validateRegister(email, password, confirmPassword)
        }

        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
