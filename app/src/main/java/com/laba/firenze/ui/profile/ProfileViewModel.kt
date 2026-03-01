package com.laba.firenze.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.local.KeychainHelper
import com.laba.firenze.data.repository.SessionRepository
import com.laba.firenze.data.service.ProfilePhotoService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val achievementManager: com.laba.firenze.data.gamification.AchievementManager,
    private val keychainHelper: KeychainHelper
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    val profilePhotoURL: StateFlow<String?> = sessionRepository.tokenManager.profilePhotoURL

    private val _isUploadingPhoto = MutableStateFlow(false)
    val isUploadingPhoto: StateFlow<Boolean> = _isUploadingPhoto.asStateFlow()

    private val _uploadPhotoError = MutableStateFlow<String?>(null)
    val uploadPhotoError: StateFlow<String?> = _uploadPhotoError.asStateFlow()
    
    // Achievement data
    val achievements: StateFlow<List<com.laba.firenze.domain.model.Achievement>> = achievementManager.achievements
    val totalPoints: StateFlow<Int> = achievementManager.totalPoints
    
    init {
        loadUserProfile()
        loadProfilePhotoFromSupabase()
    }

    /** Ricarica foto da Supabase (utente impostata su iOS → visibile su Android). */
    fun loadProfilePhotoFromSupabase() {
        viewModelScope.launch {
            sessionRepository.loadProfilePhotoFromSupabase()
        }
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
    
    fun trackSectionVisit(section: String) {
        achievementManager.trackSectionVisit(section)
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
            achievementManager.setUserEmail(null)
        }
    }
    
    fun changePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(changePasswordState = ChangePasswordState.Loading)
            _uiState.value = _uiState.value.copy(
                changePasswordState = sessionRepository.changePassword(oldPassword, newPassword)
                    .fold(
                        onSuccess = { ChangePasswordState.Success },
                        onFailure = { ChangePasswordState.Error(it.message ?: "Errore sconosciuto") }
                    )
            )
        }
    }
    
    fun clearChangePasswordState() {
        _uiState.value = _uiState.value.copy(changePasswordState = ChangePasswordState.Idle)
    }

    fun uploadProfilePhoto(imageData: ByteArray) {
        viewModelScope.launch {
            _isUploadingPhoto.value = true
            _uploadPhotoError.value = null
            try {
                sessionRepository.getProfilePhotoDeleteURL()?.let { deleteUrl ->
                    if (deleteUrl.isNotEmpty()) {
                        ProfilePhotoService.deleteImage(deleteUrl)
                    }
                }
                val profile = sessionRepository.getUserProfile()
                val name = when {
                    profile?.nome != null && profile.cognome != null -> {
                        val first = profile.nome!!.trim().split(" ").firstOrNull() ?: ""
                        val cog = profile.cognome!!.trim().replace(" ", "")
                        if (first.isNotEmpty() && cog.isNotEmpty()) "$first.$cog" else null
                    }
                    else -> keychainHelper.getUsername()?.substringBefore("@")
                }
                val result = ProfilePhotoService.upload(imageData, 512, name)
                result.fold(
                    onSuccess = { (url, deleteUrl) ->
                        sessionRepository.setProfilePhotoURL(url, deleteUrl)
                        _isUploadingPhoto.value = false
                    },
                    onFailure = { e ->
                        _uploadPhotoError.value = e.message ?: "Upload fallito"
                        _isUploadingPhoto.value = false
                    }
                )
            } catch (e: Exception) {
                _uploadPhotoError.value = e.message ?: "Upload fallito"
                _isUploadingPhoto.value = false
            }
        }
    }

    fun clearUploadPhotoError() {
        _uploadPhotoError.value = null
    }

    /** Rimuove l'URL foto profilo (chiamato quando l'immagine non è più disponibile su ImgBB). */
    fun clearProfilePhotoURL() {
        viewModelScope.launch {
            sessionRepository.tokenManager.clearProfilePhotoURL()
        }
    }
}

data class ProfileUiState(
    val userProfile: com.laba.firenze.domain.model.StudentProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val changePasswordState: ChangePasswordState = ChangePasswordState.Idle
)

sealed class ChangePasswordState {
    data object Idle : ChangePasswordState()
    data object Loading : ChangePasswordState()
    data object Success : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}
