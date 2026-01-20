package com.laba.firenze.ui.gamification

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Helper per convertire icone SF Symbol (iOS) in Material Icons (Android)
 */
object AchievementIconHelper {
    /**
     * Converte un nome SF Symbol in un'icona Material Android
     */
    fun getIconForSFSymbol(sfSymbol: String): ImageVector {
        return when (sfSymbol.lowercase()) {
            // Primi Passi
            "hand.wave.fill", "hand.wave" -> Icons.Default.WavingHand
            "arrow.down.circle.fill", "arrow.down.circle" -> Icons.Default.Download
            
            // Esami
            "graduationcap.fill", "graduationcap" -> Icons.Default.School
            "18.circle.fill", "18.circle" -> Icons.Default.Numbers
            "30.circle.fill", "30.circle" -> Icons.Default.Numbers
            "star.circle.fill", "star.circle" -> Icons.Default.Star
            "checkmark.circle.fill", "checkmark.circle" -> Icons.Default.CheckCircle
            "flag.checkered.circle.fill", "flag.checkered.circle" -> Icons.Default.Flag
            "1.circle.fill", "1.circle" -> Icons.Default.LooksOne
            "2.circle.fill", "2.circle" -> Icons.Default.LooksTwo
            "3.circle.fill", "3.circle" -> Icons.Default.Looks3
            
            // Performance
            "flame.fill", "flame" -> Icons.Default.LocalFireDepartment
            "star.leadinghalf.filled", "star.leadinghalf" -> Icons.Default.StarHalf
            "target" -> Icons.Default.TrackChanges
            "figure.run" -> Icons.Default.DirectionsRun
            
            // Seminari
            "person.fill", "person" -> Icons.Default.Person
            "person.2.fill", "person.2" -> Icons.Default.Group
            "person.3.fill", "person.3" -> Icons.Default.Groups
            "calendar.badge.clock" -> Icons.Default.Event
            
            // CFA
            "chart.bar.fill", "chart.bar" -> Icons.Default.BarChart
            "plus.circle.fill", "plus.circle" -> Icons.Default.AddCircle
            
            // App Usage
            "calendar.circle.fill", "calendar.circle" -> Icons.Default.CalendarToday
            "bell.badge.fill", "bell.badge" -> Icons.Default.NotificationsActive
            "moon.stars.fill", "moon.stars" -> Icons.Default.DarkMode
            "sunrise.fill", "sunrise" -> Icons.Default.WbSunny
            "iphone.circle.fill", "iphone.circle", "iphone" -> Icons.Default.PhoneAndroid
            "arrow.clockwise.circle.fill", "arrow.clockwise.circle" -> Icons.Default.Refresh
            "map.fill", "map" -> Icons.Default.Map
            "book.fill", "book" -> Icons.Default.Book
            "doc.text.fill", "doc.text" -> Icons.Default.Description
            "questionmark.circle.fill", "questionmark.circle" -> Icons.Default.Help
            
            // Easter Eggs
            "dice.fill", "dice" -> Icons.Default.Casino
            "rainbow" -> Icons.Default.Palette
            "birthday.cake.fill", "birthday.cake" -> Icons.Default.Cake
            "gift.fill", "gift" -> Icons.Default.CardGiftcard
            "beach.umbrella.fill", "beach.umbrella" -> Icons.Default.BeachAccess
            "sun.max.fill", "sun.max" -> Icons.Default.WbSunny
            "sparkles" -> Icons.Default.AutoAwesome
            
            // Meta
            "rosette" -> Icons.Default.MilitaryTech
            "diamond.fill", "diamond" -> Icons.Default.Diamond
            "crown.fill", "crown" -> Icons.Default.Star
            "scope" -> Icons.Default.GpsFixed
            "star.fill", "star" -> Icons.Default.Star
            
            // Traguardi
            "trophy.fill", "trophy" -> Icons.Default.EmojiEvents
            
            // Default fallback
            else -> Icons.Default.Star
        }
    }
}
