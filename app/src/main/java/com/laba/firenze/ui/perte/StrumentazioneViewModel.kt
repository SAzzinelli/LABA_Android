package com.laba.firenze.ui.perte

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StrumentazioneViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StrumentazioneUiState())
    val uiState: StateFlow<StrumentazioneUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                @Suppress("UNUSED_VARIABLE")
                val userProfile = sessionRepository.getUserProfile()
                val accessToken = sessionRepository.getAccessToken()
                
                if (accessToken.isNotEmpty()) {
                    val gestionaleUrl = buildGestionaleUrl(accessToken)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        gestionaleUrl = gestionaleUrl
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Token di accesso non disponibile"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    private fun buildGestionaleUrl(accessToken: String): String {
        // Costruisce l'URL del gestionale con il token di accesso
        return "https://gestionale.laba.biz?token=$accessToken"
    }
}

data class StrumentazioneUiState(
    val isLoading: Boolean = false,
    val gestionaleUrl: String? = null,
    val error: String? = null
)
