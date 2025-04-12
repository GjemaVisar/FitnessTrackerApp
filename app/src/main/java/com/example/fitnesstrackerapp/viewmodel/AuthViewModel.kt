package com.example.fitnesstrackerapp.viewmodel

import android.util.Log
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
import java.util.UUID

class AuthViewModel(private val userDao: UserDao) : ViewModel() {

    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> get() = _emailError

    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> get() = _passwordError

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> get() = _loginSuccess

    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> get() = _registerSuccess

    private val _resetTokenState = MutableLiveData<ResetTokenState>()
    val resetTokenState: LiveData<ResetTokenState> get() = _resetTokenState

    private val _passwordResetState = MutableLiveData<PasswordResetState>()
    val passwordResetState: LiveData<PasswordResetState> get() = _passwordResetState

    sealed class ResetTokenState {
        object Loading : ResetTokenState()
        data class Success(val token: String) : ResetTokenState()
        data class Error(val message: String) : ResetTokenState()
    }

    sealed class PasswordResetState {
        object Loading : PasswordResetState()
        object Success : PasswordResetState()
        data class Error(val message: String) : PasswordResetState()
    }

    fun validateLogin(email: String, password: String) {
        _emailError.value = when {
            email.isEmpty() -> "Email cannot be empty"
            !ValidationUtils.isValidEmail(email) -> "Invalid email format"
            else -> null
        }

        _passwordError.value = when {
            password.isEmpty() -> "Password cannot be empty"
            else -> null
        }

        if (_emailError.value == null && _passwordError.value == null) {
            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = userDao.getUserByEmail(email)
                if (user != null && BCrypt.checkpw(password, user.password)) {
                    _loginSuccess.postValue(true)
                } else {
                    _loginSuccess.postValue(false)
                    _emailError.postValue("Invalid credentials")
                }
            } catch (e: Exception) {
                _loginSuccess.postValue(false)
                _emailError.postValue("Login failed. Please try again.")
            }
        }
    }

    fun requestPasswordReset(email: String) {
        if (!ValidationUtils.isValidEmail(email)) {
            _resetTokenState.value = ResetTokenState.Error("Invalid email format")
            return
        }

        _resetTokenState.value = ResetTokenState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = userDao.getUserByEmail(email) ?: run {
                    _resetTokenState.postValue(ResetTokenState.Error("No account found with this email"))
                    return@launch
                }

                val token = UUID.randomUUID().toString().substring(0, 8)
                val expiryTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // 24 hours

                userDao.setResetToken(email, token, expiryTime)
                _resetTokenState.postValue(ResetTokenState.Success(token))
            } catch (e: Exception) {
                _resetTokenState.postValue(ResetTokenState.Error("Failed to generate reset token"))
            }
        }
    }

    fun resetPassword(email: String, token: String, newPassword: String, confirmPassword: String) {
        when {
            newPassword != confirmPassword -> {
                _passwordResetState.value = PasswordResetState.Error("Passwords don't match")
                return
            }
            !ValidationUtils.isValidPassword(newPassword) -> {
                _passwordResetState.value = PasswordResetState.Error(
                    "Password must be 8+ chars with uppercase, number, and special char"
                )
                return
            }
        }

        _passwordResetState.value = PasswordResetState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = userDao.getUserByEmail(email)

                if (user == null || user.reset_token != token) {
                    _passwordResetState.postValue(PasswordResetState.Error("Invalid token or email"))
                    return@launch
                }

                if (user.reset_token_expiry == null || user.reset_token_expiry!! < System.currentTimeMillis()) {
                    _passwordResetState.postValue(PasswordResetState.Error("Token has expired"))
                    return@launch
                }

                val hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt())
                val rowsUpdated = userDao.updatePasswordWithToken(email, token, hashedPassword)

                if (rowsUpdated > 0) {
                    _passwordResetState.postValue(PasswordResetState.Success)
                } else {
                    _passwordResetState.postValue(PasswordResetState.Error("Failed to update password"))
                }
            } catch (e: Exception) {
                _passwordResetState.postValue(PasswordResetState.Error("Something went wrong. Please try again."))
            }
        }
    }

    fun validateRegister(email: String, password: String, confirmPassword: String, name: String) {
        _emailError.value = when {
            email.isEmpty() -> "Email cannot be empty"
            !ValidationUtils.isValidEmail(email) -> "Invalid email format"
            else -> null
        }

        _passwordError.value = when {
            password.isEmpty() -> "Password cannot be empty"
            password.length < 8 -> "Password must be at least 8 characters"
            !ValidationUtils.isValidPassword(password) -> "Password must contain uppercase, number, and special char"
            password != confirmPassword -> "Passwords don't match"
            else -> null
        }

        if (_emailError.value == null && _passwordError.value == null) {
            checkDuplicateEmailAndRegister(email, password, name)
        }
    }

    private fun checkDuplicateEmailAndRegister(email: String, password: String, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Check if the email already exists in the database
                val existingUser = userDao.getUserByEmail(email)
                if (existingUser != null) {
                    // Show an error if the email already exists
                    _emailError.postValue("Email already registered")
                } else {
                    // If email does not exist, proceed with registration
                    val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
                    val user = User(email = email, password = hashedPassword, reset_token = null, reset_token_expiry = null)
                    val result = userDao.insertUser(user)
                    if (result >= 0) {
                        _registerSuccess.postValue(true)
                    } else {
                        _emailError.postValue("Registration failed. Please try again.")

                    }
                }
            } catch (e: Exception) {
                _emailError.postValue("Registration failed. Please try again.")
                Log.e("AuthViewModel", "Registration error", e) // Log the exception
            }

        }
    }

}
