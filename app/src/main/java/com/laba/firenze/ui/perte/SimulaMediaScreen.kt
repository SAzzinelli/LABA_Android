package com.laba.firenze.ui.perte

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
    // var courseQuery by remember { mutableStateOf("") } // Non utilizzata
    
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding(), // Aggiunge padding per la status bar trasparente
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp), // Aumentato per lo scroll
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Situazione attuale
                item {
                    CurrentSituationCard(uiState = uiState)
                }
                
                // Aggiungi esami - versione semplificata
                item {
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
                                    onClick = { 
                                        showAddSheet = true
                                        // Debug: verifica che la lista non sia vuota
                                        println("DEBUG: availableCourses size = ${uiState.availableCourses.size}")
                                        println("DEBUG: availableCourses = ${uiState.availableCourses}")
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Aggiungi esami")
                                }
                            }
                        } else {
                            // Lista esami simulati con voti modificabili
                            LazyColumn(
                                modifier = Modifier.height(200.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(simulated) { item ->
                                    SimulatedExamRow(
                                        item = item,
                                        onGradeChange = { newGrade ->
                                            simulated = simulated.map { 
                                                if (it.id == item.id) it.copy(grade = newGrade) else it 
                                            }
                                        },
                                        onRemove = {
                                            simulated = simulated.filter { it.id != item.id }
                                        }
                                    )
                                }
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showAddSheet = true },
                                    modifier = Modifier.weight(1f),
                                    enabled = uiState.availableCourses.isNotEmpty()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Aggiungi")
                                }
                                
                                Button(
                                    onClick = { simulated = emptyList() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Azzera")
                                }
                            }
                        }
                    }
                }
                }
                
                // Media stimata (solo se ci sono esami simulati)
                if (simulated.isNotEmpty()) {
                    item {
                        EstimatedAverageCard(
                            simulated = simulated,
                            currentAvg30 = uiState.currentAvg30,
                            currentAvg110 = uiState.currentAvg110,
                            sustainedCount = uiState.sustainedCount
                        )
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
        
        // Dialog per aggiungere esami - versione alternativa
        if (showAddSheet) {
            AddExamsDialog(
                availableCourses = uiState.availableCourses,
                simulated = simulated,
                onAddExam = { courseTitle ->
                    if (!simulated.any { it.title == courseTitle }) {
                        simulated = simulated + SimItem(title = courseTitle, grade = 30)
                    }
                    showAddSheet = false
                },
                onDismiss = { showAddSheet = false }
            )
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

@Composable
private fun AddExamsDialog(
    availableCourses: List<String>,
    simulated: List<SimItem>,
    onAddExam: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val chosenCourses = simulated.map { it.title }.toSet()
    val filteredCourses = availableCourses.filter { course ->
        course !in chosenCourses && 
        (searchQuery.isEmpty() || course.contains(searchQuery, ignoreCase = true))
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f), // Ridotto per proporzioni migliori
            shape = RoundedCornerShape(16.dp)
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aggiungi esami",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Chiudi")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cerca materia") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Lista esami
            if (filteredCourses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Book,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Nessun esame disponibile",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (searchQuery.isEmpty()) 
                                "Hai già aggiunto tutti gli esami non sostenuti" 
                            else 
                                "Nessun risultato per la ricerca",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f), // Utilizza tutto lo spazio disponibile
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCourses) { course ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAddExam(course) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = prettifyTitle(course), // Formattazione corretta
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

// Funzione per formattare correttamente i titoli degli esami
private fun prettifyTitle(title: String): String {
    return title.replace("_", " ")
        .split(" ")
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase() else it.toString() 
            }
        }
}

@Composable
private fun SimulatedExamRow(
    item: SimItem,
    onGradeChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    var showGradeMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = prettifyTitle(item.title),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Menu per cambiare voto
                Box {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showGradeMenu = true },
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = item.grade.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showGradeMenu,
                        onDismissRequest = { showGradeMenu = false }
                    ) {
                        (18..30).reversed().forEach { grade ->
                            DropdownMenuItem(
                                text = { Text("$grade") },
                                onClick = {
                                    onGradeChange(grade)
                                    showGradeMenu = false
                                }
                            )
                        }
                    }
                }
                
                // Pulsante rimuovi
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Rimuovi",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun EstimatedAverageCard(
    simulated: List<SimItem>,
    currentAvg30: Double?,
    currentAvg110: Int?, // Non utilizzato ma mantenuto per coerenza API
    sustainedCount: Int
) {
    @Suppress("UNUSED_PARAMETER")
    val newAvg30 = remember(simulated, currentAvg30, sustainedCount) {
        if (simulated.isEmpty()) null
        else {
            // Calcolo corretto: media ponderata tra esami reali e simulati
            val realSum = currentAvg30?.let { it * sustainedCount } ?: 0.0
            val simSum = simulated.sumOf { it.grade }
            val totalCount = sustainedCount + simulated.size
            
            if (totalCount > 0) {
                (realSum + simSum) / totalCount
            } else null
        }
    }
    
    val newAvg110 = newAvg30?.let { (it / 30.0 * 110.0).toInt() }
    
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
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Media stimata",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBox(
                    value = newAvg30?.let { String.format("%.2f", it) } ?: "—",
                    caption = "Media stimata",
                    icon = Icons.Default.Numbers,
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    value = newAvg110?.toString() ?: "—",
                    caption = "Voto d'Ingresso",
                    icon = Icons.Default.School,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Confronto con media attuale
            if (currentAvg30 != null && newAvg30 != null && kotlin.math.abs(newAvg30 - currentAvg30) > 0.01) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                            imageVector = if (newAvg30 >= currentAvg30) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = if (newAvg30 >= currentAvg30) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Da ${String.format("%.2f", currentAvg30)} a ${String.format("%.2f", newAvg30)} con ${simulated.size} esami simulati",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
