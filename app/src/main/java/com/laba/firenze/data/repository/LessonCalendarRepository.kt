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
    
    suspend fun syncLessons(pianoStudi: String?, currentYear: Int?, force: Boolean = true): Boolean {
        // 1. Try GitHub Pages (Primary)
        val ghSuccess = syncFromGitHubPages(pianoStudi, currentYear)
        if (ghSuccess) {
            Log.d(TAG, "Synced successfully from GitHub Pages")
            return true
        }
        
        // 2. Fallback to npoint (Reserve)
        Log.w(TAG, "GitHub Pages sync failed, falling back to npoint")
        return syncFromNpoint()
    }

    private suspend fun syncFromGitHubPages(pianoStudi: String?, currentYear: Int?): Boolean {
        val courseCode = indirizzoCode(pianoStudi)
        if (courseCode == null) {
            Log.e(TAG, "Could not determine course code from: $pianoStudi")
            return false
        }
        
        // Default to year 1 if null, just like a safe fallback, though usually should be passed
        val year = currentYear ?: 1 
        val semester = currentSemester()
        
        // Construct URL: https://SAzzinelli.github.io/LABA_Orari/orari/{CORSO}/{ANNO}/{SEMESTRE}sem.json
        val urlString = "https://SAzzinelli.github.io/LABA_Orari/orari/$courseCode/$year/${semester}sem.json"
        
        Log.d(TAG, "Attempting GitHub sync: $urlString")
        
        return try {
            val response = fetchUrl(urlString)
            val events = parseLessons(response)
            if (events.isNotEmpty()) {
                _events.value = events
                cacheFile.writeText(response)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "GitHub sync failed: ${e.message}")
            false
        }
    }

    private suspend fun syncFromNpoint(): Boolean {
        return try {
            Log.d(TAG, "Fetching lessons from npoint...")
            val response = fetchUrl(NPOINT_URL)
            val events = parseLessons(response)
            _events.value = events
            cacheFile.writeText(response)
            Log.d(TAG, "Synced ${events.size} events from npoint")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing lessons (npoint)", e)
            loadCacheIfAvailable()
            false
        }
    }

    private fun fetchUrl(urlString: String): String {
        val url = java.net.URL(urlString)
        val connection = url.openConnection() as java.net.HttpURLConnection
        connection.connectTimeout = 10000 // 10s timeout
        connection.readTimeout = 10000
        connection.connect()
        
        if (connection.responseCode !in 200..299) {
            throw java.io.IOException("HTTP ${connection.responseCode}")
        }
        
        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    // Exact iOS Logic for Semester
    private fun currentSemester(): Int {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1 // 1-12
        // S1: Oct(10), Nov(11), Dec(12), Jan(1), Feb(2)
        // S2: Mar(3) ... Sep(9)
        return if (month >= 10 || month <= 2) 1 else 2
    }

    // Exact iOS Logic for Course Mapping
    private fun indirizzoCode(piano: String?): String? {
        if (piano.isNullOrEmpty()) return null
        val ps = piano.lowercase()
        return when {
            ps.contains("interior") -> "INT"
            ps.contains("cinema") || ps.contains("audiovisiv") -> "CINEMA"
            ps.contains("graphic") || ps.contains("multimedia") || ps.contains("grafica") -> "GD"
            ps.contains("fotografia") || ps.contains("photo") -> "FOTO"
            ps.contains("fashion") -> "FD"
            ps.contains("pittura") || ps.contains("painting") -> "PIT"
            ps.contains("regia") || ps.contains("videomaking") || ps.contains("regia e video") -> "REGIA"
            ps.contains("design") -> "DES" // After Interior check
            else -> null
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
                val firstOidCorsi: String? = obj.optJSONArray("oidCorsi")?.let {
                    if (it.length() > 0) it.getString(0) else null
                }
                val oidCorsoValue: String? = obj.optString("oidCorso", null)
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
                    note = if (obj.has("note") && !obj.isNull("note")) obj.getString("note") else null,
                    gruppo = if (obj.has("gruppo") && !obj.isNull("gruppo")) obj.getString("gruppo") else null
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

