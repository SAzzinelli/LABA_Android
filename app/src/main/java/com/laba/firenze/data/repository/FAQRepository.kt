package com.laba.firenze.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.laba.firenze.domain.model.FAQCategory
import com.laba.firenze.domain.model.FAQResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.io.BufferedReader
import java.io.InputStreamReader

@Singleton
class FAQRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "FAQRepository"
        private const val BASE_URL = "https://SAzzinelli.github.io/LABA_Orari/faq/faq.json"
        private const val CACHE_KEY = "laba.faq.cache"
        private const val CACHE_TIMESTAMP_KEY = "laba.faq.cache.timestamp"
        private const val CACHE_VALIDITY_INTERVAL = 3600000L // 1 ora in millisecondi
    }
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("laba_preferences", Context.MODE_PRIVATE)
    
    private val _categories = MutableStateFlow<List<FAQCategory>>(emptyList())
    val categories: StateFlow<List<FAQCategory>> = _categories.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val gson: Gson = GsonBuilder().create()
    
    init {
        // Prova prima a caricare dal bundle locale
        if (!loadFromBundle()) {
            // Altrimenti carica dalla cache
            loadCacheIfAvailable()
        }
    }
    
    /**
     * Carica le FAQ dal bundle locale (assets)
     */
    fun loadFromBundle(): Boolean {
        return try {
            val inputStream = context.assets.open("faq.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }
            val faqResponse: FAQResponse = gson.fromJson(jsonString, FAQResponse::class.java)
            
            _categories.value = faqResponse.categories
            saveCache(faqResponse.categories)
            
            Log.d(TAG, "✅ Caricate ${faqResponse.categories.size} categorie con ${faqResponse.categories.sumOf { it.items.size }} FAQ dal bundle locale")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Errore nel caricamento dal bundle: ${e.message}", e)
            false
        }
    }
    
    /**
     * Carica le FAQ da GitHub Pages
     */
    suspend fun loadFromGitHubPages(force: Boolean = false): Boolean {
        // Controlla cache se non è un force refresh
        if (!force) {
            val cached = loadCacheIfAvailable()
            if (cached.isNotEmpty()) {
                val timestamp = prefs.getLong(CACHE_TIMESTAMP_KEY, 0)
                val age = System.currentTimeMillis() - timestamp
                if (age < CACHE_VALIDITY_INTERVAL) {
                    Log.d(TAG, "✅ Usando cache (età: ${age / 1000}s)")
                    return true
                }
            }
        }
        
        // Prova prima a caricare dal bundle locale
        if (loadFromBundle()) {
            return true
        }
        
        _isLoading.value = true
        _error.value = null
        
        return try {
            val url = java.net.URL(BASE_URL)
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            if (connection.responseCode != 200) {
                _error.value = "Errore nel caricamento (HTTP ${connection.responseCode})"
                _isLoading.value = false
                return false
            }
            
            val inputStream = connection.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }
            
            val faqResponse: FAQResponse = gson.fromJson(jsonString, FAQResponse::class.java)
            _categories.value = faqResponse.categories
            saveCache(faqResponse.categories)
            
            Log.d(TAG, "✅ Caricate ${faqResponse.categories.size} categorie con ${faqResponse.categories.sumOf { it.items.size }} FAQ")
            _isLoading.value = false
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Errore: ${e.message}", e)
            _error.value = e.message
            _isLoading.value = false
            false
        }
    }
    
    private fun saveCache(categories: List<FAQCategory>) {
        try {
            val jsonString = gson.toJson(FAQResponse(categories))
            prefs.edit()
                .putString(CACHE_KEY, jsonString)
                .putLong(CACHE_TIMESTAMP_KEY, System.currentTimeMillis())
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Errore nel salvataggio cache: ${e.message}", e)
        }
    }
    
    private fun loadCacheIfAvailable(): List<FAQCategory> {
        return try {
            val cachedJson = prefs.getString(CACHE_KEY, null) ?: return emptyList()
            val faqResponse: FAQResponse = gson.fromJson(cachedJson, FAQResponse::class.java)
            _categories.value = faqResponse.categories
            faqResponse.categories
        } catch (e: Exception) {
            Log.e(TAG, "Errore nel caricamento cache: ${e.message}", e)
            emptyList()
        }
    }
}
