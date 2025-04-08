package com.example.fitnesstrackerapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.dao.UserDao
import com.example.fitnesstrackerapp.data.entities.User
import com.example.fitnesstrackerapp.utils.ValidationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt

class AuthViewModel(private val userDao: UserDao) : ViewModel() {

    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> get() = _emailError

    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> get() = _passwordError

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> get() = _loginSuccess

    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> get() = _registerSuccess

    fun validateLogin(email: String, password: String) {
        _emailError.value = if (email.isEmpty()) "Email cannot be empty"
        else if (!ValidationUtils.isValidEmail(email)) "Invalid email format" else null

        _passwordError.value = if (password.isEmpty()) "Password cannot be empty" else null

        if (_emailError.value == null && _passwordError.value == null) {
            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = userDao.getUserByEmail(email)

            if (user != null && BCrypt.checkpw(password, user.password)) {
                _loginSuccess.postValue(true)
            } else {
                _loginSuccess.postValue(false)
            }
        }
    }

    fun validateRegister(email: String, password: String, confirmPassword: String) {
        _emailError.value = when {
            email.isEmpty() -> "Email cannot be empty"
            !ValidationUtils.isValidEmail(email) -> "Invalid email format"
            else -> null
        }

        _passwordError.value = when {
            password.isEmpty() -> "Password cannot be empty"
            password.length < 8 -> "Password must be at least 8 characters"
            !ValidationUtils.isValidPassword(password) -> "Password must be 8+ characters, 1 uppercase, 1 number, 1 special character"
            password != confirmPassword -> "Passwords do not match"
            else -> null
        }

        if (_emailError.value == null && _passwordError.value == null) {
            checkDuplicateEmailAndRegister(email, password)
        }
    }

    private fun checkDuplicateEmailAndRegister(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                _emailError.postValue("Email is already taken")
            } else {
                val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
                val user = User(email = email, password = hashedPassword)
                userDao.insertUser(user)
                _registerSuccess.postValue(true)
            }
        }
    }
}
