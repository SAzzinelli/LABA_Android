package com.laba.firenze.ui.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.LabaConfig
import com.laba.firenze.data.repository.LessonCalendarRepository
import com.laba.firenze.data.repository.SessionRepository
import com.laba.firenze.domain.model.Esame
import com.laba.firenze.domain.model.LessonEvent
import com.laba.firenze.domain.model.StudentProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class DayLessons(val date: Date, val lessons: List<LessonEvent>)

@HiltViewModel
class FullTimetableViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val lessonCalendarRepository: LessonCalendarRepository,
    private val appearancePreferences: com.laba.firenze.data.local.AppearancePreferences
) : ViewModel() {

    private val _lessonsByDay = MutableStateFlow<List<DayLessons>>(emptyList())
    val lessonsByDay: StateFlow<List<DayLessons>> = _lessonsByDay.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                lessonCalendarRepository.events,
                sessionRepository.getUserProfileFlow(),
                sessionRepository.allExams
            ) { events, profile, exams ->
                computeLessonsByDay(events, profile, exams)
            }.collect { grouped ->
                _lessonsByDay.value = grouped
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val profile = sessionRepository.getUserProfileFlow().value
            val pianoStudi = profile?.pianoStudi
            val currentYear = profile?.currentYear?.toIntOrNull()
            lessonCalendarRepository.syncLessons(pianoStudi, currentYear, force = false)
        }
    }

    private fun computeLessonsByDay(
        allEvents: List<LessonEvent>,
        profile: StudentProfile?,
        exams: List<Esame>
    ): List<DayLessons> {
        val now = Date()
        val selectedGroup = if (LabaConfig.USE_GROUP_FILTER) appearancePreferences.getSelectedGroup() else null
        val userCourseIds = exams.mapNotNull { getCourseIdFromExam(it) }.toSet()
        val currentYear = profile?.currentYear?.toIntOrNull()

        val futureLessons = allEvents
            .filter { it.start >= now }
            .filter { event ->
                val eventGroup = event.gruppo
                val matchesGroup = if (selectedGroup != null && !eventGroup.isNullOrBlank()) {
                    eventGroup.equals(selectedGroup, ignoreCase = true)
                } else true
                if (!matchesGroup) return@filter false
                val matchesCourse = event.oidCorso?.lowercase()?.let { userCourseIds.contains(it) } == true ||
                    event.oidCorsi?.any { userCourseIds.contains(it.lowercase()) } == true ||
                    userCourseIds.any { normalizeCourse(event.corso).lowercase().contains(it) || it.contains(normalizeCourse(event.corso).lowercase()) }
                if (currentYear != null && event.anno != currentYear) return@filter false
                matchesCourse
            }
            .sortedBy { it.start }

        val cal = Calendar.getInstance()
        val grouped = futureLessons.groupBy { event ->
            cal.time = event.start
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.time
        }
        return grouped.map { (date, lessons) ->
            DayLessons(date = date, lessons = lessons.sortedBy { it.start })
        }.sortedBy { it.date }
    }

    private fun getCourseIdFromExam(exam: Esame): String? =
        exam.corso.lowercase().filter { it.isLetterOrDigit() }.takeIf { it.isNotEmpty() }

    private fun normalizeCourse(s: String): String {
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
                    else -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                }
            }
    }

    fun formatTimeRange(start: Date, end: Date): String {
        val timeFormat = java.text.SimpleDateFormat("HH:mm", Locale.ITALIAN)
        return "${timeFormat.format(start)} - ${timeFormat.format(end)}"
    }

    fun formatDayHeader(date: Date): String {
        val formatter = java.text.SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ITALIAN)
        return formatter.format(date).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ITALIAN) else it.toString() }
    }
}
