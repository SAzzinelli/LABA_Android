package com.laba.firenze.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.repository.SessionRepository
import com.laba.firenze.domain.model.Esame
import com.laba.firenze.domain.model.StudentProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import java.time.Clock
import java.time.LocalDate
import java.time.Month
import java.time.Year
import java.time.ZoneId

data class HeroInfo(
    val displayName: String,
    val academicYear: String,     // es. "2025/26"
    val courseName: String,       // es. "Graphic Design & Multimedia"
    val studyYear: Int?           // es. 2 (null se non noto)
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    fun refreshOnAppear() {
        viewModelScope.launch {
            // Carica tutti i dati e aspetta il completamento
            sessionRepository.loadAll()
            // Forza il refresh dei dati
            loadData()
        }
    }
    
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
        return if (now.monthValue >= startMonth) "${y}/${(y + 1).toString().takeLast(2)}"
               else "${y - 1}/${y.toString().takeLast(2)}"
    }
    
    /**
     * Stima l'anno di corso basato sui dati disponibili
     */
    private fun estimateStudyYear(profile: StudentProfile?): Int? {
        // Cerca l'anno nel profilo
        profile?.currentYear?.let { anno ->
            return anno.toIntOrNull()
        }
        
        // Fallback: usa l'anno corrente come base
        val now = LocalDate.now(ZoneId.of("Europe/Rome"))
        @Suppress("UNUSED_VARIABLE")
        val academicStart = LocalDate.of(if (now.monthValue >= 9) now.year else now.year - 1, 9, 1)
        
        // Per ora restituiamo un anno di default, in futuro si può calcolare dal profilo
        return 2 // Default al 2º anno
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
        val academicYear = currentAcademicYearString()
        val courseName = getCompactCourseName(profile)
        val studyYear = estimateStudyYear(profile)
        
        return HeroInfo(
            displayName = profile?.nome ?: "Studente", // Solo nome, no cognome
            academicYear = academicYear,
            courseName = courseName,
            studyYear = studyYear
        )
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
                
                println("🏠 HomeViewModel: Loading data - Profile: ${profile != null}, Exams: ${exams.size}, Seminars: ${seminars.size}, Loading: $isLoading")
                println("🏠 HomeViewModel: Profile details: ${profile?.displayName} - ${profile?.currentYear} - ${profile?.pianoStudi}")
                exams.take(3).forEachIndexed { index, exam ->
                    println("🏠 HomeViewModel: Exam $index: ${exam.corso} - ${exam.voto} - ${exam.anno}")
                }
                seminars.take(3).forEachIndexed { index, seminar ->
                    println("🏠 HomeViewModel: Seminar $index: ${seminar.titolo} - ${seminar.esito}")
                }
                
                val displayName = profile?.nome ?: "Studente"
                
                // Check if graduated: either from profile status or thesis completion
                val thesisCompleted = exams.any { 
                    it.corso.contains("TESI", ignoreCase = true) && isCompleted(it) 
                }
                val isGraduated = profile?.status?.lowercase()?.contains("laureat") == true || thesisCompleted
                
                println("🏠 HomeViewModel: Graduation check - Profile status: '${profile?.status}', Thesis completed: $thesisCompleted, IsGraduated: $isGraduated")
                
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
                
                // CFA calculations (exactly like iOS)
                val cfaTarget = calculateCfaTarget(exams)
                val cfaEarned = calculateCfaEarned(exams, isGraduated)
                
                // Year progress calculations
                val yearProgress = calculateYearProgress(exams, profile?.currentYear?.toIntOrNull())
                
                // Career average calculation
                val careerAverage = calculateCareerAverage(exams)
                
                // Lezioni reali (vuote per ora, da implementare con API reali)
                val lessonsToday = emptyList<LessonUi>()
                
                // Esami in arrivo (calcolo basato su esami non sostenuti)
                val upcomingExamsCount = validExams.count { it.voto?.isEmpty() ?: true }
                
                println("🏠 HomeViewModel: Calculated - Passed: $passedExamsCount, Missing: $missingExamsCount, CFA: $cfaEarned, Average: $careerAverage")
                
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
        println("🏠 HomeViewModel: Calculating CFA target from ${exams.size} exams")
        
        // Somma TUTTI gli esami (inclusi workshop, tesi, attività)
        val totalCFA = exams.sumOf { exam ->
            val cfaValue = exam.cfa?.toIntOrNull() ?: 0
            println("🏠 HomeViewModel: Exam '${exam.corso}' - CFA: '${exam.cfa}' -> $cfaValue")
            cfaValue
        }
        
        println("🏠 HomeViewModel: Total CFA target calculated: $totalCFA")
        return totalCFA
    }
    
    private fun calculateCfaEarned(exams: List<Esame>, isGraduated: Boolean): Int {
        println("🏠 HomeViewModel: Calculating CFA from ${exams.size} exams")
        
        // Debug: stampa tutti gli esami con i loro CFA
        exams.forEach { exam ->
            println("🏠 HomeViewModel: Exam '${exam.corso}' - CFA: '${exam.cfa}', Voto: '${exam.voto}', Anno: '${exam.anno}'")
        }
        
        // 1) Esami (escludi Attività e Tesi)
        val validExams = exams.filter { !isAttivitaIntegrativa(it) && !isTesiFinale(it) }
        val completedExams = validExams.filter { isCompleted(it) }
        println("🏠 HomeViewModel: Valid exams: ${validExams.size}, Completed: ${completedExams.size}")
        
        val examsEarned = completedExams.sumOf { exam ->
            val cfaValue = exam.cfa?.toIntOrNull() ?: 0
            println("🏠 HomeViewModel: Completed Exam '${exam.corso}' - CFA: '${exam.cfa}' -> $cfaValue")
            cfaValue
        }
        
        // 2) Attività integrative (max 10 CFA totali)
        val attivita = exams.filter { isAttivitaIntegrativa(it) }
        println("🏠 HomeViewModel: Attività integrative trovate: ${attivita.size}")
        attivita.forEach { exam ->
            println("🏠 HomeViewModel: Attività '${exam.corso}' - CFA: '${exam.cfa}', Completata: ${isCompleted(exam)}")
        }
        val declaredActivitiesCFA = attivita.sumOf { it.cfa?.toIntOrNull() ?: 0 }
        val anyActivityCompleted = attivita.any { isCompleted(it) }
        val activitiesEarned = when {
            isGraduated -> declaredActivitiesCFA // laureato: tutti i CFA dichiarati sono acquisiti
            anyActivityCompleted -> min(10, declaredActivitiesCFA)
            else -> 0
        }
        println("🏠 HomeViewModel: Attività CFA - Dichiarati: $declaredActivitiesCFA, Completate: $anyActivityCompleted, Acquisiti: $activitiesEarned")
        
        // 3) Tesi finale
        val thesis = exams.filter { isTesiFinale(it) }
        println("🏠 HomeViewModel: Tesi trovate: ${thesis.size}")
        thesis.forEach { exam ->
            println("🏠 HomeViewModel: Tesi '${exam.corso}' - CFA: '${exam.cfa}', Completata: ${isCompleted(exam)}")
        }
        val thesisCFA = thesis.firstOrNull()?.cfa?.toIntOrNull() ?: 0
        val thesisCompleted = thesis.any { isCompleted(it) }
        val thesisEarned = if (isGraduated || thesisCompleted) thesisCFA else 0
        println("🏠 HomeViewModel: Tesi CFA - Dichiarati: $thesisCFA, Completata: $thesisCompleted, Acquisiti: $thesisEarned")
        
        val totalCFA = examsEarned + activitiesEarned + thesisEarned
        println("🏠 HomeViewModel: CFA calculation - Exams: $examsEarned, Activities: $activitiesEarned, Thesis: $thesisEarned, Total: $totalCFA")
        
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
        
        return YearProgress(
            year1 = year1Progress,
            year2 = year2Progress,
            year3 = year3Progress
        )
    }
    
    private fun calculateYearProgressForExams(exams: List<Esame>): Double {
        if (exams.isEmpty()) return 0.0
        val total = exams.size
        val passed = exams.count { !(it.voto?.isEmpty() ?: true) }
        return 1.0 - (max(0, total - passed).toDouble() / total.toDouble())
    }
    
    private fun calculateCareerAverage(exams: List<Esame>): Double? {
        val numericExams = exams.filter { !isAttivitaOTesi(it) && hasNumericVote(it) }
        val marks = numericExams.mapNotNull { exam ->
            val rawVote = exam.voto?.replace(" e lode", "") ?: ""
            val numericPart = rawVote.split("/").firstOrNull()?.trim()?.toIntOrNull()
            numericPart
        }
        
        return if (marks.isNotEmpty()) {
            marks.sum().toDouble() / marks.size
        } else {
            null
        }
    }
    
    private fun hasNumericVote(exam: Esame): Boolean {
        val vote = exam.voto ?: ""
        return vote.matches(Regex("""^\s*\d+\s*/\s*\d+"""))
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
            ps.contains("interior") || ps.contains("interni") || ps.contains("architettura") -> "Architettura degli Interni"
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
    val isLoading: Boolean = false
)