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
        private const val SUBSCRIBED_TOPICS_KEY = "subscribed_topics" // Lista topic effettivamente iscritti
    }
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Save the list of currently subscribed topics
     */
    private fun saveSubscribedTopics(topics: List<String>) {
        prefs.edit().putStringSet(SUBSCRIBED_TOPICS_KEY, topics.toSet()).apply()
        Log.d(TAG, "Saved ${topics.size} subscribed topics")
    }
    
    /**
     * Get the list of currently subscribed topics
     */
    fun getSubscribedTopics(): Set<String> {
        return prefs.getStringSet(SUBSCRIBED_TOPICS_KEY, emptySet()) ?: emptySet()
    }
    
    /**
     * Get the list of topics that SHOULD be subscribed based on current preferences
     * This is used to show "Topic Salvati" (saved preferences) vs "iscritto ai topic" (actual FCM subscriptions)
     */
    fun getTopicsBasedOnPreferences(course: String?, currentYear: Int?, isGraduated: Boolean): Set<String> {
        val allCategories = NotificationCategory.values().toSet()
        val enabledCategories = allCategories.filter { isCategoryEnabled(it) }
        
        val savedTopics = mutableSetOf<String>()
        
        course?.let {
            val courseCode = getCourseCode(it)
            if (courseCode != null) {
                // Add course-specific topics for all enabled categories
                enabledCategories.forEach { category ->
                    savedTopics.add("${courseCode}_${category.topicSuffix}")
                }
                
                // Add year-specific topics
                val year = currentYear?.let { maxOf(1, minOf(3, it)) }
                year?.let { y ->
                    enabledCategories.forEach { category ->
                        savedTopics.add("${courseCode}_${y}_${category.topicSuffix}")
                    }
                }
            }
        }
        
        // Add global topics
        savedTopics.add("tutti")
        if (isGraduated) {
            savedTopics.add("laureato")
            course?.let { 
                val courseCode = getCourseCode(it)
                courseCode?.let { cc ->
                    savedTopics.add("laureato_$cc")
                }
            }
        }
        
        return savedTopics
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
                // Get old subscribed topics
                val oldSubscribedTopics = getSubscribedTopics().toMutableSet()
                
                // Generate new topics
                val topics = generateTopics(course, currentYear, isGraduated, isDocente)
                
                // Build the complete list of topics to subscribe to
                val topicsToSubscribe = mutableListOf<String>()
                
                // Subscribe to "tutti" topic (all users)
                topicsToSubscribe.add("tutti")
                
                // Add user-specific topics
                topicsToSubscribe.addAll(topics)
                
                // If graduated, add laureato topics
                if (isGraduated) {
                    topicsToSubscribe.add("laureato")
                    course?.let { 
                        val courseCode = getCourseCode(it)
                        if (courseCode != null) {
                            topicsToSubscribe.add("laureato_$courseCode")
                        }
                    }
                }
                
                // Deduplicate
                val newSubscribedTopics = topicsToSubscribe.distinct()
                
                // Unsubscribe from old topics that are no longer needed
                val topicsToUnsubscribe = oldSubscribedTopics - newSubscribedTopics.toSet()
                if (topicsToUnsubscribe.isNotEmpty()) {
                    notificationManager.unsubscribeFromTopics(topicsToUnsubscribe.toList())
                    Log.d(TAG, "Unsubscribed from old topics: $topicsToUnsubscribe")
                }
                
                // Subscribe to new topics
                notificationManager.subscribeToTopics(newSubscribedTopics)
                Log.d(TAG, "Subscribed to topics: $newSubscribedTopics")
                
                // Save the new subscribed topics
                saveSubscribedTopics(newSubscribedTopics)
                
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


