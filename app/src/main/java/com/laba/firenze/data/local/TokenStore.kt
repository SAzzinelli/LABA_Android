package com.laba.firenze.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TokenStore per gestire access_token e refresh_token (identico a iOS KeychainHelper)
 * Usa EncryptedSharedPreferences per sicurezza
 */
@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        "token_store",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    // StateFlows per osservare i token
    private val _accessToken = MutableStateFlow("")
    val accessToken: StateFlow<String> = _accessToken.asStateFlow()
    
    private val _refreshToken = MutableStateFlow("")
    val refreshToken: StateFlow<String> = _refreshToken.asStateFlow()
    
    private val _tokenType = MutableStateFlow("")
    val tokenType: StateFlow<String> = _tokenType.asStateFlow()
    
    private val _expiresAt = MutableStateFlow(0L)
    val expiresAt: StateFlow<Long> = _expiresAt.asStateFlow()
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    init {
        loadTokens()
    }
    
    /**
     * Salva i token (identico a iOS KeychainHelper)
     */
    fun saveTokens(
        accessToken: String,
        refreshToken: String,
        tokenType: String = "Bearer",
        expiresIn: Long = 3600
    ) {
        val expiresAt = System.currentTimeMillis() + (expiresIn * 1000)
        
        sharedPreferences.edit().apply {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
            putString("token_type", tokenType)
            putLong("expires_at", expiresAt)
            putBoolean("is_logged_in", true)
            apply()
        }
        
        // Aggiorna StateFlows
        _accessToken.value = accessToken
        _refreshToken.value = refreshToken
        _tokenType.value = tokenType
        _expiresAt.value = expiresAt
        _isLoggedIn.value = true
        
        println("🔐 TokenStore: Tokens saved - expires at: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(expiresAt)}")
    }
    
    /**
     * Carica i token dal storage (identico a iOS KeychainHelper)
     */
    private fun loadTokens() {
        val accessToken = sharedPreferences.getString("access_token", null)
        val refreshToken = sharedPreferences.getString("refresh_token", null)
        val tokenType = sharedPreferences.getString("token_type", "Bearer")
        val expiresAt = sharedPreferences.getLong("expires_at", 0L)
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)
        
        if (accessToken != null && refreshToken != null && isLoggedIn) {
            _accessToken.value = accessToken
            _refreshToken.value = refreshToken
            _tokenType.value = tokenType ?: "Bearer"
            _expiresAt.value = expiresAt
            _isLoggedIn.value = true
            
            println("🔐 TokenStore: Tokens loaded - expires at: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(expiresAt)}")
        }
    }
    
    /**
     * Verifica se il token è scaduto (identico a iOS)
     */
    fun isTokenExpired(): Boolean {
        val now = System.currentTimeMillis()
        val expiresAt = _expiresAt.value
        return now >= expiresAt
    }
    
    /**
     * Verifica se il token scade tra poco (120 secondi) per refresh proattivo (identico a iOS)
     */
    fun shouldRefreshToken(): Boolean {
        val now = System.currentTimeMillis()
        val expiresAt = _expiresAt.value
        val timeRemaining = expiresAt - now
        return timeRemaining < 120_000 // 120 secondi
    }
    
    /**
     * Pulisce tutti i token (logout) (identico a iOS)
     */
    fun clearTokens() {
        sharedPreferences.edit().clear().apply()
        
        _accessToken.value = ""
        _refreshToken.value = ""
        _tokenType.value = ""
        _expiresAt.value = 0L
        _isLoggedIn.value = false
        
        println("🔐 TokenStore: Tokens cleared")
    }
    
    /**
     * Ottiene l'access token corrente (per API calls)
     */
    fun getCurrentAccessToken(): String {
        return _accessToken.value
    }
    
    /**
     * Ottiene il refresh token corrente (per refresh)
     */
    fun getCurrentRefreshToken(): String {
        return _refreshToken.value
    }
    
    /**
     * Verifica se ha un access token valido
     */
    fun hasAccessToken(): Boolean {
        return _accessToken.value.isNotEmpty() && !isTokenExpired()
    }
    
    /**
     * Ottiene la data di scadenza del token
     */
    fun getTokenExpiry(): Long {
        return _expiresAt.value
    }
    
    /**
     * Verifica se il token è valido e non scaduto
     */
    fun isTokenValid(): Boolean {
        return _accessToken.value.isNotEmpty() && !isTokenExpired()
    }
    
    /**
     * Ottiene il token corrente per il silent login
     */
    fun getCurrentToken(): String {
        return _accessToken.value
    }
}
