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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.laba.firenze.ui.appearance.viewmodel.AppearanceViewModel

/**
 * AnimationSettingsScreen (identica a iOS AnimationSettingsView)
 * Include: pattern di animazione di sfondo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimationSettingsScreen(
    navController: NavController,
    viewModel: AppearanceViewModel = hiltViewModel()
) {
    val selectedPattern by viewModel.selectedPattern.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Animazione") },
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
            contentPadding = PaddingValues(16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val patterns = listOf(
                Pattern("wave", "Onda", Icons.Default.WaterDrop),
                Pattern("dots", "Pallini", Icons.Default.Circle),
                Pattern("grid", "Griglia", Icons.Default.Grid3x3),
                Pattern("particles", "Particelle", Icons.Default.AutoAwesome),
                Pattern("circles", "Cerchi", Icons.Default.RadioButtonUnchecked),
                Pattern("rays", "Raggi", Icons.Default.WbSunny),
                Pattern("ripple", "Onde", Icons.Default.Waves)
            )
            
            items(patterns) { pattern ->
                PatternRow(
                    pattern = pattern,
                    isSelected = selectedPattern == pattern.key,
                    onSelect = { viewModel.selectPattern(pattern.key) }
                )
            }
        }
    }
}

@Composable
private fun PatternRow(
    pattern: Pattern,
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
            headlineContent = { Text(pattern.label) },
            leadingContent = {
                Icon(
                    imageVector = pattern.icon,
                    contentDescription = pattern.label,
                    tint = MaterialTheme.colorScheme.primary
                )
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

data class Pattern(
    val key: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
