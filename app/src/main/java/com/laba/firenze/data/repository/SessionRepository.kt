package com.laba.firenze.data.repository

import android.util.Log
import com.laba.firenze.data.api.AuthApi
import com.laba.firenze.data.api.LogosUniAPIClient
import com.laba.firenze.data.local.BackoffManager
import com.laba.firenze.data.local.JwtDecoder
import com.laba.firenze.data.local.KeychainHelper
import com.laba.firenze.data.local.SessionTokenManager
import com.laba.firenze.data.local.TokenStore
import com.laba.firenze.data.TopicManager
import com.laba.firenze.domain.model.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    val apiClient: LogosUniAPIClient,
    val tokenManager: SessionTokenManager,
    private val keychainHelper: KeychainHelper,
    val gradeCalculator: GradeCalculator,
    private val authApi: AuthApi,
    private val tokenStore: TokenStore,
    private val jwtDecoder: JwtDecoder,
    private val backoffManager: BackoffManager,
    private val topicManager: TopicManager
) {
    
    // State flows per i dati
    private val _exams = MutableStateFlow<List<Esame>>(emptyList())
    val exams: StateFlow<List<Esame>> = _exams.asStateFlow()
    val allExams: StateFlow<List<Esame>> = _exams.asStateFlow() // Alias per compatibilità
    
    private val _seminars = MutableStateFlow<List<Seminario>>(emptyList())
    val seminars: StateFlow<List<Seminario>> = _seminars.asStateFlow()
    
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Guardia concorrente per evitare più restore in parallelo (identico a iOS _restoreInFlight)
    @Volatile
    private var restoreInFlight = false
    
    // MARK: - Authentication
    
    suspend fun login(username: String, password: String): Boolean {
        return try {
            _isLoading.value = true
            _error.value = null
            
            // Normalizza username (aggiungi @labafirenze.com se manca)
            val normalizedUsername = if (username.contains("@")) username else "$username@labafirenze.com"
            
            Log.d("SessionRepository", "Attempting login for user: $normalizedUsername")
            
            val basicAuth = getBasicAuth()
            val response = authApi.login(
                basicAuth = basicAuth,
                username = normalizedUsername,
                password = password
            )
            
            Log.d("SessionRepository", "Login response code: ${response.code()}")
            Log.d("SessionRepository", "Login response body: ${response.body()}")
            
            if (response.isSuccessful) {
                val tokenResponse = response.body()
                if (tokenResponse != null) {
                    // Salva le credenziali
                    keychainHelper.saveCredentials(normalizedUsername, password)
                    
                    // Salva i token nel TokenStore (nuovo sistema OAuth2)
                    tokenStore.saveTokens(
                        accessToken = tokenResponse.access_token,
                        refreshToken = tokenResponse.refresh_token,
                        tokenType = tokenResponse.token_type,
                        expiresIn = tokenResponse.expires_in
                    )
                    
                    // Salva anche nel vecchio sistema per compatibilità
                    tokenManager.saveTokens(tokenResponse.access_token, tokenResponse.refresh_token)
                    
                    // Carica i dati dell'utente
                    loadUserProfile()
                    
                    // Carica tutti i dati
                    loadAll()
                    
                    _isLoading.value = false
                    true
                } else {
                    _error.value = "Risposta vuota dal server"
                    _isLoading.value = false
                    false
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Errore sconosciuto"
                Log.d("SessionRepository", "Login failed - $errorBody")
                _error.value = "Credenziali non valide"
                _isLoading.value = false
                false
            }
        } catch (e: Exception) {
            Log.d("SessionRepository", "Login exception: ${e.message}")
            _error.value = e.message ?: "Errore di login"
            _isLoading.value = false
            false
        }
    }
    
    /**
     * Genera Basic Auth header per client_id:client_secret (identico a iOS)
     */
    private fun getBasicAuth(): String {
        val credentials = "98C96373243D:B1355BBB-EA35-4724-AFAA-8ABAAFEDCFB6"
        val encoded = android.util.Base64.encodeToString(
            credentials.toByteArray(),
            android.util.Base64.NO_WRAP
        )
        return "Basic $encoded"
    }
    
    /**
     * Silent login con gestione backoff e anti-concorrenza (identico a iOS restoreSessionStrong)
     */
    suspend fun restoreSessionStrong(force: Boolean = false): Boolean {
        // 1. Anti-concorrenza: se un restore è già in corso → return
        if (restoreInFlight) {
            Log.d("SessionRepository", "Restore already in flight, skipping")
            return false
        }
        
        restoreInFlight = true
        try {
            // 2. Token ancora valido: se non forzato e tokenSecondsRemaining > 30 → salta
            val currentToken = tokenStore.getCurrentToken()
            if (currentToken.isNotEmpty()) {
                val remaining = jwtDecoder.getTokenSecondsRemaining(currentToken)
                if (!force && remaining != null && remaining > 30) {
                    Log.d("SessionRepository", "Token still valid for $remaining seconds, skipping restore")
                    return true
                }
            }
            
            // 3. Backoff: se esiste backoffUntil nel futuro e non forzato → salta
            if (!force && backoffManager.isInBackoff()) {
                Log.d("SessionRepository", "In backoff, skipping restore")
                return false
            }
            
            // 4. Lettura credenziali
            val credentials = keychainHelper.fetchKeychainCredentials()
            if (credentials == null) {
                Log.d("SessionRepository", "No credentials found, need login UI")
                return false
            }
            
            val (username, password) = credentials
            val normalizedUsername = keychainHelper.normalizeUsername(username)
            
            // 5. Chiamata "silente" di login (ROPC)
            return passwordLogin(normalizedUsername, password)
            
        } finally {
            restoreInFlight = false
        }
    }
    
    /**
     * Login ROPC silente (identico a iOS passwordLogin)
     */
    private suspend fun passwordLogin(username: String, password: String): Boolean {
        return try {
            Log.d("SessionRepository", "Attempting silent login for $username")
            
            val response = authApi.login(
                basicAuth = getBasicAuth(),
                username = username,
                password = password
            )
            
            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!
                
                // Salva il token
                tokenStore.saveTokens(
                    tokenResponse.access_token,
                    tokenResponse.refresh_token ?: "",
                    tokenResponse.token_type ?: "Bearer",
                    tokenResponse.expires_in?.toLong() ?: 3600L
                )
                
                // Reset backoff dopo successo
                backoffManager.resetBackoff()
                
                Log.d("SessionRepository", "Silent login successful")
                return true
                
            } else {
                val errorBody = response.errorBody()?.string()
                Log.d("SessionRepository", "Silent login failed: ${response.code()} - $errorBody")
                
                // Gestione errori (identico a iOS)
                if (errorBody?.contains("invalid_client", ignoreCase = true) == true) {
                    // Errore di configurazione del client → mostra subito Login UI
                    Log.d("SessionRepository", "Invalid client error, clearing token")
                    tokenStore.clearTokens()
                    return false
                } else {
                    // Altri errori → backoff esponenziale
                    backoffManager.bumpBackoff()
                    return false
                }
            }
            
        } catch (e: Exception) {
            Log.d("SessionRepository", "Silent login exception: ${e.message}")
            backoffManager.bumpBackoff()
            return false
        }
    }
    
    suspend fun silentLogin(): Boolean {
        return restoreSessionStrong(force = false)
    }
    
    suspend fun logout() {
        // Pulisce tutto (identico a iOS)
        tokenManager.clearCredentials()
        keychainHelper.clearCredentials()
        tokenStore.clearTokens() // Nuovo sistema OAuth2
        backoffManager.clearBackoff() // Pulisce anche il backoff
        
        // Pulisce i dati
        _exams.value = emptyList()
        _seminars.value = emptyList()
        _notifications.value = emptyList()
        
        Log.d("SessionRepository", "Logout completed")
    }
    
    // MARK: - Data Loading
    
    suspend fun loadAll() {
        _isLoading.value = true
        try {
            loadUserProfile()
            loadExams()
            loadSeminars()
            loadNotifications()
        } finally {
            _isLoading.value = false
        }
    }
    
    private suspend fun loadUserProfile() {
        try {
            val token = tokenStore.getCurrentAccessToken()
            if (token.isEmpty()) {
                Log.d("SessionRepository", "No access token available for user profile")
                return
            }
            
            Log.d("SessionRepository", "Loading user profile with token: ${token.take(20)}...")
            
            val studentPayload = apiClient.getStudentProfile(token)
            Log.d("SessionRepository", "Student payload received: ${studentPayload != null}")
            
            if (studentPayload != null) {
                Log.d("SessionRepository", "=== STUDENT PAYLOAD DETAILS ===")
                Log.d("SessionRepository", "Nome: ${studentPayload.nome}")
                Log.d("SessionRepository", "Cognome: ${studentPayload.cognome}")
                Log.d("SessionRepository", "annoAttuale: ${studentPayload.annoAttuale}")
                Log.d("SessionRepository", "pianoStudi: '${studentPayload.pianoStudi}'")
                Log.d("SessionRepository", "stato: '${studentPayload.stato}'")
                Log.d("SessionRepository", "dataStato: '${studentPayload.dataStato}'")
                Log.d("SessionRepository", "classeLaurea: '${studentPayload.classeLaurea}'")
                Log.d("SessionRepository", "NumMatricola: '${studentPayload.numMatricola}'")
                Log.d("SessionRepository", "=================================")
                
                // Update profile
                val profile = StudentProfile(
                    displayName = "${studentPayload.nome ?: ""} ${studentPayload.cognome ?: ""}".trim(),
                    status = studentPayload.stato, // Now available in payload
                    currentYear = studentPayload.annoAttuale?.toString(), // Now available in payload
                    pianoStudi = studentPayload.pianoStudi, // Now available in payload
                    nome = studentPayload.nome,
                    cognome = studentPayload.cognome,
                    matricola = studentPayload.numMatricola,
                    sesso = studentPayload.sesso,
                    emailLABA = studentPayload.emailLABA,
                    emailPersonale = studentPayload.emailPersonale,
                    telefono = studentPayload.telefono,
                    cellulare = studentPayload.cellulare,
                    pagamenti = studentPayload.pagamenti,
                    studentOid = studentPayload.oid
                )
                
                tokenManager.saveUserProfile(profile)
                Log.d("SessionRepository", "User profile saved successfully: ${profile.displayName}")
                
                // Update FCM topics based on user data
                updateFCMTopics(profile)
            } else {
                Log.d("SessionRepository", "Student payload is null")
            }
        } catch (e: Exception) {
            Log.d("SessionRepository", "Error loading user profile: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Update FCM topics based on user profile
     */
    private fun updateFCMTopics(profile: StudentProfile) {
        try {
            val isGraduated = profile.status?.lowercase()?.contains("laureat") == true
            val currentYear = profile.currentYear?.toIntOrNull()
            val pianoStudi = profile.pianoStudi
            
            Log.d("SessionRepository", "🔔 Updating FCM topics - Year: $currentYear, Course: $pianoStudi, Graduated: $isGraduated")
            
            topicManager.updateTopics(
                scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default),
                course = pianoStudi,
                currentYear = currentYear,
                isGraduated = isGraduated,
                isDocente = false
            )
        } catch (e: Exception) {
            Log.d("SessionRepository", "🔔 Error updating FCM topics: ${e.message}")
        }
    }
    
    suspend fun loadExams() {
        try {
            val token = tokenStore.getCurrentAccessToken()
            if (token.isEmpty()) {
                Log.d("SessionRepository", "No access token available for exams")
                return
            }
            
            Log.d("SessionRepository", "Loading enrollments with token: ${token.take(20)}...")
            
            // Come iOS: chiama enrollments che contiene stato, annoAttuale, pianoStudi E situazioneEsami
            val enrollments = apiClient.getEnrollments(token)
            Log.d("SessionRepository", "Received enrollments response")
            
            // Prendi i dati dal payload se disponibili
            val enrollmentsPayload = apiClient.getEnrollmentsPayload(token)
            
            if (enrollmentsPayload != null) {
                Log.d("SessionRepository", "=== ENROLLMENTS PAYLOAD ===")
                Log.d("SessionRepository", "stato: '${enrollmentsPayload.stato}'")
                Log.d("SessionRepository", "annoAttuale: ${enrollmentsPayload.annoAttuale}")
                Log.d("SessionRepository", "pianoStudi: '${enrollmentsPayload.pianoStudi}'")
                Log.d("SessionRepository", "situazioneEsami count: ${enrollmentsPayload.situazioneEsami?.size ?: 0}")
                Log.d("SessionRepository", "=============================")
                
                // Aggiorna il profilo con annoAttuale e pianoStudi (come iOS)
                val profile = tokenManager.userProfile.value
                if (profile != null) {
                    val updatedProfile = profile.copy(
                        status = enrollmentsPayload.stato,
                        currentYear = enrollmentsPayload.annoAttuale?.toString(),
                        pianoStudi = enrollmentsPayload.pianoStudi
                    )
                    tokenManager.saveUserProfile(updatedProfile)
                    Log.d("SessionRepository", "Updated profile with enrollments data")
                }
                
                // Converti e salva gli esami
                val exams = enrollmentsPayload.situazioneEsami?.map { payload ->
                    Esame(
                        oid = payload.oidCorso,
                        corso = payload.corso,
                        docente = payload.docente,
                        anno = payload.anno?.toString(),  // Converti Int a String
                        cfa = payload.cfa,
                        propedeutico = payload.propedeutico,
                        data = payload.sostenutoIl,  // sostenutoIl dal payload va in data
                        voto = payload.voto,
                        richiedibile = (payload.richiedibile ?: "N").uppercase() == "S",
                        dataRichiesta = payload.dataRichiesta,  // Aggiunto: data prenotazione
                        esitoRichiesta = payload.esitoRichiesta  // Aggiunto: esito richiesta
                    )
                } ?: emptyList()
                
                _exams.value = exams
                Log.d("SessionRepository", "Loaded ${exams.size} exams from enrollments")
                
                // Log first few exams for debugging
                exams.take(3).forEachIndexed { index, exam ->
                    Log.d("SessionRepository", "Exam $index: ${exam.corso} - ${exam.voto} - ${exam.anno}")
                }
            } else {
                // Fallback al vecchio metodo
                val exams = apiClient.getExams(token)
                _exams.value = exams
                Log.d("SessionRepository", "Loaded ${exams.size} exams using fallback method")
            }
        } catch (e: Exception) {
            Log.d("SessionRepository", "Error loading exams: ${e.message}")
            e.printStackTrace()
        }
    }
    
    suspend fun loadSeminars() {
        try {
            val token = tokenStore.getCurrentAccessToken()
            if (token.isEmpty()) return
            
            Log.d("SessionRepository", "Loading seminars with token: ${token.take(20)}...")
            val seminars = apiClient.getSeminars(token)
            _seminars.value = seminars
            Log.d("SessionRepository", "Loaded ${seminars.size} seminars")
        } catch (e: Exception) {
            Log.d("SessionRepository", "Error loading seminars: ${e.message}")
        }
    }
    
    suspend fun loadNotifications() {
        try {
            val token = tokenStore.getCurrentAccessToken()
            if (token.isEmpty()) return
            
            Log.d("SessionRepository", "Loading notifications with token: ${token.take(20)}...")
            
            val notificationPayloads = apiClient.getNotifications(token)
            val notifications = notificationPayloads.map { payload ->
                NotificationItem(
                    id = payload.id,
                    titolo = payload.oggetto,
                    messaggio = payload.messaggio,
                    data = payload.dataOraCreazione,
                    tipo = "notification",
                    isRead = payload.dataOraLetturaNotifica != null
                )
            }
            _notifications.value = notifications
            Log.d("SessionRepository", "Loaded ${notifications.size} notifications")
        } catch (e: Exception) {
            Log.d("SessionRepository", "Error loading notifications: ${e.message}")
        }
    }
    
    // MARK: - User Profile Access
    
    fun getUserProfile(): StudentProfile? {
        return tokenManager.userProfile.value
    }
    
    /**
     * Ottiene il profilo corrente come StateFlow
     */
    fun getUserProfileFlow(): StateFlow<StudentProfile?> {
        return tokenManager.userProfile
    }
    
    // MARK: - Documents
    
    suspend fun getDocuments(): List<LogosDoc> {
        return try {
            Log.d("SessionRepository", "getDocuments() called")
            val token = tokenStore.getCurrentAccessToken()
            if (token.isEmpty()) {
                Log.d("SessionRepository", "No access token available for documents")
                return emptyList()
            }
            
            Log.d("SessionRepository", "Loading documents with token: ${token.take(20)}...")
            val documents = apiClient.getDocuments(token)
            Log.d("SessionRepository", "Loaded ${documents.size} documents")
            
            // Log first few documents for debugging
            documents.take(3).forEachIndexed { index, doc ->
                Log.d("SessionRepository", "Document $index: ${doc.titolo} - ${doc.tipo}")
            }
            
            documents
        } catch (e: Exception) {
            Log.d("SessionRepository", "Error loading documents: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun downloadDocument(allegatoOid: String): ByteArray? {
        return try {
            val token = tokenStore.getCurrentAccessToken()
            if (token.isEmpty()) {
                Log.d("SessionRepository", "No access token available for document download")
                return null
            }
            
            Log.d("SessionRepository", "Downloading document $allegatoOid with token: ${token.take(20)}...")
            val documentData = apiClient.getDocumentById(token, allegatoOid)
            if (documentData != null) {
                Log.d("SessionRepository", "Successfully downloaded document $allegatoOid (${documentData.size} bytes)")
            } else {
                Log.d("SessionRepository", "Failed to download document $allegatoOid")
            }
            documentData
        } catch (e: Exception) {
            Log.d("SessionRepository", "Error downloading document $allegatoOid: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    // MARK: - Notification Management
    
    suspend fun markNotificationAsRead(notificationId: Int): Boolean {
        val token = tokenManager.accessToken.value
        if (token.isEmpty()) return false
        
        return apiClient.markNotificationAsRead(token, notificationId)
    }
    
    suspend fun markAllNotificationsAsRead(): Boolean {
        val token = tokenManager.accessToken.value
        if (token.isEmpty()) return false
        
        return apiClient.markAllNotificationsAsRead(token)
    }
    
    // Alias methods for ViewModel compatibility
    suspend fun markNotificationRead(id: Int, read: Boolean) {
        if (read) {
            markNotificationAsRead(id)
        }
        // If not read, there's no API to unmark, so we just return
    }
    
    suspend fun markAllNotificationsRead() {
        markAllNotificationsAsRead()
    }
    
    suspend fun deleteNotification(id: Int) {
        // Not implemented in API yet, just remove from local state
        _notifications.value = _notifications.value.filter { it.id != id }
    }
    
    // MARK: - Session State
    
    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn.value
    }
    
    fun getAccessToken(): String {
        return tokenManager.accessToken.value
    }
    
    suspend fun getThesisDocuments(): List<com.laba.firenze.ui.thesis.ThesisDocument> {
        return try {
            val token = tokenManager.accessToken.value
            val documents = apiClient.getThesisDocuments(token)
            
            if (documents.isEmpty()) {
                return emptyList()
            }
            
            documents.map { item ->
                com.laba.firenze.ui.thesis.ThesisDocument(
                    id = item.allegatoOid,
                    title = prettifyTitle(item.descrizione),
                    type = getDocumentType(item.descrizione),
                    icon = getDocumentIcon(item.descrizione),
                    url = "logos://document/${item.allegatoOid}"
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    
    private fun prettifyTitle(title: String): String {
        return title.lowercase()
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
    }
    
    private fun getDocumentType(descrizione: String): String {
        val desc = descrizione.lowercase()
        return when {
            desc.contains("regolamento") -> "PDF"
            desc.contains("domanda") -> "DOCX"
            desc.contains("frontespizio") -> "DOCX"
            desc.contains("bollettino") -> "PDF"
            desc.contains("pergamena") -> "PDF"
            else -> "PDF"
        }
    }
    
    private fun getDocumentIcon(descrizione: String): androidx.compose.ui.graphics.vector.ImageVector {
        val desc = descrizione.lowercase()
        return when {
            desc.contains("regolamento") -> Icons.Default.Book
            desc.contains("domanda") -> Icons.Default.Description
            desc.contains("frontespizio") -> Icons.Default.TextFields
            desc.contains("bollettino") -> Icons.Default.Download
            desc.contains("pergamena") -> Icons.Default.Verified
            else -> Icons.Default.Description
        }
    }
    
}
