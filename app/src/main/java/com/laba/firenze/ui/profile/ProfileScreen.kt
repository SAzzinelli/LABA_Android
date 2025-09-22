package com.laba.firenze.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Profile Header
        item {
            ProfileHeader(userProfile = uiState.userProfile)
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
                        title = "FAQ",
                        icon = Icons.AutoMirrored.Filled.Help,
                        onClick = { /* TODO: Implement FAQ */ }
                    )
                )
            )
        }
        
        // Preferences Section
        item {
            ProfileSection(
                title = "Preferenze",
                items = listOf(
                    ProfileToggleItem(
                        title = "Notifiche",
                        icon = Icons.Default.Notifications,
                        isChecked = uiState.notificationsEnabled,
                        onCheckedChange = viewModel::updateNotificationsEnabled
                    ),
                    ProfileMenuActionItem(
                        title = "Aspetto",
                        icon = Icons.Default.Palette,
                        onClick = { /* TODO: Implement appearance settings */ }
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
                        title = "Ricarica Dati",
                        icon = Icons.Default.Refresh,
                        onClick = { viewModel.refreshData() }
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

@Composable
private fun ProfileHeader(userProfile: com.laba.firenze.domain.model.StudentProfile?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar placeholder
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(32.dp),
                            tint = Color.White
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
                    color = Color.White.copy(alpha = 0.2f),
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
                            tint = Color.White
                        )
                        Text(
                            text = "Matricola: ${userProfile?.matricola ?: "N/A"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
