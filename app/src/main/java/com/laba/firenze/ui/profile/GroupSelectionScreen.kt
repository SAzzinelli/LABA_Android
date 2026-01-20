package com.laba.firenze.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.laba.firenze.ui.appearance.viewmodel.AppearanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSelectionScreen(
    navController: NavController,
    viewModel: AppearanceViewModel = hiltViewModel()
) {
    val selectedGroup by viewModel.selectedGroup.collectAsState()
    val groups = listOf("A", "B", "C", "D", "E", "F", "G", "H")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Il tuo gruppo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Seleziona il tuo gruppo per filtrare l'orario delle lezioni.\nSeleziona 'Nessuno' per vedere tutti i gruppi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            item {
                // "Nessuno" option
                GroupRow(
                    label = "Nessuno (Mostra tutto)",
                    isSelected = selectedGroup == null,
                    onSelect = { viewModel.selectGroup(null) }
                )
            }
            
            items(groups) { group ->
                GroupRow(
                    label = "Gruppo $group",
                    isSelected = selectedGroup == group,
                    onSelect = { viewModel.selectGroup(group) }
                )
            }
        }
    }
}

@Composable
private fun GroupRow(
    label: String,
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
            headlineContent = { Text(label) },
            trailingContent = {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        )
    }
}
