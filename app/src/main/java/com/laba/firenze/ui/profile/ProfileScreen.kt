package com.laba.firenze.ui.profile

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.net.toUri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.laba.firenze.ui.tutorial.TutorialScreen

/**
 * Estrae le matricole triennio e biennio dalla stringa.
 * Restituisce una coppia (triennio, biennio) dove uno o entrambi possono essere null.
 */
private fun parseMatricole(matricola: String?): Pair<String?, String?> {
    if (matricola.isNullOrBlank()) return Pair(null, null)
    
    val lowerMatricola = matricola.lowercase()
    var triennio: String? = null
    var biennio: String? = null
    
    // Se contiene "triennio"
    if (lowerMatricola.contains("triennio")) {
        val triennioIndex = lowerMatricola.indexOf("triennio")
        // Cerca la matricola dopo "triennio"
        var afterTriennio = matricola.substring(triennioIndex + "triennio".length).trim()
        // Rimuovi parentesi iniziali
        afterTriennio = afterTriennio.trimStart('(', ' ', ')')
        
        // Se c'è "biennio" dopo, estrai fino a lì
        val biennioIndexInAfter = afterTriennio.lowercase().indexOf("biennio")
        if (biennioIndexInAfter >= 0) {
            triennio = afterTriennio.substring(0, biennioIndexInAfter).trim()
            // Rimuovi parentesi finali
            triennio = triennio.trimEnd('(', ' ', ')').replace(Regex("""[()]"""), "").trim()
        } else {
            // Non c'è biennio dopo, prendi tutto
            triennio = afterTriennio.replace(Regex("""[()]"""), "").trim()
        }
    }
    
    // Se contiene "biennio"
    if (lowerMatricola.contains("biennio")) {
        val biennioIndex = lowerMatricola.indexOf("biennio")
        // Cerca la matricola dopo "biennio"
        var afterBiennio = matricola.substring(biennioIndex + "biennio".length).trim()
        // Rimuovi parentesi iniziali
        afterBiennio = afterBiennio.trimStart('(', ' ', ')')
        // Rimuovi parentesi finali e spazi
        biennio = afterBiennio.replace(Regex("""[()]"""), "").trim()
    }
    
    // Se non contiene né triennio né biennio, potrebbe essere una sola matricola
    if (triennio == null && biennio == null) {
        // Restituisci come biennio se non contiene "triennio", altrimenti come triennio
        if (!lowerMatricola.contains("triennio")) {
            biennio = matricola.trim()
        } else {
            triennio = matricola.trim()
        }
    }
    
    return Pair(triennio, biennio)
}

/**
 * Verifica se ci sono entrambe le matricole (triennio e biennio).
 */
