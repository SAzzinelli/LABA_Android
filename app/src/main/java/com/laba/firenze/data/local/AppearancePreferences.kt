package com.laba.firenze.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppearancePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("laba_preferences", Context.MODE_PRIVATE)
    
    private val _themePreference = MutableStateFlow(getThemePreference())
    val themePreference: Flow<String> = _themePreference.asStateFlow()
    
    private val _accentChoice = MutableStateFlow(getAccentChoice())
    val accentChoice: Flow<String> = _accentChoice.asStateFlow()
    
    private fun getThemePreference(): String {
        return prefs.getString("laba.theme", "system") ?: "system"
    }
    
    private fun getAccentChoice(): String {
        return prefs.getString("laba.accent", "system") ?: "system"
    }
    
    suspend fun updateTheme(theme: String) {
        prefs.edit().putString("laba.theme", theme).apply()
        _themePreference.value = theme
    }
    
    suspend fun updateAccent(accent: String) {
        prefs.edit().putString("laba.accent", accent).apply()
        _accentChoice.value = accent
    }
}

