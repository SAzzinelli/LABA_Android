package com.laba.firenze.data.gamification

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.laba.firenze.data.repository.SupabaseRepository
import com.laba.firenze.domain.model.*
import com.laba.firenze.domain.model.Achievement
import com.laba.firenze.domain.model.AchievementCategory
import com.laba.firenze.domain.model.AchievementRarity
import com.laba.firenze.domain.model.AchievementID
import com.laba.firenze.domain.model.AchievementFactory
import com.laba.firenze.domain.model.Esame
import com.laba.firenze.domain.model.Seminario
import com.laba.firenze.domain.model.StudentProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "achievements_prefs")

/**
 * Achievement Manager
 * Completo conforme a iOS AchievementManager.swift
 */
@Singleton
class AchievementManager @Inject constructor(
    @ApplicationContext @Suppress("UNUSED_PARAMETER") private val context: Context,
    private val supabaseRepository: SupabaseRepository
) {
    private val dataStore = context.dataStore
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val json = Json { ignoreUnknownKeys = true }
    
    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()
    
    private val _stats = MutableStateFlow(UserStats())
    val stats: StateFlow<UserStats> = _stats.asStateFlow()
    
    private val _recentlyUnlocked = MutableStateFlow<Achievement?>(null)
    val recentlyUnlocked: StateFlow<Achievement?> = _recentlyUnlocked.asStateFlow()
    
    @Suppress("UNUSED_VARIABLE")
    private val _showConfetti = MutableStateFlow(false)
    @Suppress("UNUSED_VARIABLE")
    val showConfetti: StateFlow<Boolean> = _showConfetti.asStateFlow()
    
    @Suppress("UNUSED_VARIABLE")
    private val _notificationsEnabled = MutableStateFlow(true)
    @Suppress("UNUSED_VARIABLE")
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()
    
    private var firstLoadDone = false
    private var userEmail: String? = null
    
    val totalPoints: StateFlow<Int> = _stats.map { it.totalPoints }.stateIn(
        scope = scope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    
    val unlockedCount: Int
        get() = _achievements.value.count { it.isUnlocked }
    
    val totalCount: Int
        get() = _achievements.value.size
    
    @Suppress("UNUSED_VARIABLE")
    val unlockedPercentage: Double
        get() = if (totalCount > 0) unlockedCount.toDouble() / totalCount.toDouble() else 0.0
    
    init {
        scope.launch {
            // Carica prima gli achievement, poi le stats
            // Questo assicura che quando ricalcoliamo i punti, gli achievement siano già caricati
            loadAchievements()
            loadStats()
            
            // Dopo aver caricato tutto, ricalcola i punti una volta finale
            recalculateTotalPoints()
            
            // Se c'è già un'email salvata, triggera sync
            val savedEmail = getSavedEmail()
            if (!savedEmail.isNullOrEmpty()) {
                Log.d("AchievementManager", "🔄 Found saved email, triggering sync: $savedEmail")
                syncFromSupabaseIfNeeded(savedEmail)
            }
        }
    }
    
    // MARK: - Persistence
    
    private suspend fun loadAchievements() {
        Log.d("AchievementManager", "📂 Loading achievements from local storage...")
        
        val achievementsDataKey = stringPreferencesKey("achievements_data")
        val achievementsJson = dataStore.data.first()[achievementsDataKey]
        
        if (achievementsJson != null) {
            try {
                val decoded = json.decodeFromString<List<Achievement>>(achievementsJson)
                val existingIDs = decoded.map { it.id }.toSet()
                val newAchievements = AchievementID.entries
                    .filter { !existingIDs.contains(it.rawValue) }
                    .map { AchievementFactory.createAchievement(it) }
                if (newAchievements.isNotEmpty()) {
                    Log.d("AchievementManager", "📥 Added ${newAchievements.size} new achievements from schema")
                    _achievements.value = decoded + newAchievements
                    saveAchievements()
                } else {
                    _achievements.value = decoded
                }
                val unlockedCount = _achievements.value.count { it.isUnlocked }
                Log.d("AchievementManager", "✅ Loaded ${_achievements.value.size} achievements from local storage ($unlockedCount unlocked)")
            } catch (e: Exception) {
                Log.e("AchievementManager", "❌ Failed to decode achievements", e)
                initializeAchievements()
            }
        } else {
            initializeAchievements()
        }
        
        // Non ricalcolare qui - verrà fatto dopo loadStats() in init
    }
    
    private suspend fun initializeAchievements() {
        Log.d("AchievementManager", "🆕 No local achievements found, initializing with all achievements")
        val allAchievements = AchievementID.entries.map { id ->
            AchievementFactory.createAchievement(id)
        }
        _achievements.value = allAchievements
        saveAchievements()
        Log.d("AchievementManager", "✅ Initialized ${allAchievements.size} achievements")
    }
    
    private suspend fun saveAchievements() {
        try {
            val achievementsJson = json.encodeToString(_achievements.value)
            dataStore.edit { prefs ->
                prefs[stringPreferencesKey("achievements_data")] = achievementsJson
            }
            val unlockedCount = _achievements.value.count { it.isUnlocked }
            Log.d("AchievementManager", "💾 Saved ${_achievements.value.size} achievements to local storage ($unlockedCount unlocked)")
        } catch (e: Exception) {
            Log.e("AchievementManager", "❌ Failed to encode achievements for saving", e)
        }
    }
    
    private suspend fun loadStats() {
        val statsDataKey = stringPreferencesKey("stats_data")
        val statsJson = dataStore.data.first()[statsDataKey]
        
        if (statsJson != null) {
            try {
                val decoded = json.decodeFromString<UserStats>(statsJson)
                _stats.value = decoded
                Log.d("AchievementManager", "Loaded stats - Points: ${decoded.totalPoints}, Logins: ${decoded.totalLogins}")
                
                // Non ricalcolare qui - verrà fatto dopo in init per assicurarsi che gli achievement siano caricati
            } catch (e: Exception) {
                Log.e("AchievementManager", "Failed to decode stats", e)
                _stats.value = UserStats()
                saveStats()
            }
        } else {
            _stats.value = UserStats()
            Log.d("AchievementManager", "Created new stats")
            saveStats()
        }
    }
    
    /**
     * Ricalcola i punti totali dalla somma degli achievement sbloccati
     * Questo assicura che i punti corrispondano sempre agli achievement sbloccati (come iOS)
     */
    private suspend fun recalculateTotalPoints() {
        val unlockedAchievements = _achievements.value.filter { it.isUnlocked }
        val calculatedPoints = unlockedAchievements.sumOf { it.points }
        val currentPoints = _stats.value.totalPoints
        
        if (calculatedPoints != currentPoints) {
            Log.d("AchievementManager", "⚠️ Points mismatch detected! Current: $currentPoints, Calculated: $calculatedPoints. Recalculating...")
            _stats.value = _stats.value.copy(totalPoints = calculatedPoints)
            saveStats()
            Log.d("AchievementManager", "✅ Points recalculated: $calculatedPoints")
        }
    }
    
    private suspend fun saveStats() {
        try {
            val statsJson = json.encodeToString(_stats.value)
            dataStore.edit { prefs ->
                prefs[stringPreferencesKey("stats_data")] = statsJson
            }
            Log.d("AchievementManager", "Saved stats - Points: ${_stats.value.totalPoints}")
        } catch (e: Exception) {
            Log.e("AchievementManager", "ERROR: Failed to encode stats!", e)
        }
    }
    
    // MARK: - Achievement Management
    
    private suspend fun unlockAchievement(id: String, eventDate: Long? = null) {
        val currentList = _achievements.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        
        if (index == -1 || currentList[index].isUnlocked) {
            Log.d("AchievementManager", "Already unlocked or not found: $id")
            return
        }
        
        // Check if achievements are enabled
        val achievementsEnabled = _notificationsEnabled.value
        
        val now = System.currentTimeMillis()
        val achievement = currentList[index]
        val updated = achievement.copy(
                    isUnlocked = true,
            unlockedDate = now,
            progress = achievement.maxProgress,
            eventDate = eventDate
                )
                currentList[index] = updated
                _achievements.value = currentList
                
        // Update stats
        val pointsToAdd = achievement.points
        _stats.value = _stats.value.copy(
            totalPoints = _stats.value.totalPoints + pointsToAdd,
            achievementUnlockDates = _stats.value.achievementUnlockDates + listOf(now)
        )
        
        // Store event date if provided
        if (eventDate != null) {
            val eventDates = _stats.value.achievementEventDates.toMutableMap()
            eventDates[id] = eventDate
            _stats.value = _stats.value.copy(achievementEventDates = eventDates)
        }
        
        Log.d("AchievementManager", "Unlocked '${achievement.title}' - +$pointsToAdd pts, Total now: ${_stats.value.totalPoints}")
        
        // Salva su Supabase se disponibile email
        val email = userEmail ?: getSavedEmail()
        if (!email.isNullOrEmpty()) {
            Log.d("AchievementManager", "💾 Saving unlocked achievement '$id' to Supabase for $email")
            AchievementSyncService.saveUnlockedAchievement(
                achievementID = id,
                email = email,
                eventDate = eventDate?.let { Date(it) },
                supabaseRepository = supabaseRepository
            )
        } else {
            Log.d("AchievementManager", "⚠️ Cannot save to Supabase: no user email available")
        }
        
        // Only show UI elements if achievements are enabled AND not already notified
        val alreadyNotified = _stats.value.notifiedAchievements.contains(id)
        
        if (achievementsEnabled && !alreadyNotified) {
            // Show in-app notification only if not already notified
            _recentlyUnlocked.value = updated
            
            // Show confetti for Epic+ achievements
            if (updated.rarity == AchievementRarity.EPIC || updated.rarity == AchievementRarity.LEGENDARY) {
                _showConfetti.value = true
                scope.launch {
                    kotlinx.coroutines.delay(3000)
                    _showConfetti.value = false
                }
            }
            
            // Hide in-app notification after 4 seconds
            scope.launch {
                kotlinx.coroutines.delay(4000)
                if (_recentlyUnlocked.value?.id == id) {
                    _recentlyUnlocked.value = null
                }
            }
            
            // Mark as notified
            _stats.value = _stats.value.copy(
                notifiedAchievements = _stats.value.notifiedAchievements + id
            )
            Log.d("AchievementManager", "🎉 Showing banner for newly unlocked achievement: ${updated.title}")
        } else {
            // Silently track (already notified or feature disabled)
            if (!alreadyNotified) {
                _stats.value = _stats.value.copy(
                    notifiedAchievements = _stats.value.notifiedAchievements + id
                )
            }
            if (alreadyNotified) {
                Log.d("AchievementManager", "⏭️ Skipping banner for already notified achievement: ${updated.title}")
            } else {
                Log.d("AchievementManager", "Silently tracked (feature disabled): ${updated.title}")
            }
        }
        
        saveAchievements()
        saveStats()
        
        // Sync stats a Supabase periodicamente
        if (!email.isNullOrEmpty()) {
            AchievementSyncService.syncStatsToSupabase(email, _stats.value, _achievements.value, supabaseRepository)
        }
    }
    
    private fun updateProgress(id: String, progress: Int) {
        scope.launch {
        val currentList = _achievements.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
            
            if (index == -1) return@launch
            
            val oldProgress = currentList[index].progress
            val updated = currentList[index].copy(progress = progress)
                currentList[index] = updated
                _achievements.value = currentList
                
            // Unlock if completed
            if (!updated.isUnlocked && updated.isCompleted) {
                unlockAchievement(id = id)
            } else if (oldProgress != progress) {
                saveAchievements()
            }
        }
    }
    
    // MARK: - Helper per ottenere email utente
    
    private suspend fun getSavedEmail(): String? {
        val emailKey = stringPreferencesKey("user_email")
        return dataStore.data.first()[emailKey]
    }
    
    fun setUserEmail(email: String?) {
        scope.launch {
            if (!email.isNullOrEmpty()) {
                Log.d("AchievementManager", "📧 Setting user email: $email")
                dataStore.edit { prefs ->
                    prefs[stringPreferencesKey("user_email")] = email
                }
                userEmail = email
                // Sync immediato quando si setta l'email
                Log.d("AchievementManager", "🔄 Triggering sync from Supabase...")
                syncFromSupabaseIfNeeded(email)
            } else {
                Log.d("AchievementManager", "🧹 Clearing user email (logout)")
                dataStore.edit { prefs ->
                    prefs.remove(stringPreferencesKey("user_email"))
                }
                userEmail = null
            }
        }
    }
    
    // MARK: - Sync da Supabase all'avvio
    
    fun syncFromSupabaseIfNeeded(email: String?) {
        if (email.isNullOrEmpty()) {
            Log.d("AchievementManager", "⏭️ Sync skipped: no email provided")
            return
        }
        
        Log.d("AchievementManager", "🚀 Starting Supabase sync for email: $email")
        scope.launch {
            AchievementSyncService.syncAchievementsFromSupabase(
                email = email,
                manager = this@AchievementManager,
                supabaseRepository = supabaseRepository
            )
        }
    }
    
    // MARK: - Track Actions
    
    fun trackFirstLogin() {
        scope.launch {
            if (_stats.value.firstLoginDate == null) {
                val now = System.currentTimeMillis()
                _stats.value = _stats.value.copy(firstLoginDate = now)
                saveStats()
                unlockAchievement(id = AchievementID.FIRST_LOGIN.rawValue)
            }
        }
    }
    
    fun trackDataLoad() {
        scope.launch {
            val newLoads = _stats.value.totalDataLoads + 1
            _stats.value = _stats.value.copy(totalDataLoads = newLoads)
            saveStats()
            
            // Update progress for data loading achievements
            if (newLoads == 1) {
                unlockAchievement(id = AchievementID.FIRST_DATA.rawValue)
            }
        }
    }
    
    @Suppress("UNUSED_FUNCTION")
    fun trackExamBooked() {
        scope.launch {
            val newBooked = _stats.value.totalExamsBooked + 1
            _stats.value = _stats.value.copy(totalExamsBooked = newBooked)
            saveStats()
            
            if (newBooked == 1) {
                unlockAchievement(id = AchievementID.FIRST_EXAM_BOOKED.rawValue)
            }
        }
    }
    
    fun trackLogin() {
        scope.launch {
            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = now
            
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            
            // Update stats
            val newLogins = _stats.value.totalLogins + 1
            val newRefreshes = _stats.value.totalRefreshes + 1
            
            // Check time for night owl / early bird
            var newNightLogins = _stats.value.nightLogins
            var newEarlyMorningLogins = _stats.value.earlyMorningLogins
            
            if (hour < 7) {
                newEarlyMorningLogins++
                updateProgress(
                    id = AchievementID.MATTINIERO.rawValue,
                    progress = minOf(10, newEarlyMorningLogins)
                )
            }
            if (hour < 5) {
                newNightLogins++
                updateProgress(
                    id = AchievementID.GUFO_NOTTURNO.rawValue,
                    progress = minOf(5, newNightLogins)
                )
            }
            
            // Track consecutive days
            val lastLogin = _stats.value.lastLoginDate
            val loginDates = _stats.value.loginDates.toMutableList()
            
            if (lastLogin != null) {
                val lastCalendar = Calendar.getInstance()
                lastCalendar.timeInMillis = lastLogin
                
                // If not same day, add to list
                if (!isSameDay(lastCalendar, calendar)) {
                    loginDates.add(now)
                }
            } else {
                loginDates.add(now)
            }
            
            // Check consecutive days
            val consecutiveDays = calculateConsecutiveDays(loginDates)
            if (consecutiveDays >= 7) {
                updateProgress(id = AchievementID.PUNTUALE.rawValue, progress = 7)
            }
            
            // Total logins
            updateProgress(id = AchievementID.DIPENDENTE.rawValue, progress = minOf(50, newLogins))
            updateProgress(id = AchievementID.REFRESH_MANIAC.rawValue, progress = minOf(100, newRefreshes))
            
            // Easter eggs - dates
            // Unicorno - 1 gennaio a mezzanotte
            if (month == 1 && day == 1 && hour == 0) {
                unlockAchievement(id = AchievementID.UNICORNO.rawValue)
            }
            
            // Halloween
            if (month == 10 && day == 31) {
                unlockAchievement(id = AchievementID.HALLOWEEN.rawValue)
            }
            
            // Natale (20-31 dicembre)
            if (month == 12 && day >= 20) {
                unlockAchievement(id = AchievementID.NATALE.rawValue)
            }
            
            // Agosto
            if (month == 8) {
                unlockAchievement(id = AchievementID.ESTATE_AGOSTO.rawValue)
            }
            
            // Compleanno
            _stats.value.birthday?.let { birthday ->
                val birthdayCalendar = Calendar.getInstance()
                birthdayCalendar.timeInMillis = birthday
                if (month == birthdayCalendar.get(Calendar.MONTH) + 1 &&
                    day == birthdayCalendar.get(Calendar.DAY_OF_MONTH)
                ) {
                    unlockAchievement(id = AchievementID.COMPLEANNO.rawValue)
                }
            }
            
            _stats.value = _stats.value.copy(
                totalLogins = newLogins,
                totalRefreshes = newRefreshes,
                nightLogins = newNightLogins,
                earlyMorningLogins = newEarlyMorningLogins,
                loginDates = loginDates.distinct().sorted(),
                lastLoginDate = now
            )
            
            saveStats()
        }
    }
    
    fun trackSectionVisit(section: String) {
        scope.launch {
            var stats = _stats.value
            var hasChanges = false
            
            when (section.lowercase()) {
                "home" -> {
                    if (!stats.visitedHome) {
                        stats = stats.copy(visitedHome = true)
                        hasChanges = true
                    }
                }
                "exams", "esami" -> {
                    if (!stats.visitedExams) {
                        stats = stats.copy(visitedExams = true)
                        hasChanges = true
                    }
                }
                "corsi", "courses" -> {
                    if (!stats.visitedCorsi) {
                        stats = stats.copy(visitedCorsi = true)
                        hasChanges = true
                    }
                }
                "seminari", "seminars" -> {
                    if (!stats.visitedSeminari) {
                        stats = stats.copy(visitedSeminari = true)
                        hasChanges = true
                    }
                }
                "profile", "profilo" -> {
                    if (!stats.visitedProfile) {
                        stats = stats.copy(visitedProfile = true)
                        hasChanges = true
                    }
                }
            }
            
            if (hasChanges) {
                _stats.value = stats
                saveStats()
                
                // Check if all sections visited
                if (stats.visitedHome && stats.visitedExams && stats.visitedCorsi &&
                    stats.visitedSeminari && stats.visitedProfile
                ) {
                    unlockAchievement(id = AchievementID.ESPLORATORE.rawValue)
                }
            }
        }
    }
    
    fun trackDispenseOpen(dispenseId: String) {
        scope.launch {
            val newOpened = _stats.value.dispenseOpened + dispenseId
            _stats.value = _stats.value.copy(dispenseOpened = newOpened)
            updateProgress(
                id = AchievementID.STUDIOSO.rawValue,
                progress = minOf(10, newOpened.size)
            )
            saveStats()
        }
    }
    
    fun trackRegolamentoRead(regolamentoId: String) {
        scope.launch {
            val newRead = _stats.value.rulesRead + regolamentoId
            _stats.value = _stats.value.copy(rulesRead = newRead)
            
            // Assume there are ~5 regolamenti total
            if (newRead.size >= 5) {
                unlockAchievement(id = AchievementID.INFORMATO.rawValue)
            }
            saveStats()
        }
    }
    
    fun trackFAQVisit() {
        scope.launch {
            val newVisits = _stats.value.faqVisits + 1
            _stats.value = _stats.value.copy(faqVisits = newVisits)
            updateProgress(id = AchievementID.CURIOSO.rawValue, progress = minOf(5, newVisits))
            saveStats()
        }
    }
    
    @Suppress("UNUSED_FUNCTION")
    fun setBirthday(date: Long) {
        scope.launch {
            _stats.value = _stats.value.copy(birthday = date)
            saveStats()
        }
    }
    
    // MARK: - Update from Session Data
    // Completo conforme a iOS updateAchievements(from vm: SessionVM)
    
    fun updateAchievements(
        exams: List<Esame>,
        seminars: List<Seminario>,
        profile: StudentProfile?
    ) {
        scope.launch {
            // Forza sblocco first_login se utente è loggato (come iOS)
            if (profile != null) {
                val firstLogin = _achievements.value.find { it.id == AchievementID.FIRST_LOGIN.rawValue }
                if (firstLogin != null && !firstLogin.isUnlocked) {
                    Log.d("AchievementManager", "🔓 User is logged in but first_login not unlocked, forcing unlock...")
                    unlockAchievement(id = AchievementID.FIRST_LOGIN.rawValue)
                }
            }
            
            // First exam booked - check if user has any exam
            if (exams.isNotEmpty()) {
                val firstExam = exams.minByOrNull { exam ->
                    parseDate(exam.data) ?: Long.MAX_VALUE
                }
                firstExam?.let { exam ->
                    val eventDate = parseDate(exam.data) ?: System.currentTimeMillis()
                    unlockAchievement(id = AchievementID.FIRST_EXAM_BOOKED.rawValue, eventDate = eventDate)
                }
            }
            
            // Esami analysis
            val completedExams = exams.filter { exam ->
                val voto = exam.voto?.trim()?.uppercase() ?: ""
                voto.isNotEmpty() && voto != "—" && voto != "-" && voto != "N/A"
            }
            
            // Check for 18 (with date)
            completedExams.firstOrNull { exam ->
                val voto = exam.voto?.trim()?.uppercase() ?: ""
                voto == "18"
            }?.let { first18Exam ->
                val eventDate = parseDate(first18Exam.data) ?: System.currentTimeMillis()
                val stats = _stats.value
                if (stats.first18Date == null) {
                    _stats.value = stats.copy(first18Date = eventDate)
                }
                unlockAchievement(
                    id = AchievementID.FIRST_18.rawValue,
                    eventDate = stats.first18Date ?: eventDate
                )
            }
            
            // Check for 30 (with date)
            completedExams.firstOrNull { exam ->
                val voto = exam.voto?.trim()?.uppercase() ?: ""
                voto == "30" || voto.contains("30")
            }?.let { first30Exam ->
                val eventDate = parseDate(first30Exam.data) ?: System.currentTimeMillis()
                val stats = _stats.value
                if (stats.first30Date == null) {
                    _stats.value = stats.copy(first30Date = eventDate)
                }
                unlockAchievement(
                    id = AchievementID.FIRST_30.rawValue,
                    eventDate = stats.first30Date ?: eventDate
                )
            }
            
            // Check for lode (with date)
            completedExams.firstOrNull { exam ->
                val voto = exam.voto?.trim()?.uppercase() ?: ""
                voto.contains("LODE") || voto.contains("30L") || voto == "30L"
            }?.let { firstLodeExam ->
                val eventDate = parseDate(firstLodeExam.data) ?: System.currentTimeMillis()
                val stats = _stats.value
                if (stats.firstLodeDate == null) {
                    _stats.value = stats.copy(firstLodeDate = eventDate)
                }
                unlockAchievement(
                    id = AchievementID.FIRST_LODE.rawValue,
                    eventDate = stats.firstLodeDate ?: eventDate
                )
            }
            
            // Check years completion
            val year1Exams = exams.filter { (it.anno?.toIntOrNull() ?: 0) == 1 }
            val year1Completed = year1Exams.isNotEmpty() && year1Exams.all { exam ->
                val voto = exam.voto?.trim()?.uppercase() ?: ""
                voto.isNotEmpty() && voto != "—" && voto != "-"
            }
            if (year1Completed) {
                unlockAchievement(id = AchievementID.YEAR_1_COMPLETE.rawValue)
            }
            
            val year2Exams = exams.filter { (it.anno?.toIntOrNull() ?: 0) == 2 }
            val year2Completed = year2Exams.isNotEmpty() && year2Exams.all { exam ->
                val voto = exam.voto?.trim()?.uppercase() ?: ""
                voto.isNotEmpty() && voto != "—" && voto != "-"
            }
            if (year2Completed) {
                unlockAchievement(id = AchievementID.YEAR_2_COMPLETE.rawValue)
            }
            
            val year3Exams = exams.filter { (it.anno?.toIntOrNull() ?: 0) == 3 }
            val year3Completed = year3Exams.isNotEmpty() && year3Exams.all { exam ->
                val voto = exam.voto?.trim()?.uppercase() ?: ""
                voto.isNotEmpty() && voto != "—" && voto != "-"
            }
            if (year3Completed) {
                unlockAchievement(id = AchievementID.YEAR_3_COMPLETE.rawValue)
            }
            
            // CFA calculation
            val earnedCFA = computeCFAEarned(exams, profile)
            val targetCFA = computeCFATarget(profile)
            
            // All exams completed (ready to graduate)
            val allExamsCompleted = exams.isNotEmpty() && exams.all { exam ->
                val voto = exam.voto?.trim()?.uppercase() ?: ""
                voto.isNotEmpty() && voto != "—" && voto != "-" && voto != "N/A"
            }
            
            val cfaComplete = targetCFA > 0 && earnedCFA >= targetCFA
            val isGraduated = profile?.status?.uppercase()?.contains("LAUREAT") == true
            
            Log.d("AchievementManager", "Ready to graduate check - Total exams: ${exams.size}, All exams completed: $allExamsCompleted, CFA complete: $cfaComplete ($earnedCFA/$targetCFA), Graduated: $isGraduated")
            
            if (allExamsCompleted || cfaComplete || isGraduated) {
                unlockAchievement(id = AchievementID.READY_TO_GRADUATE.rawValue)
            }
            
            // Seminari frequentati: partecipato==true (v3 API) oppure esito PRESENTE/FREQUENTATO (fallback)
            val completedSeminars = seminars.filter { seminar ->
                seminar.partecipato || run {
                    val esito = seminar.esito?.trim()?.uppercase() ?: ""
                    esito == "PRESENTE" || esito == "FREQUENTATO" || esito.contains("CONFERM")
                }
            }
            val seminarCount = completedSeminars.size
            if (seminarCount >= 1) {
                updateProgress(id = AchievementID.FIRST_SEMINAR.rawValue, progress = 1)
            }
            if (seminarCount >= 2) {
                updateProgress(id = AchievementID.TWO_SEMINARS.rawValue, progress = minOf(2, seminarCount))
            }
            if (seminarCount >= 3) {
                updateProgress(id = AchievementID.THREE_SEMINARS.rawValue, progress = minOf(3, seminarCount))
            }
            if (seminarCount >= 5) {
                updateProgress(id = AchievementID.FIVE_SEMINARS.rawValue, progress = minOf(5, seminarCount))
            }
            updateProgress(id = AchievementID.NETWORKING.rawValue, progress = minOf(10, seminarCount))

            // Seminari prenotati: dataRichiesta valorizzato dopo PUT (v3)
            val bookedSeminars = seminars.filter { it.dataRichiesta != null && !it.dataRichiesta.isNullOrBlank() }
            val bookedCount = bookedSeminars.size
            if (bookedCount >= 1) {
                updateProgress(id = AchievementID.FIRST_SEMINAR_BOOKED.rawValue, progress = 1)
            }
            if (bookedCount >= 2) {
                updateProgress(id = AchievementID.TWO_SEMINARS_BOOKED.rawValue, progress = minOf(2, bookedCount))
            }
            if (bookedCount >= 3) {
                updateProgress(id = AchievementID.THREE_SEMINARS_BOOKED.rawValue, progress = minOf(3, bookedCount))
            }
            if (bookedCount >= 5) {
                updateProgress(id = AchievementID.FIVE_SEMINARS_BOOKED.rawValue, progress = minOf(5, bookedCount))
            }
            
            // Graduated
            if (isGraduated) {
                unlockAchievement(id = AchievementID.GRADUATED.rawValue)
            }
            
            // Performance - Lodi count
            val lodeCount = completedExams.count { exam ->
                val voto = exam.voto?.trim()?.uppercase() ?: ""
                voto.contains("LODE") || voto.contains("30L")
            }
            updateProgress(id = AchievementID.PERFEZIONISTA.rawValue, progress = minOf(5, lodeCount))
            
            // Performance - Streak perfetto (3 esami consecutivi ≥28)
            val sortedExams = completedExams.sortedBy { exam ->
                parseDate(exam.data) ?: 0L
            }
            var currentStreak = 0
            var maxStreak = 0
            for (exam in sortedExams) {
                val grade = extractNumericGrade(exam.voto)
                if (grade != null && grade >= 28) {
                    currentStreak++
                    maxStreak = maxOf(maxStreak, currentStreak)
                } else {
                    currentStreak = 0
                }
            }
            if (maxStreak >= 3) {
                unlockAchievement(id = AchievementID.STREAK_PERFECT.rawValue)
            }
            
            // Performance - Media finale ≥28
            val numericGrades = completedExams.mapNotNull { extractNumericGrade(it.voto) }
            if (numericGrades.isNotEmpty()) {
                val average = numericGrades.average()
                Log.d("AchievementManager", "Media finale: $average (from ${numericGrades.size} exams)")
                if (average >= 28.0) {
                    unlockAchievement(id = AchievementID.PUNTEGGIO_PIENO.rawValue)
                }
            }
            
            // Performance - Maratoneta (3 esami in una sessione)
            val sessionCounts = mutableMapOf<String, Int>()
            completedExams.forEach { exam ->
                parseDate(exam.data)?.let { date ->
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = date
                    val key = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}"
                    sessionCounts[key] = (sessionCounts[key] ?: 0) + 1
                }
            }
            if (sessionCounts.values.any { it >= 3 }) {
                unlockAchievement(id = AchievementID.MARATONETA.rawValue)
            }
            
            // Easter egg - Estate Giugno (3 esami a giugno)
            val juneExams = completedExams.filter { exam ->
                parseDate(exam.data)?.let { date ->
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = date
                    calendar.get(Calendar.MONTH) + 1 == 6 // June
                } ?: false
            }
            if (juneExams.size >= 3) {
                unlockAchievement(id = AchievementID.ESTATE_GIUGNO.rawValue)
            }
            
            // CFA milestones
            if (targetCFA > 0) {
                val cfa25 = targetCFA / 4
                val halfTarget = targetCFA / 2
                val cfa75 = (targetCFA * 3) / 4
                
                Log.d("AchievementManager", "CFA check - Earned: $earnedCFA, Target: $targetCFA, 25%: $cfa25, 50%: $halfTarget, 75%: $cfa75")
                
                if (earnedCFA >= cfa25) {
                    unlockAchievement(id = AchievementID.CFA_25.rawValue)
                }
                if (earnedCFA >= halfTarget) {
                    unlockAchievement(id = AchievementID.CFA_HALF.rawValue)
                }
                if (earnedCFA >= cfa75) {
                    unlockAchievement(id = AchievementID.CFA_75.rawValue)
                }
                if (earnedCFA >= targetCFA) {
                    unlockAchievement(id = AchievementID.CFA_COMPLETE.rawValue)
                }
                if (earnedCFA > targetCFA) {
                    unlockAchievement(id = AchievementID.CFA_COLLECTOR.rawValue)
                }
            }
            
            // Easter Eggs - Fortunato (voto 27)
            val has27 = completedExams.any { exam ->
                extractNumericGrade(exam.voto) == 27
            }
            if (has27) {
                unlockAchievement(id = AchievementID.FORTUNATO.rawValue)
            }
            
            // Easter Eggs - Arcobaleno (tutti i voti da 18 a 30L, come iOS)
            val votiPresenti = completedExams.mapNotNull { exam ->
                exam.voto?.trim()?.uppercase()
            }.toSet()
            // Normalizza: "30 e lode" / "30L" devono contare anche come "30"
            val votiNormalizzati = votiPresenti.flatMap { v ->
                if (v.contains("LODE") || v.contains("30L")) listOf(v, "30")
                else listOf(v)
            }.toSet()
            val requiredGrades = listOf("18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30")
            val hasAllGrades = requiredGrades.all { votiNormalizzati.contains(it) }
            val hasAnyLode = votiPresenti.any { it.contains("LODE") || it.contains("30L") }
            if (hasAllGrades && hasAnyLode) {
                unlockAchievement(id = AchievementID.ARCOBALENO.rawValue)
            }
            
            // Meta achievements
            val unlockedNow = _achievements.value.count { it.isUnlocked }
            if (unlockedNow >= 10) {
                unlockAchievement(id = AchievementID.COLLEZIONISTA.rawValue)
            }
            if (unlockedNow >= 20) {
                unlockAchievement(id = AchievementID.MAESTRO.rawValue)
            }
            if (unlockedNow == _achievements.value.size) {
                unlockAchievement(id = AchievementID.LEGGENDA.rawValue)
            }
            
            // Cacciatore - 5 achievement in un giorno
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val unlockedToday = _achievements.value.count { achievement ->
                achievement.unlockedDate?.let { date ->
                    val unlockCalendar = Calendar.getInstance().apply { timeInMillis = date }
                    val todayCalendar = Calendar.getInstance().apply { timeInMillis = today }
                    isSameDay(unlockCalendar, todayCalendar)
                } ?: false
            }
            if (unlockedToday >= 5) {
                unlockAchievement(id = AchievementID.CACCIATORE.rawValue)
            }
            
            // Milionario
            if (_stats.value.totalPoints >= 1000) {
                unlockAchievement(id = AchievementID.MILIONARIO.rawValue)
            }
            
            // Mark first load as done after processing all achievements
            if (!firstLoadDone) {
                firstLoadDone = true
                Log.d("AchievementManager", "First load completed - future achievements will be notified")
            }
            
            saveAchievements()
            saveStats()
        }
    }
    
    private fun computeCFAEarned(exams: List<Esame>, profile: StudentProfile?): Int {
        // 1) Esami con voto valido
        val examsEarned = exams.sumOf { exam ->
            val voto = exam.voto?.trim()?.uppercase() ?: ""
            if (voto.isEmpty() || voto == "—" || voto == "-" || voto == "N/A") return@sumOf 0
            val cfa = exam.cfa?.toIntOrNull() ?: 0
            cfa
        }
        
        // 2) Attività integrative → max 10 CFA totali
        val corsoLower = { s: String -> s.lowercase() }
        val attivita = exams.filter { corso ->
            val lower = corsoLower(corso.corso)
            lower.contains("attivit") || lower.contains("workshop") || lower.contains("seminari")
        }
        val declaredActivitiesCFA = attivita.sumOf { it.cfa?.toIntOrNull() ?: 0 }
        val isGraduated = profile?.status?.uppercase()?.contains("LAUREAT") == true
        val anyActivityCompleted = attivita.any { course ->
            val voto = course.voto?.trim() ?: ""
            voto.isNotEmpty() && voto != "—" && voto != "-" && voto != "N/A"
        }
        val activitiesEarned = if (isGraduated) {
            10 // laureato: consideriamo acquisiti i 10 CFA totali
        } else {
            if (anyActivityCompleted) minOf(10, declaredActivitiesCFA) else 0
        }
        
        // 3) Tesi (se completo)
        val thesis = exams.filter { corso ->
            val lower = corsoLower(corso.corso)
            lower.contains("tesi") || lower.contains("prova finale")
        }
        val thesisCFA = thesis.firstOrNull()?.cfa?.toIntOrNull() ?: 0
        val thesisCompleted = thesis.any { course ->
            val voto = course.voto?.trim() ?: ""
            voto.isNotEmpty() && voto != "—" && voto != "-" && voto != "N/A"
        }
        val thesisEarned = if (isGraduated || thesisCompleted) thesisCFA else 0
        
        val total = examsEarned + activitiesEarned + thesisEarned
        Log.d("AchievementManager", "CFA earned: $examsEarned from exams + $activitiesEarned from activities + $thesisEarned from thesis = $total")
        return total
    }
    
    private fun computeCFATarget(profile: StudentProfile?): Int {
        // Triennio = 180 CFA, Biennio = 120 CFA
        val piano = profile?.pianoStudi?.uppercase() ?: ""
        val target = if (piano.contains("BIENNIO") || piano.contains("II LIVELLO") || piano.contains("SECONDO LIVELLO")) {
            120
        } else {
            180
        }
        Log.d("AchievementManager", "CFA target: $target, Piano: $piano")
        return target
    }
    
    private fun extractNumericGrade(voto: String?): Int? {
        val v = voto?.trim()?.uppercase() ?: return null
        
        // Handle lode cases (30L, 30 LODE, etc.) - treat as 30
        if (v.contains("LODE") || v.contains("30L")) {
            return 30
        }
        
        // Extract numeric part
        val clean = v.replace("L", "")
            .replace("LODE", "")
            .trim()
        return clean.toIntOrNull()
    }
    
    private fun parseDate(dateString: String?): Long? {
        if (dateString.isNullOrBlank()) return null
        
        // Try common date formats
        val formats = listOf(
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        )
        
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                return sdf.parse(dateString)?.time
            } catch (@Suppress("UNUSED_PARAMETER") e: Exception) {
                // Continue to next format
            }
        }
        
        return null
    }
    
    @Suppress("UNUSED_FUNCTION")
    fun achievementsByCategory(category: AchievementCategory): List<Achievement> {
        return _achievements.value.filter { it.category == category }
    }
    
    /**
     * Dismiss recently unlocked toast
     */
    fun dismissUnlockedToast() {
        scope.launch {
            _recentlyUnlocked.value = null
        }
    }
    
    // MARK: - Public methods for Sync Service
    
    /**
     * Restore achievement from cloud (used by sync service)
     */
    fun restoreAchievement(achievementID: String, unlockedDate: Long? = null, silent: Boolean = true) {
        scope.launch {
            val currentList = _achievements.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == achievementID }
            if (index == -1) {
                Log.d("AchievementManager", "⚠️ Achievement '$achievementID' not found")
                return@launch
            }
            
            val achievement = currentList[index]
            
            // Se è già sbloccato, non fare nulla (evita doppia somma punti)
            if (achievement.isUnlocked) {
                Log.d("AchievementManager", "⚠️ Achievement '$achievementID' already unlocked locally, skipping restore to avoid duplicate points")
                return@launch
            }
            
            // Sblocca l'achievement
            currentList[index] = achievement.copy(
                isUnlocked = true,
                unlockedDate = unlockedDate ?: System.currentTimeMillis(),
                progress = achievement.maxProgress
            )
            _achievements.value = currentList
            
            // Update stats (points) - SOLO se non era già sbloccato
            val pointsToAdd = achievement.points
            _stats.value = _stats.value.copy(
                totalPoints = _stats.value.totalPoints + pointsToAdd,
                achievementUnlockDates = _stats.value.achievementUnlockDates + listOf(unlockedDate ?: System.currentTimeMillis())
            )
            
            // Mark as notified se già notificato in passato (per evitare banner duplicati)
            if (silent && !_stats.value.notifiedAchievements.contains(achievementID)) {
                _stats.value = _stats.value.copy(
                    notifiedAchievements = _stats.value.notifiedAchievements + achievementID
                )
            }
            
            saveAchievements()
            saveStats()
            
            Log.d("AchievementManager", "✅ Restored achievement '$achievementID' (silent=$silent, +$pointsToAdd pts)")
        }
    }
    
    /**
     * Update stats from cloud (used by sync service)
     * Come iOS: usa max per evitare di perdere punti, ma poi ricalcola dalla somma degli achievement
     */
    fun updateStatsFromCloud(cloudStats: UserStats) {
        scope.launch {
            val current = _stats.value
            // Prima usa max come iOS
            val maxPoints = maxOf(current.totalPoints, cloudStats.totalPoints)
            _stats.value = current.copy(
                totalPoints = maxPoints,
                totalLogins = maxOf(current.totalLogins, cloudStats.totalLogins),
                totalRefreshes = maxOf(current.totalRefreshes, cloudStats.totalRefreshes),
                nightLogins = maxOf(current.nightLogins, cloudStats.nightLogins),
                earlyMorningLogins = maxOf(current.earlyMorningLogins, cloudStats.earlyMorningLogins),
                faqVisits = maxOf(current.faqVisits, cloudStats.faqVisits),
                dispenseOpened = current.dispenseOpened + cloudStats.dispenseOpened,
                rulesRead = current.rulesRead + cloudStats.rulesRead,
                loginDates = (current.loginDates + cloudStats.loginDates).distinct().sorted(),
                totalDataLoads = maxOf(current.totalDataLoads, cloudStats.totalDataLoads),
                totalExamsBooked = maxOf(current.totalExamsBooked, cloudStats.totalExamsBooked)
            )
            
            // Dopo aver aggiornato le stats, ricalcola i punti dalla somma degli achievement sbloccati
            // Questo assicura che i punti corrispondano sempre agli achievement sbloccati (come iOS)
            recalculateTotalPoints()
            
            saveStats()
        }
    }
    
    // MARK: - Statistics Helpers
    
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
    
    private fun calculateConsecutiveDays(dates: List<Long>): Int {
        if (dates.isEmpty()) return 0
        
        val calendar = Calendar.getInstance()
        val sortedDates = dates.sorted()
        var consecutiveCount = 1
        var maxConsecutive = 1
        
        for (i in 1 until sortedDates.size) {
            calendar.timeInMillis = sortedDates[i - 1]
            val prevDay = calendar.get(Calendar.DAY_OF_YEAR)
            val prevYear = calendar.get(Calendar.YEAR)
            
            calendar.timeInMillis = sortedDates[i]
            val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
            val currentYear = calendar.get(Calendar.YEAR)
            
            val daysBetween = when {
                currentYear == prevYear -> currentDay - prevDay
                currentYear == prevYear + 1 -> {
                    val daysInPrevYear = calendar.getActualMaximum(Calendar.DAY_OF_YEAR)
                    (daysInPrevYear - prevDay) + currentDay
                }
                else -> -1 // More than a year apart
            }
            
            if (daysBetween == 1) {
                consecutiveCount++
                maxConsecutive = maxOf(maxConsecutive, consecutiveCount)
            } else if (daysBetween > 1) {
                consecutiveCount = 1
            }
        }
        
        return maxConsecutive
    }
    
    // MARK: - Reset (for debugging)
    
    @Suppress("UNUSED_FUNCTION")
    fun resetAllAchievements() {
        scope.launch {
            _achievements.value = AchievementID.entries.map { id ->
                AchievementFactory.createAchievement(id)
            }
            _stats.value = UserStats()
            saveAchievements()
            saveStats()
        }
    }
}
