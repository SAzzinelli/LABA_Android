package com.laba.firenze.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    navController: NavController,
    viewModel: AppearanceSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aspetto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sezione Tema
            item {
                Text(
                    text = "Tema",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                Column {
                    ThemeRow(
                        theme = ThemeOption("system", "Sistema", Icons.Default.Settings),
                        isSelected = uiState.themePreference == "system",
                        onSelect = { viewModel.updateTheme("system") }
                    )
                    ThemeRow(
                        theme = ThemeOption("light", "Chiaro", Icons.Default.LightMode),
                        isSelected = uiState.themePreference == "light",
                        onSelect = { viewModel.updateTheme("light") }
                    )
                    ThemeRow(
                        theme = ThemeOption("dark", "Scuro", Icons.Default.DarkMode),
                        isSelected = uiState.themePreference == "dark",
                        onSelect = { viewModel.updateTheme("dark") }
                    )
                }
            }
            
            // Sezione Colore accento
            item {
                Text(
                    text = "Colore accento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                Column {
                    AccentRow(
                        accent = AccentOption("system", "Sistema", Color(0xFF007AFF)),
                        isSelected = uiState.accentChoice == "system",
                        onSelect = { viewModel.updateAccent("system") }
                    )
                    AccentRow(
                        accent = AccentOption("peach", "Pesca", Color(0xFFFF9500)),
                        isSelected = uiState.accentChoice == "peach",
                        onSelect = { viewModel.updateAccent("peach") }
                    )
                    AccentRow(
                        accent = AccentOption("lavender", "Lavanda", Color(0xFFAF52DE)),
                        isSelected = uiState.accentChoice == "lavender",
                        onSelect = { viewModel.updateAccent("lavender") }
                    )
                    AccentRow(
                        accent = AccentOption("mint", "Menta", Color(0xFF00C7BE)),
                        isSelected = uiState.accentChoice == "mint",
                        onSelect = { viewModel.updateAccent("mint") }
                    )
                    AccentRow(
                        accent = AccentOption("sand", "Sabbia", Color(0xFFF2CC8C)),
                        isSelected = uiState.accentChoice == "sand",
                        onSelect = { viewModel.updateAccent("sand") }
                    )
                    AccentRow(
                        accent = AccentOption("sky", "Cielo", Color(0xFF5AC8FA)),
                        isSelected = uiState.accentChoice == "sky",
                        onSelect = { viewModel.updateAccent("sky") }
                    )
                }
            }
            
            // Sezione Colori speciali
            item {
                Text(
                    text = "Colori speciali",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                Column {
                    AccentRow(
                        accent = AccentOption("brand", "Blu LABA", Color(0xFF0A84FF)),
                        isSelected = uiState.accentChoice == "brand",
                        onSelect = { viewModel.updateAccent("brand") }
                    )
                    AccentRow(
                        accent = AccentOption("IED", "Il male che risIEDe in voi", Color(0xFFBB271A)),
                        isSelected = uiState.accentChoice == "IED",
                        onSelect = { viewModel.updateAccent("IED") }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeRow(
    theme: ThemeOption,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = theme.icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = theme.title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AccentRow(
    accent: AccentOption,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Cerchio colorato
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(accent.color)
                .padding(1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            )
        }
        
        Text(
            text = accent.title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

data class ThemeOption(
    val key: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class AccentOption(
    val key: String,
    val title: String,
    val color: Color
)
