package com.laba.firenze.ui.profile

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.view.Window
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import com.laba.firenze.ui.theme.LABAFirenzeTheme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import coil.compose.AsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.rememberRipple
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.laba.firenze.data.service.ProfilePhotoImageCache
import com.laba.firenze.data.service.ProfilePhotoService
import com.laba.firenze.ui.tutorial.TutorialScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

/**
 * Estrae le matricole triennio e biennio dalla stringa.
 * Restituisce una coppia (triennio, biennio) dove uno o entrambi possono essere null.
 * Gestisce vari formati: "triennio 3747 FI biennio 1234 AB", "triennio (3747 FI) biennio (1234 AB)", 
 * "3747 FI / 1234 AB", "triennio 3747 FI - biennio 1234 AB", ecc.
 */
private fun parseMatricole(matricola: String?): Pair<String?, String?> {
    if (matricola.isNullOrBlank()) return Pair(null, null)
    
    android.util.Log.d("ProfileScreen", "Parsing matricola: '$matricola'")
    
    val originalMatricola = matricola.trim()
    val lowerMatricola = originalMatricola.lowercase()
    var triennio: String? = null
    var biennio: String? = null
    
    // Controlla se contiene entrambe le keyword
    val hasTriennio = lowerMatricola.contains("triennio")
    val hasBiennio = lowerMatricola.contains("biennio")
    
    // Gestisce formato: "NUMERO (triennio) NUMERO (biennio)" o "triennio NUMERO biennio NUMERO"
    if (hasTriennio) {
        // Pattern 1: "NUMERO (triennio)" - numero PRIMA di triennio
        val pattern1 = Regex("""([0-9A-Za-z]+(?:\s+[A-Z]{2})?)\s*(?:\()?\s*triennio\s*(?:\))?""", RegexOption.IGNORE_CASE)
        val match1 = pattern1.find(originalMatricola)
        if (match1 != null) {
            triennio = match1.groupValues[1].trim().takeIf { it.isNotBlank() }
            android.util.Log.d("ProfileScreen", "Extracted triennio (pattern1): '$triennio'")
        } else {
            // Pattern 2: "triennio NUMERO" - triennio PRIMA del numero
            val pattern2 = Regex("""triennio\s*(?:\()?\s*([0-9A-Za-z\s]+?)\s*(?:\))?\s*(?:biennio|/|-|$)""", RegexOption.IGNORE_CASE)
            val match2 = pattern2.find(originalMatricola)
            if (match2 != null) {
                triennio = match2.groupValues[1]
                    .trim()
                    .replace(Regex("""[()]"""), "")
                    .trim()
                    .takeIf { it.isNotBlank() }
                android.util.Log.d("ProfileScreen", "Extracted triennio (pattern2): '$triennio'")
            } else {
                // Fallback: cerca dopo "triennio" fino a "biennio" o fine stringa
                val triennioIndex = lowerMatricola.indexOf("triennio")
                val beforeTriennio = originalMatricola.substring(0, triennioIndex).trim()
                val afterTriennio = originalMatricola.substring(triennioIndex + "triennio".length).trim()
                val biennioIndexInAfter = afterTriennio.lowercase().indexOf("biennio")
                
                // Se c'è qualcosa prima di "triennio", potrebbe essere il numero
                if (beforeTriennio.isNotBlank() && !beforeTriennio.contains("biennio", ignoreCase = true)) {
                    triennio = beforeTriennio
                        .replace(Regex("""[()]"""), "")
                        .trim()
                        .takeIf { it.isNotBlank() }
                    android.util.Log.d("ProfileScreen", "Extracted triennio (before): '$triennio'")
                } else if (biennioIndexInAfter >= 0) {
                    // Estrai tra "triennio" e "biennio"
                    val triennioText = afterTriennio.substring(0, biennioIndexInAfter)
                        .replace(Regex("""[()/\-]"""), " ")
                        .trim()
                        .split(Regex("""\s+"""))
                        .take(2)
                        .joinToString(" ")
                        .takeIf { it.isNotBlank() }
                    triennio = triennioText
                    android.util.Log.d("ProfileScreen", "Extracted triennio (fallback): '$triennio'")
                }
            }
        }
    }
    
    if (hasBiennio) {
        // Pattern 1: "NUMERO (biennio)" - numero PRIMA di biennio
        val pattern1 = Regex("""([0-9A-Za-z]+(?:\s+[A-Z]{2})?)\s*(?:\()?\s*biennio\s*(?:\))?""", RegexOption.IGNORE_CASE)
        val match1 = pattern1.find(originalMatricola)
        if (match1 != null) {
            biennio = match1.groupValues[1].trim().takeIf { it.isNotBlank() && it.length >= 2 }
            android.util.Log.d("ProfileScreen", "Extracted biennio (pattern1): '$biennio'")
        } else {
            // Pattern 2: "biennio NUMERO" - biennio PRIMA del numero
            val pattern2 = Regex("""biennio\s*(?:\()?\s*([0-9A-Za-z\s]+?)\s*(?:\))?\s*$""", RegexOption.IGNORE_CASE)
            val match2 = pattern2.find(originalMatricola)
            if (match2 != null) {
                biennio = match2.groupValues[1]
                    .trim()
                    .replace(Regex("""[()]"""), "")
                    .trim()
                    .takeIf { it.isNotBlank() && it.length >= 2 }
                android.util.Log.d("ProfileScreen", "Extracted biennio (pattern2): '$biennio'")
            } else {
                // Fallback: cerca dopo "biennio" fino alla fine
                val biennioIndex = lowerMatricola.indexOf("biennio")
                val beforeBiennio = originalMatricola.substring(0, biennioIndex).trim()
                val afterBiennio = originalMatricola.substring(biennioIndex + "biennio".length).trim()
                
                // Se c'è qualcosa prima di "biennio" e dopo "triennio", potrebbe essere il numero
                if (beforeBiennio.isNotBlank() && beforeBiennio.contains("triennio", ignoreCase = true)) {
                    // Estrai la parte dopo l'ultima occorrenza di "triennio" e prima di "biennio"
                    val triennioIndexInBefore = beforeBiennio.lowercase().lastIndexOf("triennio")
                    if (triennioIndexInBefore >= 0) {
                        val afterLastTriennio = beforeBiennio.substring(triennioIndexInBefore + "triennio".length)
                            .replace(Regex("""[()/\-]"""), " ")
                            .trim()
                            .split(Regex("""\s+"""))
                            .take(2)
                            .joinToString(" ")
                            .takeIf { it.isNotBlank() && it.length >= 2 }
                        biennio = afterLastTriennio
                        android.util.Log.d("ProfileScreen", "Extracted biennio (before): '$biennio'")
                    }
                }
                
                if (biennio == null && afterBiennio.isNotBlank()) {
                    biennio = afterBiennio
                        .replace(Regex("""[()/\-]"""), " ")
                        .trim()
                        .split(Regex("""\s+"""))
                        .take(2)
                        .joinToString(" ")
                        .takeIf { it.isNotBlank() && it.length >= 2 }
                    android.util.Log.d("ProfileScreen", "Extracted biennio (fallback): '$biennio'")
                }
            }
        }
    }
    
    // Se non contiene keyword ma potrebbe essere formato "3747 FI / 1234 AB" o simile
    if (triennio == null && biennio == null && !hasTriennio && !hasBiennio) {
        // Prova a dividere per "/" o "-"
        val separators = listOf(" / ", " - ", " /", "/ ", " -", "- ")
        for (separator in separators) {
            if (originalMatricola.contains(separator)) {
                val parts = originalMatricola.split(separator)
                if (parts.size >= 2) {
                    // Prima parte = triennio, seconda = biennio
                    triennio = parts[0].trim().takeIf { it.isNotBlank() }
                    biennio = parts[1].trim().takeIf { it.isNotBlank() && it.length >= 2 }
                    android.util.Log.d("ProfileScreen", "Split by '$separator' - Triennio: '$triennio', Biennio: '$biennio'")
                    break
                }
            }
        }
        
        // Se ancora null, assumi che sia una sola matricola (biennio più comune)
        if (triennio == null && biennio == null) {
            biennio = originalMatricola.trim()
            android.util.Log.d("ProfileScreen", "No keywords found, assuming biennio: '$biennio'")
        }
    }
    
    android.util.Log.d("ProfileScreen", "Final result - Triennio: '$triennio', Biennio: '$biennio'")
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
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Track section visit + ricarica foto da Supabase (impostata su iOS → visibile su Android)
    LaunchedEffect(Unit) {
        viewModel.trackSectionVisit("profile")
        viewModel.loadProfilePhotoFromSupabase()
    }
    // Ricarica foto quando il profilo diventa disponibile (email da API può arrivare dopo keychain)
    val profileEmail = uiState.userProfile?.emailLABA ?: uiState.userProfile?.emailPersonale
    LaunchedEffect(profileEmail) {
        if (!profileEmail.isNullOrBlank()) viewModel.loadProfilePhotoFromSupabase()
    }
    
    // Logic to disable group selection
    val status = uiState.userProfile?.status?.lowercase() ?: ""
    val currentYear = uiState.userProfile?.currentYear
    val isGraduated = status.contains("laureat")
    val isFuoricorso = currentYear == null && !isGraduated
    val shouldDisableGroup = isGraduated || isFuoricorso
    
    // Profile photo picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                val bytes = stream.readBytes()
                viewModel.uploadProfilePhoto(bytes)
            }
        }
    }

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

    val uploadPhotoError by viewModel.uploadPhotoError.collectAsState()
    LaunchedEffect(uploadPhotoError) {
        uploadPhotoError?.let { err ->
            when (snackbarHostState.showSnackbar(err, actionLabel = "OK")) {
                SnackbarResult.ActionPerformed, SnackbarResult.Dismissed -> viewModel.clearUploadPhotoError()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            val profilePhotoURL by viewModel.profilePhotoURL.collectAsState()
            val isUploadingPhoto by viewModel.isUploadingPhoto.collectAsState()
            val uploadPhotoError by viewModel.uploadPhotoError.collectAsState()
            ProfileHeader(
                onMatricoleClick = { showMatricoleDialog = true },
                onImageNotFound = { viewModel.clearProfilePhotoURL() },
                userProfile = uiState.userProfile,
                profilePhotoURL = profilePhotoURL,
                isUploadingPhoto = isUploadingPhoto,
                canUploadPhoto = true,
                onPhotoClick = {
                    // Come iOS: il picker si apre sempre; l'upload fallirà con messaggio se IMGBB non configurato
                    imagePickerLauncher.launch("image/*")
                },
                onAnagraficaClick = { navController.navigate("anagrafica") },
                achievementsEnabled = achievementsEnabled,
                unlockedCount = unlockedCount,
                totalPoints = totalPoints,
                onAchievementsClick = { navController.navigate("achievements") },
                onServiziClick = { navController.navigate("servizi") }
            )
        }
        
        // La tua carriera Section (senza Traguardi, che è nel widget)
        item {
            ProfileSection(
                title = "La tua carriera",
                items = buildList {
                    add(
                        ProfileMenuActionItem(
                            title = "Tessera studente",
                            icon = Icons.Default.Badge,
                            onClick = { navController.navigate("student_card") }
                        )
                    )
                    if (com.laba.firenze.LabaConfig.USE_GROUP_FILTER) {
                        add(
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
                            )
                        )
                    }
                    add(
                        ProfileMenuActionItem(
                            title = "Agevolazioni",
                            icon = Icons.Default.LocalOffer,
                            onClick = { navController.navigate("benefits") }
                        )
                    )
                }
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
                        title = "Funzionalità e Servizi",
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
                                    // opened viene usato per controllare se aprire il toast
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
                        title = "Cosa posso fare?",
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
    
    // Fullscreen tutorial dialog (barre di sistema trasparenti per evitare barre nere)
    if (showTutorial) {
        val activity = LocalContext.current as? Activity
        if (activity != null) {
            DisposableEffect(Unit) {
                val dialog = android.app.Dialog(activity, android.R.style.Theme_NoTitleBar_Fullscreen).apply {
                    requestWindowFeature(Window.FEATURE_NO_TITLE)
                    setContentView(ComposeView(activity).apply {
                        setContent {
                            LABAFirenzeTheme {
                                TutorialScreen(
                                    onDismiss = {
                                        dismiss()
                                        showTutorial = false
                                    },
                                    profileViewModel = viewModel
                                )
                            }
                        }
                    })
                    window?.apply {
                        statusBarColor = android.graphics.Color.TRANSPARENT
                        navigationBarColor = android.graphics.Color.TRANSPARENT
                        WindowCompat.setDecorFitsSystemWindows(this, false)
                        WindowCompat.getInsetsController(this, decorView)?.apply {
                            isAppearanceLightStatusBars = true
                            isAppearanceLightNavigationBars = true
                        }
                    }
                    setOnCancelListener { showTutorial = false }
                    show()
                }
                onDispose { dialog.dismiss() }
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
                    // Debug: mostra la stringa originale se non sono state trovate matricole
                    if (triennioMatricola == null && biennioMatricola == null) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Stringa originale:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = uiState.userProfile?.matricola ?: "N/A",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
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
    profilePhotoURL: String?,
    isUploadingPhoto: Boolean,
    canUploadPhoto: Boolean,
    onPhotoClick: () -> Unit,
    onAnagraficaClick: () -> Unit,
    onImageNotFound: () -> Unit,
    achievementsEnabled: Boolean,
    unlockedCount: Int,
    totalPoints: Int,
    onAchievementsClick: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onServiziClick: () -> Unit,
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
                // Avatar: foto ImgBB o placeholder (cliccabile sempre per foto – come iOS, mai anagrafica)
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable { onPhotoClick() },
                    shape = CircleShape,
                    color = if (profilePhotoURL != null) Color.Transparent else MaterialTheme.colorScheme.primary
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.clip(CircleShape)
                    ) {
                        when {
                            isUploadingPhoto -> CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 2.dp
                            )
                            profilePhotoURL != null -> ProfilePhotoFromURL(
                                url = profilePhotoURL,
                                isDarkTheme = isDarkTheme,
                                onImageNotFound = onImageNotFound
                            )
                            else -> Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
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
            
            // Pillole sotto il nome (stessa riga)
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                
                // Pillola numero matricola (stessa larghezza di Traguardi, nella stessa riga)
                val (triennioMatricola, biennioMatricola) = parseMatricole(userProfile?.matricola)
                val hasMultiple = hasMultipleMatricole(userProfile?.matricola)
                
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = hasMultiple) {
                            if (hasMultiple) {
                                onMatricoleClick()
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
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
                                    "Matricola: ${biennioMatricola ?: triennioMatricola ?: "N/A"}"
                                },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                        if (hasMultiple) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Widget Traguardi (solo se abilitato)
            if (achievementsEnabled) {
                Spacer(modifier = Modifier.height(4.dp))
                ProfileAchievementWidget(
                    unlockedCount = unlockedCount,
                    totalPoints = totalPoints,
                    enabled = achievementsEnabled,
                    onClick = onAchievementsClick // Sempre apre la schermata Traguardi
                )
            }
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
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(18.dp)
    val isDarkTheme = isSystemInDarkTheme()
    val accent = MaterialTheme.colorScheme.primary
    val gradientColors = if (isDarkTheme) {
        listOf(accent.copy(alpha = 0.25f), accent.copy(alpha = 0.08f))
    } else {
        listOf(accent.copy(alpha = 0.18f), accent.copy(alpha = 0.06f))
    }
    val gradient = androidx.compose.ui.graphics.Brush.linearGradient(
        colors = gradientColors,
        start = androidx.compose.ui.geometry.Offset.Zero,
        end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(gradient)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true),
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = shape
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
                            text = "+ $totalPoints CFApp",
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
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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

/**
 * Carica la foto profilo da URL (ImgBB).
 * Usa ProfilePhotoImageCache: prima controlla cache, poi loadAndCache.
 * Se 404 o placeholder (&lt;15KB) → icona Person e onImageNotFound.
 */
@Composable
private fun ProfilePhotoFromURL(
    url: String,
    isDarkTheme: Boolean,
    onImageNotFound: () -> Unit,
    modifier: Modifier = Modifier
) {
    var loadState by remember(url) { mutableStateOf<ProfilePhotoLoadState>(ProfilePhotoLoadState.Loading) }
    
    LaunchedEffect(url) {
        loadState = withContext(Dispatchers.IO) {
            // 1. Controlla cache in-memory (evita reload in Profilo)
            ProfilePhotoImageCache.imageDataFor(url)?.let { cached ->
                return@withContext ProfilePhotoLoadState.Success(cached)
            }
            // 2. Load e salva in cache
            ProfilePhotoImageCache.loadAndCache(url)?.let { data ->
                return@withContext ProfilePhotoLoadState.Success(data)
            }
            ProfilePhotoLoadState.Invalid
        }
        if (loadState == ProfilePhotoLoadState.Invalid) {
            onImageNotFound()
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
    when (val s = loadState) {
        ProfilePhotoLoadState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
        }
        ProfilePhotoLoadState.Invalid -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar",
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        is ProfilePhotoLoadState.Success -> {
            val bitmap = remember(s.data) {
                android.graphics.BitmapFactory.decodeByteArray(s.data, 0, s.data.size)
            }
            bitmap?.let { bmp ->
                androidx.compose.foundation.Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize()
                )
            } ?: Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = "Avatar", modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
    }
}

private sealed class ProfilePhotoLoadState {
    data object Loading : ProfilePhotoLoadState()
    data object Invalid : ProfilePhotoLoadState()
    data class Success(val data: ByteArray) : ProfilePhotoLoadState()
}

