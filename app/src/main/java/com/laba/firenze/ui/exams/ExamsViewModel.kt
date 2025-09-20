package com.laba.firenze.ui.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.repository.SessionRepository
import com.laba.firenze.domain.model.Esame
import com.laba.firenze.domain.model.StudentProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExamsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ExamsUiState())
    val uiState: StateFlow<ExamsUiState> = _uiState.asStateFlow()
    
    // Expose exams directly for the new ExamsScreen
    val exams: StateFlow<List<Esame>> = sessionRepository.allExams
    
    init {
        loadExams()
    }
    
    fun loadExams() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Load all data
            sessionRepository.loadAll()
            
            // Observe exams
            sessionRepository.allExams.collect { exams ->
                _uiState.update { 
                    it.copy(
                        allExams = exams,
                        isLoading = false
                    ) 
                }
            }
        }
    }
    
    fun getCurrentYear(): Int? {
        val profile = sessionRepository.getUserProfileFlow().value
        return profile?.currentYear?.toIntOrNull()
    }
    
    fun getExamById(examId: String): Esame? {
        val exams = _uiState.value.allExams
        
        // Se è un ID basato su indice (per esami senza oid)
        if (examId.startsWith("index_")) {
            val index = examId.removePrefix("index_").toIntOrNull()
            return if (index != null && index < exams.size) {
                exams[index]
            } else {
                null
            }
        }
        
        // Altrimenti cerca per oid
        return exams.find { it.oid == examId }
    }
    
    fun refreshExams() {
        viewModelScope.launch {
            sessionRepository.loadExams()
        }
    }
}

data class ExamsUiState(
    val allExams: List<Esame> = emptyList(),
    val filteredExams: List<Esame> = emptyList(),
    val selectedFilter: ExamFilter = ExamFilter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class ExamFilter(val displayName: String) {
    ALL("Tutti"),
    BOOKABLE("Prenotabili"),
    PASSED("Superati"),
    PENDING("In attesa")
}
