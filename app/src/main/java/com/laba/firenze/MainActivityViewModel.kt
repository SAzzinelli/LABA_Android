package com.laba.firenze

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    val appearancePreferences: com.laba.firenze.data.local.AppearancePreferences
) : ViewModel() {
    
    val accentChoice: StateFlow<String> = appearancePreferences.accentChoice
    val themePreference: kotlinx.coroutines.flow.Flow<String> = appearancePreferences.themePreference
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    /** True durante il refresh del token (per mostrare "Aggiornamento accesso" come su iOS). */
    val isRefreshingSession: StateFlow<Boolean> = sessionRepository.isRefreshingSession
    
    /** Deep link laba://lesson/{lessonId} (identico a iOS handleDeepLink). */
    private val _pendingDeepLink = MutableStateFlow<String?>(null)
    val pendingDeepLink: StateFlow<String?> = _pendingDeepLink.asStateFlow()
    
    fun setPendingDeepLink(lessonId: String?) {
        _pendingDeepLink.value = lessonId
    }
    
    fun clearPendingDeepLink() {
        _pendingDeepLink.value = null
    }
    
    init {
        // Monitor login state (mantieni isLoading attivo se i dati stanno caricando)
        viewModelScope.launch {
            sessionRepository.tokenManager.isLoggedIn.collect { isLoggedIn ->
                if (isLoggedIn) {
                    // Se è loggato, mantieni isLoading true per mostrare lo splash
                    _authState.value = _authState.value.copy(isLoggedIn = true)
                } else {
                    // Se non è loggato, sicuramente non carica
                    _authState.value = _authState.value.copy(
                        isLoggedIn = false,
                        isLoading = false
                    )
                }
            }
        }
        
        // Monitor loading state - quando finisce di caricare, aggiorna isLoading
        viewModelScope.launch {
            sessionRepository.isLoading.collect { isLoading ->
                if (!isLoading && _authState.value.isLoggedIn) {
                    // I dati sono stati caricati e l'utente è loggato
                    _authState.value = _authState.value.copy(isLoading = false)
                }
            }
        }
        
        // Silent login all'avvio (identico a iOS)
        viewModelScope.launch {
            try {
                println("🔐 MainActivityViewModel: Attempting silent login on startup")
                val success = sessionRepository.restoreSessionStrong(force = false)
                if (success) {
                    println("🔐 MainActivityViewModel: Silent login successful")
                    
                    // Verifica se i dati sono già stati caricati (per restore session)
                    if (!sessionRepository.isLoading.value) {
                        // I dati sono già caricati, nascondi lo splash
                        _authState.value = _authState.value.copy(isLoading = false)
                    }
                } else {
                    println("🔐 MainActivityViewModel: Silent login failed, showing login UI")
                }
            } catch (e: Exception) {
                println("🔐 MainActivityViewModel: Silent login exception: ${e.message}")
            }
        }
    }
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            try {
                val success = sessionRepository.login(username, password)
                if (!success) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Credenziali non valide"
                    )
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Errore durante il login"
                )
            }
        }
    }
    
    fun clearError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }
    
    /**
     * Forza il restore della sessione (utile per "Riprova adesso")
     */
    fun forceRestoreSession() {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            try {
                val success = sessionRepository.restoreSessionStrong(force = true)
                if (!success) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Impossibile ripristinare la sessione"
                    )
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Errore durante il ripristino"
                )
            }
        }
    }
}

data class AuthState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
