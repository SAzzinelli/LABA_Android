package com.laba.firenze.ui.notifications.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.NotificationCategory
import com.laba.firenze.data.TopicManager
import com.laba.firenze.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val topicManager: TopicManager,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    
    data class NotificationSettingsState(
        val notificationsEnabled: Boolean = true,
        val categoriesEnabled: Map<NotificationCategory, Boolean> = NotificationCategory.values().associateWith { true }
    )
    
    private val _uiState = MutableStateFlow(NotificationSettingsState())
    val uiState: StateFlow<NotificationSettingsState> = _uiState
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            val enabled = topicManager.isNotificationsEnabled()
            val categories = NotificationCategory.values().associateWith { category ->
                topicManager.isCategoryEnabled(category)
            }
            
            _uiState.value = NotificationSettingsState(
                notificationsEnabled = enabled,
                categoriesEnabled = categories
            )
        }
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            topicManager.setNotificationsEnabled(enabled)
            _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
        }
    }
    
    fun setCategoryEnabled(category: NotificationCategory, enabled: Boolean) {
        viewModelScope.launch {
            topicManager.setCategoryEnabled(category, enabled)
            val newCategories = _uiState.value.categoriesEnabled.toMutableMap()
            newCategories[category] = enabled
            _uiState.value = _uiState.value.copy(categoriesEnabled = newCategories)
            
            // Re-update FCM topics when category preference changes
            updateFCMTopics()
        }
    }
    
    /**
     * Force resync all topics - can be called manually to fix sync issues
     */
    fun forceResyncTopics() {
        updateFCMTopics()
    }
    
    /**
     * Re-update FCM topics when preferences change
     */
    private fun updateFCMTopics() {
        viewModelScope.launch {
            try {
                val profile = sessionRepository.tokenManager.userProfile.value
                if (profile != null) {
                    val isGraduated = profile.status?.lowercase()?.contains("laureat") == true
                    val currentYear = profile.currentYear?.toIntOrNull()
                    val pianoStudi = profile.pianoStudi
                    
                    topicManager.updateTopics(
                        scope = viewModelScope,
                        course = pianoStudi,
                        currentYear = currentYear,
                        isGraduated = isGraduated,
                        isDocente = false
                    )
                }
            } catch (e: Exception) {
                println("🔔 NotificationSettingsViewModel: Error updating FCM topics: ${e.message}")
            }
        }
    }
    
    fun getCategoryEnabled(category: NotificationCategory): Boolean {
        return _uiState.value.categoriesEnabled[category] ?: true
    }
}

