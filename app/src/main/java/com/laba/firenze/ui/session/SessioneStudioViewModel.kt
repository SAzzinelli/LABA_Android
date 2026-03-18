package com.laba.firenze.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActiveSession(
    val subjectName: String,
    val totalSeconds: Int,
    val remainingSeconds: Int,
    val isPaused: Boolean
)

@HiltViewModel
class SessioneStudioViewModel @Inject constructor() : ViewModel() {

    private val _activeSession = MutableStateFlow<ActiveSession?>(null)
    val activeSession: StateFlow<ActiveSession?> = _activeSession.asStateFlow()

    private val _sessionsToday = MutableStateFlow(0)
    val sessionsToday: StateFlow<Int> = _sessionsToday.asStateFlow()

    private val _totalMinutesToday = MutableStateFlow(0)
    val totalMinutesToday: StateFlow<Int> = _totalMinutesToday.asStateFlow()

    private var tickJob: Job? = null

    fun startSession(subjectName: String, durationMinutes: Int) {
        val totalSeconds = durationMinutes * 60
        _activeSession.value = ActiveSession(
            subjectName = subjectName,
            totalSeconds = totalSeconds,
            remainingSeconds = totalSeconds,
            isPaused = false
        )
        startTicker()
    }

    fun pauseSession() {
        _activeSession.value = _activeSession.value?.copy(isPaused = true)
        tickJob?.cancel()
    }

    fun resumeSession() {
        _activeSession.value = _activeSession.value?.copy(isPaused = false)
        startTicker()
    }

    fun endSession() {
        _activeSession.value?.let { session ->
            val completedMinutes = (session.totalSeconds - session.remainingSeconds) / 60
            if (completedMinutes > 0) {
                _sessionsToday.value += 1
                _totalMinutesToday.value += completedMinutes
            }
        }
        _activeSession.value = null
        tickJob?.cancel()
    }

    private fun startTicker() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val current = _activeSession.value ?: break
                if (current.isPaused) continue
                val next = current.remainingSeconds - 1
                if (next <= 0) {
                    endSession()
                    break
                }
                _activeSession.value = current.copy(remainingSeconds = next)
            }
        }
    }

    fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%d:%02d".format(m, s)
    }
}
