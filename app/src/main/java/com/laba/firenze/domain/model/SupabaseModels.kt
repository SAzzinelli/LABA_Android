package com.laba.firenze.domain.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class SupabaseAchievement(
    @SerializedName("id") val id: String,
    @SerializedName("user_email") val userEmail: String,
    @SerializedName("achievement_id") val achievementId: String,
    @SerializedName("unlocked_at") val unlockedAt: Date,
    @SerializedName("event_date") val eventDate: Date?,
    @SerializedName("created_at") val createdAt: Date,
    @SerializedName("updated_at") val updatedAt: Date
)

data class SupabaseUserStats(
    @SerializedName("user_email") val userEmail: String,
    @SerializedName("total_points") val totalPoints: Int,
    @SerializedName("unlocked_achievements") val unlockedAchievements: List<String>,
    @SerializedName("stats_data") val statsData: String?, // JSON string encoded UserStats
    @SerializedName("updated_at") val updatedAt: Date
)

// Helper class for decoding just the ID from Supabase response
data class AchievementIdResponse(
    @SerializedName("achievement_id") val achievementId: String
)
