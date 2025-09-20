package com.laba.firenze.domain.model

import android.os.Parcelable
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
    val enrollments: List<Enrollment>
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
    val data: String? = null,
    val tipo: String? = null,
    val stato: String? = null,
    val propedeutico: String? = null,
    val richiedibile: Boolean = false
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
    val descrizioneEstesa: String? = null,
    val esito: String? = null,
    val gruppiStudenti: List<String> = emptyList()
) : Parcelable

@Parcelize
data class SeminarioPayload(
    val seminarioOid: String,
    val descrizione: String,
    val descrizioneEstesa: String? = null,
    val documentOid: String? = null,
    val dataRichiesta: String? = null,
    val esitoRichiesta: String? = null,
    val richiedibile: String? = null
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
