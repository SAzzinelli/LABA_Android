package com.laba.firenze.ui.appearance.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _selectedPattern = MutableStateFlow("wave")
    val selectedPattern = _selectedPattern.asStateFlow()
    
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        "LABA_PREFS", Context.MODE_PRIVATE
    )
    
    init {
        _selectedPattern.value = sharedPrefs.getString("hero_background_pattern", "wave") ?: "wave"
    }
    
    fun selectPattern(pattern: String) {
        _selectedPattern.value = pattern
        sharedPrefs.edit().putString("hero_background_pattern", pattern).apply()
    }
    
    fun getSelectedPattern(): String {
        return sharedPrefs.getString("hero_background_pattern", "wave") ?: "wave"
    }
}

