package com.cookly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookly.data.auth.Validators
import com.cookly.data.repository.AuthRepository
import com.cookly.data.repository.LoginResult
import com.cookly.data.repository.RegisterResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Todo lo que muestran las pantallas de Login y Registro. */
data class AuthUiState(
    val name: String = "",
    val username: String = "",
    val password: String = "",
    val nameError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val message: String? = null,
    val busy: Boolean = false,
    val lockSeconds: Int = 0,
) {
    private val normalized get() = Validators.normalizeUsername(username)

    val canLogin: Boolean
        get() = Validators.isValidUsername(normalized) && password.isNotEmpty() && !busy && lockSeconds == 0

    val canRegister: Boolean
        get() = name.isNotBlank() && Validators.isValidUsername(normalized) &&
            Validators.passwordProblem(password, normalized) == null && !busy
}

/** ViewModel de Login y Registro (cada pantalla crea el suyo). */
class AuthViewModel(private val auth: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private var countdown: Job? = null

    fun onNameChange(value: String) = _state.update { it.copy(name = value, nameError = null) }

    fun onUsernameChange(value: String) = _state.update {
        val normalized = Validators.normalizeUsername(value)
        val error = if (normalized.isNotEmpty() && !Validators.isValidUsername(normalized)) Validators.USERNAME_HINT else null
        it.copy(username = value, usernameError = error, message = if (it.lockSeconds > 0) it.message else null)
    }

    fun onPasswordChange(value: String) = _state.update {
        it.copy(password = value, passwordError = null, message = if (it.lockSeconds > 0) it.message else null)
    }

    fun login() {
        val current = _state.value
        if (!current.canLogin) return
        _state.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            when (val result = auth.login(current.username, current.password)) {
                LoginResult.Success -> _state.update { it.copy(busy = false, password = "") }
                LoginResult.Invalid -> _state.update {
                    it.copy(busy = false, password = "", message = "Usuario o contraseña incorrectos.")
                }
                is LoginResult.Locked -> {
                    _state.update { it.copy(busy = false, password = "") }
                    startCountdown(result.seconds)
                }
            }
        }
    }

    fun register() {
        val current = _state.value
        if (!current.canRegister) {
            _state.update {
                it.copy(
                    nameError = if (it.name.isBlank()) "Escribe tu nombre." else null,
                    usernameError = if (!Validators.isValidUsername(Validators.normalizeUsername(it.username))) Validators.USERNAME_HINT else null,
                    passwordError = Validators.passwordProblem(it.password, it.username),
                )
            }
            return
        }
        _state.update { it.copy(busy = true) }
        viewModelScope.launch {
            val result = auth.register(current.name, current.username, current.password)
            _state.update {
                when (result) {
                    RegisterResult.SUCCESS -> it.copy(busy = false, password = "")
                    RegisterResult.DUPLICATE_USERNAME -> it.copy(busy = false, usernameError = "Ese usuario ya existe.")
                    RegisterResult.INVALID_NAME -> it.copy(busy = false, nameError = "Escribe tu nombre.")
                    RegisterResult.INVALID_USERNAME -> it.copy(busy = false, usernameError = Validators.USERNAME_HINT)
                    RegisterResult.WEAK_PASSWORD -> it.copy(busy = false, passwordError = Validators.PASSWORD_HINT)
                }
            }
        }
    }

    /** Cuenta regresiva visible cuando la cuenta queda bloqueada por intentos fallidos. */
    private fun startCountdown(seconds: Int) {
        countdown?.cancel()
        countdown = viewModelScope.launch {
            var left = seconds
            while (left > 0) {
                _state.update { it.copy(lockSeconds = left, message = "Demasiados intentos. Intenta de nuevo en $left s.") }
                delay(1_000)
                left--
            }
            _state.update { it.copy(lockSeconds = 0, message = null) }
        }
    }
}
