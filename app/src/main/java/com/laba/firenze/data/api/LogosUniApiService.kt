package com.laba.firenze.data.api

import com.laba.firenze.domain.model.*
import retrofit2.Response
import retrofit2.http.*

interface LogosUniApiService {

    // Il login è ora gestito da AuthApi

                @GET("Students") // Corretto da Student a Students (plurale)
                suspend fun getStudentProfile(@Header("Authorization") token: String): Response<StudentResponse>

                @GET("Enrollments")
                suspend fun getEnrollments(@Header("Authorization") token: String): Response<EnrollmentsResponse>

                @GET("Enrollments") // Gli esami sono in Enrollments
                suspend fun getExams(@Header("Authorization") token: String): Response<ExamsResponse>

    @GET("Seminars")
    suspend fun getSeminars(@Header("Authorization") token: String): Response<SeminariResponseV3>

    /** PUT /api/Seminars?seminarOid= — Register for seminar. Task 67: response may include warning when full. */
    @PUT("Seminars")
    suspend fun bookSeminar(
        @Header("Authorization") token: String,
        @Query("seminarOid") seminarOid: String
    ): Response<BookSeminarResponse>

    /** GET /api/Internships - solo API Test (v3) */
    @GET("Internships")
    suspend fun getInternships(@Header("Authorization") token: String): Response<InternshipsResponse>

    @POST("Notification/GetNotifications") // Corretto: POST secondo documentazione API
    @Headers("Content-Type: application/json")
    suspend fun getNotifications(
        @Header("Authorization") token: String,
        @Body request: Map<String, Any> = emptyMap() // Empty request body for POST
    ): Response<NotificationsResponse>


    @POST("Notifications/MarkAsRead")
    suspend fun markNotificationAsRead(
        @Header("Authorization") token: String,
        @Body notificationId: Map<String, Int>
    ): Response<Unit>

    @POST("Notifications/MarkAllAsRead")
    suspend fun markAllNotificationsAsRead(@Header("Authorization") token: String): Response<Unit>

    // MARK: - API v3 Notifications (Firebase) — baseURL già include /api, evita doppio api/api
    @POST("Notifications/GetNotifications")
    @Headers("Content-Type: application/json")
    suspend fun getNotificationsV3(
        @Header("Authorization") token: String,
        @Body request: NotificationsRequestV3
    ): Response<NotificationsResponseV3>
    
    @GET("Notifications/MarkNotificationsAsRead")
    suspend fun markAllNotificationsAsReadV3(@Header("Authorization") token: String): Response<Unit>
    
    @POST("setFcmToken")
    @Headers("Content-Type: application/json")
    suspend fun setFcmToken(
        @Header("Authorization") token: String,
        @Body request: FcmTokenRequest
    ): Response<Unit>

    // MARK: - Documents (identico a iOS endpoints)
    @GET("Documents")
    suspend fun getDocuments(@Header("Authorization") token: String): Response<LogosDocumentsResponse>

    @GET("Documents/GetDocument")
    @Headers("Accept: application/pdf, application/octet-stream, */*")
    @retrofit2.http.Streaming
    suspend fun getDocumentById(
        @Header("Authorization") token: String,
        @Query("id") allegatoOid: String
    ): Response<okhttp3.ResponseBody>
    
    // MARK: - Thesis (identico a iOS endpoints)
    @GET("ThesisInfo")
    suspend fun getThesisInfo(@Header("Authorization") token: String): Response<ThesisInfoResponse>
    
    @GET("ThesisInfo")
    suspend fun getThesisDocuments(@Header("Authorization") token: String): Response<ThesisDocumentsResponse>

    /** Cambio password (identico a iOS APIClient.changePassword): POST con query params. */
    @POST("User/ChangePassword")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Query("oldPassword") oldPassword: String,
        @Query("newPassword") newPassword: String
    ): Response<ChangePasswordResponse>
}

data class ChangePasswordResponse(
    val success: Boolean,
    val errorSummary: String? = null
)

/** Risposta PUT Seminars. Task 67: warning popolato quando success=true ma es. seminario pieno. */
data class BookSeminarResponse(
    val success: Boolean = false,
    val warning: String? = null,
    val payload: Boolean? = null,
    val errors: List<ApiError>? = null,
    val errorSummary: String? = null
)
