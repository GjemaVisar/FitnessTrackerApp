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
import kotlinx.coroutines.*
import com.example.fitnesstrackerapp.auth.SessionManager
import org.mindrot.jbcrypt.BCrypt

class LoginActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by lazy {
        ViewModelProvider(
            this,
            AuthViewModelFactory(AppDatabase.Companion.getDatabase(this).userDao())
        ).get(AuthViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSignUp = findViewById<TextView>(R.id.btnSignUp)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            authViewModel.validateLogin(email, password)
        }

        authViewModel.emailError.observe(this, Observer { error ->
            etEmail.error = error
        })

        authViewModel.passwordError.observe(this, Observer { error ->
            etPassword.error = error
        })

        authViewModel.loginSuccess.observe(this, Observer { success ->
            if (success) {
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()

                CoroutineScope(Dispatchers.IO).launch {
                    val user = AppDatabase.getDatabase(this@LoginActivity)
                        .userDao()
                        .getUserByEmail(email)

                    user?.let {
                        if (BCrypt.checkpw(password, it.password)) {
                            val session = SessionManager.getInstance(this@LoginActivity)
                            session.currentUserId = it.id
                            session.saveUserEmail(it.email)

                            withContext(Dispatchers.Main) {
                                val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        })

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
