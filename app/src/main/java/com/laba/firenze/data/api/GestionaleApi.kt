package com.laba.firenze.data.api

import com.laba.firenze.domain.model.CreateEquipmentReport
import com.laba.firenze.domain.model.CreateEquipmentRequest
import com.laba.firenze.domain.model.EquipmentLoan
import com.laba.firenze.domain.model.EquipmentReport
import com.laba.firenze.domain.model.EquipmentRequest
import com.laba.firenze.domain.model.UserEquipment
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * API Service LABA (Gestionale attrezzatura) - identico a iOS LABAGestionaleNetworkManager.
 * Base URL: https://attrezzatura.laba.biz/api
 */
interface GestionaleApi {

    @POST("auth/login")
    suspend fun login(@Body request: GestionaleLoginRequest): Response<GestionaleLoginResponse>

    @GET("auth/me")
    suspend fun getMe(@Header("Authorization") authorization: String): Response<GestionaleUser>

    @GET("inventario/disponibili")
    suspend fun getAvailableEquipment(@Header("Authorization") authorization: String): Response<List<UserEquipment>>

    @GET("richieste/mie")
    suspend fun getUserRequests(@Header("Authorization") authorization: String): Response<List<EquipmentRequest>>

    @POST("richieste")
    suspend fun createRequest(
        @Header("Authorization") authorization: String,
        @Body request: CreateEquipmentRequest
    ): Response<EquipmentRequest>

    @GET("prestiti/mie")
    suspend fun getUserLoans(@Header("Authorization") authorization: String): Response<List<EquipmentLoan>>

    @GET("segnalazioni/mie")
    suspend fun getUserReports(@Header("Authorization") authorization: String): Response<List<EquipmentReport>>

    @POST("segnalazioni")
    suspend fun createReport(
        @Header("Authorization") authorization: String,
        @Body report: CreateEquipmentReport
    ): Response<EquipmentReport>
}

data class GestionaleLoginRequest(
    val email: String,
    val password: String
)

data class GestionaleLoginResponse(
    val token: String,
    val user: GestionaleUser
)

data class GestionaleUser(
    val id: Int,
    val email: String,
    val name: String,
    val surname: String,
    val ruolo: String,
    val corso_accademico: String? = null,
    val matricola: String? = null,
    val phone: String? = null,
    val penalty_strikes: Int? = null,
    val is_blocked: Boolean? = null,
    val blocked_reason: String? = null
) {
    val fullName: String get() = "$name $surname".trim()
}
