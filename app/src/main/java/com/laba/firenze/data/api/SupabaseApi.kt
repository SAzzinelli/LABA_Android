package com.laba.firenze.data.api

import com.laba.firenze.domain.model.AchievementIdResponse
import com.laba.firenze.domain.model.SupabaseAchievement
import com.laba.firenze.domain.model.SupabaseUserStats
import retrofit2.Response
import retrofit2.http.*

interface SupabaseApi {

    @GET("rest/v1/user_achievements")
    suspend fun getUserAchievements(
        @Query("user_email") userEmail: String,
        @Query("select") select: String = "achievement_id",
        @Header("Authorization") authorization: String,
        @Header("apikey") apiKey: String
    ): Response<List<AchievementIdResponse>>

    @POST("rest/v1/user_achievements")
    suspend fun saveAchievement(
        @Body achievement: SupabaseAchievement,
        @Header("Authorization") authorization: String,
        @Header("apikey") apiKey: String,
        @Header("Prefer") prefer: String = "return=representation,resolution=merge-duplicates"
    ): Response<Void>

    @GET("rest/v1/user_stats")
    suspend fun getUserStats(
        @Query("user_email") userEmail: String,
        @Query("select") select: String = "*",
        @Header("Authorization") authorization: String,
        @Header("apikey") apiKey: String
    ): Response<List<SupabaseUserStats>>

    @POST("rest/v1/user_stats")
    suspend fun saveUserStats(
        @Body stats: SupabaseUserStats,
        @Header("Authorization") authorization: String,
        @Header("apikey") apiKey: String,
        @Header("Prefer") prefer: String = "return=representation,resolution=merge-duplicates"
    ): Response<Void>
}
