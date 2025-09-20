package com.laba.firenze.data.local

import android.util.Base64
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * JWT Decoder per estrarre informazioni dal token (identico a iOS)
 * Decodifica l'exp dal payload per calcolare tokenSecondsRemaining
 */
@Singleton
class JwtDecoder @Inject constructor() {
    
    /**
     * Estrae l'exp dal JWT e calcola i secondi rimanenti (identico a iOS tokenSecondsRemaining)
     */
    fun getTokenSecondsRemaining(token: String): Int? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            
            val payload = parts[1]
            val decodedPayload = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING)
            val payloadJson = JSONObject(String(decodedPayload))
            
            val exp = payloadJson.getLong("exp")
            val now = System.currentTimeMillis() / 1000
            val remaining = (exp - now).toInt()
            
            println("🔐 JwtDecoder: Token exp: $exp, now: $now, remaining: $remaining seconds")
            remaining
        } catch (e: Exception) {
            println("🔐 JwtDecoder: Error decoding JWT: ${e.message}")
            null
        }
    }
    
    /**
     * Verifica se il token è scaduto (identico a iOS isTokenExpired)
     */
    fun isTokenExpired(token: String): Boolean {
        val remaining = getTokenSecondsRemaining(token)
        return remaining == null || remaining <= 0
    }
    
    /**
     * Verifica se il token scade tra poco (identico a iOS shouldRefreshToken)
     */
    fun shouldRefreshToken(token: String, thresholdSeconds: Int = 30): Boolean {
        val remaining = getTokenSecondsRemaining(token)
        return remaining != null && remaining <= thresholdSeconds
    }
    
    /**
     * Estrae la data di scadenza del token (identico a iOS tokenExpiryDate)
     */
    fun getTokenExpiryDate(token: String): Long? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            
            val payload = parts[1]
            val decodedPayload = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING)
            val payloadJson = JSONObject(String(decodedPayload))
            
            val exp = payloadJson.getLong("exp")
            exp * 1000 // Converti in millisecondi
        } catch (e: Exception) {
            println("🔐 JwtDecoder: Error extracting expiry date: ${e.message}")
            null
        }
    }
}