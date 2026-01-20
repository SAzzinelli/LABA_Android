package com.laba.firenze.data.repository

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
}
