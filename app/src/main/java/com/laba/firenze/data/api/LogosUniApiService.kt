package com.laba.firenze.data.api

import com.laba.firenze.domain.model.*
import retrofit2.Response
import retrofit2.http.*

interface LogosUniApiService {

    // Il login è ora gestito da AuthApi

                @GET("logosuni.servicesv2/api/Students") // Corretto da Student a Students (plurale)
                suspend fun getStudentProfile(@Header("Authorization") token: String): Response<StudentResponse>

                @GET("logosuni.servicesv2/api/Enrollments")
                suspend fun getEnrollments(@Header("Authorization") token: String): Response<EnrollmentsResponse>

                @GET("logosuni.servicesv2/api/Enrollments") // Gli esami sono in Enrollments
                suspend fun getExams(@Header("Authorization") token: String): Response<ExamsResponse>

    @GET("logosuni.servicesv2/api/Seminars")
    suspend fun getSeminars(@Header("Authorization") token: String): Response<SeminariResponse>

    @POST("logosuni.servicesv2/api/Notification/GetNotifications") // Corretto: POST secondo documentazione API
    @Headers("Content-Type: application/json")
    suspend fun getNotifications(
        @Header("Authorization") token: String,
        @Body request: Map<String, Any> = emptyMap() // Empty request body for POST
    ): Response<NotificationsResponse>


    @POST("logosuni.servicesv2/api/Notifications/MarkAsRead")
    suspend fun markNotificationAsRead(
        @Header("Authorization") token: String,
        @Body notificationId: Map<String, Int>
    ): Response<Unit>

    @POST("logosuni.servicesv2/api/Notifications/MarkAllAsRead")
    suspend fun markAllNotificationsAsRead(@Header("Authorization") token: String): Response<Unit>

    // MARK: - Documents (identico a iOS endpoints)
    @GET("logosuni.servicesv2/api/Documents")
    suspend fun getDocuments(@Header("Authorization") token: String): Response<LogosDocumentsResponse>

    @GET("logosuni.servicesv2/api/Documents/GetDocument")
    suspend fun getDocumentById(
        @Header("Authorization") token: String,
        @Query("id") allegatoOid: String
    ): Response<ByteArray>
    
    // MARK: - Thesis (identico a iOS endpoints)
    @GET("logosuni.servicesv2/api/ThesisInfo")
    suspend fun getThesisInfo(@Header("Authorization") token: String): Response<ThesisInfoResponse>
    
    @GET("logosuni.servicesv2/api/ThesisInfo")
    suspend fun getThesisDocuments(@Header("Authorization") token: String): Response<ThesisDocumentsResponse>
}
