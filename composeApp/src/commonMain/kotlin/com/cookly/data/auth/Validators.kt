package com.cookly.data.auth

/** Reglas de usuario y contraseña (se usan al registrarse). */
object Validators {
    const val PASSWORD_HINT = "Mínimo 8 caracteres, con una letra y un número. No uses 12345678, password ni tu usuario."
    const val USERNAME_HINT = "Usa de 3 a 20 letras minúsculas, números o guion bajo."

    private val usernameRegex = Regex("^[a-z0-9_]{3,20}$")
    private val common = setOf("12345678", "password", "contraseña", "qwerty123", "123456789")

    fun normalizeUsername(raw: String): String = raw.trim().lowercase()

    fun isValidUsername(normalized: String): Boolean = usernameRegex.matches(normalized)

    /** Devuelve el problema de la contraseña, o null si es válida. */
    fun passwordProblem(password: String, username: String): String? = when {
        password.length < 8 -> PASSWORD_HINT
        password.none { it.isLetter() } || password.none { it.isDigit() } -> PASSWORD_HINT
        password.lowercase() in common -> PASSWORD_HINT
        password.lowercase() == normalizeUsername(username) -> PASSWORD_HINT
        else -> null
    }
}
