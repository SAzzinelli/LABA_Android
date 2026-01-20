package com.laba.firenze.data.gamification

import android.util.Log
import com.laba.firenze.data.repository.SupabaseRepository
import com.laba.firenze.domain.model.*
import com.laba.firenze.domain.model.Achievement
import com.laba.firenze.domain.model.AchievementID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*

object AchievementSyncService {
    private val json = Json { ignoreUnknownKeys = true }

    
    // Achievement locali (che si sbloccano in locale senza dipendere da voti/esami/CFA)
    private val localAchievementIDs = setOf(
        AchievementID.FIRST_LOGIN.rawValue,
        AchievementID.FIRST_DATA.rawValue,
        AchievementID.ESPLORATORE.rawValue,
        AchievementID.PUNTUALE.rawValue,
        AchievementID.GUFO_NOTTURNO.rawValue,
        AchievementID.MATTINIERO.rawValue,
        AchievementID.DIPENDENTE.rawValue,
        AchievementID.REFRESH_MANIAC.rawValue,
        AchievementID.STUDIOSO.rawValue,
        AchievementID.INFORMATO.rawValue,
        AchievementID.CURIOSO.rawValue,
        AchievementID.UNICORNO.rawValue,
        AchievementID.HALLOWEEN.rawValue,
        AchievementID.NATALE.rawValue,
        AchievementID.ESTATE_AGOSTO.rawValue,
        AchievementID.COMPLEANNO.rawValue,
        AchievementID.COLLEZIONISTA.rawValue,
        AchievementID.MAESTRO.rawValue,
        AchievementID.LEGGENDA.rawValue,
        AchievementID.CACCIATORE.rawValue,
        AchievementID.MILIONARIO.rawValue
    )
    
    fun isLocalAchievement(achievementID: String): Boolean {
        return localAchievementIDs.contains(achievementID)
    }
    
    /**
     * Salva achievement sbloccato su Supabase
     */
    suspend fun saveUnlockedAchievement(
        achievementID: String,
        email: String,
        eventDate: Date? = null,
        supabaseRepository: SupabaseRepository
    ) {
        if (email.isEmpty() || achievementID.isEmpty()) return
        
        Log.d("AchievementSyncService", "💾 Saving achievement '$achievementID' for email: $email")
        withContext(Dispatchers.IO) {
            try {
                supabaseRepository.saveAchievement(achievementID, email)
                Log.d("AchievementSyncService", "✅ Successfully saved achievement '$achievementID' to Supabase for $email")
            } catch (e: Exception) {
                Log.e("AchievementSyncService", "❌ Failed to save achievement '$achievementID': ${e.message}", e)
            }
        }
    }
    
