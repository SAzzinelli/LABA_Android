package com.laba.firenze.data.api

import android.content.Context
import com.google.gson.Gson
import com.laba.firenze.domain.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogosUniAPIClient @Inject constructor(
    private val apiService: LogosUniApiService,
    private val gson: Gson,
    @ApplicationContext private val context: Context
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
    
    /**
     * Ottiene il payload completo di Enrollments con stato, annoAttuale, pianoStudi e situazioneEsami
     * Come iOS che chiama enrollments per ottenere questi dati
     */
    suspend fun getEnrollmentsPayload(token: String): EnrollmentsPayload? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getEnrollments("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.payload
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    // MARK: - Exams
    
    suspend fun getExams(token: String): List<Esame> {
        return withContext(Dispatchers.IO) {
            try {
                println("🔐 LogosUniAPIClient: Getting exams with token: ${token.take(20)}...")
                println("🔐 LogosUniAPIClient: Making API call to Enrollments (base da laba.apiVersion)")
                
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
                    val payloadList = response.body()?.payloadAsList(gson) ?: emptyList()
                    val seminars = payloadList.map { seminarPayload ->
                        val esito = seminarPayload.esitoRichiesta
                        val part = seminarPayload.partecipato?.uppercase() == "Y" ||
                            esito?.lowercase()?.let { e -> e.contains("complet") || e.contains("approv") || e.contains("valid") } == true
                        Seminario(
                            oid = seminarPayload.seminarioOid,
                            titolo = seminarPayload.descrizione,
                            docente = null, // Estratto da descrizioneEstesa
                            dataInizio = null,
                            dataFine = null,
                            aula = null,
                            prenotabile = seminarPayload.richiedibile == "Y",
                            richiedibile = seminarPayload.richiedibile == "Y",
                            dataRichiesta = seminarPayload.dataRichiesta,
                            descrizioneEstesa = seminarPayload.descrizioneEstesa,
                            documentOid = seminarPayload.documentOid,
                            esito = esito,
                            gruppiStudenti = emptyList(),
                            partecipato = part
                        )
                    }
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
    
    // MARK: - API v3 Notifications (Firebase)
    
    suspend fun getNotificationsV3(
        token: String,
        start: Int = 0,
        count: Int = 100,
        orderBy: String = "CreatedDate",
        descending: Boolean = true
    ): List<NotificationPayloadV3> {
        return withContext(Dispatchers.IO) {
            try {
                val request = NotificationsRequestV3(
                    start = start,
                    count = count,
                    orderBy = orderBy,
                    descending = descending
                )
                val response = apiService.getNotificationsV3("Bearer $token", request)
                
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
    
    suspend fun markAllNotificationsAsReadV3(token: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.markAllNotificationsAsReadV3("Bearer $token")
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }
    
    suspend fun setFcmToken(token: String, fcmToken: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = FcmTokenRequest(fcmToken = fcmToken)
                val response = apiService.setFcmToken("Bearer $token", request)
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
                println("🔐 LogosUniAPIClient: Getting documents with token: ${token.take(20)}...")
                val response = apiService.getDocuments("Bearer $token")
                println("🔐 LogosUniAPIClient: Documents response code: ${response.code()}")
                println("🔐 LogosUniAPIClient: Documents response body: ${response.body()}")
                println("🔐 LogosUniAPIClient: Documents response error: ${response.errorBody()?.string()}")
                
                if (response.isSuccessful) {
                    val documents = response.body()?.payload?.documents ?: emptyList()
                    println("🔐 LogosUniAPIClient: Successfully loaded ${documents.size} documents")
                    documents
                } else {
                    println("🔐 LogosUniAPIClient: Documents request failed")
                    emptyList()
                }
            } catch (e: Exception) {
                println("🔐 LogosUniAPIClient: Documents exception: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
    }
    
    suspend fun getDocumentById(token: String, allegatoOid: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDocumentById("Bearer $token", allegatoOid)
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val bytes = responseBody.bytes()
                        responseBody.close()
                        return@withContext bytes
                    }
                }
                // Se v3 fallisce, prova v2 Documents (come iOS) - URL allineato a APIConfig
                val version = context.getSharedPreferences("laba_preferences", Context.MODE_PRIVATE)
                    .getString("laba.apiVersion", "v2") ?: "v2"
                if (version == "v3") {
                    try {
                        val v2Url = "https://logosuni.laba.biz/api/api/Documents/GetDocument?id=$allegatoOid"
                        val conn = URL(v2Url).openConnection() as java.net.HttpURLConnection
                        conn.requestMethod = "GET"
                        conn.setRequestProperty("Authorization", "Bearer $token")
                        conn.setRequestProperty("Accept", "application/pdf, application/octet-stream, */*")
                        conn.connectTimeout = 15_000
                        conn.readTimeout = 30_000
                        if (conn.responseCode in 200..299) {
                            conn.inputStream.use { it.readBytes() }
                        } else null
                    } catch (_: Exception) {
                        null
                    }
                } else null
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
    
    // MARK: - Thesis Documents
    
    suspend fun getThesisDocuments(token: String): List<ThesisDocumentItem> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getThesisDocuments("Bearer $token")
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
    
    /** Cambio password (identico a iOS APIClient.changePassword). */
    suspend fun changePassword(token: String, oldPassword: String, newPassword: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.changePassword(
                    "Bearer $token",
                    oldPassword = oldPassword,
                    newPassword = newPassword
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception(body?.errorSummary ?: "Errore sconosciuto"))
                    }
                } else {
                    val msg = response.errorBody()?.string() ?: "Errore ${response.code()}"
                    Result.failure(Exception("Errore cambio password: $msg"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
