package com.laba.firenze.ui.seminars

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.repository.SessionRepository
import com.laba.firenze.domain.model.Seminario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeminarsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SeminarsUiState())
    val uiState: StateFlow<SeminarsUiState> = _uiState.asStateFlow()
    
    init {
        loadSeminars()
    }
    
    private fun loadSeminars() {
        viewModelScope.launch {
            sessionRepository.seminars.collect { seminars ->
                _uiState.value = _uiState.value.copy(
                    allSeminars = seminars,
                    seminars = filterSeminars(seminars, _uiState.value.searchQuery)
                )
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            searchQuery = query,
            seminars = filterSeminars(currentState.allSeminars, query)
        )
    }
    
    fun setSelectedTab(tab: SeminariTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }
    
    fun sendMissingSeminarEmail(seminarName: String, context: android.content.Context) {
        val profile = sessionRepository.getUserProfile()
        val nomeCompleto = profile?.displayName ?: "Nome Cognome"
        val corsoFormattato = profile?.pianoStudi?.capitalize() ?: "Corso"
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
    
    private fun filterSeminars(seminars: List<Seminario>, searchQuery: String): List<Seminario> {
        if (searchQuery.isBlank()) return seminars
        
        return seminars.filter { seminar ->
            seminar.titolo.contains(searchQuery, ignoreCase = true) ||
            seminar.docente?.contains(searchQuery, ignoreCase = true) == true
        }
    }
    
    fun refreshSeminars() {
        viewModelScope.launch {
            sessionRepository.loadSeminars()
        }
    }
}

enum class SeminariTab(val label: String) {
    RICHIESTE("Richieste"),
    FREQUENTATI("Frequentati")
}

data class SeminarsUiState(
    val allSeminars: List<Seminario> = emptyList(),
    val seminars: List<Seminario> = emptyList(),
    val searchQuery: String = "",
    val selectedTab: SeminariTab = SeminariTab.RICHIESTE,
    val isLoading: Boolean = false,
    val error: String? = null
)
