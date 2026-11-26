package com.cookly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookly.data.prefs.UserPrefs
import com.cookly.data.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Perfil: nombre del usuario, modo oscuro, días de aviso y cerrar sesión. */
class ProfileViewModel(private val auth: AuthRepository, private val prefs: UserPrefs) : ViewModel() {
    val user = auth.currentUser
    val darkMode: StateFlow<Boolean> = prefs.darkMode
    val warningDays: StateFlow<Int> = prefs.warningDays

    fun setDarkMode(enabled: Boolean) = prefs.setDarkMode(enabled)
    fun setWarningDays(days: Int) = prefs.setWarningDays(days)
    fun saveName(name: String) { viewModelScope.launch { auth.updateName(name) } }
    fun logout() = auth.logout()
}
