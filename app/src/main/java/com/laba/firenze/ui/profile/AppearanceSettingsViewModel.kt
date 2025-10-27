package com.laba.firenze.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.local.AppearancePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppearanceSettingsViewModel @Inject constructor(
    private val appearancePreferences: AppearancePreferences
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AppearanceSettingsUiState())
    val uiState: StateFlow<AppearanceSettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                appearancePreferences.themePreference,
                appearancePreferences.accentChoice
            ) { theme, accent ->
                _uiState.value = _uiState.value.copy(
                    themePreference = theme,
                    accentChoice = accent
                )
            }.collect()
        }
    }
    
    fun updateTheme(theme: String) {
        viewModelScope.launch {
            appearancePreferences.updateTheme(theme)
        }
    }
    
    fun updateAccent(accent: String) {
        viewModelScope.launch {
            appearancePreferences.updateAccent(accent)
        }
    }
}

data class AppearanceSettingsUiState(
    val themePreference: String = "system",
    val accentChoice: String = "system"
)
