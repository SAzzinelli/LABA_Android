package com.laba.firenze.ui.seminars

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.repository.SessionRepository
import com.laba.firenze.domain.model.Esame
import com.laba.firenze.domain.model.Seminario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Tab della sezione Attività a scelta (come iOS AttivitaSceltaTab). */
enum class AttivitaSceltaTab {
    SEMINARI,
    ATTIVITA_INTEGRATIVE
}

@HiltViewModel
class SeminarsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val achievementManager: com.laba.firenze.data.gamification.AchievementManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SeminarsUiState())
    val uiState: StateFlow<SeminarsUiState> = _uiState.asStateFlow()
    
    init {
        loadSeminars()
        loadThesisExams()
    }
    
    private fun loadThesisExams() {
        viewModelScope.launch {
            sessionRepository.allExams.collect { exams ->
                val thesis = exams.filter { it.corso.uppercase().contains("TESI FINALE") }
                val state = _uiState.value
                val queryAltro = state.searchQueryAttivitaIntegrative
                _uiState.value = state.copy(
                    thesisExams = thesis,
                    filteredThesisExams = filterThesisByQuery(thesis, queryAltro)
                )
            }
        }
    }
    
    fun trackSectionVisit(section: String) {
        achievementManager.trackSectionVisit(section)
    }
    
    private fun loadSeminars() {
        viewModelScope.launch {
            sessionRepository.seminars.collect { seminars ->
                val state = _uiState.value
                val filtered = applyFilterAndSearch(seminars, state.filter, state.searchQuery)
                _uiState.value = state.copy(
                    allSeminars = seminars,
                    seminars = filtered
                )
            }
        }
    }
    
    private fun filterThesisByQuery(thesis: List<Esame>, query: String): List<Esame> {
        if (query.isBlank()) return thesis
        val q = query.lowercase()
        return thesis.filter { prettifyTitle(it.corso).lowercase().contains(q) }
    }
    
    fun setSelectedTab(tab: AttivitaSceltaTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }
    
    fun updateSearchQueryAttivitaIntegrative(query: String) {
        val state = _uiState.value
        _uiState.value = state.copy(
            searchQueryAttivitaIntegrative = query,
            filteredThesisExams = filterThesisByQuery(state.thesisExams, query)
        )
    }
    
    fun updateSearchQuery(query: String) {
        val state = _uiState.value
        _uiState.value = state.copy(
            searchQuery = query,
            seminars = applyFilterAndSearch(state.allSeminars, state.filter, query)
        )
    }
    
    fun setFilter(filter: SeminariFilter) {
        val state = _uiState.value
        _uiState.value = state.copy(
            filter = filter,
            seminars = applyFilterAndSearch(state.allSeminars, filter, state.searchQuery)
        )
    }
    
    private fun applyFilterAndSearch(
        seminars: List<Seminario>,
        filter: SeminariFilter,
        searchQuery: String
    ): List<Seminario> {
        val base = when (filter) {
            SeminariFilter.TUTTI -> seminars
            SeminariFilter.PRENOTABILI -> seminars.filter { it.richiedibile }
            SeminariFilter.FREQUENTATI -> seminars.filter { it.partecipato }
        }
        return if (searchQuery.isBlank()) base else {
            val q = searchQuery.lowercase()
            base.filter {
                seminarTitle(it.titolo).lowercase().contains(q) ||
                prettifyTitle(seminarTitle(it.titolo)).lowercase().contains(q) ||
                it.docente?.lowercase()?.contains(q) == true
            }
        }
    }
    
    fun sendMissingSeminarEmail(seminarName: String, context: android.content.Context) {
        val profile = sessionRepository.getUserProfile()
        val nomeCompleto = profile?.displayName ?: "Nome Cognome"
        val corsoFormattato = profile?.pianoStudi?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } ?: "Corso"
        val anno = profile?.currentYear?.let { "$it° anno" } ?: "anno accademico"
        val cellulareContact = profile?.cellulare ?: profile?.telefono ?: "non disponibile"
        
        val subject = "Seminario frequentato non presente in APP"
        val body = """
        Buongiorno,
        
        sono $nomeCompleto, frequento il corso di $corsoFormattato, sono al $anno e non trovo il seminario "$seminarName".
        
        Cordiali saluti,
        $nomeCompleto
        
        ---
        Inviato pre-compilato dall'applicazione LABA. Contatta lo studente al numero $cellulareContact per ulteriori chiarimenti.
        """
        
        val intent = android.content.Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("info@laba.biz"))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        
        context.startActivity(Intent.createChooser(intent, "Invia email"))
    }
    
    
    fun refreshSeminars() {
        viewModelScope.launch {
            sessionRepository.loadSeminars()
        }
    }
    
    fun getSeminarById(seminarId: String): Seminario? {
        return _uiState.value.allSeminars.find { it.oid == seminarId }
    }
    
    /** CFA senza zero iniziale (08 → 8) per UX, come iOS. */
    fun formatCFA(raw: String?): String? {
        val s = raw?.trim() ?: return null
        if (s.isEmpty()) return null
        return s.toIntOrNull()?.toString() ?: s
    }
    
    /** True se tesi superata (voto valorizzato, data sostenuto, o idoneo). */
    fun isTesiSuperata(e: Esame): Boolean {
        val v = (e.voto ?: "").trim()
        if (v.isNotEmpty()) return true
        if (e.data != null) return true
        val low = v.lowercase()
        return low.contains("idoneo") || low.contains("idonea") || low.contains("idoneità")
    }
}

enum class SeminariFilter(val label: String) {
    TUTTI("Tutti"),
    PRENOTABILI("Prenotabili"),
    FREQUENTATI("Frequentati")
}

data class SeminarsUiState(
    val selectedTab: AttivitaSceltaTab = AttivitaSceltaTab.SEMINARI,
    val allSeminars: List<Seminario> = emptyList(),
    val seminars: List<Seminario> = emptyList(),
    val searchQuery: String = "",
    val searchQueryAttivitaIntegrative: String = "",
    val filter: SeminariFilter = SeminariFilter.TUTTI,
    val thesisExams: List<Esame> = emptyList(),
    val filteredThesisExams: List<Esame> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
