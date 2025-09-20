package com.laba.firenze.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BackoffManager per gestire il backoff esponenziale (identico a iOS)
 * Persiste failCount e backoffUntil in SharedPreferences cifrate
 */
@Singleton
class BackoffManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        "backoff_store",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    companion object {
        private const val KEY_FAIL_COUNT = "laba.auth.failCount"
        private const val KEY_BACKOFF_UNTIL = "laba.auth.backoffUntil"
        private const val KEY_LAST_SUCCESS = "laba.auth.lastSuccessAt"
        
        // Progressione backoff: 5, 10, 20, 40, 80, ... max 300s
        private fun calculateDelay(failCount: Int): Long {
            val delay = 5 * (1 shl (failCount - 1)) // 5 * 2^(failCount-1)
            return minOf(delay.toLong(), 300L) // Max 300 secondi
        }
    }
    
    /**
     * Incrementa il failCount e imposta il backoffUntil (identico a iOS bumpBackoff)
     */
    fun bumpBackoff() {
        val failCount = getFailCount() + 1
        val delay = calculateDelay(failCount)
        val backoffUntil = System.currentTimeMillis() + (delay * 1000)
        
        sharedPreferences.edit().apply {
            putInt(KEY_FAIL_COUNT, failCount)
            putLong(KEY_BACKOFF_UNTIL, backoffUntil)
            apply()
        }
        
        println("🔐 BackoffManager: Bumped backoff - failCount: $failCount, delay: ${delay}s, until: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(backoffUntil)}")
    }
    
    /**
     * Reset del backoff dopo successo (identico a iOS resetBackoff)
     */
    fun resetBackoff() {
        val now = System.currentTimeMillis()
        sharedPreferences.edit().apply {
            putInt(KEY_FAIL_COUNT, 0)
            putLong(KEY_BACKOFF_UNTIL, 0L)
            putLong(KEY_LAST_SUCCESS, now)
            apply()
        }
        
        println("🔐 BackoffManager: Reset backoff - success at: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now)}")
    }
    
    /**
     * Verifica se siamo in backoff (identico a iOS isInBackoff)
     */
    fun isInBackoff(): Boolean {
        val backoffUntil = getBackoffUntil()
        val now = System.currentTimeMillis()
        val inBackoff = backoffUntil > now
        
        if (inBackoff) {
            val remaining = (backoffUntil - now) / 1000
            println("🔐 BackoffManager: In backoff - ${remaining}s remaining")
        }
        
        return inBackoff
    }
    
    /**
     * Ottiene il failCount corrente
     */
    fun getFailCount(): Int {
        return sharedPreferences.getInt(KEY_FAIL_COUNT, 0)
    }
    
    /**
     * Ottiene il backoffUntil corrente
     */
    fun getBackoffUntil(): Long {
        return sharedPreferences.getLong(KEY_BACKOFF_UNTIL, 0L)
    }
    
    /**
     * Ottiene l'ultimo successo
     */
    fun getLastSuccessAt(): Long {
        return sharedPreferences.getLong(KEY_LAST_SUCCESS, 0L)
    }
    
    /**
     * Pulisce tutto il backoff (per logout)
     */
    fun clearBackoff() {
        sharedPreferences.edit().clear().apply()
        println("🔐 BackoffManager: Cleared backoff")
    }
}