package com.cookly.data.prefs

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Preferencias pequeñas con Multiplatform Settings:
 * en Android usa SharedPreferences y en Desktop usa java.util.prefs.Preferences.
 * Aquí van el modo oscuro, los días de aviso y quién tiene la sesión abierta.
 */
class UserPrefs(private val settings: Settings = Settings()) {

    private val _darkMode = MutableStateFlow(settings.getBoolean(KEY_DARK_MODE, false))
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    private val _warningDays = MutableStateFlow(settings.getInt(KEY_WARNING_DAYS, 3))
    val warningDays: StateFlow<Int> = _warningDays.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        settings.putBoolean(KEY_DARK_MODE, enabled)
        _darkMode.value = enabled
    }

    fun setWarningDays(days: Int) {
        val value = days.coerceIn(1, 7)
        settings.putInt(KEY_WARNING_DAYS, value)
        _warningDays.value = value
    }

    var sessionUserId: Long?
        get() = settings.getLongOrNull(KEY_SESSION)
        set(value) {
            if (value == null) settings.remove(KEY_SESSION) else settings.putLong(KEY_SESSION, value)
        }

    private companion object {
        const val KEY_DARK_MODE = "darkMode"
        const val KEY_WARNING_DAYS = "warningDays"
        const val KEY_SESSION = "sessionUserId"
    }
}
