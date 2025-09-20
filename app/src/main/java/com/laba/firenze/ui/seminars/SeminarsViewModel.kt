package com.laba.firenze.ui.seminars

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.repository.SessionRepository
import com.laba.firenze.domain.model.Seminario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeminarsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SeminarsUiState())
    val uiState: StateFlow<SeminarsUiState> = _uiState.asStateFlow()
    
    init {
        loadSeminars()
    }
    
    private fun loadSeminars() {
        viewModelScope.launch {
            sessionRepository.seminars.collect { seminars ->
                _uiState.value = _uiState.value.copy(
                    allSeminars = seminars,
                    seminars = filterSeminars(seminars, _uiState.value.searchQuery)
                )
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            searchQuery = query,
            seminars = filterSeminars(currentState.allSeminars, query)
        )
    }
    
    private fun filterSeminars(seminars: List<Seminario>, searchQuery: String): List<Seminario> {
        if (searchQuery.isBlank()) return seminars
        
        return seminars.filter { seminar ->
            seminar.titolo.contains(searchQuery, ignoreCase = true) ||
            seminar.docente?.contains(searchQuery, ignoreCase = true) == true
        }
    }
    
    fun refreshSeminars() {
        viewModelScope.launch {
            sessionRepository.loadSeminars()
        }
    }
}

data class SeminarsUiState(
    val allSeminars: List<Seminario> = emptyList(),
    val seminars: List<Seminario> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
