package com.laba.firenze.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.laba.firenze.ui.common.AppLoadingScreen
import com.laba.firenze.ui.common.LoginScreen
import com.laba.firenze.ui.courses.CoursesScreen
import com.laba.firenze.ui.courses.CourseDetailScreen
import com.laba.firenze.ui.documents.ProgrammiScreen
import com.laba.firenze.ui.documents.DispenseScreen
import com.laba.firenze.ui.documents.DocumentViewerScreen
import com.laba.firenze.ui.exams.ExamsScreen
import com.laba.firenze.ui.exams.ExamDetailScreen
import com.laba.firenze.ui.exams.BookedExamsScreen
import com.laba.firenze.ui.home.HomeScreen
import com.laba.firenze.ui.perte.*
import com.laba.firenze.ui.profile.ProfileScreen
import com.laba.firenze.ui.thesis.ThesisScreen
import com.laba.firenze.ui.thesis.PergamenaScreen
import com.laba.firenze.ui.regolamenti.RegolamentiScreen
import com.laba.firenze.ui.seminars.SeminarsScreen
import com.laba.firenze.ui.seminars.SeminarDetailScreen
import com.laba.firenze.ui.notifications.NotificationSettingsScreen
import com.laba.firenze.ui.notifications.InboxNotificationsScreen
import com.laba.firenze.ui.appearance.AppearanceSettingsScreen
import com.laba.firenze.ui.benefits.AgevolazioniScreen
import com.laba.firenze.ui.profile.StudentCardScreen
import com.laba.firenze.ui.profile.ServiziScreen
import com.laba.firenze.ui.profile.AnagraficaScreen
import com.laba.firenze.ui.library.BibliotecaScreen
import com.laba.firenze.ui.guides.WiFiLABAScreen
import com.laba.firenze.ui.guides.StudentServerGuideScreen
import com.laba.firenze.ui.guides.PrinterGuideScreen
import com.laba.firenze.ui.gamification.AchievementsScreen
import com.laba.firenze.ui.gamification.AchievementUnlockedToast
import com.laba.firenze.ui.gamification.AchievementDetailDialog
import com.laba.firenze.domain.model.Achievement

