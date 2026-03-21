package com.laba.firenze.domain.model

import android.os.Parcelable
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.parcelize.Parcelize

// MARK: - Authentication Models

@Parcelize
data class LogosUniTokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val refresh_token: String? = null
) : Parcelable

@Parcelize
data class LogosUniLoginRequest(
    val username: String,
    val password: String,
    val grant_type: String = "password"
) : Parcelable

// MARK: - Student Profile Models

@Parcelize
data class StudentProfile(
    val displayName: String? = null,
    val status: String? = null,
    val currentYear: String? = null,
    val pianoStudi: String? = null,
    /** API Test (v3): CFA da LOGOS. Se presenti, usare somma come cfaEarned. */
    val cfaEsami: Int? = null,
    val cfaSeminari: Int? = null,
    val cfaTirocini: Int? = null,
    val nome: String? = null,
    val cognome: String? = null,
    val matricola: String? = null,
    val sesso: String? = null,
    val emailLABA: String? = null,
    val emailPersonale: String? = null,
    val telefono: String? = null,
    val cellulare: String? = null,
    val pagamenti: String? = null,
    val studentOid: String? = null
) : Parcelable

@Parcelize
data class StudentPayload(
    val oid: String,
    val nome: String? = null,
    val cognome: String? = null,
    val numMatricola: String? = null,
    val sesso: String? = null,
    val emailLABA: String? = null,
    val emailPersonale: String? = null,
    val telefono: String? = null,
    val cellulare: String? = null,
    val pagamenti: String? = null,
    val pianoStudi: String? = null,
    val stato: String? = null,
    val dataStato: String? = null,
    val annoAttuale: Int? = null,
    val classeLaurea: String? = null,
    val regolamentoAllegatoOid: String? = null
) : Parcelable

@Parcelize
data class StudentResponse(
    val success: Boolean,
    val payload: StudentPayload? = null,
    val errorSummary: String? = null,
    val errorMessage: String? = null
) : Parcelable

// MARK: - Enrollment Models

@Parcelize
data class EnrollmentsPayload(
    val enrollments: List<Enrollment>? = null,
    val stato: String? = null,         // Come iOS
    val annoAttuale: Int? = null,       // Come iOS
    val pianoStudi: String? = null,     // Come iOS
    val situazioneEsami: List<EsamePayload>? = null,  // Come iOS
    /** API Test (v3): CFA da LOGOS - esami, seminari, tirocini (solo numerici in test) */
    val cfaEsami: Int? = null,
    val cfaSeminari: Int? = null,
    val cfaTirocini: Int? = null
) : Parcelable

@Parcelize
data class Enrollment(
    val oid: String,
    val annoAccademico: String? = null,
    val corsoDiStudio: String? = null,
    val annoCorso: String? = null,
    val stato: String? = null
) : Parcelable

@Parcelize
data class EnrollmentsResponse(
    val success: Boolean,
    val payload: EnrollmentsPayload? = null,
    val errorSummary: String? = null,
    val errorMessage: String? = null
) : Parcelable

// MARK: - Exam Models

@Parcelize
data class Esame(
    val oid: String? = null, // Made nullable as it can be null in API response
    val corso: String,
    val docente: String? = null,
    val cfa: String? = null, // Changed from cfu to cfa to match API
    val anno: String? = null,
    val voto: String? = null,
    val data: String? = null, // sostenutoIl - data in cui è stato sostenuto
    val tipo: String? = null,
    val stato: String? = null,
    val propedeutico: String? = null,
    val richiedibile: Boolean = false,
    val dataRichiesta: String? = null, // Data in cui è stato prenotato
    val esitoRichiesta: String? = null // Esito della richiesta
) : Parcelable

@Parcelize
data class EsamePayload(
    val oidCorso: String? = null, // Actual API field name
    val ordine: Int? = null,
    val corso: String,
    val docente: String? = null,
    val anno: String? = null,
    val cfa: String? = null,
    val propedeutico: String? = null,
    val sostenutoIl: String? = null, // Actual API field name for exam date
    val voto: String? = null,
    val ssd: String? = null,
    val dataRichiesta: String? = null,
    val esitoRichiesta: String? = null,
    val richiedibile: String? = null // API returns "N" or "Y" as string
) : Parcelable

@Parcelize
data class ExamsPayload(
    val exams: List<EsamePayload>? = null,
    val situazioneEsami: List<EsamePayload>? = null // La vera struttura dell'API
) : Parcelable

@Parcelize
data class ExamsResponse(
    val success: Boolean,
    val payload: ExamsPayload? = null,
    val errorSummary: String? = null,
    val errorMessage: String? = null
) : Parcelable

// MARK: - Seminar Models

@Parcelize
data class Seminario(
    val oid: String,
    val titolo: String,
    val docente: String? = null,
    val dataInizio: String? = null,
    val dataFine: String? = null,
    val aula: String? = null,
    val prenotabile: Boolean = false,
    val richiedibile: Boolean = false,
    val dataRichiesta: String? = null,
    val descrizioneEstesa: String? = null,
    val documentOid: String? = null,
    val esito: String? = null,
    val gruppiStudenti: List<String> = emptyList(),
    /** true quando partecipato/convalidato dalla segreteria */
    val partecipato: Boolean = false,
    /** Task 72: cfa numerico da GET Seminars (v3) */
    val cfa: Int? = null
) : Parcelable

