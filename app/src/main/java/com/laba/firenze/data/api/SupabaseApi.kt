package com.laba.firenze.data.api

import com.laba.firenze.data.api.ProfilePhotoRow
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

    /** Foto profilo (ImgBB URL) - allineato a iOS ProfilePhotoSupabaseService */
    @GET("rest/v1/user_profile_photos")
    suspend fun getProfilePhoto(
        @Query("user_email") userEmail: String,
        @Query("select") select: String = "imgbb_url,delete_url",
        @Header("Authorization") authorization: String,
        @Header("apikey") apiKey: String
    ): Response<List<ProfilePhotoRow>>

    @POST("rest/v1/user_profile_photos")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun saveProfilePhoto(
        @Body body: ProfilePhotoSaveBody,
        @Header("Authorization") authorization: String,
        @Header("apikey") apiKey: String
    ): Response<Void>

    /** Upsert FCM token per portale notifiche (dropdown destinatari) - allineato a iOS NotificheTokenService */
    @POST("rest/v1/user_fcm_tokens")
    @Headers("Prefer: return=minimal, resolution=merge-duplicates")
    suspend fun upsertFcmToken(
        @Body body: UserFcmTokenBody,
        @Header("Authorization") authorization: String,
        @Header("apikey") apiKey: String
    ): Response<Void>
}

data class UserFcmTokenBody(
    val user_email: String,
    val fcm_token: String,
    val updated_at: String,
    val display_name: String? = null
)

data class ProfilePhotoSaveBody(
    val user_email: String,
    val imgbb_url: String,
    val delete_url: String?,
    val updated_at: String
)
