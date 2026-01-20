package com.laba.firenze.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeychainHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "labakeychain",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveCredentials(username: String, password: String) {
        sharedPreferences.edit()
            .putString("username", username)
            .putString("password", password)
            .apply()
    }
    
    fun getUsername(): String? {
        return sharedPreferences.getString("username", null)
    }
    
    fun getPassword(): String? {
        return sharedPreferences.getString("password", null)
    }
    
    fun clearCredentials() {
        sharedPreferences.edit()
            .remove("username")
            .remove("password")
            .apply()
    }
    
    /**
     * Fetch credentials da Keychain/UserDefaults (identico a iOS fetchKeychainCredentials)
     * Cerca username/password in diversi formati e normalizza il username
     */
    fun fetchKeychainCredentials(): Pair<String, String>? {
        // Cerca prima nel Keychain (EncryptedSharedPreferences)
        val username = getUsername()
        val password = getPassword()
        
        if (username != null && password != null) {
            println("🔐 KeychainHelper: Found credentials in Keychain")
            return Pair(username, password)
        }
        
        // Fallback a UserDefaults (SharedPreferences normali)
        val userDefaults = context.getSharedPreferences("laba_userdefaults", Context.MODE_PRIVATE)
        val fallbackUsername = userDefaults.getString("laba.username", null)
        val fallbackPassword = userDefaults.getString("laba.password", null)
        
        if (fallbackUsername != null && fallbackPassword != null) {
            println("🔐 KeychainHelper: Found credentials in UserDefaults fallback")
            return Pair(fallbackUsername, fallbackPassword)
        }
        
        println("🔐 KeychainHelper: No credentials found")
        return null
    }
    
    /**
     * Normalizza il username aggiungendo @labafirenze.com se manca (identico a iOS normalizeUsername)
     */
    fun normalizeUsername(username: String): String {
        return if (username.contains("@")) {
            username
        } else {
            "$username@labafirenze.com"
        }
    }
}
