package com.laba.firenze.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopicManager @Inject constructor(
    private val context: Context
) {
    
    private val notificationManager = NotificationManager()
    
    companion object {
        private const val TAG = "TopicManager"
        private const val PREFS_NAME = "notification_prefs"
    }
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Check if a notification category is enabled
     */
    fun isCategoryEnabled(category: NotificationCategory): Boolean {
        return prefs.getBoolean(category.preferenceKey, true)
    }
    
    /**
     * Set category enabled/disabled
     */
    fun setCategoryEnabled(category: NotificationCategory, enabled: Boolean) {
        prefs.edit().putBoolean(category.preferenceKey, enabled).apply()
    }
    
    /**
     * Check if notifications are globally enabled
     */
    fun isNotificationsEnabled(): Boolean {
        return prefs.getBoolean("notifications.enabled", true)
    }
    
    /**
     * Set global notifications enabled/disabled
     */
    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notifications.enabled", enabled).apply()
    }
    
    /**
     * Update FCM topic subscriptions based on course, year, and notification preferences
     */
    fun updateTopics(
        scope: CoroutineScope,
        course: String?,
        currentYear: Int?,
        isGraduated: Boolean,
        isDocente: Boolean = false
    ) {
        scope.launch {
            try {
                val topics = generateTopics(course, currentYear, isGraduated, isDocente)
                
                // Subscribe to "tutti" topic (all users)
                notificationManager.subscribeToTopics(listOf("tutti"))
                
                // Subscribe to user-specific topics
                if (topics.isNotEmpty()) {
                    notificationManager.subscribeToTopics(topics)
                    Log.d(TAG, "Subscribed to topics: $topics")
                }
                
                // If graduated, subscribe to "laureato" topic
                if (isGraduated) {
                    notificationManager.subscribeToTopics(listOf("laureato"))
                    
                    // Also subscribe to course-specific laureato topic
                    course?.let { 
                        val courseCode = getCourseCode(it)
                        if (courseCode != null) {
                            notificationManager.subscribeToTopics(listOf("laureato_$courseCode"))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating topics", e)
            }
        }
    }
    
    /**
     * Generate topics based on course, year, and enabled categories
     */
    private fun generateTopics(
        course: String?,
        currentYear: Int?,
        _isGraduated: Boolean,
        _isDocente: Boolean
    ): List<String> {
        val topics = mutableListOf<String>()
        
        if (course == null) return emptyList()
        
        val courseCode = getCourseCode(course) ?: return emptyList()
        
        // Get year (clamped between 1 and 3)
        val year = currentYear?.let { maxOf(1, minOf(3, it)) }
        
        // Generate topics for each enabled category
        NotificationCategory.values().forEach { category ->
            if (isCategoryEnabled(category)) {
                // Course-specific topic: {course}_{category}
                topics.add("${courseCode}_${category.topicSuffix}")
                
                // Year-specific topic: {course}_{year}_{category}
                year?.let { y -> topics.add("${courseCode}_${y}_${category.topicSuffix}") }
            }
        }
        
        // Deduplicate topics
        return topics.distinct()
    }
    
    /**
     * Convert course name to code (e.g., "Grafica Digitale" -> "GD")
     */
    private fun getCourseCode(course: String): String? {
        return when {
            course.contains("Grafica Digitale", ignoreCase = true) -> "GD"
            course.contains("Fotografia", ignoreCase = true) -> "FO"
            course.contains("Video", ignoreCase = true) -> "VI"
            course.contains("Pittura", ignoreCase = true) -> "PI"
            course.contains("Scultura", ignoreCase = true) -> "SC"
            course.contains("Pittura", ignoreCase = true) -> "PI"
            else -> null
        }
    }
    
    init {
        Log.d(TAG, "TopicManager initialized")
    }
}

