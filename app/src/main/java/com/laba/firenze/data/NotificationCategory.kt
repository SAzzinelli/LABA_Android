package com.laba.firenze.data

enum class NotificationCategory(
    val value: String,
    val displayName: String,
    val preferenceKey: String
) {
    EXAMS("esami", "Esami", "notifications.exams"),
    PROFESSORS("comunicazioni", "Comunicazioni dai docenti", "notifications.professors"),
    GENERAL("generali", "Comunicazioni generali", "notifications.general"),
    EVENTS("eventi_laba", "Eventi LABA", "notifications.events"),
    SEMINARS("seminari", "Seminari", "notifications.seminars"),
    GRADES("voti", "Voti", "notifications.grades"),
    MATERIALS("materiali", "Dispense e materiali", "notifications.materials"),
    ABSENCES("assenze", "Assenze docenti", "notifications.absences");
    
    val topicSuffix: String get() = value
}

