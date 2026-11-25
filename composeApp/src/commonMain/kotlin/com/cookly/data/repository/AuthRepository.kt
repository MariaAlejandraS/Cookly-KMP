package com.cookly.data.repository

import com.cookly.data.auth.PasswordHasher
import com.cookly.data.auth.Validators
import com.cookly.data.db.UserDao
import com.cookly.data.model.User
import com.cookly.data.prefs.UserPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

sealed interface LoginResult {
    data object Success : LoginResult
    data object Invalid : LoginResult
    data class Locked(val seconds: Int) : LoginResult
}

enum class RegisterResult { SUCCESS, INVALID_NAME, INVALID_USERNAME, WEAK_PASSWORD, DUPLICATE_USERNAME }

/**
 * Sesión y cuentas locales. currentUser es la "fuente de verdad":
 * la App muestra el login si es null y las pestañas si tiene un usuario.
 */
class AuthRepository(private val userDao: UserDao, private val prefs: UserPrefs) {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private var failedAttempts = 0
    private var lockCount = 0
    private var lockedUntilMillis = 0L

    /** Al abrir la app recupera la sesión guardada en las preferencias. */
    suspend fun restoreSession() {
        val id = prefs.sessionUserId ?: return
        _currentUser.value = userDao.findById(id)
    }

    suspend fun register(name: String, username: String, password: String): RegisterResult {
        val cleanName = name.trim()
        val normalized = Validators.normalizeUsername(username)
        if (cleanName.isEmpty()) return RegisterResult.INVALID_NAME
        if (!Validators.isValidUsername(normalized)) return RegisterResult.INVALID_USERNAME
        if (Validators.passwordProblem(password, normalized) != null) return RegisterResult.WEAK_PASSWORD
        if (userDao.findByUsername(normalized) != null) return RegisterResult.DUPLICATE_USERNAME

        val salt = PasswordHasher.newSalt()
        // Las 50.000 vueltas de SHA-256 se hacen fuera del hilo principal para no congelar la pantalla.
        val hash = withContext(Dispatchers.Default) { PasswordHasher.hash(password, salt) }
        val user = User(name = cleanName, username = normalized, salt = salt, passwordHash = hash)
        val id = userDao.insert(user)
        adopt(user.copy(id = id))
        return RegisterResult.SUCCESS
    }

    suspend fun login(username: String, password: String): LoginResult {
        lockRemaining()?.let { return LoginResult.Locked(it) }

        val user = userDao.findByUsername(Validators.normalizeUsername(username))
        val ok = withContext(Dispatchers.Default) {
            if (user == null) {
                // Hace el mismo trabajo aunque el usuario no exista, para no delatarlo por el tiempo.
                PasswordHasher.hash(password, "sin-usuario")
                false
            } else {
                PasswordHasher.matches(password, user.salt, user.passwordHash)
            }
        }
        if (ok && user != null) {
            failedAttempts = 0
            lockCount = 0
            adopt(user)
            return LoginResult.Success
        }

        failedAttempts++
        if (failedAttempts >= MAX_ATTEMPTS) {
            failedAttempts = 0
            val seconds = (BASE_LOCK_SECONDS shl lockCount).coerceAtMost(MAX_LOCK_SECONDS)
            lockCount++
            lockedUntilMillis = now() + seconds * 1000L
            return LoginResult.Locked(seconds)
        }
        return LoginResult.Invalid
    }

    fun logout() {
        prefs.sessionUserId = null
        _currentUser.value = null
    }

    suspend fun updateName(name: String) {
        val user = _currentUser.value ?: return
        val clean = name.trim().ifEmpty { return }
        val updated = user.copy(name = clean)
        userDao.update(updated)
        _currentUser.value = updated
    }

    private fun adopt(user: User) {
        prefs.sessionUserId = user.id
        _currentUser.value = user
    }

    private fun lockRemaining(): Int? {
        val left = ((lockedUntilMillis - now()) / 1000L).toInt()
        return if (left > 0) left else null
    }

    private fun now(): Long = Clock.System.now().toEpochMilliseconds()

    private companion object {
        const val MAX_ATTEMPTS = 5
        const val BASE_LOCK_SECONDS = 30
        const val MAX_LOCK_SECONDS = 300
    }
}
