package com.example.fitnesstrackerapp.views

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitnesstrackerapp.databinding.ActivityMainBinding
import android.view.animation.*
import com.example.fitnesstrackerapp.R

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val logoAnim = AnimationUtils.loadAnimation(this, R.anim.logo_anim)
        binding.logo.startAnimation(logoAnim)

        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        binding.appNameText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in).apply {
            startOffset = 300
        })

        binding.welcomeText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in).apply {
            startOffset = 600
        })

        binding.btnLogin.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in).apply {
            startOffset = 900
        })

        binding.btnSignUp.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in).apply {
            startOffset = 1200
        })


        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
