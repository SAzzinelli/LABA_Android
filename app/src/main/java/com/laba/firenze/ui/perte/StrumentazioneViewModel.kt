package com.laba.firenze.ui.perte

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.api.GestionaleUser
import com.laba.firenze.data.repository.GestionaleRepository
import com.laba.firenze.domain.model.CreateEquipmentReport
import com.laba.firenze.domain.model.EquipmentLoan
import com.laba.firenze.domain.model.EquipmentReport
import com.laba.firenze.domain.model.EquipmentRequest
import com.laba.firenze.domain.model.UserEquipment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import javax.inject.Inject

/** ViewModel Service LABA (Gestionale) - identico a iOS LABAGestionaleDashboardView + EquipmentCatalogView. */
@HiltViewModel
class StrumentazioneViewModel @Inject constructor(
    private val gestionaleRepository: GestionaleRepository
) : ViewModel() {

    val token: StateFlow<String> = gestionaleRepository.token
    val user: StateFlow<GestionaleUser?> = gestionaleRepository.user

    private val _uiState = MutableStateFlow(StrumentazioneUiState())
    val uiState: StateFlow<StrumentazioneUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (gestionaleRepository.isAuthenticated) {
                gestionaleRepository.getCurrentUser()
                    .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
                refreshData()
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            gestionaleRepository.login(email, password)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                    refreshData()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Errore di accesso"
                    )
                }
        }
    }

    fun logout() {
        gestionaleRepository.logout()
        _uiState.value = _uiState.value.copy(
            equipment = emptyList(),
            requests = emptyList(),
            loans = emptyList(),
            reports = emptyList(),
            error = null
        )
    }

    fun refreshData() {
        viewModelScope.launch {
            if (!gestionaleRepository.isAuthenticated) return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)
            val equipmentDeferred = async { gestionaleRepository.getAvailableEquipment() }
            val requestsDeferred = async { gestionaleRepository.getUserRequests() }
            val loansDeferred = async { gestionaleRepository.getUserLoans() }
            val reportsDeferred = async { gestionaleRepository.getUserReports() }
            val equipment = equipmentDeferred.await().getOrNull() ?: emptyList()
            val requests = requestsDeferred.await().getOrNull() ?: emptyList()
            val loans = loansDeferred.await().getOrNull() ?: emptyList()
            val reports = reportsDeferred.await().getOrNull() ?: emptyList()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                equipment = equipment,
                requests = requests,
                loans = loans,
                reports = reports
            )
        }
    }

    fun refreshUser() {
        viewModelScope.launch {
            if (gestionaleRepository.isAuthenticated) {
                gestionaleRepository.getCurrentUser()
                    .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun openCreateReport(loan: EquipmentLoan) {
        _uiState.value = _uiState.value.copy(createReportLoan = loan)
    }

    fun dismissCreateReport() {
        _uiState.value = _uiState.value.copy(createReportLoan = null)
    }

    fun submitReport(report: CreateEquipmentReport) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingReport = true)
            gestionaleRepository.createReport(report)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        createReportLoan = null,
                        isSubmittingReport = false
                    )
                    refreshData()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isSubmittingReport = false,
                        error = it.message ?: "Errore invio segnalazione"
                    )
                }
        }
    }
}

data class StrumentazioneUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val equipment: List<UserEquipment> = emptyList(),
    val requests: List<EquipmentRequest> = emptyList(),
    val loans: List<EquipmentLoan> = emptyList(),
    val reports: List<EquipmentReport> = emptyList(),
    val createReportLoan: EquipmentLoan? = null,
    val isSubmittingReport: Boolean = false
)
