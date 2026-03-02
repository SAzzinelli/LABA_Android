package com.laba.firenze.ui.appearance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
 * ColorSettingsScreen (identica a iOS ColorSettingsView)
 * Include: Colori sistema e Colori speciali
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSettingsScreen(
    navController: NavController,
    viewModel: AppearanceViewModel = hiltViewModel()
) {
    val selectedAccent by viewModel.getAccentChoice().collectAsState(initial = "system")
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Colori") },
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
            // Colori sistema
            item {
                Section(title = "Colori sistema") {
                    val systemColors = listOf(
                        AccentColor("system", "Sistema", Color(0xFF007AFF)),
                        AccentColor("peach", "Pesca", Color(0xFFFF9500)),
                        AccentColor("lavender", "Lavanda", Color(0xFFAF52DE)),
                        AccentColor("mint", "Menta", Color(0xFF00C896)),
                        AccentColor("sand", "Sabbia", Color(0xFFF1C40F)),
                        AccentColor("sky", "Cielo", Color(0xFF5AC8FA))
                    )
                    
                    systemColors.forEach { accent ->
                        AccentColorRow(
                            accent = accent,
                            isSelected = selectedAccent == accent.key,
                            onSelect = {
                                scope.launch {
                                    viewModel.setAccentChoice(accent.key)
                                }
                            }
                        )
                    }
                }
            }
            
            // Colori speciali
            item {
                Section(title = "Colori speciali") {
                    val specialColors = listOf(
                        AccentColor("brand", "Blu LABA", Color(0xFF007AFF)),
                        AccentColor("dark", "Il lato oscuro", Color(0xFF1C1C1E)),
                        AccentColor("IED", "Un rosso un po' bruttino", Color(0xFFBB271A))
                    )
                    
                    specialColors.forEach { accent ->
                        AccentColorRow(
                            accent = accent,
                            isSelected = selectedAccent == accent.key,
                            onSelect = {
                                scope.launch {
                                    viewModel.setAccentChoice(accent.key)
                                }
                            }
                        )
                    }
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
private fun AccentColorRow(
    accent: AccentColor,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        ListItem(
            headlineContent = { Text(accent.label) },
            leadingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(accent.color)
                    )
                }
            },
            trailingContent = {
                if (isSelected) {
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

data class AccentColor(
    val key: String,
    val label: String,
    val color: Color
)