sealed class LABANavigation(val route: String, val icon: ImageVector, val title: String) {
    object Home : LABANavigation("home", Icons.Default.Home, "Home")
    object Exams : LABANavigation("exams", Icons.AutoMirrored.Filled.Assignment, "Esami")
    object Courses : LABANavigation("courses", Icons.Default.School, "Corsi")
    object Seminars : LABANavigation("seminars", Icons.Default.Event, "Seminari")
    object Profile : LABANavigation("profile", Icons.Default.Person, "Profilo")
    object Benefits : LABANavigation("benefits", Icons.Default.Loyalty, "Convenzioni")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LABANavigation(
    modifier: Modifier = Modifier,
    navigationViewModel: com.laba.firenze.ui.navigation.NavigationViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val activeTabs by navigationViewModel.activeTabs.collectAsStateWithLifecycle()
    
    // Achievement Unlocked Toast Banner (global) - shown in all screens
    val achievementManagerViewModel: com.laba.firenze.ui.gamification.AchievementsViewModel = hiltViewModel()
    val recentlyUnlocked by achievementManagerViewModel.recentlyUnlocked.collectAsStateWithLifecycle()
    val stats by achievementManagerViewModel.stats.collectAsStateWithLifecycle()
    var selectedAchievement by remember { 
        mutableStateOf<Achievement?>(null) 
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                activeTabs.forEach { tab ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                tab.icon, 
                                contentDescription = tab.title,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            ) 
                        },
                        label = { 
                            Text(
                                tab.title,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            if (currentDestination?.route == tab.route) {
                                return@NavigationBarItem
                            }
                            
                            val isInSubsection = currentDestination?.route?.startsWith(tab.route + "/") == true
                            if (isInSubsection) {
                                navController.navigate(tab.route) {
                                    popUpTo(tab.route) {
                                        inclusive = false
                                    }
                                }
                                return@NavigationBarItem
                            }
                            
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = LABANavigation.Home.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) }
        ) {
            composable(
                route = LABANavigation.Home.route,
                enterTransition = { fadeIn(animationSpec = tween(200)) },
                exitTransition = { fadeOut(animationSpec = tween(200)) }
            ) {
                HomeScreen(navController)
            }
            composable(
                route = LABANavigation.Exams.route,
                enterTransition = { fadeIn(animationSpec = tween(200)) },
                exitTransition = { fadeOut(animationSpec = tween(200)) }
            ) {
                ExamsScreen(navController)
            }
            composable(
                route = "exam_detail/{examId}",
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 4 }, animationSpec = tween(300)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 4 }, animationSpec = tween(300)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) }
            ) { backStackEntry ->
                val examId = backStackEntry.arguments?.getString("examId") ?: ""
                ExamDetailScreen(examId, navController)
            }
            composable(
                route = "esami-prenotati",
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 4 }, animationSpec = tween(300)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 4 }, animationSpec = tween(300)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) }
            ) {
                BookedExamsScreen(navController)
            }
            composable(
                route = LABANavigation.Courses.route,
                enterTransition = { fadeIn(animationSpec = tween(200)) },
                exitTransition = { fadeOut(animationSpec = tween(200)) }
            ) {
                CoursesScreen(navController)
            }
            composable(
                route = "course_detail/{courseId}",
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 4 }, animationSpec = tween(300)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 4 }, animationSpec = tween(300)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) }
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
                CourseDetailScreen(navController, courseId)
            }
            composable(
                route = LABANavigation.Seminars.route,
                enterTransition = { fadeIn(animationSpec = tween(200)) },
                exitTransition = { fadeOut(animationSpec = tween(200)) }
            ) {
                SeminarsScreen(navController)
            }
            composable(
                route = "seminar-detail/{seminarId}",
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 4 }, animationSpec = tween(300)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 4 }, animationSpec = tween(300)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) }
            ) { backStackEntry ->
                val seminarId = backStackEntry.arguments?.getString("seminarId") ?: ""
                SeminarDetailScreen(navController, seminarId)
            }
            composable(
                route = LABANavigation.Profile.route,
                enterTransition = { fadeIn(animationSpec = tween(200)) },
                exitTransition = { fadeOut(animationSpec = tween(200)) }
            ) {
                ProfileScreen(navController)
            }
            
            // Per Te Section Routes
            composable("calcola-voto-laurea") {
                CalcolaVotoLaureaScreen(navController)
            }
            composable("simula-media") {
                SimulaMediaScreen(navController)
            }
            composable("strumentazione") {
                StrumentazioneScreen(navController)
            }
            composable("prenotazione-aule") {
                PrenotazioneAuleScreen(navController)
            }
            composable("grades/trend") {
                GradeTrendScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToExams = { navController.navigate(LABANavigation.Exams.route) }
                )
            }
            composable("thesis") {
                ThesisScreen(navController)
            }
            composable("pergamena") {
                PergamenaScreen(navController)
            }
            composable("regulations") {
                RegolamentiScreen(navController)
            }
            
            // Document Section Routes
            composable("materials") {
                ProgrammiScreen(navController)
            }
            composable("handouts") {
                DispenseScreen(navController)
            }
            composable("document_viewer/{allegatoOid}/{title}") { backStackEntry ->
                val allegatoOid = backStackEntry.arguments?.getString("allegatoOid") ?: ""
                val title = backStackEntry.arguments?.getString("title") ?: ""
                DocumentViewerScreen(navController, allegatoOid, title)
            }
            
            // Notification Settings
            composable("notifications") {
                NotificationSettingsScreen(navController)
            }
            
            // Inbox Notifications
            composable("inbox") {
                InboxNotificationsScreen(navController)
            }
            
            // Appearance Settings
            composable("appearance") {
                AppearanceSettingsScreen(navController)
            }
            
            composable("color_settings") {
                com.laba.firenze.ui.appearance.ColorSettingsScreen(navController)
            }
            
            composable("animation_settings") {
                com.laba.firenze.ui.appearance.AnimationSettingsScreen(navController)
            }
            
            composable("navigation_customization") {
                com.laba.firenze.ui.appearance.NavigationCustomizationScreen(navController, navigationViewModel.navigationManager)
            }

            // New Features
            composable(LABANavigation.Benefits.route) {
                AgevolazioniScreen(navController)
            }
            composable("student_card") {
                StudentCardScreen(navController)
            }
            composable("achievements") {
                AchievementsScreen(navController = navController)
            }
            composable("group_selection") {
                com.laba.firenze.ui.profile.GroupSelectionScreen(navController = navController)
            }
            composable("servizi") {
                ServiziScreen(navController)
            }
            composable("faq") {
                com.laba.firenze.ui.faq.FAQScreen(navController)
            }
            composable("privacy-security") {
                com.laba.firenze.ui.profile.PrivacySecurityScreen(navController)
            }
            composable("debug") {
                com.laba.firenze.ui.profile.DebugScreen(navController)
            }
            
            // Anagrafica
            composable("anagrafica") {
                AnagraficaScreen(navController)
            }
            
            // Biblioteca
            composable("biblioteca") {
                BibliotecaScreen(navController)
            }
            
            // Guide
            composable("wifi-laba") {
                WiFiLABAScreen(navController)
            }
            composable("student-server-guide") {
                StudentServerGuideScreen(navController)
            }
            composable("printer-guide") {
                PrinterGuideScreen(navController)
            }
        }
        
        // Global Achievement Unlocked Toast Banner (shown in all screens) - in basso
        recentlyUnlocked?.let { achievement ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                AchievementUnlockedToast(
                    achievement = achievement,
                    onDismiss = { achievementManagerViewModel.dismissUnlockedToast() },
                    onClick = { selectedAchievement = achievement }
                )
            }
        }
        
        // Achievement Detail Dialog (global)
        selectedAchievement?.let { achievement ->
            AchievementDetailDialog(
                achievement = achievement,
                onDismiss = { selectedAchievement = null },
                stats = stats
            )
        }
        }
    }
}
