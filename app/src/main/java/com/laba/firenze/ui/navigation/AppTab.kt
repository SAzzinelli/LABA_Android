package com.laba.firenze.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppTab(
    val route: String, 
    val title: String, 
    val icon: ImageVector, 
    val isLocked: Boolean = false
) {
    HOME("home", "Home", Icons.Default.Home, true),
    COURSES("courses", "Corsi", Icons.Default.School), // LABANavigation.Courses
    EXAMS("exams", "Esami", Icons.AutoMirrored.Filled.Assignment), // LABANavigation.Exams
    SEMINARS("seminars", "Seminari", Icons.Default.Event), // LABANavigation.Seminars
    PROFILE("profile", "Profilo", Icons.Default.Person, true),
    
    // Optional / Hidden by default usually
    THESIS("thesis", "Tesi", Icons.Default.School), // Using School as graduationcap not always available, or check if available
    HANDOUTS("handouts", "Dispense", Icons.Default.Description),
    REGULATIONS("regulations", "Regolamenti", Icons.AutoMirrored.Filled.Rule), // Or Gavel? Rule is good
    PROGRAMS("materials", "Programmi", Icons.AutoMirrored.Filled.MenuBook); // materials route

    companion object {
        fun fromRoute(route: String): AppTab? {
            return values().find { it.route == route }
        }
    }
}
