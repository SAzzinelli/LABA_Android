package com.laba.firenze.ui.perte

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.repository.SessionRepository
import com.laba.firenze.domain.model.Esame
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
            sessionRepository.allExams.collect { exams ->
                val coreExams = getCoreExams(exams)
                val numericGrades = getNumericGrades(coreExams)
                val sustainedCount = getSustainedCount(coreExams)
                val totalCount = coreExams.size
                val notSustainedCount = maxOf(0, totalCount - sustainedCount)
                val unsustainedCourses = getUnsustainedCourses(coreExams)
                
                val currentAvg30 = if (numericGrades.isNotEmpty()) {
                    numericGrades.sum().toDouble() / numericGrades.size
                } else null
                
                val currentAvg110 = currentAvg30?.let { (it / 30.0 * 110.0).toInt() }
                
                _uiState.value = _uiState.value.copy(
                    coreExams = coreExams,
                    numericGrades = numericGrades,
                    sustainedCount = sustainedCount,
                    totalCount = totalCount,
                    notSustainedCount = notSustainedCount,
                    availableCourses = unsustainedCourses,
                    currentAvg30 = currentAvg30,
                    currentAvg110 = currentAvg110,
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * Esami core (niente tesi/attività integrative) - identico a iOS
     */
    private fun getCoreExams(exams: List<Esame>): List<Esame> {
        return exams.filter { exam ->
            val title = exam.corso.lowercase()
            !title.contains("attivit") && !title.contains("tesi")
        }
    }
    
    /**
     * Voti numerici già registrati (18–30, lode→30, no idoneità) - identico a iOS
     */
    private fun getNumericGrades(coreExams: List<Esame>): List<Int> {
        return coreExams.mapNotNull { exam ->
            parseVoteForThesis(exam.voto)
        }
    }
    
    /**
     * Parse del voto come in iOS (30/30 e lode → 30, idoneo → null)
     */
    private fun parseVoteForThesis(voto: String?): Int? {
        if (voto.isNullOrBlank()) return null
        val cleaned = voto.trim().replace(" e lode", "")
        
        // Check if it's "idoneo" or similar
        if (cleaned.lowercase().contains("idoneo") || cleaned.lowercase().contains("idonea")) {
            return null
        }
        
        // Extract first number from "30/30" format
        val firstPart = cleaned.split("/").firstOrNull()?.trim()
        return firstPart?.toIntOrNull()
    }
    
    private fun getSustainedCount(coreExams: List<Esame>): Int {
        return coreExams.count { exam ->
            val voto = exam.voto?.trim() ?: ""
            voto.isNotEmpty() || exam.data != null
        }
    }
    
    private fun getUnsustainedCourses(coreExams: List<Esame>): List<String> {
        return coreExams.filter { exam ->
            val voto = exam.voto?.trim() ?: ""
            voto.isEmpty() && exam.data == null
        }.map { prettifyTitle(it.corso) }
    }
    
    private fun prettifyTitle(title: String): String {
        return title.replace("_", " ")
    }
    
    /**
     * Calcola nuova media includendo esami simulati
     */
    fun calculateNewAverage(simulated: List<SimItem>): Double? {
        val baseSum = _uiState.value.numericGrades.sum()
        val baseCount = _uiState.value.numericGrades.size
        val simSum = simulated.sumOf { it.grade }
        val simCount = simulated.size
        
        val totalCount = baseCount + simCount
        if (totalCount == 0) return null
        
        return (baseSum + simSum).toDouble() / totalCount
    }
    
    /**
     * Trova il cognome del professore per un corso
     */
    fun getProfessorSurname(courseTitle: String): String? {
        val exam = _uiState.value.coreExams.find { 
            prettifyTitle(it.corso) == courseTitle || it.corso == courseTitle 
        }
        val fullName = exam?.docente?.trim()
        if (fullName.isNullOrEmpty()) return null
        
        val parts = fullName.split(" ", "\t", "\n").filter { it.isNotBlank() }
        return parts.lastOrNull()?.replaceFirstChar { it.uppercase() }
    }
    
    /**
     * Trova i CFA per un corso
     */
    fun getCfaForCourse(courseTitle: String): Int? {
        val exam = _uiState.value.coreExams.find { 
            prettifyTitle(it.corso) == courseTitle || it.corso == courseTitle 
        }
        return exam?.cfa?.toIntOrNull()
    }
}

data class SimulaMediaUiState(
    val isLoading: Boolean = true,
    val coreExams: List<Esame> = emptyList(),
    val numericGrades: List<Int> = emptyList(),
    val sustainedCount: Int = 0,
    val totalCount: Int = 0,
    val notSustainedCount: Int = 0,
    val availableCourses: List<String> = emptyList(),
    val currentAvg30: Double? = null,
    val currentAvg110: Int? = null,
    val error: String? = null
)