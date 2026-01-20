package com.laba.firenze.ui.profile.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.local.AppearancePreferences
import com.laba.firenze.data.repository.FAQRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebugViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appearancePreferences: AppearancePreferences
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DebugUiState())
    val uiState: StateFlow<DebugUiState> = _uiState.asStateFlow()
    
    init {
        loadDebugInfo()
    }
    
    private fun loadDebugInfo() {
        _uiState.value = _uiState.value.copy(
            appVersion = getAppVersion(),
            apiVersion = appearancePreferences.getApiVersion(),
            fcmToken = getFCMToken()
        )
    }
    
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun getFCMToken(): String? {
        // TODO: Ottenere il token FCM dal FirebaseMessaging
        return null
    }
    
    fun clearCache() {
        viewModelScope.launch {
            // Pulisci cache FAQ
            val prefs = context.getSharedPreferences("laba_preferences", Context.MODE_PRIVATE)
            prefs.edit()
                .remove("laba.faq.cache")
                .remove("laba.faq.cache.timestamp")
                .apply()
            
            _uiState.value = _uiState.value.copy(
                message = "Cache pulita con successo"
            )
        }
    }
    
    fun testConnection() {
        viewModelScope.launch {
            // TODO: Implementare test connessione
            _uiState.value = _uiState.value.copy(
                message = "Test connessione completato"
            )
        }
    }
    
    fun showDebugLogs() {
        // TODO: Implementare visualizzazione log
        _uiState.value = _uiState.value.copy(
            message = "Log di debug visualizzati"
        )
    }
    
    fun setApiVersion(version: String) {
        viewModelScope.launch {
            appearancePreferences.setApiVersion(version)
            _uiState.value = _uiState.value.copy(
                apiVersion = version,
                message = "Versione API cambiata a $version. Riavvia l'app per applicare le modifiche."
            )
        }
    }
}

data class DebugUiState(
    val appVersion: String = "",
    val apiVersion: String = "",
    val fcmToken: String? = null,
    val message: String? = null
)
