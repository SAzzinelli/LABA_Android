package com.laba.firenze.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadUserProfile()
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            sessionRepository.tokenManager.userProfile.collect { profile ->
                _uiState.value = _uiState.value.copy(
                    userProfile = profile
                )
            }
        }
    }
    
    fun updateNotificationsEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            notificationsEnabled = enabled
        )
        // TODO: Save to preferences
    }
    
    fun refreshData() {
        viewModelScope.launch {
            sessionRepository.loadAll()
        }
    }
    
    fun showDebugInfo() {
        // TODO: Show debug information
    }
    
    fun logout() {
        viewModelScope.launch {
            sessionRepository.logout()
        }
    }
}

data class ProfileUiState(
    val userProfile: com.laba.firenze.domain.model.StudentProfile? = null,
    val notificationsEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)
