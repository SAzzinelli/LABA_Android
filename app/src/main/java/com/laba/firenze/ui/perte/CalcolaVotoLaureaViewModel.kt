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
class CalcolaVotoLaureaViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CalcolaVotoLaureaUiState())
    val uiState: StateFlow<CalcolaVotoLaureaUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val exams = sessionRepository.allExams.value
                val mediaCarriera = sessionRepository.gradeCalculator.calculateAverage(exams)
                val votoLaurea = sessionRepository.gradeCalculator.calculatePresentationGrade(exams)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    mediaCarriera = mediaCarriera,
                    votoLaurea = votoLaurea
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}

data class CalcolaVotoLaureaUiState(
    val isLoading: Boolean = false,
    val mediaCarriera: Double? = null,
    val votoLaurea: String? = null,
    val error: String? = null
)
