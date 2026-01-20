package com.laba.firenze.ui.profile

import android.content.Intent
import android.net.Uri
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showTutorial by remember { mutableStateOf(false) }
    var showGroupDisabledAlert by remember { mutableStateOf(false) }
    
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
            ProfileHeader(userProfile = uiState.userProfile)
        }
        
        // La tua carriera Section
        item {
            ProfileSection(
                title = "La tua carriera",
                items = listOf(
                    ProfileMenuActionItem(
                        title = "Anagrafica",
                        icon = Icons.Default.Person,
                        onClick = { navController.navigate("anagrafica") }
                    ),
                    ProfileMenuActionItem(
                        title = "Tessera studente",
                        icon = Icons.Default.Badge, // Or CardMembership if Badge not available
                        onClick = { navController.navigate("student_card") }
                    ),
                    ProfileMenuActionItem(
                        title = "Traguardi",
                        icon = Icons.Default.EmojiEvents, // Trophy icon
                        onClick = { navController.navigate("achievements") }
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
                        onClick = { navController.navigate("benefits") } // This is LABANavigation.Benefits.route ("benefits")
                    )
                )
            )
        }
        
        // Resources Section
        item {
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
                        title = "Tesi di laurea",
                        icon = Icons.Default.School,
                        onClick = { navController.navigate("thesis") }
                    )
                )
            )
        }
        
        // Utilità Section (identica a iOS)
        item {
            ProfileSection(
                title = "Utilità",
                items = listOf(
                    ProfileMenuActionItem(
                        title = "FAQ",
                        icon = Icons.AutoMirrored.Filled.Help,
                        onClick = { navController.navigate("faq") }
                    ),
                    ProfileMenuActionItem(
                        title = "Servizi",
                        icon = Icons.Default.Settings,
                        onClick = { navController.navigate("servizi") }
                    )
                )
            )
        }
        
        // Assistance Section
        item {
            ProfileSection(
                title = "Assistenza",
                items = listOf(
                    ProfileMenuActionItem(
                        title = "Regolamenti",
                        icon = Icons.AutoMirrored.Filled.Rule,
                        onClick = { navController.navigate("regulations") }
                    ),
                    ProfileMenuActionItem(
                        title = "Privacy e Sicurezza",
                        icon = Icons.Default.PrivacyTip,
                        onClick = { navController.navigate("privacy-security") }
                    )
                )
            )
        }
        
        // Preferences Section
        item {
            ProfileSection(
                title = "Preferenze",
                items = listOf(
                    ProfileMenuActionItem(
                        title = "Notifiche",
                        icon = Icons.Default.Notifications,
                        onClick = { 
                            // Naviga alla schermata di impostazioni notifiche
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
        
        // Contacts Section
        item {
            val context = LocalContext.current
            ProfileSection(
                title = "Contatti",
                items = listOf(
                    ProfileMenuActionItem(
                        title = "Email Segreteria",
                        icon = Icons.Default.Email,
                        subtitle = "info@laba.biz",
                        onClick = {
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:info@laba.biz")
                                putExtra(Intent.EXTRA_SUBJECT, "Richiesta informazioni")
                            }
                            try {
                                if (emailIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(emailIntent)
                                } else {
                                    // Fallback: apri browser con mailto
                                    val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("mailto:info@laba.biz")
                                    }
                                    context.startActivity(fallbackIntent)
                                }
                            } catch (e: Exception) {
                                // Log dell'errore per debug
                                android.util.Log.e("ProfileScreen", "Errore apertura email: ${e.message}")
                            }
                        }
                    ),
                    ProfileMenuActionItem(
                        title = "Telefono Segreteria",
                        icon = Icons.Default.Phone,
                        subtitle = "055 653 0786",
                        onClick = {
                            val phoneIntent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:0556530786")
                            }
                            try {
                                if (phoneIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(phoneIntent)
                                } else {
                                    // Fallback: mostra numero in toast
                                    android.widget.Toast.makeText(context, "Numero: 055 653 0786", android.widget.Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("ProfileScreen", "Errore apertura telefono: ${e.message}")
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
                        title = "Sito LABA",
                        icon = Icons.Default.Language,
                        subtitle = "www.laba.biz",
                        onClick = {
                            try {
                                // Crea Intent con chooser per selezionare browser
                                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://www.laba.biz")
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
                                            data = Uri.parse("https://www.laba.biz")
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
                        icon = Icons.Default.Payment,
                        iconColor = MaterialTheme.colorScheme.error,
                        onClick = {
                            try {
                                // Crea Intent con chooser per selezionare browser
                                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://iris.rete.toscana.it/public")
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
                                            data = Uri.parse("https://iris.rete.toscana.it/public")
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
                        subtitle = "Informativa sulla privacy",
                        onClick = {
                            try {
                                // Crea Intent con chooser per selezionare browser
                                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://www.laba.biz/privacy-policy")
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
                                            data = Uri.parse("https://www.laba.biz/privacy-policy")
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
                    ProfileMenuActionItem(
                        title = "Rivedi tutorial",
                        icon = Icons.Default.Info,
                        onClick = { 
                            // Apri il tutorial
                            showTutorial = true
                        }
                    ),
                    ProfileActionItem(
                        title = "Esci",
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        iconColor = MaterialTheme.colorScheme.primary,
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
}

@Composable
private fun ProfileHeader(userProfile: com.laba.firenze.domain.model.StudentProfile?) {
    val isDarkTheme = isSystemInDarkTheme()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                // In light mode, usa un colore più chiaro e delicato
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
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar placeholder
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                
                Column {
                            Text(
                                text = userProfile?.displayName ?: "Studente LABA",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                }
            }
            
            // Pillole sotto il nome
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                        // Pillola stato pagamenti (verde o rossa)
                        val pagamentiInRegola = userProfile?.pagamenti?.lowercase()?.contains("ok") == true
                Surface(
                    color = if (pagamentiInRegola) {
                        Color(0xFF4CAF50) // Verde per pagamenti in regola
                    } else {
                        Color(0xFFF44336) // Rosso per pagamenti non in regola
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
                            text = if (pagamentiInRegola) "Pagamenti in regola" else "Pagamenti in ritardo",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Pillola numero matricola (grigia)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Badge,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Matricola: ${userProfile?.matricola ?: "N/A"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
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
                    supportingContent = { Text(item.value) },
                    leadingContent = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = MaterialTheme.colorScheme.primary
                        )
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

@Composable
fun ProfileViewModel(): ProfileViewModel {
    return hiltViewModel()
}
