package com.laba.firenze.data.repository

import com.laba.firenze.data.api.AuthApi
import com.laba.firenze.data.api.LogosUniAPIClient
import com.laba.firenze.data.local.BackoffManager
import com.laba.firenze.data.local.JwtDecoder
import com.laba.firenze.data.local.KeychainHelper
import com.laba.firenze.data.local.SessionTokenManager
import com.laba.firenze.data.local.TokenStore
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
    private val backoffManager: BackoffManager
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
            
            println("🔐 SessionRepository: Attempting login for user: $normalizedUsername")
            
            val basicAuth = getBasicAuth()
            val response = authApi.login(
                basicAuth = basicAuth,
                username = normalizedUsername,
                password = password
            )
            
            println("🔐 SessionRepository: Login response code: ${response.code()}")
            println("🔐 SessionRepository: Login response body: ${response.body()}")
            
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
                println("🔐 SessionRepository: Login failed - $errorBody")
                _error.value = "Credenziali non valide"
                _isLoading.value = false
                false
            }
        } catch (e: Exception) {
            println("🔐 SessionRepository: Login exception: ${e.message}")
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
            println("🔐 SessionRepository: Restore already in flight, skipping")
            return false
        }
        
        restoreInFlight = true
        try {
            // 2. Token ancora valido: se non forzato e tokenSecondsRemaining > 30 → salta
            val currentToken = tokenStore.getCurrentToken()
            if (currentToken.isNotEmpty()) {
                val remaining = jwtDecoder.getTokenSecondsRemaining(currentToken)
                if (!force && remaining != null && remaining > 30) {
                    println("🔐 SessionRepository: Token still valid for $remaining seconds, skipping restore")
                    return true
                }
            }
            
            // 3. Backoff: se esiste backoffUntil nel futuro e non forzato → salta
            if (!force && backoffManager.isInBackoff()) {
                println("🔐 SessionRepository: In backoff, skipping restore")
                return false
            }
            
            // 4. Lettura credenziali
            val credentials = keychainHelper.fetchKeychainCredentials()
            if (credentials == null) {
                println("🔐 SessionRepository: No credentials found, need login UI")
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
            println("🔐 SessionRepository: Attempting silent login for $username")
            
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
                
                println("🔐 SessionRepository: Silent login successful")
                return true
                
            } else {
                val errorBody = response.errorBody()?.string()
                println("🔐 SessionRepository: Silent login failed: ${response.code()} - $errorBody")
                
                // Gestione errori (identico a iOS)
                if (errorBody?.contains("invalid_client", ignoreCase = true) == true) {
                    // Errore di configurazione del client → mostra subito Login UI
                    println("🔐 SessionRepository: Invalid client error, clearing token")
                    tokenStore.clearTokens()
                    return false
                } else {
                    // Altri errori → backoff esponenziale
                    backoffManager.bumpBackoff()
                    return false
                }
            }
            
        } catch (e: Exception) {
            println("🔐 SessionRepository: Silent login exception: ${e.message}")
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
        
        println("🔐 SessionRepository: Logout completed")
    }
    
    // MARK: - Data Loading
    
    suspend fun loadAll() {
        loadUserProfile()
        loadExams()
        loadSeminars()
        loadNotifications()
    }
    
    private suspend fun loadUserProfile() {
        try {
            val token = tokenStore.getCurrentAccessToken()
            if (token.isEmpty()) {
                println("🔐 SessionRepository: No access token available for user profile")
                return
            }
            
            println("🔐 SessionRepository: Loading user profile with token: ${token.take(20)}...")
            
            val studentPayload = apiClient.getStudentProfile(token)
            println("🔐 SessionRepository: Student payload received: ${studentPayload != null}")
            
            if (studentPayload != null) {
                println("🔐 SessionRepository: Student data - Nome: ${studentPayload.nome}, Cognome: ${studentPayload.cognome}")
                
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
                println("🔐 SessionRepository: User profile saved successfully: ${profile.displayName}")
            } else {
                println("🔐 SessionRepository: Student payload is null")
            }
        } catch (e: Exception) {
            println("🔐 SessionRepository: Error loading user profile: ${e.message}")
            e.printStackTrace()
        }
    }
    
    suspend fun loadExams() {
        try {
            val token = tokenStore.getCurrentAccessToken()
            if (token.isEmpty()) {
                println("🔐 SessionRepository: No access token available for exams")
                return
            }
            
            println("🔐 SessionRepository: Loading exams with token: ${token.take(20)}...")
            println("🔐 SessionRepository: TokenStore state - hasAccessToken: ${tokenStore.hasAccessToken()}")
            println("🔐 SessionRepository: TokenStore state - tokenExpiry: ${tokenStore.getTokenExpiry()}")
            
            val exams = apiClient.getExams(token)
            println("🔐 SessionRepository: Received ${exams.size} exams from API client")
            
            _exams.value = exams
            println("🔐 SessionRepository: Updated _exams StateFlow with ${exams.size} exams")
            
            // Log first few exams for debugging
            exams.take(3).forEachIndexed { index, exam ->
                println("🔐 SessionRepository: Exam $index: ${exam.corso} - ${exam.voto} - ${exam.anno}")
            }
        } catch (e: Exception) {
            println("🔐 SessionRepository: Error loading exams: ${e.message}")
            e.printStackTrace()
        }
    }
    
    suspend fun loadSeminars() {
        try {
            val token = tokenStore.getCurrentAccessToken()
            if (token.isEmpty()) return
            
            println("🔐 SessionRepository: Loading seminars with token: ${token.take(20)}...")
            val seminars = apiClient.getSeminars(token)
            _seminars.value = seminars
            println("🔐 SessionRepository: Loaded ${seminars.size} seminars")
        } catch (e: Exception) {
            println("🔐 SessionRepository: Error loading seminars: ${e.message}")
        }
    }
    
    suspend fun loadNotifications() {
        try {
            val token = tokenStore.getCurrentAccessToken()
            if (token.isEmpty()) return
            
            println("🔐 SessionRepository: Loading notifications with token: ${token.take(20)}...")
            
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
            println("🔐 SessionRepository: Loaded ${notifications.size} notifications")
        } catch (e: Exception) {
            println("🔐 SessionRepository: Error loading notifications: ${e.message}")
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
    
    
    suspend fun downloadDocument(allegatoOid: String): ByteArray? {
        return try {
            val token = tokenManager.accessToken.value
            apiClient.getDocumentById(token, allegatoOid)
        } catch (e: Exception) {
            null
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
