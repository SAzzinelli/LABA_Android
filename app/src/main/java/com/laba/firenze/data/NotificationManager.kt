package com.laba.firenze.data

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManager @Inject constructor() {
    
    companion object {
        private const val TAG = "NotificationManager"
    }
    
    /**
     * Subscribe to a list of topics
     */
    suspend fun subscribeToTopics(topics: List<String>) {
        topics.forEach { topic ->
            try {
                FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
                Log.d(TAG, "Subscribed to topic: $topic")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to subscribe to topic: $topic", e)
            }
        }
    }
    
    /**
     * Unsubscribe from a list of topics
     */
    suspend fun unsubscribeFromTopics(topics: List<String>) {
        topics.forEach { topic ->
            try {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).await()
                Log.d(TAG, "Unsubscribed from topic: $topic")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to unsubscribe from topic: $topic", e)
            }
        }
    }
    
    /**
     * Get the current FCM token
     */
    suspend fun getToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get FCM token", e)
            null
        }
    }
}

