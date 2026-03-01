package com.laba.firenze.ui.perte

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.repository.SessionRepository
import com.laba.firenze.data.repository.SuperSaasRepository
import com.laba.firenze.domain.model.CreateSuperSaasAppointment
import com.laba.firenze.domain.model.SuperSaasAppointment
import com.laba.firenze.domain.model.SuperSaasAvailabilitySlot
import com.laba.firenze.domain.model.SuperSaasRoom
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class PrenotazioneAuleViewModel @Inject constructor(
    private val superSaasRepository: SuperSaasRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    val isAuthenticated = superSaasRepository.isAuthenticated
    val user = superSaasRepository.user
    val token = superSaasRepository.token

    private val _uiState = MutableStateFlow(PrenotazioneAuleUiState())
    val uiState: StateFlow<PrenotazioneAuleUiState> = _uiState.asStateFlow()

    val labaEmail: String?
        get() = sessionRepository.getUserProfile()?.emailLABA?.replace("labafifrenze", "labafirenze")

    init {
        viewModelScope.launch {
            combine(
                superSaasRepository.user,
                superSaasRepository.token
            ) { u, t -> u to t }.collect { (u, t) ->
                if (t.isNotEmpty() && u != null) {
                    loadUserAppointments()
                    loadRooms()
                } else {
                    _uiState.value = _uiState.value.copy(userAppointments = emptyList())
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            superSaasRepository.login(email, password, labaEmail)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                    loadUserAppointments()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Email o password errate"
                    )
                }
        }
    }

    fun logout() {
        superSaasRepository.logout()
        _uiState.value = _uiState.value.copy(userAppointments = emptyList(), error = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun loadUserAppointments() {
        viewModelScope.launch {
            val u = superSaasRepository.user.value ?: return@launch
            superSaasRepository.getUserAppointments(u.email, u.id)
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(userAppointments = list)
                }
        }
    }

    fun loadRooms() {
        viewModelScope.launch {
            val rooms = superSaasRepository.getAvailableRooms()
            _uiState.value = _uiState.value.copy(rooms = rooms)
        }
    }

    fun loadSlots(room: SuperSaasRoom, date: Date) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(selectedRoomSlotsLoading = true, selectedRoomSlots = null)
            superSaasRepository.getAvailableSlots(room.numericId, date)
                .onSuccess { slots ->
                    _uiState.value = _uiState.value.copy(
                        selectedRoomSlots = slots,
                        selectedRoomSlotsLoading = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        selectedRoomSlotsLoading = false,
                        selectedRoomSlots = emptyList(),
                        error = it.message
                    )
                }
        }
    }

    fun createBooking(room: SuperSaasRoom, slot: SuperSaasAvailabilitySlot) {
        viewModelScope.launch {
            val u = superSaasRepository.user.value ?: return@launch
            _uiState.value = _uiState.value.copy(isCreatingBooking = true, error = null)
            val appointment = CreateSuperSaasAppointment(
                schedule_id = room.numericId,
                name = if (u.name.contains("@")) "Utente" else u.name,
                email = u.email,
                phone = u.phone ?: "",
                matricola = u.matricola ?: "",
                start = slot.start,
                finish = slot.finish,
                description = "Prenotazione da app Android LABA"
            )
            superSaasRepository.createAppointment(appointment)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isCreatingBooking = false,
                        lastCreatedBooking = it,
                        showBookingSuccess = true
                    )
                    loadUserAppointments()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isCreatingBooking = false,
                        error = it.message
                    )
                }
        }
    }

    fun deleteAppointment(appointment: SuperSaasAppointment) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeletingBooking = true, error = null)
            superSaasRepository.deleteAppointment(appointment.id, appointment.schedule_id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isDeletingBooking = false)
                    loadUserAppointments()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isDeletingBooking = false,
                        error = it.message
                    )
                }
        }
    }

    fun dismissBookingSuccess() {
        _uiState.value = _uiState.value.copy(showBookingSuccess = false, lastCreatedBooking = null)
    }

    fun clearSlotsSelection() {
        _uiState.value = _uiState.value.copy(
            selectedRoomSlots = null,
            selectedRoomSlotsLoading = false
        )
    }
}

data class PrenotazioneAuleUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val rooms: List<SuperSaasRoom> = emptyList(),
    val userAppointments: List<SuperSaasAppointment> = emptyList(),
    val selectedRoomSlots: List<SuperSaasAvailabilitySlot>? = null,
    val selectedRoomSlotsLoading: Boolean = false,
    val isCreatingBooking: Boolean = false,
    val isDeletingBooking: Boolean = false,
    val showBookingSuccess: Boolean = false,
    val lastCreatedBooking: SuperSaasAppointment? = null,
    val superSaasUrl: String? = "https://prenotazioni.laba.biz"
)
