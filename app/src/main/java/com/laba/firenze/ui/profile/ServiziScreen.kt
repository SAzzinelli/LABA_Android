package com.laba.firenze.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Schermata Servizi (identica a iOS ServiziView)
 * Contiene toggle per funzionalità: Orari, Traguardi, Esami prenotati
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiziScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("LABA_PREFS", android.content.Context.MODE_PRIVATE)
    
    // State per toggle
    var timetableEnabled by remember {
        mutableStateOf(
            sharedPrefs.getBoolean("laba.timetable.enabled", false)
        )
    }
    var timetableDisclaimerAccepted by remember {
        mutableStateOf(
            sharedPrefs.getBoolean("laba.timetable.disclaimer.accepted", false)
        )
    }
    var achievementsEnabled by remember {
        mutableStateOf(
            sharedPrefs.getBoolean("laba.achievements.enabled", false)
        )
    }
    var achievementsDisclaimerAccepted by remember {
        mutableStateOf(
            sharedPrefs.getBoolean("laba.achievements.disclaimer.accepted", false)
        )
    }
    var bookedExamsEnabled by remember {
        mutableStateOf(
            sharedPrefs.getBoolean("laba.bookedExams.enabled", true) // Default true
        )
    }
    
    // State per alert
    var showTimetableDisclaimer by remember { mutableStateOf(false) }
    var showAchievementsDisclaimer by remember { mutableStateOf(false) }
    
    // Inizializza default per bookedExams se non esiste
    LaunchedEffect(Unit) {
        if (!sharedPrefs.contains("laba.bookedExams.enabled")) {
            sharedPrefs.edit { putBoolean("laba.bookedExams.enabled", true) }
            bookedExamsEnabled = true
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Funzionalità e Servizi") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Funzionalità Section
            item {
                Section(title = "Funzionalità") {
                    // Beta Info KPI (simplified version)
                    BetaInfoKPI()
                    
                    // Toggle Orari
                    SwitchItem(
                        checked = timetableEnabled,
                        onCheckedChange = { newValue ->
                            if (newValue) {
                                if (!timetableDisclaimerAccepted) {
                                    showTimetableDisclaimer = true
                                } else {
                                    timetableEnabled = true
                                    sharedPrefs.edit { putBoolean("laba.timetable.enabled", true) }
                                }
                            } else {
                                timetableEnabled = false
                                sharedPrefs.edit { putBoolean("laba.timetable.enabled", false) }
                            }
                        },
                        title = "Orari",
                        icon = Icons.Default.CalendarMonth,
                        iconColor = Color(0xFF9C27B0), // Purple
                        showBeta = true
                    )
                    
                    // Toggle Traguardi
                    SwitchItem(
                        checked = achievementsEnabled,
                        onCheckedChange = { newValue ->
                            if (newValue) {
                                if (!achievementsDisclaimerAccepted) {
                                    showAchievementsDisclaimer = true
                                } else {
                                    achievementsEnabled = true
                                    sharedPrefs.edit { putBoolean("laba.achievements.enabled", true) }
                                }
                            } else {
                                achievementsEnabled = false
                                sharedPrefs.edit { putBoolean("laba.achievements.enabled", false) }
                            }
                        },
                        title = "Traguardi",
                        icon = Icons.Default.EmojiEvents,
                        iconColor = Color(0xFF9C27B0), // Purple
                        showBeta = true
                    )
                    
                    // Toggle Esami prenotati in home
                    SwitchItem(
                        checked = bookedExamsEnabled,
                        onCheckedChange = { newValue ->
                            bookedExamsEnabled = newValue
                            sharedPrefs.edit { putBoolean("laba.bookedExams.enabled", newValue) }
                        },
                        title = "Esami prenotati",
                        icon = Icons.Default.CalendarMonth,
                        iconColor = Color(0xFFE53935), // Rosso
                        showBeta = false
                    )
                }
            }
            
            // Accesso e Connessioni Section
            item {
                Section(title = "Accesso e Connessioni") {
                    NavigationItem(
                        title = "Wi-Fi LABA",
                        icon = Icons.Default.Wifi,
                        onClick = { navController.navigate("wifi-laba") }
                    )
                    NavigationItem(
                        title = "Server Studenti",
                        icon = Icons.Default.CloudQueue,
                        onClick = { navController.navigate("student-server-guide") }
                    )
                    NavigationItem(
                        title = "Guida alla stampa",
                        icon = Icons.Default.Print,
                        onClick = { navController.navigate("printer-guide") }
                    )
                }
            }
        }
    }
    
    // Alert per Orari Beta
    if (showTimetableDisclaimer) {
        AlertDialog(
            onDismissRequest = { showTimetableDisclaimer = false },
            title = { Text("Orari in Beta") },
            text = {
                Text("Gli orari sono in fase di inserimento e non si assicura la correttezza. Per informazioni ufficiali, consulta l'orario fornito dalla segreteria.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        timetableEnabled = true
                        timetableDisclaimerAccepted = true
                        sharedPrefs.edit {
                            putBoolean("laba.timetable.enabled", true)
                            putBoolean("laba.timetable.disclaimer.accepted", true)
                        }
                        showTimetableDisclaimer = false
                    }
                ) {
                    Text("Attiva")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        timetableEnabled = false
                        showTimetableDisclaimer = false
                    }
                ) {
                    Text("Annulla")
                }
            }
        )
    }
    
    // Alert per Traguardi Beta
    if (showAchievementsDisclaimer) {
        AlertDialog(
            onDismissRequest = { showAchievementsDisclaimer = false },
            title = { Text("Traguardi in Beta") },
            text = {
                Text("Il sistema di traguardi è in fase di sviluppo e potrebbe subire modifiche. I tuoi progressi verranno salvati localmente.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        achievementsEnabled = true
                        achievementsDisclaimerAccepted = true
                        sharedPrefs.edit {
                            putBoolean("laba.achievements.enabled", true)
                            putBoolean("laba.achievements.disclaimer.accepted", true)
                        }
                        showAchievementsDisclaimer = false
                    }
                ) {
                    Text("Attiva")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        achievementsEnabled = false
                        showAchievementsDisclaimer = false
                    }
                ) {
                    Text("Annulla")
                }
            }
        )
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            content()
        }
    }
}

@Composable
private fun SwitchItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    showBeta: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            if (showBeta) {
                BetaBadge()
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun NavigationItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun BetaBadge() {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
    ) {
        Text(
            text = "BETA",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun BetaInfoKPI() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Funzionalità in sviluppo",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Alcune funzionalità sono ancora in fase di sviluppo e potrebbero subire modifiche.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
