package com.laba.firenze.ui.seminars

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.laba.firenze.domain.model.Seminario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeminarDetailScreen(
    navController: NavController,
    seminarId: String,
    viewModel: SeminarsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val seminar = viewModel.getSeminarById(seminarId)
    var showBookingAlert by remember { mutableStateOf(false) }
    
    if (seminar == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Seminario non trovato") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Seminario non trovato")
            }
        }
        return
    }
    
    val details = parseSeminarDetails(seminar.descrizioneEstesa, seminar.esito)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dettagli seminario") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Titolo del seminario
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = prettifyTitle(seminar.titolo),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (details.completed) {
                            Row(
                                modifier = Modifier.padding(top = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Seminario conseguito",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            
            // Docente
            details.docente?.let { docente ->
                item {
                    DetailSection(
                        title = "Docente",
                        icon = Icons.Default.Person
                    ) {
                        Text(
                            text = docente,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            // Date
            if (details.dateLines.isNotEmpty()) {
                item {
                    DetailSection(
                        title = "Date",
                        icon = Icons.Default.DateRange
                    ) {
                        details.dateLines.forEach { date ->
                            Text(
                                text = date,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            
            // Aula
            details.aula?.let { aula ->
                item {
                    DetailSection(
                        title = "Aula",
                        icon = Icons.Default.Room
                    ) {
                        Text(
                            text = aula,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            // Disponibile per
            details.allievi?.let { allievi ->
                item {
                    DetailSection(
                        title = "Disponibile per:",
                        icon = Icons.Default.Group
                    ) {
                        val groups = allieviGroups(allievi)
                        if (groups.isEmpty()) {
                            Text(
                                text = allievi,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                groups.forEach { group ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        group.anno?.let { anno ->
                                            YearPill(year = anno)
                                        }
                                        group.corso?.let { corso ->
                                            CoursePill(course = corso)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Gruppi e orari
            if (details.groups.isNotEmpty()) {
                item {
                    DetailSection(
                        title = "Gruppi e orari",
                        icon = Icons.Default.Schedule
                    ) {
                        details.groups.forEach { group ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "GRUPPO ${group.label}:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (group.time.isEmpty()) "—" else group.time,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
            
            // CFA acquisibili
            details.cfa?.let { cfa ->
                item {
                    DetailSection(
                        title = "CFA acquisibili",
                        icon = Icons.Default.School
                    ) {
                        Text(
                            text = cfa,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Assenze consentite
            details.assenze?.let { assenze ->
                item {
                    DetailSection(
                        title = "Assenze consentite",
                        icon = Icons.Default.Warning,
                        iconTint = MaterialTheme.colorScheme.error
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = assenze,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            // Prenotazione
            item {
                DetailSection(
                    title = "Prenotazione",
                    icon = Icons.Default.Event
                ) {
                    if (seminar.prenotabile) {
                        Button(
                            onClick = { showBookingAlert = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Prenota ora")
                        }
                    } else {
                        Text(
                            text = "Prenotazione non disponibile",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    
    // Alert prenotazione
    if (showBookingAlert) {
        AlertDialog(
            onDismissRequest = { showBookingAlert = false },
            title = { Text("Prenotazione") },
            text = { Text("Collegheremo qui l'endpoint di prenotazione appena disponibile.") },
            confirmButton = {
                TextButton(onClick = { showBookingAlert = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun DetailSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            content()
        }
    }
}

@Composable
private fun YearPill(year: Int) {
    val yearText = when (year) {
        1 -> "1° anno"
        2 -> "2° anno"
        3 -> "3° anno"
        else -> "${year}° anno"
    }
    
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = CircleShape,
        modifier = Modifier.border(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            CircleShape
        )
    ) {
        Text(
            text = yearText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun CoursePill(course: String) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = CircleShape,
        modifier = Modifier.border(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            CircleShape
        )
    ) {
        Text(
            text = course,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

