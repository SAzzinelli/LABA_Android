package com.laba.firenze.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {
    
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
