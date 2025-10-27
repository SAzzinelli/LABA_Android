package com.laba.firenze.ui.perte

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.domain.model.Esame
import com.laba.firenze.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import com.laba.firenze.ui.common.prettifyTitle

@HiltViewModel
class GradeTrendViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GradeTrendUiState())
    val uiState: StateFlow<GradeTrendUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            sessionRepository.exams.collect { exams ->
                val processedExams = processExams(exams)
                val avgPoints = calculateAvgPoints(processedExams)
                
                _uiState.value = _uiState.value.copy(
                    exams = processedExams,
                    avgPoints = avgPoints,
                    currentAvg = avgPoints.lastOrNull()?.avg,
                    deltaAvg = calculateDeltaAvg(avgPoints),
                    bestGrade = processedExams.maxOfOrNull { it.grade },
                    worstGrade = processedExams.minOfOrNull { it.grade },
                    lodiCount = calculateLodiCount(exams),
                    totalEligibleExams = calculateTotalEligibleExams(exams),
                    examTitles = processedExams.map { prettifyTitle(it.title) },
                    examDates = processedExams.map { formatDate(it.date) },
                    examGrades = processedExams.map { it.grade }
                )
            }
        }
    }
    
    private fun processExams(exams: List<Esame>): List<ExamData> {
        return exams
            .filter { exam ->
                // Escludi tesi & affini + attività integrative
                val titleNorm = exam.corso.lowercase()
                !titleNorm.contains("tesi") && 
                !titleNorm.contains("prova finale") && 
                !titleNorm.contains("elaborato finale") && 
                !titleNorm.contains("attivit")
            }
            .filter { exam ->
                // Solo voti numerici validi
                exam.data != null && parseVote(exam.voto) != null
            }
            .sortedBy { it.data }
            .mapIndexed { index, exam ->
                ExamData(
                    id = index,
                    title = exam.corso,
                    grade = parseVote(exam.voto) ?: 0,
                    date = parseDate(exam.data) ?: Date()
                )
            }
    }
    
    private fun calculateAvgPoints(exams: List<ExamData>): List<AvgPoint> {
        val avgPoints = mutableListOf<AvgPoint>()
        var sum = 0.0
        
        exams.forEachIndexed { index, exam ->
            sum += exam.grade
            avgPoints.add(
                AvgPoint(
                    id = index,
                    avg = sum / (index + 1),
                    date = exam.date
                )
            )
        }
        
        return avgPoints
    }
    
    private fun calculateDeltaAvg(avgPoints: List<AvgPoint>): Double {
        return if (avgPoints.size >= 2) {
            avgPoints.last().avg - avgPoints[avgPoints.size - 2].avg
        } else {
            0.0
        }
    }
    
    private fun calculateLodiCount(exams: List<Esame>): Int {
        return exams.count { exam ->
            val title = exam.corso.lowercase()
            val vote = (exam.voto ?: "").lowercase()
            
            !title.contains("attivit") &&
            !title.contains("tesi") &&
            !vote.contains("idone") &&
            vote.contains("lode")
        }
    }
    
    private fun calculateTotalEligibleExams(exams: List<Esame>): Int {
        return exams.count { exam ->
            val title = exam.corso.lowercase()
            val vote = (exam.voto ?: "").lowercase()
            
            !title.contains("attivit") &&
            !title.contains("tesi") &&
            !vote.contains("idone")
        }
    }
    
    private fun parseVote(voto: String?): Int? {
        val vote = voto?.lowercase() ?: return null
        
        if (vote.contains("idoneo") || vote.contains("idonea") || vote.contains("idoneità")) {
            return null
        }
        
        if (vote.contains("lode")) {
            return 30
        }
        
        val digits = vote.filter { it.isDigit() }
        if (digits.isNotEmpty()) {
            val number = digits.toIntOrNull()
            if (number != null && number in 18..30) {
                return number
            }
        }
        
        // Try to parse "18/30" format
        val parts = vote.split("/")
        if (parts.size >= 2) {
            val firstPart = parts[0].filter { it.isDigit() }
            if (firstPart.isNotEmpty()) {
                val number = firstPart.toIntOrNull()
                if (number != null && number in 18..30) {
                    return number
                }
            }
        }
        
        return null
    }
    
    private fun parseDate(dateString: String?): Date? {
        if (dateString == null) return null
        
        return try {
            // Try different date formats
            val formats = listOf(
                "yyyy-MM-dd",
                "yyyy-MM-dd'T'HH:mm:ss",
                "dd/MM/yyyy",
                "dd-MM-yyyy"
            )
            
            for (format in formats) {
                try {
                    val formatter = SimpleDateFormat(format, Locale.getDefault())
                    return formatter.parse(dateString)
                } catch (e: Exception) {
                    // Try next format
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
    
    fun selectIndex(index: Int?) {
        _uiState.value = _uiState.value.copy(selectedIndex = index)
    }
    
    fun toggleSort() {
        _uiState.value = _uiState.value.copy(sortAscending = !_uiState.value.sortAscending)
    }
    
    fun showKPIInfo() {
        _uiState.value = _uiState.value.copy(showKPIInfo = true)
    }
    
    fun hideKPIInfo() {
        _uiState.value = _uiState.value.copy(showKPIInfo = false)
    }
    
    fun showLodiInfo() {
        _uiState.value = _uiState.value.copy(showLodiInfo = true)
    }
    
    fun hideLodiInfo() {
        _uiState.value = _uiState.value.copy(showLodiInfo = false)
    }
    
    private fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale("it", "IT"))
        return formatter.format(date)
    }
}

data class GradeTrendUiState(
    val exams: List<ExamData> = emptyList(),
    val avgPoints: List<AvgPoint> = emptyList(),
    val currentAvg: Double? = null,
    val deltaAvg: Double = 0.0,
    val bestGrade: Int? = null,
    val worstGrade: Int? = null,
    val lodiCount: Int = 0,
    val totalEligibleExams: Int = 0,
    val selectedIndex: Int? = null,
    val sortAscending: Boolean = false,
    val showKPIInfo: Boolean = false,
    val showLodiInfo: Boolean = false,
    val examTitles: List<String> = emptyList(),
    val examDates: List<String> = emptyList(),
    val examGrades: List<Int> = emptyList()
)
