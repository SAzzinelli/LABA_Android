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
class SimulaMediaViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SimulaMediaUiState())
    val uiState: StateFlow<SimulaMediaUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            try {
                val exams = sessionRepository.allExams.value
                val mediaAttuale = sessionRepository.gradeCalculator.calculateAverage(exams)
                
                _uiState.value = _uiState.value.copy(
                    mediaAttuale = mediaAttuale
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message
                )
            }
        }
    }
    
    fun addSimulatedExam(nome: String, cfu: Int, voto: Double) {
        // TODO: Implement simulation logic
    }
}

data class SimulaMediaUiState(
    val isLoading: Boolean = false,
    val mediaAttuale: Double? = null,
    val mediaSimulata: Double? = null,
    val error: String? = null
)
