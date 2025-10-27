package com.laba.firenze.data.repository

import android.content.Context
import android.util.Log
import com.laba.firenze.domain.model.LessonEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonCalendarRepository @Inject constructor(
    private val context: Context
) {
    private val _events = MutableStateFlow<List<LessonEvent>>(emptyList())
    val events: StateFlow<List<LessonEvent>> = _events.asStateFlow()
    
    private val cacheFile: File by lazy {
        File(context.cacheDir, "lesson_calendar_cache.json")
    }
    
    companion object {
        private const val TAG = "LessonCalendar"
        private const val NPOINT_URL = "https://api.npoint.io/7ad5addbea4721f9483c"
    }
    
    fun loadCacheIfAvailable() {
        try {
            if (cacheFile.exists()) {
                val cacheData = cacheFile.readText()
                val cachedEvents = parseLessons(cacheData)
                _events.value = cachedEvents
                Log.d(TAG, "Cache loaded: ${cachedEvents.size} events")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading cache", e)
        }
    }
    
    suspend fun syncFromURL(force: Boolean = true): Boolean {
        return try {
            Log.d(TAG, "Fetching lessons from npoint...")
            
            val url = java.net.URL(NPOINT_URL)
            val connection = url.openConnection()
            connection.connect()
            
            val inputStream = connection.getInputStream()
            val response = inputStream.bufferedReader().use { it.readText() }
            
            inputStream.close()
            
            val events = parseLessons(response)
            _events.value = events
            
            // Save to cache
            cacheFile.writeText(response)
            
            Log.d(TAG, "Synced ${events.size} events")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing lessons", e)
            loadCacheIfAvailable()
            false
        }
    }
    
    private fun parseLessons(jsonString: String): List<LessonEvent> {
        val events = mutableListOf<LessonEvent>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        
        try {
            val jsonArray = JSONArray(jsonString)
            
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                
                // Parse dates
                val startDate = parseDate(obj.getString("start"))
                val endDate = parseDate(obj.getString("end"))
                
                // Generate ID
                val firstOidCorsi = obj.optJSONArray("oidCorsi")?.let {
                    if (it.length() > 0) it.getString(0) else null
                }
                val oidCorsoValue = obj.optString("oidCorso", null)
                val id = generateId(
                    obj.getString("corso"),
                    oidCorsoValue,
                    firstOidCorsi,
                    obj.getInt("anno"),
                    startDate
                )
                
                val event = LessonEvent(
                    id = id,
                    corso = obj.getString("corso"),
                    oidCorso = if (obj.has("oidCorso") && !obj.isNull("oidCorso")) {
                        obj.optString("oidCorso", null)
                    } else null,
                    oidCorsi = if (obj.has("oidCorsi") && !obj.isNull("oidCorsi")) {
                        val arr = obj.getJSONArray("oidCorsi")
                        (0 until arr.length()).map { arr.getString(it) }
                    } else null,
                    anno = obj.getInt("anno"),
                    aula = if (obj.has("aula") && !obj.isNull("aula")) obj.getString("aula") else null,
                    docente = if (obj.has("docente") && !obj.isNull("docente")) obj.getString("docente") else null,
                    start = startDate,
                    end = endDate,
                    note = if (obj.has("note") && !obj.isNull("note")) obj.getString("note") else null
                )
                
                events.add(event)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing lessons", e)
        }
        
        return events
    }
    
    private fun parseDate(dateString: String): Date {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS"
        )
        
        val timeZone = TimeZone.getTimeZone("UTC")
        
        for (format in formats) {
            try {
                val df = SimpleDateFormat(format, Locale.US)
                df.timeZone = timeZone
                return df.parse(dateString) ?: continue
            } catch (e: Exception) {
                // Try next format
            }
        }
        
        throw IllegalArgumentException("Unable to parse date: $dateString")
    }
    
    private fun generateId(
        corso: String,
        oidCorso: String?,
        firstOidCorsi: String?,
        anno: Int,
        start: Date
    ): String {
        val base = when {
            !oidCorso.isNullOrEmpty() -> oidCorso.lowercase()
            !firstOidCorsi.isNullOrEmpty() -> firstOidCorsi.lowercase()
            else -> corso.filter { it.isLetterOrDigit() }.lowercase()
        }
        
        val timestamp = (start.time / 1000).toInt()
        return "$base-$anno-$timestamp"
    }
}

