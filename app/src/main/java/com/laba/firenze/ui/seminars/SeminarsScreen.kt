package com.laba.firenze.ui.seminars

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.laba.firenze.domain.model.Seminario
import com.laba.firenze.ui.common.prettifyTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeminarsScreen(
    navController: NavController,
    viewModel: SeminarsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Track section visit
    LaunchedEffect(Unit) {
        viewModel.trackSectionVisit("seminari")
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Seminari") }
        )
        
        // Filtro - SOTTO la barra di ricerca (come iOS: Tutti / Disponibili / Frequentati)
        var selectedFilter by remember { mutableStateOf(SeminariFilter.TUTTI) }
        
        // Barra di ricerca - PRIMA dei tabs
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::updateSearchQuery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Cerca seminari") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(28.dp), // Forma capsula
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { keyboardController?.hide() }
            )
        )
        
        // Filtro - SOTTO la barra di ricerca
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SeminariFilter.entries.forEach { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    onClick = {
                        selectedFilter = filter
                        viewModel.setFilter(filter)
                    },
                    label = { Text(filter.label) },
                    selected = isSelected,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp)),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
        
        // Lista seminari (filtrata)
        if (uiState.seminars.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyFilterState(filter = uiState.filter)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.seminars) { seminar ->
                    SeminarCard(
                        seminar = seminar,
                        onClick = {
                            navController.navigate("seminar-detail/${seminar.oid}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SeminarCard(
    seminar: Seminario,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = prettifyTitle(seminarTitle(seminar.titolo)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!seminar.partecipato && seminar.richiedibile) {
                        BookablePill()
                    }
                    if (seminar.partecipato) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFF4CAF50)
                        )
                    }
                }
            }
            
            if (seminar.docente != null) {
                Text(
                    text = "Docente: ${seminar.docente}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (seminar.dataInizio != null) {
                    Text(
                        text = seminar.dataInizio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (seminar.aula != null) {
                    Text(
                        text = seminar.aula,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BookablePill() {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = "Prenotabile",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun EmptyFilterState(filter: SeminariFilter) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = if (filter == SeminariFilter.FREQUENTATI) Icons.Default.CalendarToday else Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = when (filter) {
                    SeminariFilter.FREQUENTATI -> "Nessun seminario frequentato e convalidato"
                    else -> if (filter == SeminariFilter.TUTTI) "Nessun seminario disponibile" else "Nessun seminario trovato"
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SeminarsViewModel(): SeminarsViewModel {
    return hiltViewModel()
}

