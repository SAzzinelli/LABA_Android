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
    private val sessionRepository: SessionRepository
) : ViewModel() {
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        // Monitor login state
        viewModelScope.launch {
            sessionRepository.tokenManager.isLoggedIn.collect { isLoggedIn ->
                _authState.value = _authState.value.copy(
                    isLoggedIn = isLoggedIn,
                    isLoading = false
                )
            }
        }
        
        // Monitor loading state
        viewModelScope.launch {
            sessionRepository.isLoading.collect { isLoading ->
                _authState.value = _authState.value.copy(
                    isLoading = isLoading
                )
            }
        }
        
        // Silent login all'avvio (identico a iOS)
        viewModelScope.launch {
            try {
                println("🔐 MainActivityViewModel: Attempting silent login on startup")
                val success = sessionRepository.restoreSessionStrong(force = false)
                if (success) {
                    println("🔐 MainActivityViewModel: Silent login successful")
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
