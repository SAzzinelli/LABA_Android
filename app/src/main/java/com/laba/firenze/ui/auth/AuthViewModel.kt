package com.laba.firenze.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.laba.firenze.data.local.AppearancePreferences
import com.laba.firenze.data.gamification.AchievementManager

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val appearancePreferences: AppearancePreferences,
    private val achievementManager: AchievementManager
) : ViewModel() {
    
    fun getApiVersion(): String = appearancePreferences.getApiVersion()
    
    fun setApiVersion(version: String) {
        appearancePreferences.setApiVersion(version)
    }
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        // Check initial login state
        _authState.value = _authState.value.copy(
            isLoggedIn = sessionRepository.isLoggedIn(),
            isLoading = false
        )
    }
    
    fun signIn(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(
                isLoading = true,
                error = null
            )
            
            try {
                val success = sessionRepository.login(username, password)
                if (!success) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = "Credenziali non valide"
                    )
                } else {
                    // Login successful
                    // Track achievements
                    achievementManager.trackFirstLogin()
                    achievementManager.trackLogin()
                    
                    // Sync achievements
                    val profile = sessionRepository.getUserProfile()
                    val email = profile?.emailLABA ?: profile?.emailPersonale ?: "$username@laba.biz"
                    achievementManager.setUserEmail(email)
                    
                    // Sync achievements from Supabase after login
                    achievementManager.syncFromSupabaseIfNeeded(email)
                    
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = null,
                        isLoggedIn = true
                    )
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Errore di connessione"
                )
            }
        }
    }
    
    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}

data class AuthState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
