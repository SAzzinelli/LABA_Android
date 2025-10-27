package com.laba.firenze.ui.notifications.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class InboxNotificationsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {
    
    data class NotificationDisplayItem(
        val id: String,
        val title: String,
        val body: String,
        val timestamp: Long,
        val isRead: Boolean,
        val dateString: String
    )
    
    private val _notifications = MutableStateFlow<List<NotificationDisplayItem>>(emptyList())
    val notifications: StateFlow<List<NotificationDisplayItem>> = _notifications
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    init {
        loadNotifications()
    }
    
    private fun loadNotifications() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val items = sessionRepository.notifications.value
                
                _notifications.value = items.map { item ->
                    val date = try {
                        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        format.parse(item.data) ?: Date(System.currentTimeMillis())
                    } catch (e: Exception) {
                        Date(System.currentTimeMillis())
                    }
                    
                    NotificationDisplayItem(
                        id = item.id.toString(),
                        title = item.titolo,
                        body = item.messaggio,
                        timestamp = date.time,
                        isRead = item.isRead,
                        dateString = formatDate(date)
                    )
                }
            } catch (e: Exception) {
                println("Error loading notifications: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun setRead(id: String, read: Boolean) {
        viewModelScope.launch {
            try {
                val intId = id.toIntOrNull() ?: return@launch
                sessionRepository.markNotificationRead(intId, read)
                loadNotifications()
            } catch (e: Exception) {
                println("Error marking notification read: ${e.message}")
            }
        }
    }
    
    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                sessionRepository.markAllNotificationsRead()
                loadNotifications()
            } catch (e: Exception) {
                println("Error marking all notifications read: ${e.message}")
            }
        }
    }
    
    fun dismiss(id: String) {
        viewModelScope.launch {
            try {
                val intId = id.toIntOrNull() ?: return@launch
                sessionRepository.deleteNotification(intId)
                loadNotifications()
            } catch (e: Exception) {
                println("Error dismissing notification: ${e.message}")
            }
        }
    }
    
    private fun formatDate(date: Date): String {
        val now = System.currentTimeMillis()
        val diff = now - date.time
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            minutes < 1 -> "Ora"
            hours < 1 -> "$minutes min fa"
            days < 1 -> "$hours ore fa"
            days < 7 -> "$days giorni fa"
            else -> SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN).format(date)
        }
    }
}

