package com.laba.firenze.data.repository

import com.laba.firenze.data.api.ProfilePhotoRow
import com.laba.firenze.data.api.ProfilePhotoSaveBody
import com.laba.firenze.data.api.SupabaseApi
import com.laba.firenze.domain.model.SupabaseAchievement
import com.laba.firenze.domain.model.SupabaseUserStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID

@Singleton
class SupabaseRepository @Inject constructor(
    private val api: SupabaseApi
) {
    // Hardcoded credentials for now, as they are public/anon keys in the client
    // In a real production app these should be in BuildConfig or secured better, 
    // but here we follow the iOS pattern of using the Anon key.
    private val AUTH_HEADER = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlrYmJtZ29id2tvYmNzbXZ3YXRxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjczNzUwMzcsImV4cCI6MjA4Mjk1MTAzN30.PgqOM2tZP0enOOg0b0DzPTBPsGLYWQEWcogUP2ye5Pg"
    private val API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlrYmJtZ29id2tvYmNzbXZ3YXRxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjczNzUwMzcsImV4cCI6MjA4Mjk1MTAzN30.PgqOM2tZP0enOOg0b0DzPTBPsGLYWQEWcogUP2ye5Pg"

    suspend fun fetchUserAchievements(email: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.getUserAchievements(
                userEmail = "eq.$email",
                authorization = AUTH_HEADER,
                apiKey = API_KEY
            )
            
            if (response.isSuccessful) {
                return@withContext response.body()?.map { it.achievementId } ?: emptyList()
            } else {
                // Log error
                return@withContext emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }

    suspend fun saveAchievement(achievementId: String, email: String) = withContext(Dispatchers.IO) {
        try {
            val now = java.util.Date()
            val achievement = SupabaseAchievement(
                id = UUID.randomUUID().toString(),
                userEmail = email,
                achievementId = achievementId,
                unlockedAt = now,
                eventDate = null,
                createdAt = now,
                updatedAt = now
            )
            
            api.saveAchievement(
                achievement = achievement,
                authorization = AUTH_HEADER,
                apiKey = API_KEY
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun fetchUserStats(email: String): SupabaseUserStats? = withContext(Dispatchers.IO) {
        try {
            val response = api.getUserStats(
                userEmail = "eq.$email",
                authorization = AUTH_HEADER,
                apiKey = API_KEY
            )
            
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                return@withContext response.body()!!.first()
            } else {
                return@withContext null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    suspend fun saveUserStats(stats: SupabaseUserStats) = withContext(Dispatchers.IO) {
        try {
            api.saveUserStats(
                stats = stats,
                authorization = AUTH_HEADER,
                apiKey = API_KEY
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Foto profilo da Supabase (allineato a iOS) - restituisce Pair(imgbbUrl, deleteUrl?) */
    suspend fun fetchProfilePhoto(email: String): Pair<String, String?>? = withContext(Dispatchers.IO) {
        try {
            val norm = email.trim().lowercase()
            if (norm.isEmpty()) return@withContext null
            val response = api.getProfilePhoto(
                userEmail = "eq.$norm",
                authorization = AUTH_HEADER,
                apiKey = API_KEY
            )
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val row = response.body()!!.first()
                return@withContext Pair(row.imgbb_url, row.delete_url)
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveProfilePhoto(email: String, imgbbUrl: String, deleteUrl: String?) = withContext(Dispatchers.IO) {
        try {
            val norm = email.trim().lowercase()
            if (norm.isEmpty() || imgbbUrl.isEmpty()) return@withContext
            val body = ProfilePhotoSaveBody(
                user_email = norm,
                imgbb_url = imgbbUrl,
                delete_url = deleteUrl,
                updated_at = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.US).format(java.util.Date())
            )
            api.saveProfilePhoto(body, authorization = AUTH_HEADER, apiKey = API_KEY)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Upsert FCM token su user_fcm_tokens (portale notifiche dropdown) - allineato a iOS NotificheTokenService */
    suspend fun upsertFcmToken(userEmail: String, fcmToken: String, displayName: String?) = withContext(Dispatchers.IO) {
        try {
            val email = userEmail.trim().lowercase()
            if (email.isEmpty() || fcmToken.isEmpty()) return@withContext
            val body = com.laba.firenze.data.api.UserFcmTokenBody(
                user_email = email,
                fcm_token = fcmToken,
                updated_at = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.US).format(java.util.Date()),
                display_name = displayName?.trim()?.takeIf { it.isNotEmpty() }
            )
            val response = api.upsertFcmToken(body, authorization = AUTH_HEADER, apiKey = API_KEY)
            if (!response.isSuccessful) {
                android.util.Log.w("SupabaseRepository", "upsertFcmToken failed: ${response.code()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
