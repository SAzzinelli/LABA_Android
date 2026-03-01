package com.laba.firenze.data.api

import com.laba.firenze.domain.model.CreateSuperSaasAppointment
import com.laba.firenze.domain.model.SuperSaasAppointment
import com.laba.firenze.domain.model.SuperSaasAvailabilityAPIResponse
import com.laba.firenze.domain.model.SuperSaasBookingItem
import com.laba.firenze.domain.model.SuperSaasUserResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * SuperSaas API - Retrofit interface.
 * Base URL: https://www.supersaas.it
 * Auth: Basic (account:api_key)
 * MD5 checksum for login: MD5(account + api_key + user_email)
 */
interface SuperSaasApi {

    @GET("api/users.json")
    suspend fun getUsers(
        @Query("limit") limit: Int = 0
    ): Response<List<SuperSaasUserResponse>>

    @GET("api/free/{scheduleId}.json")
    suspend fun getFreeSlots(
        @retrofit2.http.Path("scheduleId") scheduleId: Int,
        @Query("from") from: String,
        @Query("maxresults") maxResults: Int = 50
    ): Response<SuperSaasAvailabilityAPIResponse>

    @GET("api/bookings.json")
    suspend fun getBookings(
        @Query("schedule_id") scheduleId: Int
    ): Response<List<SuperSaasBookingItem>>

    @POST("api/bookings.json")
    suspend fun createBooking(
        @Query("schedule_id") scheduleId: Int,
        @Query("api_key") apiKey: String,
        @Query("booking[full_name]") fullName: String,
        @Query("booking[email]") email: String,
        @Query("booking[phone]") phone: String,
        @Query("booking[start]") start: String,
        @Query("booking[finish]") finish: String
    ): Response<SuperSaasAppointment>

    @DELETE("api/bookings/{bookingId}.json")
    suspend fun deleteBooking(
        @retrofit2.http.Path("bookingId") bookingId: Int,
        @Query("schedule_id") scheduleId: Int
    ): Response<Unit>
}
