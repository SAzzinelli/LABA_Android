package com.laba.firenze.data.local

import android.content.Context
import android.content.SharedPreferences
import com.laba.firenze.domain.model.StudentProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionTokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("labasession", Context.MODE_PRIVATE)
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    private val _accessToken = MutableStateFlow("")
    val accessToken: StateFlow<String> = _accessToken.asStateFlow()
    
    private val _userProfile = MutableStateFlow<StudentProfile?>(null)
    val userProfile: StateFlow<StudentProfile?> = _userProfile.asStateFlow()

    private val _profilePhotoURL = MutableStateFlow<String?>(null)
    val profilePhotoURL: StateFlow<String?> = _profilePhotoURL.asStateFlow()
    
    init {
        loadCredentials()
    }
    
    fun saveTokens(accessToken: String, refreshToken: String?) {
        sharedPreferences.edit().apply {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
            putLong("token_expires_at", System.currentTimeMillis() + (3600 * 1000)) // 1 hour
            putBoolean("is_logged_in", true)
            apply()
        }
        
        _accessToken.value = accessToken
        _isLoggedIn.value = true
    }
    
    fun saveUserProfile(profile: StudentProfile) {
        sharedPreferences.edit().apply {
            putString("user_display_name", profile.displayName)
            putString("user_status", profile.status)
            putString("user_current_year", profile.currentYear)
            putString("user_piano_studi", profile.pianoStudi)
            putString("user_nome", profile.nome)
            putString("user_cognome", profile.cognome)
            putString("user_matricola", profile.matricola)
            putString("user_sesso", profile.sesso)
            putString("user_email_laba", profile.emailLABA)
            putString("user_email_personale", profile.emailPersonale)
            putString("user_telefono", profile.telefono)
            putString("user_cellulare", profile.cellulare)
            putString("user_pagamenti", profile.pagamenti)
            putString("user_student_oid", profile.studentOid)
            apply()
        }
        
        _userProfile.value = profile
    }
    
    fun clearCredentials() {
        sharedPreferences.edit().clear().apply()
        _isLoggedIn.value = false
        _accessToken.value = ""
        _userProfile.value = null
        _profilePhotoURL.value = null
    }

    private fun userKey(): String {
        val email = sharedPreferences.getString("user_email_laba", null)
            ?: sharedPreferences.getString("user_email_personale", null)
            ?: "default"
        return email
    }

    /** Email utente corrente (per FCM upsert Supabase in background, es. onNewToken). */
    fun getStoredUserEmail(): String? {
        return sharedPreferences.getString("user_email_laba", null)
            ?: sharedPreferences.getString("user_email_personale", null)
    }

    /** Nome/cognome per portale notifiche (dropdown destinatari). */
    fun getStoredUserDisplayName(): String? {
        return sharedPreferences.getString("user_display_name", null)?.takeIf { it.isNotBlank() }
    }

    fun getProfilePhotoURL(): String? =
        sharedPreferences.getString("profile_photo_url_${userKey()}", null)

    fun getProfilePhotoDeleteURL(): String? =
        sharedPreferences.getString("profile_photo_delete_url_${userKey()}", null)

    fun setProfilePhotoURL(url: String, deleteURL: String?) {
        val key = userKey()
        sharedPreferences.edit().apply {
            putString("profile_photo_url_$key", url)
            putString("profile_photo_delete_url_$key", deleteURL ?: "")
            apply()
        }
        _profilePhotoURL.value = url
    }

    /** Rimuove l'URL foto profilo (es. quando l'immagine non è più disponibile su ImgBB). */
    fun clearProfilePhotoURL() {
        val key = userKey()
        sharedPreferences.edit().apply {
            remove("profile_photo_url_$key")
            remove("profile_photo_delete_url_$key")
            apply()
        }
        _profilePhotoURL.value = null
    }

    fun loadProfilePhotoURL() {
        _profilePhotoURL.value = getProfilePhotoURL()
    }
    
    fun getRefreshToken(): String? {
        return sharedPreferences.getString("refresh_token", null)
    }
    
    fun isTokenExpired(): Boolean {
        val expiresAt = sharedPreferences.getLong("token_expires_at", 0)
        return System.currentTimeMillis() > expiresAt
    }
    
    private fun loadCredentials() {
        val accessToken = sharedPreferences.getString("access_token", null)
        val isLoggedInPref = sharedPreferences.getBoolean("is_logged_in", false)
        
        if (accessToken != null && isLoggedInPref) {
            _accessToken.value = accessToken
            _isLoggedIn.value = true
            
            // Load user profile
            val profile = StudentProfile(
                displayName = sharedPreferences.getString("user_display_name", null),
                status = sharedPreferences.getString("user_status", null),
                currentYear = sharedPreferences.getString("user_current_year", null),
                pianoStudi = sharedPreferences.getString("user_piano_studi", null),
                nome = sharedPreferences.getString("user_nome", null),
                cognome = sharedPreferences.getString("user_cognome", null),
                matricola = sharedPreferences.getString("user_matricola", null),
                sesso = sharedPreferences.getString("user_sesso", null),
                emailLABA = sharedPreferences.getString("user_email_laba", null),
                emailPersonale = sharedPreferences.getString("user_email_personale", null),
                telefono = sharedPreferences.getString("user_telefono", null),
                cellulare = sharedPreferences.getString("user_cellulare", null),
                pagamenti = sharedPreferences.getString("user_pagamenti", null),
                studentOid = sharedPreferences.getString("user_student_oid", null)
            )
            
            _userProfile.value = profile
        }
        loadProfilePhotoURL()
    }
}
