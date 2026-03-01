package com.laba.firenze.ui.gamification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.gamification.AchievementManager
import com.laba.firenze.data.repository.SessionRepository
import com.laba.firenze.domain.model.Achievement
import com.laba.firenze.domain.model.Esame
import com.laba.firenze.domain.model.GradeCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class YearRecapData(
    val year: Int,
    val totalExams: Int,
    val averageGrade: Double,
    val bestGrade: String,
    val worstGrade: String,
    val totalCFA: Int,
    val lodeCount: Int,
    val bestMonth: String?,
    val examsPerSession: Map<String, Int>,
    val mostProductiveSession: String?,
    val totalAchievements: Int,
    val totalPoints: Int,
    val topAchievements: List<Achievement>
)

@HiltViewModel
class YearRecapViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val achievementManager: AchievementManager,
    private val gradeCalculator: GradeCalculator
) : ViewModel() {

    private val _recapData = MutableStateFlow<YearRecapData?>(null)
    val recapData: StateFlow<YearRecapData?> = _recapData.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                sessionRepository.allExams,
                sessionRepository.getUserProfileFlow(),
                achievementManager.achievements,
                achievementManager.stats
            ) { exams, profile, achievements, stats ->
                generateRecapData(exams, profile, achievements, stats)
            }.collect { _recapData.value = it }
        }
    }

    private fun generateRecapData(
        exams: List<Esame>,
        profile: com.laba.firenze.domain.model.StudentProfile?,
        achievements: List<Achievement>,
        stats: com.laba.firenze.domain.model.UserStats
    ): YearRecapData {
        val calendar = Calendar.getInstance(Locale.ITALY)
        val currentYear = calendar.get(Calendar.YEAR)
        val dateFormatter = SimpleDateFormat("MMMM yyyy", Locale("it", "IT"))

        val coreExams = exams.filter { e ->
            val t = e.corso.lowercase()
            !t.contains("attivit") && !t.contains("tesi")
        }
        val completedExams = coreExams.filter { exam ->
            val voto = exam.voto?.trim()?.uppercase() ?: ""
            voto.isNotEmpty() && voto != "—" && voto != "-" && voto != "N/A"
        }

        val averageGrade = gradeCalculator.calculateAverage(completedExams)

        val bestGrade = completedExams.maxByOrNull { e ->
            gradeCalculator.parseVoteForThesis(e.voto) ?: 0
        }?.voto ?: "-"
        val worstGrade = completedExams.minByOrNull { e ->
            gradeCalculator.parseVoteForThesis(e.voto) ?: 30
        }?.voto ?: "-"

        val lodeCount = completedExams.count { exam ->
            val voto = exam.voto?.uppercase() ?: ""
            voto.contains("LODE") || voto.contains("30L")
        }

        val totalCFA = computeCFAEarned(exams, profile)

        var sessionCounts = mutableMapOf<String, Int>()
        for (exam in completedExams) {
            val dateStr = exam.data
            if (!dateStr.isNullOrBlank()) {
                try {
                    val formats = listOf(
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US),
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                    )
                    var date: Date? = null
                    for (fmt in formats) {
                        try {
                            date = fmt.parse(dateStr)
                            if (date != null) break
                        } catch (_: Exception) {}
                    }
                    date?.let {
                        val sessionKey = dateFormatter.format(it)
                        sessionCounts[sessionKey] = (sessionCounts[sessionKey] ?: 0) + 1
                    }
                } catch (_: Exception) {}
            }
        }
        val mostProductiveSession = sessionCounts.maxByOrNull { it.value }?.key

        var monthCount = mutableMapOf<String, Int>()
        for (exam in completedExams) {
            val dateStr = exam.data
            if (!dateStr.isNullOrBlank()) {
                try {
                    val formats = listOf(
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US),
                        SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    )
                    var date: Date? = null
                    for (fmt in formats) {
                        try {
                            date = fmt.parse(dateStr)
                            if (date != null) break
                        } catch (_: Exception) {}
                    }
                    date?.let {
                        val monthName = dateFormatter.format(it).split(" ").firstOrNull() ?: ""
                        if (monthName.isNotEmpty()) {
                            monthCount[monthName] = (monthCount[monthName] ?: 0) + 1
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        val bestMonth = monthCount.maxByOrNull { it.value }?.key

        val unlocked = achievements.filter { it.isUnlocked }
        val topAchievements = unlocked.sortedByDescending { it.points }.take(3)

        return YearRecapData(
            year = currentYear,
            totalExams = completedExams.size,
            averageGrade = averageGrade,
            bestGrade = bestGrade,
            worstGrade = worstGrade,
            totalCFA = totalCFA,
            lodeCount = lodeCount,
            bestMonth = bestMonth,
            examsPerSession = sessionCounts,
            mostProductiveSession = mostProductiveSession,
            totalAchievements = unlocked.size,
            totalPoints = stats.totalPoints,
            topAchievements = topAchievements
        )
    }

    private fun computeCFAEarned(
        exams: List<Esame>,
        profile: com.laba.firenze.domain.model.StudentProfile?
    ): Int {
        val isGraduated = profile?.status?.lowercase()?.contains("laureat") == true
        fun isAttivita(e: Esame) = e.corso.lowercase().contains("attivit")
        fun isTesi(e: Esame) = e.corso.lowercase().contains("tesi")
        fun isCompleted(e: Esame) = !(e.voto?.trim().isNullOrEmpty()) || e.data != null

        val examsEarned = exams
            .filter { !isAttivita(it) && !isTesi(it) && isCompleted(it) }
            .sumOf { it.cfa?.toIntOrNull() ?: 0 }

        val attivita = exams.filter { isAttivita(it) }
        val declaredActivitiesCFA = attivita.sumOf { it.cfa?.toIntOrNull() ?: 0 }
        val anyActivityCompleted = attivita.any { isCompleted(it) }
        val activitiesEarned = when {
            isGraduated -> 10
            anyActivityCompleted -> minOf(10, declaredActivitiesCFA)
            else -> 0
        }

        val thesis = exams.filter { isTesi(it) }
        val thesisCFA = thesis.firstOrNull()?.cfa?.toIntOrNull() ?: 0
        val thesisCompleted = thesis.any { isCompleted(it) }
        val thesisEarned = if (isGraduated || thesisCompleted) thesisCFA else 0

        return examsEarned + activitiesEarned + thesisEarned
    }
}
