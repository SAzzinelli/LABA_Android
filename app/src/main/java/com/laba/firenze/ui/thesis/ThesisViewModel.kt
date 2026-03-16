package com.laba.firenze.ui.thesis

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.repository.SessionRepository
import com.laba.firenze.domain.model.Esame
import com.laba.firenze.domain.model.StudentProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThesisViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ThesisUiState())
    val uiState: StateFlow<ThesisUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Carica esami e profilo per calcolare statistiche
                val exams = sessionRepository.allExams.value
                val profile = sessionRepository.getUserProfile()
                
                // Determina se è biennio o triennio
                val isBiennio = isBiennioLevel(profile)
                val minPages = if (isBiennio) 90 else 80
                
                // Calcola stato esami
                val (examsStatus, allCompleted) = calculateExamsStatus(exams)
                
                // Verifica se lo studente può laurearsi
                val canGraduate = canStudentGraduate(profile, exams)
                
                // Calcola voto di presentazione solo se può laurearsi
                val presentationGrade = if (canGraduate) {
                    calculatePresentationGrade(exams)
                } else {
                    "—"
                }
                
                // Carica documenti dall'API
                val documents = loadThesisDocuments()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    documents = documents,
                    examsCompletedStatus = examsStatus,
                    allExamsCompleted = allCompleted,
                    presentationGrade = presentationGrade,
                    minPages = minPages,
                    canGraduate = canGraduate
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun isBiennioLevel(profile: StudentProfile?): Boolean {
        if (profile == null) return false
        val ps = profile.pianoStudi?.lowercase() ?: ""
        val matricola = profile.matricola?.lowercase() ?: ""
        if (ps.contains("biennio") || ps.contains("ii livello") || ps.contains("2° livello") || ps.contains("secondo livello")) return true
        if (ps.contains("interior") || ps.contains("cinema") || ps.contains("audiovisiv")) return true
        if (matricola.contains("biennio") && !matricola.contains("triennio")) return true
        return false
    }

    /**
     * Verifica se lo studente può laurearsi basandosi su:
     * 1. Anno di corso (deve essere al 3° anno per triennio o 2° anno per biennio)
     * 2. Esami completati (deve aver completato almeno l'80% degli esami)
     */
    private fun canStudentGraduate(profile: StudentProfile?, exams: List<Esame>): Boolean {
        // Se non abbiamo informazioni sufficienti, non mostrare voto
        if (profile == null || exams.isEmpty()) {
            return false
        }

        // Verifica anno di corso
        val currentYear = profile.currentYear?.toIntOrNull() ?: 0
        val isBiennio = isBiennioLevel(profile)
        
        val requiredYear = if (isBiennio) 2 else 3
        if (currentYear < requiredYear) {
            return false
        }

        // Verifica esami completati
        val coreExams = exams.filter { exam ->
            val title = exam.corso.lowercase()
            !title.contains("attivit") && !title.contains("tesi")
        }

        if (coreExams.isEmpty()) {
            return false
        }

        val completedExams = coreExams.filter { exam ->
            val voto = exam.voto?.trim() ?: ""
            voto.isNotEmpty() || exam.data != null
        }

        // Deve aver completato almeno l'80% degli esami
        val completionPercentage = (completedExams.size.toDouble() / coreExams.size) * 100
        return completionPercentage >= 80.0
    }

    private fun calculateExamsStatus(exams: List<Esame>): Pair<String, Boolean> {
        if (exams.isEmpty()) {
            return "Da verificare" to false
        }

        // Considera solo esami di corso, escludendo attività e tesi
        val coreExams = exams.filter { exam ->
            val title = exam.corso.lowercase()
            !title.contains("attivit") && !title.contains("tesi")
        }

        if (coreExams.isEmpty()) {
            return "Da verificare" to false
        }

        // Completato = voto presente (numerico o lode) oppure data sostenimento
        val completedExams = coreExams.filter { exam ->
            val voto = exam.voto?.trim() ?: ""
            voto.isNotEmpty() || exam.data != null
        }

        return when {
            completedExams.size == coreExams.size -> "Terminati" to true
            completedExams.isEmpty() -> "Da terminare" to false
            else -> "In corso" to false
        }
    }

    private fun calculatePresentationGrade(exams: List<Esame>): String {
        // Media ARITMETICA dei soli esami di corso (no tesi, no attività integrative), poi conversione su 110
        val grades = exams.mapNotNull { exam ->
            val title = exam.corso.lowercase()
            if (title.contains("attivit") || title.contains("tesi")) {
                return@mapNotNull null
            }
            parseVoteForThesis(exam.voto)
        }

        return if (grades.isEmpty()) {
            "—"
        } else {
            val avg = grades.sum().toDouble() / grades.size
            val presentation = ((avg / 30.0) * 110.0).toInt()
            presentation.toString()
        }
    }

    private fun parseVoteForThesis(voto: String?): Int? {
        if (voto.isNullOrEmpty()) return null
        
        val trimmed = voto.trim().uppercase()
        
        return when {
            trimmed.contains("LODE") -> 30
            trimmed.matches(Regex("\\d+")) -> {
                val grade = trimmed.toIntOrNull()
                if (grade != null && grade in 18..30) grade else null
            }
            else -> null
        }
    }

    private suspend fun loadThesisDocuments(): List<ThesisDocument> {
        return try {
            sessionRepository.getThesisDocuments()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun loadMockDocuments(): List<ThesisDocument> {
        // Mock documents come fallback
        return listOf(
            ThesisDocument(
                id = "regolamento",
                title = "Regolamento tesi",
                type = "PDF",
                icon = Icons.Default.Book,
                url = "https://www.laba.biz/regolamento-tesi.pdf"
            ),
            ThesisDocument(
                id = "domanda",
                title = "Domanda di tesi",
                type = "DOCX",
                icon = Icons.Default.Description,
                url = "https://www.laba.biz/domanda-tesi.docx"
            ),
            ThesisDocument(
                id = "frontespizio",
                title = "Modello frontespizio",
                type = "DOCX",
                icon = Icons.Default.TextFields,
                url = "https://www.laba.biz/frontespizio.docx"
            )
        )
    }

    fun refresh() {
        loadData()
    }
    
    
    suspend fun downloadDocument(allegatoOid: String): ByteArray? {
        return try {
            sessionRepository.downloadDocument(allegatoOid)
        } catch (e: Exception) {
            null
        }
    }
}
