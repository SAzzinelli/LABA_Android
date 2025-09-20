package com.laba.firenze.ui.perte

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

data class SimItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    var grade: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulaMediaScreen(
    navController: NavController,
    viewModel: SimulaMediaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var simulated by remember { mutableStateOf<List<SimItem>>(emptyList()) }
    var showAddSheet by remember { mutableStateOf(false) }
    var showGraduatedGate by remember { mutableStateOf(false) }
    var courseQuery by remember { mutableStateOf("") }
    
    // Check if graduated on appear
    LaunchedEffect(uiState.notSustainedCount) {
        showGraduatedGate = (uiState.notSustainedCount == 0)
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Simulatore media") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Situazione attuale
                CurrentSituationCard(uiState = uiState)
                
                // Aggiungi esami - versione semplificata
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Aggiungi esami non sostenuti",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        if (simulated.isEmpty()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Nessuna simulazione attiva",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Button(
                                    onClick = { showAddSheet = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Aggiungi esami")
                                }
                            }
                        } else {
                            Text(
                                text = "Esami simulati: ${simulated.size}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = { simulated = emptyList() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Azzera simulazioni")
                            }
                        }
                    }
                }
            }
        }
        
        // Graduation Gate Overlay - versione semplificata
        if (showGraduatedGate) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(24.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Sei laureato, a cosa ti serve questa sezione?",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { navController.popBackStack() }
                            ) {
                                Text("Indietro")
                            }
                            Button(
                                onClick = { showGraduatedGate = false }
                            ) {
                                Text("Entra comunque")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentSituationCard(uiState: SimulaMediaUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Situazione attuale",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBox(
                    value = uiState.currentAvg30?.let { String.format("%.2f", it) } ?: "—",
                    caption = "Media attuale",
                    icon = Icons.Default.Numbers,
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    value = uiState.currentAvg110?.toString() ?: "—",
                    caption = "Voto d'Ingresso",
                    icon = Icons.Default.School,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusPill(
                    title = "Sostenuti",
                    value = uiState.sustainedCount.toString(),
                    tint = MaterialTheme.colorScheme.primary
                )
                StatusPill(
                    title = "Totali", 
                    value = uiState.totalCount.toString(),
                    tint = null
                )
                StatusPill(
                    title = "Da sostenere",
                    value = uiState.notSustainedCount.toString(),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun StatBox(
    value: String,
    caption: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StatusPill(
    title: String,
    value: String,
    tint: Color?
) {
    val backgroundColor = tint?.copy(alpha = 0.15f) ?: MaterialTheme.colorScheme.surfaceVariant
    val contentColor = tint ?: MaterialTheme.colorScheme.onSurfaceVariant
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(backgroundColor, CircleShape)
            .border(
                1.dp,
                (tint?.copy(alpha = 0.25f) ?: MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                CircleShape
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}
