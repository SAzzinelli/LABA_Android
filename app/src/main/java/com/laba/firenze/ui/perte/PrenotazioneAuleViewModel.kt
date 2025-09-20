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
class PrenotazioneAuleViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PrenotazioneAuleUiState())
    val uiState: StateFlow<PrenotazioneAuleUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val userProfile = sessionRepository.getUserProfile()
                val accessToken = sessionRepository.getAccessToken()
                
                if (accessToken.isNotEmpty()) {
                    val superSaasUrl = buildSuperSaasUrl(userProfile?.emailLABA)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        superSaasUrl = superSaasUrl
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
    
    private fun buildSuperSaasUrl(email: String?): String {
        // Costruisce l'URL di SuperSaaS per la prenotazione aule
        return email?.let { "https://supersaas.com/schedule/LABA?email=$it" } 
            ?: "https://supersaas.com/schedule/LABA"
    }
}

data class PrenotazioneAuleUiState(
    val isLoading: Boolean = false,
    val superSaasUrl: String? = null,
    val error: String? = null
)
