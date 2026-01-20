package com.laba.firenze.domain.model

import kotlinx.serialization.Serializable

/**
 * User Stats for Achievement Tracking
 * Conforme a iOS UserStats struct
 */
@Serializable
data class UserStats(
    var totalPoints: Int = 0,
    var totalDataLoads: Int = 0,
    var totalExamsBooked: Int = 0,
    var firstLoginDate: Long? = null, // Timestamp
    
    // App Usage tracking
    var totalLogins: Int = 0,
    var lastLoginDate: Long? = null, // Timestamp
    var loginDates: List<Long> = emptyList(), // List of timestamps for consecutive days
    var nightLogins: Int = 0, // After midnight
    var earlyMorningLogins: Int = 0, // Before 7am
    var totalRefreshes: Int = 0,
    
    // Sections visited
    var visitedHome: Boolean = false,
    var visitedExams: Boolean = false,
    var visitedCorsi: Boolean = false,
    var visitedSeminari: Boolean = false,
    var visitedProfile: Boolean = false,
    
    // Content accessed
    var dispenseOpened: Set<String> = emptySet(), // IDs of opened dispense
    var rulesRead: Set<String> = emptySet(), // IDs of read rules
    var faqVisits: Int = 0,
    
    // Notifications
    var notificationsReadOnTime: Int = 0, // Streak of days reading all notifications within 24h
    var lastNotificationCheckDate: Long? = null, // Timestamp
    
    // Achievement tracking
    var achievementUnlockDates: List<Long> = emptyList(), // List of unlock timestamps
    var notifiedAchievements: Set<String> = emptySet(), // Achievement IDs already notified
    var achievementEventDates: Map<String, Long> = emptyMap(), // Achievement ID -> Timestamp of actual event (not unlock)
    
    // Birthday (if available from profile)
    var birthday: Long? = null, // Timestamp
    
    // Exams history for tracking
    var lastExamDates: List<Long> = emptyList(), // List of exam dates
    var examSessionCounts: Map<String, Int> = emptyMap(), // "2024-06": 3 exams
    
    // Exam-specific event dates
    var first18Date: Long? = null, // Timestamp
    var first30Date: Long? = null, // Timestamp
    var firstLodeDate: Long? = null, // Timestamp
    var graduationDate: Long? = null // Timestamp
)
