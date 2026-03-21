package com.laba.firenze.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.repository.LessonCalendarRepository
import com.laba.firenze.data.repository.SessionRepository
import com.laba.firenze.domain.model.Esame
import com.laba.firenze.domain.model.LessonEvent
import com.laba.firenze.domain.model.StudentProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.json.JSONArray
import androidx.core.content.edit
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import java.time.Clock
import java.time.LocalDate
import java.time.Month
import java.time.Year
import java.time.ZoneId
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import com.laba.firenze.LabaConfig

data class HeroInfo(
    val displayName: String,
    val academicYear: String,     // es. "2025/26"
    val courseName: String,       // es. "Graphic Design & Multimedia"
    val studyYear: Int?           // es. 2 (null se non noto)
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val lessonCalendarRepository: LessonCalendarRepository,
    private val appearancePreferences: com.laba.firenze.data.local.AppearancePreferences,
    private val achievementManager: com.laba.firenze.data.gamification.AchievementManager,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    // ... (rest of code)
    

    private fun getTodaysLessonsForUser(profile: StudentProfile?, exams: List<Esame>): List<LessonUi> {
        val allEvents = lessonCalendarRepository.events.value
        val calendar = java.util.Calendar.getInstance()
        val now = Date()
        val selectedGroup = if (LabaConfig.USE_GROUP_FILTER) appearancePreferences.getSelectedGroup() else null
        
        // Filtra lezioni di oggi e domani
        val todayStart = calendar.time
        calendar.add(java.util.Calendar.DAY_OF_MONTH, 2)
        val tomorrowEnd = calendar.time
        
        val relevantLessons = allEvents.filter { event ->
            event.start >= todayStart && event.start < tomorrowEnd
        }
        
        // Filtra per corsi dell'utente
        val userCourseIds = exams.mapNotNull { exam ->
             getCourseIdFromExam(exam)
        }.toSet()
        
        val userLessons = relevantLessons.filter { event ->
            // 1. Check Group Filter
            val eventGroup = event.gruppo
            val matchesGroup = if (selectedGroup != null && !eventGroup.isNullOrBlank()) {
                eventGroup.equals(selectedGroup, ignoreCase = true)
            } else {
                true // No group selected OR event has no group -> show it
            }
            
            if (!matchesGroup) return@filter false
            
            // 2. Check Course Match
            // Match per oidCorso singolo
            event.oidCorso?.lowercase()?.let { oid ->
                userCourseIds.contains(oid)
            } ?: false || 
            // Match per oidCorsi multipli
            event.oidCorsi?.any { oid ->
                userCourseIds.contains(oid.lowercase())
            } == true ||
            // Match per nome corso normalizzato
            normalizeCourse(event.corso).let { normalized ->
                userCourseIds.any { it.contains(normalized.lowercase()) || normalized.lowercase().contains(it) }
            }
        }
        
        // Converti in LessonUi
        return userLessons.map { event ->
             // ...
             // (Using existing logic)
             LessonUi(
                title = event.corso,
                time = formatTimeRange(event.start, event.end),
                room = event.aula,
                teacher = if (event.gruppo != null) "${event.docente ?: ""} (Gr. ${event.gruppo})" else event.docente, // Show group in teacher field if exists
                date = formatDate(event.start)
            )
        }
    }
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val prefs = context.getSharedPreferences("LABA_PREFS", Context.MODE_PRIVATE)
    private val defaultSectionOrder = listOf("hero", "kpi", "progress", "lessons", "exams", "quickActions")
    
    private val _sectionOrder = MutableStateFlow(loadSectionOrder())
    val sectionOrder: StateFlow<List<String>> = _sectionOrder.asStateFlow()
    
    private fun loadSectionOrder(): List<String> {
        val json = prefs.getString("home.sectionOrder", null) ?: return defaultSectionOrder
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (_: Exception) {
            defaultSectionOrder
        }
    }
    
    fun saveSectionOrder(order: List<String>) {
        prefs.edit { putString("home.sectionOrder", JSONArray(order).toString()) }
        _sectionOrder.value = order
    }
    
    fun getSectionOrderList(): List<String> = _sectionOrder.value
    
    // Flag per tracciare se i dati sono già stati caricati
    private var hasLoadedData = false
    
    init {
        // Load lessons cache
        lessonCalendarRepository.loadCacheIfAvailable()
        loadData()
        hasLoadedData = true
    }
    
    fun refreshOnAppear() {
        viewModelScope.launch {
            // Controlla se i dati sono già stati caricati guardando se il profilo esiste
            val profile = sessionRepository.getUserProfileFlow().value
            val exams = sessionRepository.allExams.value
            
            // Se i dati sono già stati caricati (profilo e esami esistono), non ricaricare
            if (hasLoadedData && profile != null && exams.isNotEmpty()) {
                // Solo track section visit senza ricaricare i dati
                achievementManager.trackSectionVisit("home")
                return@launch
            }
            
            // Carica tutti i dati e aspetta il completamento
            sessionRepository.loadAll()
            
            // Track achievements solo al primo caricamento
            if (!hasLoadedData) {
                achievementManager.trackDataLoad()
                achievementManager.trackLogin()
            }
            
            // Track section visit
            achievementManager.trackSectionVisit("home")
            
            // Sync lessons (solo se necessario)
            val updatedProfile = sessionRepository.getUserProfileFlow().value
            val pianoStudi = updatedProfile?.pianoStudi
            val currentYear = updatedProfile?.currentYear?.toIntOrNull()
            lessonCalendarRepository.syncLessons(pianoStudi, currentYear, force = !hasLoadedData)
            
            // Update achievements from session data
            val updatedExams = sessionRepository.allExams.value
            val seminars = sessionRepository.seminars.value
            achievementManager.updateAchievements(updatedExams, seminars, updatedProfile)
            
            // Forza il refresh dei dati solo se non erano già caricati
            if (!hasLoadedData) {
                loadData()
                hasLoadedData = true
            }
        }
    }
    
    /**
     * Ottiene il profilo utente corrente
     */
    fun getUserProfile() = sessionRepository.getUserProfile()
    
    /**
     * Ottiene tutti gli esami
     */
    fun getAllExams() = sessionRepository.allExams.value
    
    /**
     * Ottiene tutti i seminari
     */
    fun getAllSeminars() = sessionRepository.seminars.value
    
    // MARK: - Helper Functions
    
    /**
     * Calcola l'anno accademico corrente (identico a iOS)
     * Regola: se mese >= 9 (set-dic): anno accademico = YYYY / (YYYY+1)
     *         se mese <= 8 (gen-ago): anno accademico = (YYYY-1) / YYYY
     */
    fun currentAcademicYearString(clock: Clock = Clock.system(ZoneId.of("Europe/Rome"))): String {
        val now = LocalDate.now(clock)
        val y = now.year
        val startMonth = Month.SEPTEMBER.value // 9
        // iOS usa solo le ultime 2 cifre per l'anno accademico (es. 23/24 invece di 2023/2024)
        val currentYearLast2 = y.toString().takeLast(2)
        val nextYearLast2 = (y + 1).toString().takeLast(2)
        val prevYearLast2 = (y - 1).toString().takeLast(2)
        
        val academicYear = if (now.monthValue >= startMonth) "$currentYearLast2/$nextYearLast2"
               else "$prevYearLast2/$currentYearLast2"
        return "A.A. $academicYear"
    }
    
    /**
     * Stima l'anno di corso basato sui dati disponibili
     */
    private fun estimateStudyYear(profile: StudentProfile?): Int? {
        Log.d("HomeViewModel", "estimateStudyYear - profile.currentYear: '${profile?.currentYear}'")
        
        // Cerca l'anno nel profilo
        profile?.currentYear?.let { anno ->
            val yearInt = anno.toIntOrNull()
            Log.d("HomeViewModel", "Parsed currentYear to int: $yearInt")
            // Accetta anche anni > 3 per fuoricorso (come iOS)
            if (yearInt != null && yearInt >= 1) {
                return yearInt
            }
        }
        
        // Se non abbiamo l'anno dal backend, prova a dedurlo dall'anno accademico
        profile?.pianoStudi?.let { pianoStudi ->
            val academicYear = extractAcademicYearFromPianoStudi(pianoStudi)
            Log.d("HomeViewModel", "Could not get year from backend, but got academic year: $academicYear")
            // Potremmo provare a dedurre l'anno dall'anno accademico
            // ma per ora ritorniamo null se non abbiamo l'info
        }
        
        Log.d("HomeViewModel", "No currentYear found in profile, returning null")
        return null
    }
    
    /**
     * Crea una versione compatta del nome del corso (identica a iOS)
     */
    private fun getCompactCourseName(profile: StudentProfile?): String {
        // Cerca il corso nel profilo (pianoStudi contiene il nome del corso)
        profile?.pianoStudi?.let { piano ->
            val courseInfo = getCourseDisplayInfo(piano)
            if (courseInfo.isNotEmpty()) {
                return courseInfo
            }
            return normalizeCourse(piano)
        }
        
        // Fallback
        return "Graphic Design"
    }
    
    /**
     * Normalizza il nome del corso (identica a iOS)
     */
    private fun normalizeCourse(s: String): String {
        // Rimuove accenti, converte in lowercase, mantiene solo alfanumerici
        val folded = s.lowercase()
            .replace("à", "a").replace("è", "e").replace("é", "e")
            .replace("ì", "i").replace("ò", "o").replace("ù", "u")
        
        return folded.filter { it.isLetterOrDigit() || it.isWhitespace() }
            .trim()
            .split("\\s+".toRegex())
            .joinToString(" ") { word ->
                when {
                    word.contains("graphic") && word.contains("design") -> "Graphic Design"
                    word.contains("multimedia") -> "Multimedia"
                    word.contains("communication") -> "Communication"
                    word.contains("fashion") -> "Fashion"
                    word.contains("interior") -> "Interior"
                    word.contains("product") -> "Product"
                    else -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            }
    }
    
    /**
     * Genera le informazioni per la Hero section
     */
    fun getHeroInfo(): HeroInfo {
        val profile = sessionRepository.getUserProfileFlow().value
        
        Log.d("HomeViewModel", "=== getHeroInfo ===")
        Log.d("HomeViewModel", "Profile: ${profile != null}")
        Log.d("HomeViewModel", "Profile displayName: ${profile?.displayName}")
        Log.d("HomeViewModel", "Profile currentYear: '${profile?.currentYear}'")
        Log.d("HomeViewModel", "Profile pianoStudi: '${profile?.pianoStudi}'")
        Log.d("HomeViewModel", "Profile status: '${profile?.status}'")
        
        val academicYear = extractAcademicYearFromPianoStudi(profile?.pianoStudi)
        val courseName = getCompactCourseName(profile)
        val studyYear = estimateStudyYear(profile)
        
        Log.d("HomeViewModel", "=== RESULT ===")
        Log.d("HomeViewModel", "Final academicYear: '$academicYear'")
        Log.d("HomeViewModel", "Final courseName: '$courseName'")
        Log.d("HomeViewModel", "Final studyYear: $studyYear")
        
        return HeroInfo(
            displayName = profile?.nome ?: "Studente", // Solo nome, no cognome
            academicYear = academicYear,
            courseName = courseName,
            studyYear = studyYear
        )
    }
    
    /**
     * Estrae l'anno accademico dal piano studi (ESATTAMENTE come iOS)
     * Implementazione basata su courseDisplayInfo in ContentView.swift righe 395-425
     * Esempio: "Graphic Design A.A. 23/24" -> "A.A. 23/24"
     */
    private fun extractAcademicYearFromPianoStudi(pianoStudi: String?): String {
        Log.d("HomeViewModel", "extractAcademicYearFromPianoStudi - pianoStudi: '$pianoStudi'")
        
        if (pianoStudi.isNullOrEmpty()) {
            Log.d("HomeViewModel", "pianoStudi is null/empty, using fallback")
            return currentAcademicYearString() // Fallback: calcola da data corrente
        }
        
        // iOS divide la stringa usando "A.A." come separatore
        val upper = pianoStudi.uppercase()
        val parts = upper.split("A.A.")
        
        if (parts.size <= 1) {
            Log.d("HomeViewModel", "No A.A. found in pianoStudi, using fallback")
            return currentAcademicYearString()
        }
        
        // Se ci sono più "A.A.", prendi l'ultimo (es. "Graphic Design A.A. 2020/2023 A.A. 2023/2024" -> "2023/2024")
        val yearsRaw = parts.last()
        Log.d("HomeViewModel", "yearsRaw after split (last part): '$yearsRaw'")
        
        // Estrai tutti i numeri separati da caratteri non-numerici
        // Esempio: "2020/2023 2023/2024" -> ["2020", "2023", "2023", "2024"]
        val allNumbers = yearsRaw.split(Regex("[^0-9]+")).filter { it.isNotEmpty() }
        Log.d("HomeViewModel", "All numbers: $allNumbers")
        
        if (allNumbers.size >= 2) {
            // Prendi gli ULTIMI 2 numeri (gli anni accademici più recenti)
            val lastTwo = allNumbers.takeLast(2)
            Log.d("HomeViewModel", "Last two numbers: $lastTwo")
            
            val start = lastTwo[0].takeLast(2)  // Ultime 2 cifre del primo anno
            val end = lastTwo[1].takeLast(2)    // Ultime 2 cifre del secondo anno
            val result = "A.A. $start/$end"
            Log.d("HomeViewModel", "Found A.A. in pianoStudi: $result")
            return result
        }
        
        Log.d("HomeViewModel", "Not enough numbers in pianoStudi: '$yearsRaw', using fallback")
        // Fallback: calcola da data corrente
        return currentAcademicYearString()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            combine(
                sessionRepository.getUserProfileFlow(),
                sessionRepository.allExams,
                sessionRepository.seminars,
                sessionRepository.notifications,
                sessionRepository.isLoading
            ) { profile, exams, seminars, notifications, isLoading ->
                @Suppress("UNUSED_PARAMETER")
                
                Log.d("HomeViewModel", "Loading data - Profile: ${profile != null}, Exams: ${exams.size}, Seminars: ${seminars.size}, Loading: $isLoading")
                Log.d("HomeViewModel", "Profile details: ${profile?.displayName} - ${profile?.currentYear} - ${profile?.pianoStudi}")
                exams.take(3).forEachIndexed { index, exam ->
                    Log.d("HomeViewModel", "Exam $index: ${exam.corso} - ${exam.voto} - ${exam.anno}")
                }
                seminars.take(3).forEachIndexed { index, seminar ->
                    Log.d("HomeViewModel", "Seminar $index: ${seminar.titolo} - ${seminar.esito}")
                }
                
                val displayName = profile?.nome ?: "Studente"
                
                // Check if graduated: either from profile status or thesis completion
                val thesisCompleted = exams.any { 
                    it.corso.contains("TESI", ignoreCase = true) && isCompleted(it) 
                }
                val isGraduated = profile?.status?.lowercase()?.contains("laureat") == true || thesisCompleted
                
                Log.d("HomeViewModel", "Graduation check - Profile status: '${profile?.status}', Thesis completed: $thesisCompleted, IsGraduated: $isGraduated")
                
                // Status pills logic
                val statusPills = mutableListOf<String>()
                if (isGraduated) {
                    statusPills.add("Laureato")
                } else {
                    profile?.currentYear?.let { year ->
                        statusPills.add(getItalianOrdinalYear(year.toIntOrNull() ?: 1))
                    }
                }
                
                // Always add course info if available
                profile?.pianoStudi?.let { piano ->
                    val courseInfo = getCourseDisplayInfo(piano)
                    if (courseInfo.isNotEmpty()) {
                        statusPills.add(courseInfo)
                    }
                }
                
                // Always add academic year
                statusPills.add(currentAcademicYearString())
                
                // Complex exam calculations (exactly like iOS)
                val validExams = exams.filter { isCountableForTotals(it) }
                val passedExamsCount = validExams.count { !(it.voto?.isEmpty() ?: true) }
                val totalExamsCount = validExams.size
                val missingExamsCount = max(0, totalExamsCount - passedExamsCount)
                
                // CFA calculations: da API (solo test) se disponibili, altrimenti calcolo locale
                val cfaTarget = calculateCfaTarget(exams)
                val cfaEarned = calculateCfaEarned(exams, isGraduated, profile)
                
                // Year progress calculations
                val yearProgress = calculateYearProgress(exams, profile?.currentYear?.toIntOrNull())
                
                // Career average calculation
                val careerAverage = calculateCareerAverage(exams)
                
                // Lezioni reali - filtra per l'utente
                val lessonsToday = getTodaysLessonsForUser(profile, exams)
                
                // Esami in arrivo (calcolo basato su esami non sostenuti)
                val upcomingExamsCount = validExams.count { it.voto?.isEmpty() ?: true }
                
                Log.d("HomeViewModel", "Calculated - Passed: $passedExamsCount, Missing: $missingExamsCount, CFA: $cfaEarned, Average: $careerAverage")
                
                // Calcola esami prenotati (identico a iOS)
                val bookedExams = calculateBookedExams(exams, profile)
                
                _uiState.value = HomeUiState(
                    displayName = displayName,
                    statusPills = statusPills,
                    isGraduated = isGraduated,
                    passedExamsCount = passedExamsCount,
                    missingExamsCount = missingExamsCount,
                    totalExamsCount = totalExamsCount,
                    cfaEarned = cfaEarned,
                    cfaTarget = cfaTarget,
                    yearProgress = yearProgress,
                    careerAverage = careerAverage,
                    lessonsToday = lessonsToday,
                    upcomingExamsCount = upcomingExamsCount,
                    bookedExams = bookedExams,
                    isLoading = isLoading
                )
            }.collect { }
        }
    }
    
    // MARK: - Helper Functions (exactly like iOS)
    
    private fun isAttivitaOTesi(exam: Esame): Boolean {
        val title = exam.corso.lowercase()
        return title.contains("attivit") || title.contains("tesi")
    }
    
    private fun isCountableForTotals(exam: Esame): Boolean {
        return !isAttivitaOTesi(exam)
    }
    
    private fun isAttivitaIntegrativa(exam: Esame): Boolean {
        return exam.corso.lowercase().contains("attivit")
    }
    
    private fun isTesiFinale(exam: Esame): Boolean {
        return exam.corso.lowercase().contains("tesi")
    }
    
    private fun isCompleted(exam: Esame): Boolean {
        return !(exam.voto?.isEmpty() ?: true) || exam.data != null
    }
    
    /**
     * CFA TARGET: somma di TUTTI i CFA (esami, idoneità, attività, tesi)
     * Identico a iOS: vm.esami.reduce(0) { $0 + (Int($1.cfa ?? "") ?? 0) }
     */
    private fun calculateCfaTarget(exams: List<Esame>): Int {
        Log.d("HomeViewModel", "Calculating CFA target from ${exams.size} exams")
        
        // Somma TUTTI gli esami (inclusi workshop, tesi, attività)
        val totalCFA = exams.sumOf { exam ->
            val cfaValue = exam.cfa?.toIntOrNull() ?: 0
            Log.d("HomeViewModel", "Exam '${exam.corso}' - CFA: '${exam.cfa}' -> $cfaValue")
            cfaValue
        }
        
        Log.d("HomeViewModel", "Total CFA target calculated: $totalCFA")
        return totalCFA
    }
    
    private fun calculateCfaEarned(exams: List<Esame>, isGraduated: Boolean, profile: StudentProfile?): Int {
        // API Test (v3): usa cfaEsami + cfaSeminari + cfaTirocini da LOGOS se presenti
        if (profile != null && (profile.cfaEsami != null || profile.cfaSeminari != null || profile.cfaTirocini != null)) {
            val sum = (profile.cfaEsami ?: 0) + (profile.cfaSeminari ?: 0) + (profile.cfaTirocini ?: 0)
            Log.d("HomeViewModel", "Using CFA from API: cfaEsami=${profile.cfaEsami}, cfaSeminari=${profile.cfaSeminari}, cfaTirocini=${profile.cfaTirocini} -> $sum")
            return sum
        }
        Log.d("HomeViewModel", "Calculating CFA from ${exams.size} exams (computed)")
        
        // Debug: stampa tutti gli esami con i loro CFA
        exams.forEach { exam ->
            Log.d("HomeViewModel", "Exam '${exam.corso}' - CFA: '${exam.cfa}', Voto: '${exam.voto}', Anno: '${exam.anno}'")
        }
        
        // 1) Esami (escludi Attività e Tesi)
        val validExams = exams.filter { !isAttivitaIntegrativa(it) && !isTesiFinale(it) }
        val completedExams = validExams.filter { isCompleted(it) }
        Log.d("HomeViewModel", "Valid exams: ${validExams.size}, Completed: ${completedExams.size}")
        
        val examsEarned = completedExams.sumOf { exam ->
            val cfaValue = exam.cfa?.toIntOrNull() ?: 0
            Log.d("HomeViewModel", "Completed Exam '${exam.corso}' - CFA: '${exam.cfa}' -> $cfaValue")
            cfaValue
        }
        
        // 2) Attività integrative (max 10 CFA totali)
        val attivita = exams.filter { isAttivitaIntegrativa(it) }
        Log.d("HomeViewModel", "Attività integrative trovate: ${attivita.size}")
        attivita.forEach { exam ->
            Log.d("HomeViewModel", "Attività '${exam.corso}' - CFA: '${exam.cfa}', Completata: ${isCompleted(exam)}")
        }
        val declaredActivitiesCFA = attivita.sumOf { it.cfa?.toIntOrNull() ?: 0 }
        val anyActivityCompleted = attivita.any { isCompleted(it) }
        val activitiesEarned = when {
            isGraduated -> declaredActivitiesCFA // laureato: tutti i CFA dichiarati sono acquisiti
            anyActivityCompleted -> min(10, declaredActivitiesCFA)
            else -> 0
        }
        Log.d("HomeViewModel", "Attività CFA - Dichiarati: $declaredActivitiesCFA, Completate: $anyActivityCompleted, Acquisiti: $activitiesEarned")
        
        // 3) Tesi finale
        val thesis = exams.filter { isTesiFinale(it) }
        Log.d("HomeViewModel", "Tesi trovate: ${thesis.size}")
        thesis.forEach { exam ->
            Log.d("HomeViewModel", "Tesi '${exam.corso}' - CFA: '${exam.cfa}', Completata: ${isCompleted(exam)}")
        }
        val thesisCFA = thesis.firstOrNull()?.cfa?.toIntOrNull() ?: 0
        val thesisCompleted = thesis.any { isCompleted(it) }
        val thesisEarned = if (isGraduated || thesisCompleted) thesisCFA else 0
        Log.d("HomeViewModel", "Tesi CFA - Dichiarati: $thesisCFA, Completata: $thesisCompleted, Acquisiti: $thesisEarned")
        
        val totalCFA = examsEarned + activitiesEarned + thesisEarned
        Log.d("HomeViewModel", "CFA calculation - Exams: $examsEarned, Activities: $activitiesEarned, Thesis: $thesisEarned, Total: $totalCFA")
        
        return totalCFA
    }
    
    private fun calculateYearProgress(exams: List<Esame>, currentYear: Int?): YearProgress {
        @Suppress("UNUSED_PARAMETER")
        val year1Exams = exams.filter { !isAttivitaOTesi(it) && it.anno == "1" }
        val year2Exams = exams.filter { !isAttivitaOTesi(it) && it.anno == "2" }
        val year3Exams = exams.filter { !isAttivitaOTesi(it) && it.anno == "3" }
        
        val year1Progress = calculateYearProgressForExams(year1Exams)
        val year2Progress = calculateYearProgressForExams(year2Exams)
        val year3Progress = calculateYearProgressForExams(year3Exams)
        
        // Calculate missing exams for each year
        val year1Missing = year1Exams.count { it.voto?.isEmpty() ?: true }
        val year2Missing = year2Exams.count { it.voto?.isEmpty() ?: true }
        val year3Missing = year3Exams.count { it.voto?.isEmpty() ?: true }
        
        return YearProgress(
            year1 = year1Progress,
            year1Total = year1Exams.size,
            year1Missing = year1Missing,
            year2 = year2Progress,
            year2Total = year2Exams.size,
            year2Missing = year2Missing,
            year3 = year3Progress,
            year3Total = year3Exams.size,
            year3Missing = year3Missing
        )
    }
    
    private fun calculateYearProgressForExams(exams: List<Esame>): Double {
        if (exams.isEmpty()) return 0.0
        val total = exams.size
        val passed = exams.count { !(it.voto?.isEmpty() ?: true) }
        return 1.0 - (max(0, total - passed).toDouble() / total.toDouble())
    }
    
    private fun calculateCareerAverage(exams: List<Esame>): Double? {
        // EXACT SAME LOGIC as GradeTrendViewModel.processExams (righe 52-75)
        val filteredExams = exams
            .filter { exam ->
                // Escludi tesi & affini + attività integrative
                val titleNorm = exam.corso.lowercase()
                !titleNorm.contains("tesi") && 
                !titleNorm.contains("prova finale") && 
                !titleNorm.contains("elaborato finale") && 
                !titleNorm.contains("attivit")
            }
            .filter { exam ->
                // Solo voti numerici validi (stesso check di GradeTrendViewModel)
                exam.data != null && parseVote(exam.voto) != null
            }
        
        val marks = filteredExams.mapNotNull { parseVote(it.voto) }
        
        return if (marks.isNotEmpty()) {
            marks.sum().toDouble() / marks.size
        } else {
            null
        }
    }
    
    /**
     * Stessa funzione di GradeTrendViewModel.parseVote (righe 126-158)
     * Gestisce: idoneo, lode, 18/30, ecc.
     */
    private fun parseVote(voto: String?): Int? {
        val vote = voto?.lowercase() ?: return null
        
        if (vote.contains("idoneo") || vote.contains("idonea") || vote.contains("idoneità")) {
            return null
        }
        
        if (vote.contains("lode")) {
            return 30
        }
        
        val digits = vote.filter { it.isDigit() }
        if (digits.isNotEmpty()) {
            val number = digits.toIntOrNull()
            if (number != null && number in 18..30) {
                return number
            }
        }
        
        // Try to parse "18/30" format
        val parts = vote.split("/")
        if (parts.size >= 2) {
            val firstPart = parts[0].filter { it.isDigit() }
            if (firstPart.isNotEmpty()) {
                val number = firstPart.toIntOrNull()
                if (number != null && number in 18..30) {
                    return number
                }
            }
        }
        
        return null
    }
    
    private fun getItalianOrdinalYear(year: Int): String {
        return when (year) {
            1 -> "1º"
            2 -> "2º"
            3 -> "3º"
            else -> "${year}º"
        }
    }
    
    private fun getCourseDisplayInfo(pianoStudi: String): String {
        val ps = pianoStudi.lowercase()
        return when {
            ps.contains("fashion") -> "Fashion Design"
            ps.contains("interior") || ps.contains("interni") -> "Interior Design"
            ps.contains("architettura") && !ps.contains("interior") && !ps.contains("interni") -> "Architettura degli Interni"
            ps.contains("cinema") || ps.contains("audiovisiv") -> "Cinema"
            ps.contains("regia") || ps.contains("videomaking") -> "Regia"
            ps.contains("graphic") || ps.contains("grafica") || ps.contains("multimedia") -> "Graphic Design"
            ps.contains("fotografia") || ps.contains("photo") -> "Fotografia"
            ps.contains("pittura") || ps.contains("painting") -> "Pittura"
            ps.contains("product") -> "Product Design"
            ps.contains("communication") -> "Communication Design"
            ps.contains("web") || ps.contains("digital") -> "Digital Design"
            ps.contains("design") -> "Design"
            else -> ""
        }
    }
    
    // MARK: - Lessons Filtering
    

    
    private fun getCourseIdFromExam(exam: Esame): String? {
        // Prova a trovare oidCorso tramite reflection (come in iOS)
        return exam.corso.lowercase().filter { it.isLetterOrDigit() }
    }
    
    private fun formatTimeRange(start: Date, end: Date): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.ITALIAN)
        return "${timeFormat.format(start)} - ${timeFormat.format(end)}"
    }
    
    private fun formatDate(date: Date): String {
        val calendar = java.util.Calendar.getInstance()
        val today = java.util.Calendar.getInstance()
        
        calendar.time = date
        
        return when {
            calendar.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
            calendar.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR) -> "Oggi"
            
            calendar.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
            calendar.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR) + 1 -> "Domani"
            
            else -> {
                val dateFormat = SimpleDateFormat("EEEE dd MMMM", Locale.ITALIAN)
                dateFormat.format(date)
            }
        }
    }
    
    // MARK: - Booked Exams Logic (identica a iOS)
    
    /**
     * Calcola gli esami prenotati (identico a iOS bookedExams computed property)
     * Mostra solo esami con dataRichiesta != null e senza voto, escludendo attività e tesi
     */
    private fun calculateBookedExams(exams: List<Esame>, profile: StudentProfile?): List<Esame> {
        val isGraduated = profile?.status?.lowercase()?.contains("laureat") == true
        val currentYear = profile?.currentYear?.toIntOrNull()
        
        // Nascondi se laureato o fuoricorso (come iOS)
        if (isGraduated || currentYear == null) {
            return emptyList()
        }
        
        return exams.filter { exam ->
            // Deve avere dataRichiesta
            exam.dataRichiesta != null &&
            // Non deve avere voto (non ancora verbalizzato)
            (exam.voto == null || exam.voto.isEmpty()) &&
            // Escludi attività e tesi
            !isAttivitaOTesi(exam)
        }.sortedBy { exam ->
            // Ordina per dataRichiesta (più vecchi primi)
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                exam.dataRichiesta?.let { dateFormat.parse(it)?.time } ?: Long.MAX_VALUE
            } catch (e: Exception) {
                Long.MAX_VALUE
            }
        }
    }
    
    /**
     * Verifica se l'utente ha completato tutti gli esami per la laurea (identico a iOS hasCompletedAllGraduationExams)
     */
    fun hasCompletedAllGraduationExams(): Boolean {
        val allExams = sessionRepository.allExams.value
        if (allExams.isEmpty()) return false
        
        // Verifica se ci sono esami "pending for graduation" (come iOS)
        return !allExams.any { exam ->
            isPendingForGraduation(exam)
        }
    }
    
    /**
     * Verifica se un esame è "pending for graduation" (identico a iOS isPendingForGraduation)
     */
    private fun isPendingForGraduation(exam: Esame): Boolean {
        val loweredName = exam.corso.lowercase()
        
        // Escludi tesi e prova finale
        if (loweredName.contains("tesi") || loweredName.contains("prova finale")) {
            return false
        }
        
        // Escludi attività, workshop, seminari
        if (loweredName.contains("attivit") || loweredName.contains("workshop") || loweredName.contains("seminar")) {
            return false
        }
        
        // Se è richiedibile (non prenotato), è pending
        if (exam.richiedibile == true) {
            return true
        }
        
        // Se ha voto, non è pending
        val trimmedVote = (exam.voto ?: "").trim()
        if (trimmedVote.isNotEmpty()) {
            return false
        }
        
        // Se è idoneità, non è pending
        if (isIdoneitaVote(exam.voto)) {
            return false
        }
        
        // Se è sostenuto (ha data), non è pending
        if (exam.data != null) {
            return false
        }
        
        // Altrimenti è pending
        return true
    }
    
    private fun isIdoneitaVote(voto: String?): Boolean {
        val v = voto?.lowercase() ?: return false
        return v.contains("idoneo") || v.contains("idonea") || v.contains("idoneità")
    }
    
    /**
     * Finestre di sessione d'esame: la sezione esami in Home è visibile solo in questi periodi.
     * Allineato a iOS examSessionWindows().
     */
    private fun examSessionWindows(): List<Pair<LocalDate, LocalDate>> {
        return listOf(
            Pair(LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 20)),
            Pair(LocalDate.of(2026, 6, 22), LocalDate.of(2026, 6, 30)),
            Pair(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 10))
            // Aggiungere altre sessioni qui
        )
    }
    
    /**
     * True se oggi (in Europa/Roma) ricade in una delle finestre di sessione d'esame.
     */
    fun isTodayInExamSession(): Boolean {
        val today = LocalDate.now(ZoneId.of("Europe/Rome"))
        for ((start, end) in examSessionWindows()) {
            if (!today.isBefore(start) && !today.isAfter(end)) return true
        }
        return false
    }
    
    /**
     * Verifica se mostrare la sezione esami prenotati (identica a iOS shouldShowBookedExams)
     */
    fun shouldShowBookedExams(profile: StudentProfile?, exams: List<Esame>): Boolean {
        // Verifica se l'utente ha disabilitato la sezione nelle impostazioni
        // Default è true se la chiave non esiste ancora
        val sharedPrefs = context.getSharedPreferences("LABA_PREFS", Context.MODE_PRIVATE)
        val isEnabled = if (!sharedPrefs.contains("laba.bookedExams.enabled")) {
            true // Default true se non esiste
        } else {
            sharedPrefs.getBoolean("laba.bookedExams.enabled", true)
        }
        
        if (!isEnabled) return false
        
        // Nascondi se laureato o fuoricorso (come iOS)
        val isGraduated = profile?.status?.lowercase()?.contains("laureat") == true
        val currentYear = profile?.currentYear?.toIntOrNull()
        if (isGraduated || currentYear == null) return false
        
        // Mostra solo se ci sono esami prenotati (con dataRichiesta) senza voto
        val bookedExams = calculateBookedExams(exams, profile)
        return bookedExams.isNotEmpty()
    }
}

data class HomeUiState(
    val displayName: String? = null,
    val statusPills: List<String> = emptyList(),
    val isGraduated: Boolean = false,
    val passedExamsCount: Int = 0,
    val missingExamsCount: Int = 0,
    val totalExamsCount: Int = 0,
    val cfaEarned: Int = 0,
    val cfaTarget: Int = 0,
    val yearProgress: YearProgress? = null,
    val careerAverage: Double? = null,
    val lessonsToday: List<LessonUi> = emptyList(),
    val upcomingExamsCount: Int = 0,
    val bookedExams: List<Esame> = emptyList(), // Esami prenotati
    val isLoading: Boolean = false
)