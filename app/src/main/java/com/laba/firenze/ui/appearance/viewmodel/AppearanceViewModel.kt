package com.laba.firenze.ui.appearance.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val appearancePreferences: com.laba.firenze.data.local.AppearancePreferences
) : ViewModel() {
    
    private val _selectedPattern = MutableStateFlow("wave")
    val selectedPattern = _selectedPattern.asStateFlow()
    
    private val _selectedGroup = MutableStateFlow<String?>(null)
    val selectedGroup = _selectedGroup.asStateFlow()
    
    init {
        _selectedGroup.value = appearancePreferences.getSelectedGroup()
        _selectedPattern.value = appearancePreferences.getPattern()
    }
    
    fun selectPattern(pattern: String) {
        _selectedPattern.value = pattern
        appearancePreferences.setPattern(pattern)
    }
    
    fun selectGroup(group: String?) {
        _selectedGroup.value = group
        appearancePreferences.setSelectedGroup(group)
    }
    
    fun getThemePreference(): String {
        return appearancePreferences.getThemePreference()
    }
    
    suspend fun setThemePreference(theme: String) {
        appearancePreferences.updateTheme(theme)
    }
    
    fun getAccentChoice(): StateFlow<String> {
        return appearancePreferences.accentChoice
    }
    
    suspend fun setAccentChoice(accent: String) {
        appearancePreferences.updateAccent(accent)
    }
}

