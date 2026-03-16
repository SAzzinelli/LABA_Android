package com.laba.firenze.data.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.laba.firenze.data.api.LogosUniAPIClient
import com.laba.firenze.data.local.SessionTokenManager
import com.laba.firenze.data.local.TokenStore
import com.laba.firenze.data.repository.SupabaseRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LABAFirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "LABAFCMService"
        private const val CHANNEL_ID = "laba_notifications"
        private const val CHANNEL_NAME = "LABA Notifiche"
    }
    
    @Inject
    lateinit var apiClient: LogosUniAPIClient
    
    @Inject
    lateinit var tokenStore: TokenStore
    
    @Inject
    lateinit var supabaseRepository: SupabaseRepository
    
    @Inject
    lateinit var sessionTokenManager: SessionTokenManager
    
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    
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
        
        // Invia il token al server (v3) e upsert su Supabase con display_name (portale notifiche) - allineato a iOS
        serviceScope.launch {
            try {
                val prefs = getSharedPreferences("laba_preferences", Context.MODE_PRIVATE)
                val version = prefs.getString("laba.apiVersion", "v2") ?: "v2"
                
                if (version == "v3") {
                    val accessToken = tokenStore.getCurrentAccessToken()
                    if (accessToken.isNotEmpty()) {
                        val success = apiClient.setFcmToken(accessToken, token)
                        if (success) {
                            Log.d(TAG, "✅ FCM token aggiornato con successo sul server")
                        } else {
                            Log.w(TAG, "⚠️ Fallito aggiornamento FCM token sul server")
                        }
                    } else {
                        Log.d(TAG, "ℹ️ Utente non loggato, token FCM verrà inviato al prossimo login")
                    }
                }
                // Supabase: dropdown destinatari nel portale notifiche (come iOS NotificheTokenService)
                val email = sessionTokenManager.getStoredUserEmail()
                if (!email.isNullOrEmpty()) {
                    supabaseRepository.upsertFcmToken(email, token, sessionTokenManager.getStoredUserDisplayName())
                    Log.d(TAG, "✅ FCM token upsert su Supabase (display_name per portale)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Errore invio FCM token: ${e.message}", e)
            }
        }
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

