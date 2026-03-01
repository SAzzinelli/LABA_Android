package com.laba.firenze.ui.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.repository.SessionRepository
import com.laba.firenze.domain.model.Esame
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoursesViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val achievementManager: com.laba.firenze.data.gamification.AchievementManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CoursesUiState())
    val uiState: StateFlow<CoursesUiState> = _uiState.asStateFlow()
    
    init {
        loadCourses()
    }
    
    fun trackSectionVisit(section: String) {
        achievementManager.trackSectionVisit(section)
    }
    
    private fun loadCourses() {
        viewModelScope.launch {
            sessionRepository.allExams.collect { exams ->
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    allCourses = exams,
                    courses = filterCourses(exams, currentState.searchQuery, currentState.selectedYear)
                )
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            searchQuery = query,
            courses = filterCourses(currentState.allCourses, query, currentState.selectedYear)
        )
    }
    
    fun updateYearFilter(year: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            selectedYear = year,
            courses = filterCourses(currentState.allCourses, currentState.searchQuery, year)
        )
    }
    
    private fun filterCourses(courses: List<Esame>, searchQuery: String, selectedYear: String): List<Esame> {
        var filtered = courses
        if (selectedYear != "Tutti") {
            val yearNumber = when (selectedYear) {
                "1° anno", "1° Anno" -> "1"
                "2° anno", "2° Anno" -> "2"
                "3° anno", "3° Anno" -> "3"
                else -> null
            }
            if (yearNumber != null) {
                filtered = filtered.filter { it.anno == yearNumber }
            }
        }
        val q = searchQuery.trim()
        if (q.isNotEmpty()) {
            val qLower = q.lowercase()
            val numericQuery = q.filter { it.isDigit() }.toIntOrNull()
            filtered = filtered.filter { c ->
                prettifyTitle(c.corso).contains(q, ignoreCase = true) ||
                (c.docente ?: "").contains(q, ignoreCase = true) ||
                (numericQuery != null && voteNumber(c.voto) == numericQuery) ||
                (qLower.contains("idone") && isIdoneitaVote(c.voto))
            }
        }
        return filtered
    }
    
    private fun prettifyTitle(title: String) = title.replace("_", " ")
        .split(" ").joinToString(" ") { it.lowercase().replaceFirstChar { ch -> ch.uppercase() } }
    private fun voteNumber(voto: String?): Int? {
        val v = voto?.trim() ?: return null
        if (v.isEmpty()) return null
        return v.split("/").firstOrNull()?.trim()?.toIntOrNull()
    }
    private fun isIdoneitaVote(voto: String?) =
        voto?.lowercase()?.let { it.contains("idoneo") || it.contains("idonea") || it.contains("idoneità") } == true
    
    @Suppress("UNUSED_FUNCTION")
    fun refreshCourses() {
        viewModelScope.launch {
            sessionRepository.loadAll()
        }
    }
    
    fun getUserProfile(): com.laba.firenze.domain.model.StudentProfile? {
        return sessionRepository.getUserProfileFlow().value
    }
    
    val userProfile: StateFlow<com.laba.firenze.domain.model.StudentProfile?> = sessionRepository.getUserProfileFlow()
}

data class CoursesUiState(
    val allCourses: List<Esame> = emptyList(),
    val courses: List<Esame> = emptyList(),
    val searchQuery: String = "",
    val selectedYear: String = "Tutti",
    val isLoading: Boolean = false,
    val error: String? = null
)
