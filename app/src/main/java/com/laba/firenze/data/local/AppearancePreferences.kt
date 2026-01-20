package com.laba.firenze.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    val accentChoice: StateFlow<String> = _accentChoice.asStateFlow()
    
    fun getThemePreference(): String {
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
    
    fun getApiVersion(): String {
        return prefs.getString("laba.apiVersion", "v2") ?: "v2"
    }
    
    fun setApiVersion(version: String) {
        prefs.edit().putString("laba.apiVersion", version).commit()
    }
    
    fun getSelectedGroup(): String? {
        val g = prefs.getString("laba.selectedGroup", null)
        return if (g.isNullOrEmpty()) null else g
    }
    
    fun setSelectedGroup(group: String?) {
        prefs.edit().putString("laba.selectedGroup", group).apply()
    }
    
    fun getPattern(): String {
        return prefs.getString("hero_background_pattern", "wave") ?: "wave"
    }
    
    fun setPattern(pattern: String) {
        prefs.edit().putString("hero_background_pattern", pattern).apply()
    }

    fun getActiveTabs(): List<String>? {
        val raw = prefs.getString("laba.nav.activeTabs", null) ?: return null
        return try {
            raw.split(",").filter { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }
    
    fun setActiveTabs(tabs: List<String>) {
        val raw = tabs.joinToString(",")
        prefs.edit().putString("laba.nav.activeTabs", raw).apply()
    }
}