private fun hasMultipleMatricole(matricola: String?): Boolean {
    if (matricola.isNullOrBlank()) return false
    val lower = matricola.lowercase()
    return lower.contains("triennio") && lower.contains("biennio")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showTutorial by remember { mutableStateOf(false) }
    var showGroupDisabledAlert by remember { mutableStateOf(false) }
    var showMatricoleDialog by remember { mutableStateOf(false) }
    
    // Track section visit
    LaunchedEffect(Unit) {
        viewModel.trackSectionVisit("profile")
    }
    
    // Logic to disable group selection
    val status = uiState.userProfile?.status?.lowercase() ?: ""
    val currentYear = uiState.userProfile?.currentYear
    val isGraduated = status.contains("laureat")
    val isFuoricorso = currentYear == null && !isGraduated
    val shouldDisableGroup = isGraduated || isFuoricorso
    
    // Achievement data
    val sharedPrefs: SharedPreferences = remember { 
        context.getSharedPreferences("LABA_PREFS", android.content.Context.MODE_PRIVATE)
    }
    val achievementsEnabled = remember { 
        sharedPrefs.getBoolean("laba.achievements.enabled", false) 
    }
    val achievements by viewModel.achievements.collectAsState()
    val totalPoints by viewModel.totalPoints.collectAsState()
    val unlockedCount = achievements.count { it.isUnlocked }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profilo") },
                actions = {
                    IconButton(onClick = { navController.navigate("inbox") }) {
                        Icon(Icons.Default.Notifications, "Notifiche")
                        // TODO: Show badge if there are unread notifications
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
        // Profile Header
        item {
            ProfileHeader(
                onMatricoleClick = { showMatricoleDialog = true },
                userProfile = uiState.userProfile,
                achievementsEnabled = achievementsEnabled,
                unlockedCount = unlockedCount,
                totalPoints = totalPoints,
                onAchievementsClick = { navController.navigate("achievements") },
                onAnagraficaClick = { navController.navigate("anagrafica") },
                onServiziClick = { navController.navigate("servizi") }
            )
        }
        
        // La tua carriera Section (senza Traguardi, che è nel widget)
        item {
            ProfileSection(
                title = "La tua carriera",
                items = listOf(
                    ProfileMenuActionItem(
                        title = "Tessera studente",
                        icon = Icons.Default.Badge,
                        onClick = { navController.navigate("student_card") }
                    ),
                    ProfileMenuActionItem(
                        title = "Il tuo gruppo",
                        icon = Icons.Default.Groups,
                        onClick = { 
                            if (shouldDisableGroup) {
                                showGroupDisabledAlert = true
                            } else {
                                navController.navigate("group_selection")
                            }
                        },
                        subtitle = if (shouldDisableGroup) "Non disponibile" else null
                    ),
                    ProfileMenuActionItem(
                        title = "Agevolazioni",
                        icon = Icons.Default.LocalOffer,
                        onClick = { navController.navigate("benefits") }
                    )
                )
            )
        }
        
        // Risorse Section (con Regolamenti e descrizione footer)
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileSection(
                    title = "Risorse",
                    items = listOf(
                        ProfileMenuActionItem(
                            title = "Programmi didattici",
                            icon = Icons.Default.School,
                            onClick = { navController.navigate("materials") }
                        ),
                        ProfileMenuActionItem(
                            title = "Dispense",
                            icon = Icons.Default.Description,
                            onClick = { navController.navigate("handouts") }
                        ),
                        ProfileMenuActionItem(
                            title = "Regolamenti",
                            icon = Icons.AutoMirrored.Filled.Rule,
                            onClick = { navController.navigate("regulations") }
                        ),
                        ProfileMenuActionItem(
                            title = "Tesi di laurea",
                            icon = Icons.Default.School,
                            onClick = { navController.navigate("thesis") }
                        )
                    )
                )
                
                // Footer description (come iOS)
                Text(
                    text = "Puoi personalizzare la barra di navigazione e spostare qui le sezioni che usi meno da Profilo > Aspetto.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        
        // Utilità Section (identica a iOS)
        item {
            ProfileSection(
                title = "Utilità",
                items = listOf(
                    ProfileMenuActionItem(
                        title = "Consulta FAQ",
                        icon = Icons.AutoMirrored.Filled.Help,
                        onClick = { navController.navigate("faq") }
                    ),
                    ProfileMenuActionItem(
                        title = "Servizi",
                        icon = Icons.Default.Build,
                        onClick = { navController.navigate("servizi") }
                    )
                )
            )
        }
        
        // Preferences Section (con Apple Watch)
        item {
            ProfileSection(
                title = "Preferenze",
                items = listOf(
                    ProfileMenuActionItem(
                        title = "Notifiche",
                        icon = Icons.Default.Notifications,
                        onClick = { 
                            navController.navigate("notifications") {
                                launchSingleTop = true
                            }
                        }
                    ),
                    ProfileMenuActionItem(
                        title = "Aspetto",
                        icon = Icons.Default.Palette,
                        onClick = { 
                            navController.navigate("appearance") {
                                launchSingleTop = true
                            }
                        }
                )
            )
           )
        }
        
        // Contatti Section (identica a iOS)
        item {
            ProfileSection(
                title = "Contatti",
                items = listOf(
                    ProfileMenuActionItem(
                        title = "Scrivi alla Segreteria",
                        icon = Icons.Default.Email,
                        onClick = {
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:info@laba.biz".toUri()
                                putExtra(Intent.EXTRA_SUBJECT, "Richiesta informazioni")
                            }
                            try {
                                if (emailIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(emailIntent)
                                } else {
                                    val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                                        data = "mailto:info@laba.biz".toUri()
                                    }
                                    context.startActivity(fallbackIntent)
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("ProfileScreen", "Errore apertura email: ${e.message}")
                            }
                        }
                    ),
                    ProfileMenuActionItem(
                        title = "Scrivici su WhatsApp",
                        icon = Icons.AutoMirrored.Filled.Message,
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = "https://wa.me/393516905915".toUri()
                                }
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                } else {
                                    android.widget.Toast.makeText(context, "WhatsApp non installato", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("ProfileScreen", "Errore apertura WhatsApp: ${e.message}")
                            }
                        }
                    )
                )
            )
        }
        
        // Useful Links Section
        item {
            val context = LocalContext.current
            ProfileSection(
                title = "Link utili",
                items = listOf(
                    ProfileMenuActionItem(
                        title = "Sito web LABA",
                        icon = Icons.Default.Language,
                        onClick = {
                            try {
                                // Crea Intent con chooser per selezionare browser
                                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                                    data = "https://www.laba.biz".toUri()
                                }
                                
                                // Crea chooser per permettere selezione browser
                                val chooserIntent = Intent.createChooser(webIntent, "Apri con...")
                                
                                if (chooserIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(chooserIntent)
                                } else {
                                    // Fallback: prova browser specifici
                                    val browserPackages = listOf(
                                        "com.android.chrome",           // Chrome
                                        "com.android.browser",          // Browser di sistema
                                        "org.mozilla.firefox",          // Firefox
                                        "com.opera.browser",            // Opera
                                        "com.sec.android.app.sbrowser"  // Samsung Browser
                                    )
                                    
                                    var opened = false
                                    for (packageName in browserPackages) {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            data = "https://www.laba.biz".toUri()
                                            setPackage(packageName)
                                        }
                                        if (intent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(intent)
                                            opened = true
                                            break
                                        }
                                    }
                                    
                                    if (!opened) {
                                        android.widget.Toast.makeText(context, "Sito: https://www.laba.biz", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("ProfileScreen", "Errore apertura browser: ${e.message}")
                                android.widget.Toast.makeText(context, "Errore apertura browser", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    ),
                    ProfileActionItem(
                        title = "Pagamento DSU Toscana",
                        icon = Icons.Default.CreditCard,
                        iconColor = Color(0xFFF44336), // Rosso come iOS
                        onClick = {
                            try {
                                // Crea Intent con chooser per selezionare browser
                                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                                    data = "https://iris.rete.toscana.it/public".toUri()
                                }
                                
                                // Crea chooser per permettere selezione browser
                                val chooserIntent = Intent.createChooser(webIntent, "Apri con...")
                                
                                if (chooserIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(chooserIntent)
                                } else {
                                    // Fallback: prova browser specifici
                                    val browserPackages = listOf(
                                        "com.android.chrome",           // Chrome
                                        "com.android.browser",          // Browser di sistema
                                        "org.mozilla.firefox",          // Firefox
                                        "com.opera.browser",            // Opera
                                        "com.sec.android.app.sbrowser"  // Samsung Browser
                                    )
                                    
                                    var opened = false
                                    for (packageName in browserPackages) {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            data = "https://iris.rete.toscana.it/public".toUri()
                                            setPackage(packageName)
                                        }
                                        if (intent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(intent)
                                            opened = true
                                            break
                                        }
                                    }
                                    
                                    if (!opened) {
                                        android.widget.Toast.makeText(context, "DSU: https://iris.rete.toscana.it/public", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("ProfileScreen", "Errore apertura DSU: ${e.message}")
                                android.widget.Toast.makeText(context, "Errore apertura DSU", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    ),
                    ProfileMenuActionItem(
                        title = "Privacy Policy",
                        icon = Icons.Default.PrivacyTip,
                        onClick = {
                            try {
                                // Crea Intent con chooser per selezionare browser
                                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                                    data = "https://www.laba.biz/privacy-policy".toUri()
                                }
                                
                                // Crea chooser per permettere selezione browser
                                val chooserIntent = Intent.createChooser(webIntent, "Apri con...")
                                
                                if (chooserIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(chooserIntent)
                                } else {
                                    // Fallback: prova browser specifici
                                    val browserPackages = listOf(
                                        "com.android.chrome",           // Chrome
                                        "com.android.browser",          // Browser di sistema
                                        "org.mozilla.firefox",          // Firefox
                                        "com.opera.browser",            // Opera
                                        "com.sec.android.app.sbrowser"  // Samsung Browser
                                    )
                                    
                                    var opened = false
                                    for (packageName in browserPackages) {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            data = "https://www.laba.biz/privacy-policy".toUri()
                                            setPackage(packageName)
                                        }
                                        if (intent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(intent)
                                            opened = true
                                            break
                                        }
                                    }
                                    
                                    if (!opened) {
                                        android.widget.Toast.makeText(context, "Privacy: https://www.laba.biz/privacy-policy", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("ProfileScreen", "Errore apertura privacy: ${e.message}")
                                android.widget.Toast.makeText(context, "Errore apertura privacy", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                )
            )
        }
        
        // Actions Section
        item {
            ProfileSection(
                title = "Azioni",
                items = listOf(
                    ProfileActionItem(
                        title = "Rivedi tutorial",
                        icon = Icons.Default.Info,
                        iconColor = MaterialTheme.colorScheme.primary,
                        onClick = { 
                            showTutorial = true
                        }
                    ),
                    ProfileActionItem(
                        title = "Esci",
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        iconColor = Color(0xFFF44336), // Rosso come iOS
                        onClick = { viewModel.logout() }
                    )
                )
            )
        }
        }
    }
    
    // Fullscreen tutorial dialog
    if (showTutorial) {
        Dialog(
            onDismissRequest = { showTutorial = false },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                TutorialScreen(
                    onDismiss = { showTutorial = false }
                )
            }
        }
    }
    
    if (showGroupDisabledAlert) {
        AlertDialog(
            onDismissRequest = { showGroupDisabledAlert = false },
            title = { Text("Attenzione") },
            text = { Text("Per gli studenti fuori corso o laureati non è possibile selezionare un gruppo.") },
            confirmButton = {
                TextButton(onClick = { showGroupDisabledAlert = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Dialog per mostrare entrambe le matricole
    if (showMatricoleDialog) {
        val (triennioMatricola, biennioMatricola) = parseMatricole(uiState.userProfile?.matricola)
        AlertDialog(
            onDismissRequest = { showMatricoleDialog = false },
            title = {
                Text(
                    text = "Matricole",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (triennioMatricola != null) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Triennio:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = triennioMatricola,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    if (biennioMatricola != null) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Biennio:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = biennioMatricola,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMatricoleDialog = false }) {
                    Text("Chiudi")
                }
            }
        )
    }
}

@Composable
private fun ProfileHeader(
    userProfile: com.laba.firenze.domain.model.StudentProfile?,
    achievementsEnabled: Boolean,
    unlockedCount: Int,
    totalPoints: Int,
    onAchievementsClick: () -> Unit,
    onAnagraficaClick: () -> Unit,
    onServiziClick: () -> Unit,
    onMatricoleClick: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            }
        ),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar placeholder (cliccabile per anagrafica)
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable { onAnagraficaClick() },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onAnagraficaClick() }
                ) {
                    Text(
                        text = userProfile?.displayName ?: "Studente LABA",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tocca per i tuoi dati anagrafici",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Pillole sotto il nome
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pillola stato pagamenti
                val pagamentiInRegola = userProfile?.pagamenti?.uppercase() == "OK"
                Surface(
                    color = if (pagamentiInRegola) {
                        Color(0xFF4CAF50) // Verde
                    } else {
                        Color(0xFFF44336) // Rosso
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (pagamentiInRegola) Icons.Filled.Check else Icons.Filled.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        Text(
                            text = if (pagamentiInRegola) "Pagamenti in regola" else "Pagamenti non in regola",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Pillola numero matricola
                val (triennioMatricola, biennioMatricola) = parseMatricole(userProfile?.matricola)
                val hasMultiple = hasMultipleMatricole(userProfile?.matricola)
                
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.clickable(enabled = hasMultiple) {
                        if (hasMultiple) {
                            onMatricoleClick()
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Tag,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (hasMultiple) {
                                "Matricole"
                            } else {
                                "# Matricola: ${biennioMatricola ?: triennioMatricola ?: "N/A"}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        if (hasMultiple) {
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Widget Traguardi (sempre visibile, come iOS)
            Spacer(modifier = Modifier.height(12.dp))
            ProfileAchievementWidget(
                unlockedCount = unlockedCount,
                totalPoints = totalPoints,
                enabled = achievementsEnabled,
                onClick = onAchievementsClick // Sempre apre la schermata Traguardi
            )
        }
    }
}

@Composable
private fun ProfileAchievementWidget(
    unlockedCount: Int,
    totalPoints: Int,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = true) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) {
                Color(0xFFFFF9C4) // Giallo chiaro come iOS quando abilitato
            } else {
                Color(0xFFFFF9C4).copy(alpha = 0.5f) // Più trasparente quando disabilitato
            }
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icona trofeo in cerchio
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Traguardi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$unlockedCount",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "+ $totalPoints punti",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    items: List<ProfileMenuItem>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items.forEach { item ->
                ProfileMenuItem(item = item)
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(item: ProfileMenuItem) {
    when (item) {
        is ProfileMenuActionItem -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { item.onClick() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                ListItem(
                    headlineContent = { Text(item.title) },
                    supportingContent = item.subtitle?.let { { Text(it) } },
                    leadingContent = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Navigate",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            }
        }
        is ProfileToggleItem -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                ListItem(
                    headlineContent = { Text(item.title) },
                    leadingContent = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = item.isChecked,
                            onCheckedChange = item.onCheckedChange
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            }
        }
        is ProfileLinkItem -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                ListItem(
                    headlineContent = { Text(item.title) },
                    leadingContent = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.value,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            // Punto colorato (verde per "Sì", rosso per "No")
                            Surface(
                                modifier = Modifier.size(8.dp),
                                shape = CircleShape,
                                color = if (item.value == "Sì") Color(0xFF4CAF50) else Color(0xFFF44336)
                            ) {}
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            }
        }
        is ProfileActionItem -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { item.onClick() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                ListItem(
                    headlineContent = { 
                        Text(
                            text = item.title,
                            color = item.iconColor
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = item.iconColor
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            }
        }
    }
}

// Sealed class for different menu item types
sealed class ProfileMenuItem

data class ProfileMenuActionItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val subtitle: String? = null
) : ProfileMenuItem()

data class ProfileToggleItem(
    val title: String,
    val icon: ImageVector,
    val isChecked: Boolean,
    val onCheckedChange: (Boolean) -> Unit
) : ProfileMenuItem()

data class ProfileLinkItem(
    val title: String,
    val icon: ImageVector,
    val value: String
) : ProfileMenuItem()

data class ProfileActionItem(
    val title: String,
    val icon: ImageVector,
    val iconColor: Color = Color.Black,
    val onClick: () -> Unit
) : ProfileMenuItem()