    /**
     * Sync completo (fetch + restore)
     * Conforme a iOS syncAchievementsFromSupabase
     * Nota: AchievementManager deve esporre metodi per aggiornare achievements e stats
     */
    suspend fun syncAchievementsFromSupabase(
        email: String,
        manager: AchievementManager,
        supabaseRepository: SupabaseRepository
    ) = withContext(Dispatchers.IO) {
        if (email.isEmpty()) {
            Log.d("AchievementSyncService", "⏭️ Sync skipped: email is empty")
            return@withContext
        }
        
        Log.d("AchievementSyncService", "🔄 Starting sync for email: $email")
        
        try {
            // 1. Fetch achievement sbloccati
                Log.d("AchievementSyncService", "📥 Fetching achievements from Supabase...")
                val unlockedIDs = supabaseRepository.fetchUserAchievements(email)
                Log.d("AchievementSyncService", "✅ Fetched ${unlockedIDs.size} achievement IDs from Supabase: $unlockedIDs")
                
                // 2. Restore achievement locali (solo quelli che non dipendono da dati)
                var restoredCount = 0
                var skippedCount = 0
                var notLocalCount = 0
                
            unlockedIDs.forEach { achievementID ->
                val isLocal = isLocalAchievement(achievementID)
                Log.d("AchievementSyncService", "🔍 Processing achievement '$achievementID': isLocal=$isLocal")
                
                if (isLocal) {
                    // Sblocca solo se è locale e non già sbloccato
                    val achievements = manager.achievements.value
                    val existing = achievements.firstOrNull { it.id == achievementID }
                    if (existing != null && !existing.isUnlocked) {
                        manager.restoreAchievement(achievementID, System.currentTimeMillis())
                        Log.d("AchievementSyncService", "✅ Restored local achievement: '$achievementID'")
                        restoredCount++
                    } else if (existing != null && existing.isUnlocked) {
                        skippedCount++
                        Log.d("AchievementSyncService", "⏭️ Skipped '$achievementID': already unlocked locally")
                    } else {
                        Log.d("AchievementSyncService", "⚠️ Achievement '$achievementID' not found in local list")
                    }
                } else {
                    notLocalCount++
                    Log.d("AchievementSyncService", "ℹ️ Skipped '$achievementID': not a local achievement (will be recalculated from data)")
                }
            }
            
            Log.d("AchievementSyncService", "📊 Restore summary: $restoredCount restored, $skippedCount already unlocked, $notLocalCount not local")
            
            // 3. Fetch e restore stats se disponibili
            Log.d("AchievementSyncService", "📥 Fetching user stats from Supabase...")
            val supabaseStats = supabaseRepository.fetchUserStats(email)
            if (supabaseStats != null) {
                Log.d("AchievementSyncService", "✅ Fetched stats: total_points=${supabaseStats.totalPoints}, unlocked_count=${supabaseStats.unlockedAchievements.size}")
                
                // Restore stats complesse se disponibili
                if (supabaseStats.statsData != null) {
                    try {
                        val restoredStats = json.decodeFromString<com.laba.firenze.domain.model.UserStats>(supabaseStats.statsData)
                        Log.d("AchievementSyncService", "📊 Restoring detailed stats...")
                        manager.updateStatsFromCloud(restoredStats)
                        Log.d("AchievementSyncService", "💰 Updated stats from cloud")
                    } catch (e: Exception) {
                        Log.e("AchievementSyncService", "Failed to decode stats_data", e)
                        // Fallback: just update points
                        val currentStats = manager.stats.value
                        if (supabaseStats.totalPoints > currentStats.totalPoints) {
                            manager.updateStatsFromCloud(com.laba.firenze.domain.model.UserStats(totalPoints = supabaseStats.totalPoints))
                        }
                    }
                } else {
                    // Fallback: just update points
                    val currentStats = manager.stats.value
                    if (supabaseStats.totalPoints > currentStats.totalPoints) {
                        manager.updateStatsFromCloud(com.laba.firenze.domain.model.UserStats(totalPoints = supabaseStats.totalPoints))
                    }
                }
            } else {
                Log.d("AchievementSyncService", "ℹ️ No user stats found in Supabase (first time user or no stats saved yet)")
            }
            
            Log.d("AchievementSyncService", "✅ Sync completed successfully! Restored $restoredCount local achievements for $email")
                
        } catch (e: Exception) {
            Log.e("AchievementSyncService", "❌ Sync error: ${e.message}", e)
        }
    }
    
    /**
     * Sync stats periodico
     */
    suspend fun syncStatsToSupabase(
        email: String,
        stats: UserStats,
        achievements: List<Achievement>,
        supabaseRepository: SupabaseRepository
    ) {
        if (email.isEmpty()) return
        
        withContext(Dispatchers.IO) {
            try {
                val unlockedIDs = achievements.filter { it.isUnlocked }.map { it.id }
                
                val statsJSONString = try {
                    json.encodeToString(stats)
                } catch (e: Exception) {
                    Log.e("AchievementSyncService", "Failed to encode stats", e)
                    null
                }
                
                val userStats = SupabaseUserStats(
                    userEmail = email,
                    totalPoints = stats.totalPoints,
                    unlockedAchievements = unlockedIDs,
                    statsData = statsJSONString,
                    updatedAt = Date()
                )
                
                supabaseRepository.saveUserStats(userStats)
                Log.d("AchievementSyncService", "Synced stats to Supabase for $email")
            } catch (e: Exception) {
                Log.e("AchievementSyncService", "Failed to sync stats: ${e.message}", e)
            }
        }
    }
}
