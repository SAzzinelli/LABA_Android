package com.laba.firenze.ui.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.repository.SessionRepository
import com.laba.firenze.domain.model.LogosDoc
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DocumentsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val achievementManager: com.laba.firenze.data.gamification.AchievementManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DocumentsUiState())
    val uiState: StateFlow<DocumentsUiState> = _uiState.asStateFlow()
    
    fun loadDocuments() {
        try {
            println("DocumentsViewModel: loadDocuments() called")
            viewModelScope.launch {
                try {
                    println("DocumentsViewModel: Starting to load documents")
                    _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                    
                    println("DocumentsViewModel: Calling sessionRepository.getDocuments()")
                    val documents = sessionRepository.getDocuments()
                    println("DocumentsViewModel: Loaded ${documents.size} documents")
                    
                    _uiState.value = _uiState.value.copy(
                        documents = documents,
                        isLoading = false,
                        error = null
                    )
                    println("DocumentsViewModel: Documents loaded successfully")
                } catch (e: Exception) {
                    println("DocumentsViewModel: Error loading documents: ${e.message}")
                    e.printStackTrace()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Errore sconosciuto"
                    )
                }
            }
        } catch (e: Exception) {
            println("DocumentsViewModel: Error in loadDocuments(): ${e.message}")
            e.printStackTrace()
        }
    }
    
    suspend fun downloadDocument(allegatoOid: String): ByteArray? {
        return try {
            sessionRepository.downloadDocument(allegatoOid)
        } catch (e: Exception) {
            null
        }
    }
    
    fun trackDispenseOpen(dispenseId: String) {
        achievementManager.trackDispenseOpen(dispenseId)
    }
}

data class DocumentsUiState(
    val documents: List<LogosDoc> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
