package com.laba.firenze.data.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LABAFirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "LABAFCMService"
        private const val CHANNEL_ID = "laba_notifications"
        private const val CHANNEL_NAME = "LABA Notifiche"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        Log.d(TAG, "Message from: ${message.from}")
        
        // Check if message contains data payload
        message.data.isNotEmpty().let {
            Log.d(TAG, "Message data: ${message.data}")
        }
        
        // Check if message contains notification payload
        message.notification?.let {
            val title = it.title ?: "LABA"
            val body = it.body ?: ""
            
            Log.d(TAG, "Notification title: $title")
            Log.d(TAG, "Notification body: $body")
            
            // Show the notification
            showNotification(title, body, message.data)
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        // TODO: Send token to server if needed
    }
    
    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Use custom icon
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifiche dall'app LABA"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}

