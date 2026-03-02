package com.laba.firenze.ui.appearance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.laba.firenze.ui.appearance.viewmodel.AppearanceViewModel
import kotlinx.coroutines.launch

/**
 * AppearanceSettingsScreen completa (identica a iOS AppearanceSettingsView)
 * Include: Tema, Colori, Animazione di sfondo, Personalizza menu
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    navController: NavController,
    viewModel: AppearanceViewModel = hiltViewModel()
) {
    @Suppress("UNUSED_VARIABLE")
    val selectedPattern by viewModel.selectedPattern.collectAsState()
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Aspetto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Colori dell'applicazione
            item {
                Section(title = "Colori dell'applicazione") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("color_settings") },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        ListItem(
                            headlineContent = { Text("Colori dell'applicazione") },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                }
            }
            
            // Animazione di sfondo
            item {
                Section(title = "Animazione di sfondo") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("animation_settings") },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        ListItem(
                            headlineContent = { Text("Animazione di sfondo") },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Waves,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                }
            }
            
            // Personalizza barra di navigazione
            item {
                Section(title = "Personalizza navigazione") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("navigation_custom") },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        ListItem(
                            headlineContent = { Text("Personalizza barra di navigazione") },
                            supportingContent = { Text("Riordina le tab nella barra in basso", style = MaterialTheme.typography.bodySmall) },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Reorder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }

            // Tema
            item {
                Section(title = "Tema") {
                    ThemeRow(
                        selectedTheme = viewModel.getThemePreference(),
                        onThemeSelected = { theme ->
                            scope.launch {
                                viewModel.setThemePreference(theme)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        content()
    }
}

@Composable
private fun ThemeRow(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit
) {
    val themes = listOf(
        ThemeOption("system", "Sistema", Icons.Default.Settings),
        ThemeOption("light", "Chiaro", Icons.Default.LightMode),
        ThemeOption("dark", "Scuro", Icons.Default.DarkMode)
    )
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        themes.forEach { theme ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onThemeSelected(theme.key) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                ListItem(
                    headlineContent = { Text(theme.label) },
                    leadingContent = {
                        Icon(
                            imageVector = theme.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        if (selectedTheme == theme.key) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            }
        }
    }
}

data class ThemeOption(
    val key: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
