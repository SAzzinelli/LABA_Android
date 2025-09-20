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
    private val sessionRepository: SessionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CoursesUiState())
    val uiState: StateFlow<CoursesUiState> = _uiState.asStateFlow()
    
    init {
        loadCourses()
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
        // First filter out TESI and ATTIVITÀ A SCELTA
        var filtered = courses.filter { course ->
            val courseTitle = course.corso.uppercase()
            !courseTitle.contains("TESI") && !courseTitle.contains("ATTIVITÀ A SCELTA")
        }
        
        // Filter by year
        if (selectedYear != "Tutti") {
            val yearNumber = when (selectedYear) {
                "1° Anno" -> "1"
                "2° Anno" -> "2" 
                "3° Anno" -> "3"
                else -> null
            }
            if (yearNumber != null) {
                filtered = filtered.filter { it.anno == yearNumber }
            }
        }
        
        // Filter by search query
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { course ->
                course.corso.contains(searchQuery, ignoreCase = true) ||
                course.docente?.contains(searchQuery, ignoreCase = true) == true
            }
        }
        
        return filtered
    }
    
    fun refreshCourses() {
        viewModelScope.launch {
            sessionRepository.loadAll()
        }
    }
}

data class CoursesUiState(
    val allCourses: List<Esame> = emptyList(),
    val courses: List<Esame> = emptyList(),
    val searchQuery: String = "",
    val selectedYear: String = "Tutti",
    val isLoading: Boolean = false,
    val error: String? = null
)
