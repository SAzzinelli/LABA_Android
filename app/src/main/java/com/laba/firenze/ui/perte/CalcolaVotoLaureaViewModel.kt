package com.laba.firenze.ui.perte

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.repository.SessionRepository
import com.laba.firenze.domain.model.Esame
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.floor

@HiltViewModel
class CalcolaVotoLaureaViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CalcolaVotoLaureaUiState())
    val uiState: StateFlow<CalcolaVotoLaureaUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            sessionRepository.allExams.collect { exams ->
                val suggested = suggestedAverageFromApp(exams)
                _uiState.value = _uiState.value.copy(
                    suggestedAverage = suggested,
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * Calcola la media suggerita dall'app (identico a iOS)
     */
    private fun suggestedAverageFromApp(exams: List<Esame>): Double? {
        val numericGrades = exams.mapNotNull { exam ->
            val lowered = exam.corso.lowercase()
            // Escludi attività e tesi
            if (lowered.contains("attivit") || lowered.contains("tesi")) return@mapNotNull null
            
            val voto = exam.voto
            if (voto.isNullOrEmpty()) return@mapNotNull null
            
            // Rimuovi "e lode" e prendi il primo numero
            val cleaned = voto.replace(" e lode", "")
            val first = cleaned.split("/").firstOrNull()?.trim()
            return@mapNotNull first?.toIntOrNull()
        }
        
        if (numericGrades.isEmpty()) return null
        
        val sum = numericGrades.sum()
        return sum.toDouble() / numericGrades.size
    }
    
    /**
     * Converte media da /30 a /110 (identico a iOS)
     */
    fun computeScaledTo110(avg30: Double): Pair<Double, Int> {
        val raw = avg30 * (110.0 / 30.0)
        val fraction = raw - floor(raw)
        val official = if (fraction > 0.50) ceil(raw).toInt() else floor(raw).toInt()
        return Pair(raw, official)
    }
}

data class CalcolaVotoLaureaUiState(
    val isLoading: Boolean = false,
    val suggestedAverage: Double? = null,
    val error: String? = null
)