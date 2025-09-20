package com.laba.firenze.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.laba.firenze.R
import com.laba.firenze.ui.common.AppLoadingScreen
import com.laba.firenze.ui.common.LoginScreen
import com.laba.firenze.ui.courses.CoursesScreen
import com.laba.firenze.ui.documents.*
import com.laba.firenze.ui.exams.ExamsScreen
import com.laba.firenze.ui.exams.ExamDetailScreen
import com.laba.firenze.ui.home.HomeScreen
import com.laba.firenze.ui.perte.*
import com.laba.firenze.ui.profile.ProfileScreen
import com.laba.firenze.ui.seminars.SeminarsScreen

sealed class LABANavigation(val route: String, val icon: ImageVector, val title: String) {
    object Home : LABANavigation("home", Icons.Default.Home, "Home")
    object Exams : LABANavigation("exams", Icons.Default.Assignment, "Esami")
    object Courses : LABANavigation("courses", Icons.Default.School, "Corsi")
    object Seminars : LABANavigation("seminars", Icons.Default.Event, "Seminari")
    object Profile : LABANavigation("profile", Icons.Default.Person, "Profilo")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LABANavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val items = listOf(
        LABANavigation.Home,
        LABANavigation.Exams,
        LABANavigation.Courses,
        LABANavigation.Seminars,
        LABANavigation.Profile
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = LABANavigation.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(LABANavigation.Home.route) {
                HomeScreen(navController)
            }
            composable(LABANavigation.Exams.route) {
                ExamsScreen(navController)
            }
            composable("exam_detail/{examId}") { backStackEntry ->
                val examId = backStackEntry.arguments?.getString("examId") ?: ""
                ExamDetailScreen(examId, navController)
            }
            composable(LABANavigation.Courses.route) {
                CoursesScreen(navController)
            }
            composable(LABANavigation.Seminars.route) {
                SeminarsScreen(navController)
            }
            composable(LABANavigation.Profile.route) {
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
            
            // Document Section Routes
            composable("materials") {
                ProgrammiScreen(navController)
            }
            composable("handouts") {
                DispenseScreen(navController)
            }
            composable("thesis") {
                TesiScreen(navController)
            }
        }
    }
}
