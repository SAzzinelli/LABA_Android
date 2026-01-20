package com.laba.firenze.ui.faq

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.repository.FAQRepository
import com.laba.firenze.domain.model.FAQCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FAQViewModel @Inject constructor(
    private val faqRepository: FAQRepository,
    private val achievementManager: com.laba.firenze.data.gamification.AchievementManager
) : ViewModel() {
    
    private val _categories = MutableStateFlow<List<FAQCategory>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    
    val uiState: StateFlow<FAQUiState> = combine(
        _categories,
        _isLoading,
        _error
    ) { categories, isLoading, error ->
        FAQUiState(
            categories = categories,
            isLoading = isLoading,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FAQUiState()
    )
    
    init {
        viewModelScope.launch {
            faqRepository.categories.collect { categories ->
                _categories.value = categories
            }
        }
        viewModelScope.launch {
            faqRepository.isLoading.collect { isLoading ->
                _isLoading.value = isLoading
            }
        }
        viewModelScope.launch {
            faqRepository.error.collect { error ->
                _error.value = error
            }
        }
    }
    
    fun loadFromBundle(): Boolean {
        return faqRepository.loadFromBundle()
    }
    
    fun loadFromGitHubPages(force: Boolean = false) {
        viewModelScope.launch {
            faqRepository.loadFromGitHubPages(force)
        }
    }
    
    fun trackFAQVisit() {
        achievementManager.trackFAQVisit()
    }
}

data class FAQUiState(
    val categories: List<FAQCategory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
