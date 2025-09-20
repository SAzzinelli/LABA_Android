package com.laba.firenze.data.api

import com.laba.firenze.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogosUniAPIClient @Inject constructor(
    private val apiService: LogosUniApiService
) {
    
    // MARK: - Authentication
    // Il login è ora gestito da AuthApi nel SessionRepository
    
    // MARK: - Student Data
    
    suspend fun getStudentProfile(token: String): StudentPayload? {
        return withContext(Dispatchers.IO) {
            try {
                println("🔐 LogosUniAPIClient: Getting student profile with token: ${token.take(20)}...")
                val response = apiService.getStudentProfile("Bearer $token")
                println("🔐 LogosUniAPIClient: Student profile response code: ${response.code()}")
                println("🔐 LogosUniAPIClient: Student profile response body: ${response.body()}")
                println("🔐 LogosUniAPIClient: Student profile response error: ${response.errorBody()?.string()}")
                
                if (response.isSuccessful) {
                    val payload = response.body()?.payload
                    println("🔐 LogosUniAPIClient: Student payload: $payload")
                    payload
                } else {
                    println("🔐 LogosUniAPIClient: Student profile request failed")
                    null
                }
            } catch (e: Exception) {
                println("🔐 LogosUniAPIClient: Student profile exception: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }
    
    suspend fun getEnrollments(token: String): List<Enrollment> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getEnrollments("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.payload?.enrollments ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    // MARK: - Exams
    
    suspend fun getExams(token: String): List<Esame> {
        return withContext(Dispatchers.IO) {
            try {
                println("🔐 LogosUniAPIClient: Getting exams with token: ${token.take(20)}...")
                println("🔐 LogosUniAPIClient: Making API call to logosuni.servicesv2/api/Enrollments")
                
                val response = apiService.getExams("Bearer $token")
                println("🔐 LogosUniAPIClient: Exams response code: ${response.code()}")
                println("🔐 LogosUniAPIClient: Exams response headers: ${response.headers()}")
                
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    println("🔐 LogosUniAPIClient: Exams response body success: ${responseBody?.success}")
                    println("🔐 LogosUniAPIClient: Exams response body payload: ${responseBody?.payload}")
                    println("🔐 LogosUniAPIClient: Exams response body payload exams: ${responseBody?.payload?.exams}")
                    println("🔐 LogosUniAPIClient: Exams response body payload exams count: ${responseBody?.payload?.exams?.size}")
                    println("🔐 LogosUniAPIClient: Exams response body payload situazioneEsami: ${responseBody?.payload?.situazioneEsami}")
                    println("🔐 LogosUniAPIClient: Exams response body payload situazioneEsami count: ${responseBody?.payload?.situazioneEsami?.size}")
                    
                                // Prova prima situazioneEsami (la vera struttura), poi fallback a exams
                                val examList = responseBody?.payload?.situazioneEsami ?: responseBody?.payload?.exams
                                val exams = examList?.map { examPayload ->
                                    Esame(
                                        oid = examPayload.oidCorso, // Map from oidCorso
                                        corso = examPayload.corso,
                                        docente = examPayload.docente,
                                        cfa = examPayload.cfa,
                                        anno = examPayload.anno,
                                        voto = examPayload.voto,
                                        data = examPayload.sostenutoIl, // Map from sostenutoIl
                                        tipo = null, // Not present in API response
                                        stato = examPayload.esitoRichiesta, // Map from esitoRichiesta
                                        propedeutico = examPayload.propedeutico,
                                        richiedibile = examPayload.richiedibile == "Y" // Convert string to boolean
                                    )
                                } ?: emptyList()
                    println("🔐 LogosUniAPIClient: Mapped ${exams.size} exams from API response")
                    exams
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("🔐 LogosUniAPIClient: Exams request failed with code ${response.code()}")
                    println("🔐 LogosUniAPIClient: Error body: $errorBody")
                    emptyList()
                }
            } catch (e: Exception) {
                println("🔐 LogosUniAPIClient: Exams exception: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
    }
    
    // MARK: - Seminars
    
    suspend fun getSeminars(token: String): List<Seminario> {
        return withContext(Dispatchers.IO) {
            try {
                println("🔐 LogosUniAPIClient: Getting seminars with token: ${token.take(20)}...")
                val response = apiService.getSeminars("Bearer $token")
                println("🔐 LogosUniAPIClient: Seminars response code: ${response.code()}")
                println("🔐 LogosUniAPIClient: Seminars response body: ${response.body()}")
                
                if (response.isSuccessful) {
                    val seminars = response.body()?.payload?.map { seminarPayload ->
                        Seminario(
                            oid = seminarPayload.seminarioOid,
                            titolo = seminarPayload.descrizione,
                            docente = null, // Not provided in API response
                            dataInizio = null, // Not provided in API response
                            dataFine = null, // Not provided in API response
                            aula = null, // Not provided in API response
                            prenotabile = seminarPayload.richiedibile == "Y",
                            descrizioneEstesa = seminarPayload.descrizioneEstesa,
                            esito = seminarPayload.esitoRichiesta,
                            gruppiStudenti = emptyList() // Not provided in API response
                        )
                    } ?: emptyList()
                    println("🔐 LogosUniAPIClient: Mapped ${seminars.size} seminars from API response")
                    seminars
                } else {
                    println("🔐 LogosUniAPIClient: Seminars request failed")
                    emptyList()
                }
            } catch (e: Exception) {
                println("🔐 LogosUniAPIClient: Seminars exception: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
    }
    
    // MARK: - Notifications
    
    suspend fun getNotifications(token: String): List<NotificationPayload> {
        return withContext(Dispatchers.IO) {
            try {
                println("🔐 LogosUniAPIClient: Getting notifications with token: ${token.take(20)}...")
                val response = apiService.getNotifications("Bearer $token", emptyMap())
                println("🔐 LogosUniAPIClient: Notifications response code: ${response.code()}")
                println("🔐 LogosUniAPIClient: Notifications response body: ${response.body()}")
                
                if (response.isSuccessful) {
                    val notificationPayloads = decodeNotifications(response.body())
                    println("🔐 LogosUniAPIClient: Decoded ${notificationPayloads?.size ?: 0} notifications")
                    notificationPayloads ?: emptyList()
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("🔐 LogosUniAPIClient: Notifications request failed with code ${response.code()}")
                    println("🔐 LogosUniAPIClient: Error body: $errorBody")
                    emptyList()
                }
            } catch (e: Exception) {
                println("🔐 LogosUniAPIClient: Notifications exception: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
    }
    
    suspend fun markNotificationAsRead(token: String, notificationId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.markNotificationAsRead(
                    "Bearer $token",
                    mapOf("notificationId" to notificationId)
                )
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }
    
    suspend fun markAllNotificationsAsRead(token: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.markAllNotificationsAsRead("Bearer $token")
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }
    
    // MARK: - Documents (identico a iOS endpoints)
    
    suspend fun getDocuments(token: String): List<LogosDoc> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDocuments("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.payload?.documents ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    suspend fun getDocumentById(token: String, allegatoOid: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDocumentById("Bearer $token", allegatoOid)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    // MARK: - Thesis (identico a iOS endpoints)
    
    suspend fun getThesisInfo(token: String): List<ThesisItem> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getThesisInfo("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.payload ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    // MARK: - Helper Methods
    
    private fun decodeNotifications(response: NotificationsResponse?): List<NotificationPayload>? {
        if (response == null) return null
        
        // Identico alla logica iOS decodeNotifications
        return when {
            response.success && response.payload != null -> response.payload.notifications
            response.payload != null -> response.payload.notifications
            else -> null
        }
    }
}