@Parcelize
data class SeminarioPayload(
    val seminarioOid: String,
    val descrizione: String,
    val descrizioneEstesa: String? = null,
    val documentOid: String? = null,
    val dataRichiesta: String? = null,
    val esitoRichiesta: String? = null,
    val richiedibile: String? = null,
    /** v3: Y quando la segreteria spunta "partecipato" in LOGOS.UNI; N default */
    val partecipato: String? = null,
    /** Task 72: cfa numerico in GET Seminars (v3) */
    val cfa: Int? = null
) : Parcelable

@Parcelize
data class SeminariResponse(
    val success: Boolean,
    val errors: List<String> = emptyList(),
    val start: Int = 0,
    val count: Int = 0,
    val totalCount: Int = 0,
    val payload: List<SeminarioPayload> = emptyList(),
    val errorSummary: String? = null
) : Parcelable

/** Risposta v3: payload può essere singolo oggetto o array. Usare payloadAsList() per ottenere la lista. */
data class SeminariResponseV3(
    val success: Boolean = false,
    val payload: JsonElement? = null,
    val start: Int? = null,
    val count: Int? = null,
    val totalCount: Int? = null,
    val errors: List<ApiError>? = null,
    val errorSummary: String? = null
) {
    fun payloadAsList(gson: com.google.gson.Gson): List<SeminarioPayload> {
        val el = payload ?: return emptyList()
        return when (el) {
            is JsonArray -> el.mapNotNull { gson.fromJson(it, SeminarioPayload::class.java) }
            is JsonObject -> listOfNotNull(gson.fromJson(el, SeminarioPayload::class.java))
            else -> emptyList()
        }
    }
}

// MARK: - Internships (API Test v3)
@Parcelize
data class InternshipPayload(
    val oid: String? = null,
    val descrizione: String? = null,
    val annoAccademicoInizio: Int? = null,
    val annoAccademicoFine: Int? = null,
    val periodoTirocinioDal: String? = null,
    val periodoTirocinioAl: String? = null,
    val cfa: Int? = null,
    val relazioneFinale: Boolean? = null,
    val moduloOre: Boolean? = null,
    val biennio: Boolean? = null,
    val triennio: Boolean? = null
) : Parcelable

data class InternshipsResponse(
    val success: Boolean = false,
    val payload: List<InternshipPayload>? = null,
    val errors: List<ApiError>? = null,
    val errorSummary: String? = null
)

// MARK: - Notification Models

@Parcelize
data class NotificationItem(
    val id: Int,
    val titolo: String,
    val messaggio: String,
    val data: String,
    val tipo: String,
    val isRead: Boolean
) : Parcelable

@Parcelize
data class NotificationPayload(
    val id: Int,
    val oggetto: String,
    val messaggio: String,
    val dataOraCreazione: String,
    val dataOraLetturaNotifica: String? = null
) : Parcelable

@Parcelize
data class NotificationsPayload(
    val notifications: List<NotificationPayload>
) : Parcelable

@Parcelize
data class NotificationsResponse(
    val success: Boolean,
    val payload: NotificationsPayload? = null,
    val errorSummary: String? = null,
    val errorMessage: String? = null
) : Parcelable

// MARK: - API v3 Notification Models (Firebase)

data class NotificationsRequestV3(
    val start: Int = 0,
    val count: Int = 100,
    val orderBy: String = "CreatedDate",
    val descending: Boolean = true
)

@Parcelize
data class NotificationPayloadV3(
    val id: Int,
    val dataOraCreazione: String,
    val tipo: String,
    val oggetto: String,
    val messaggio: String,
    val parametro: String? = null,
    val allievoOId: String? = null,
    val dataOraLetturaNotifica: String? = null,
    val fcmToken: String? = null,
    // Campi per allegati (quando presenti)
    val attachmentType: String? = null, // "0" per Programma, "1" per Dispensa
    val oid: String? = null // OID dell'allegato
) : Parcelable

@Parcelize
data class NotificationsResponseV3(
    val success: Boolean,
    val errors: List<ApiError>? = null,
    val start: Int = 0,
    val count: Int = 0,
    val totalCount: Int = 0,
    val payload: List<NotificationPayloadV3> = emptyList(),
    val errorSummary: String? = null
) : Parcelable

@Parcelize
data class ApiError(
    val code: String? = null,
    val message: String? = null
) : Parcelable

data class FcmTokenRequest(
    val fcmToken: String
)

// MARK: - Documents Models

@Parcelize
data class LogosDocumentsResponse(
    val success: Boolean,
    val payload: DocumentsPayload? = null,
    val errorSummary: String? = null,
    val errorMessage: String? = null
) : Parcelable

@Parcelize
data class DocumentsPayload(
    val pianoStudi: String? = null,
    val documents: List<LogosDoc>
) : Parcelable

@Parcelize
data class LogosDoc(
    val oid: String,
    val titolo: String,
    val tipo: String? = null,
    val descrizione: String? = null,
    val url: String? = null,
    val dataCreazione: String? = null
) : Parcelable

// MARK: - Thesis Models

@Parcelize
data class ThesisItem(
    val id: String,
    val title: String,
    val type: String? = null,
    val course: String? = null,
    val directURL: String? = null
) : Parcelable

@Parcelize
data class ThesisInfoResponse(
    val success: Boolean,
    val payload: List<ThesisItem>? = null,
    val errorSummary: String? = null,
    val errorMessage: String? = null
) : Parcelable

