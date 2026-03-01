package com.laba.firenze.ui.seminars

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeminarDetailScreen(
    navController: NavController,
    seminarId: String,
    viewModel: SeminarsViewModel = hiltViewModel()
) {
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
    
    val rawDetails = parseSeminarDetails(seminar.descrizioneEstesa, seminar.esito)
    val details = rawDetails.copy(completed = seminar.partecipato || rawDetails.completed)
    
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
            contentPadding = PaddingValues(bottom = 140.dp)
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
                            text = prettifyTitle(seminarTitle(seminar.titolo)),
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
            
            // Allegati (documentOid) — mostra solo se OID valido (no placeholder)
            seminar.documentOid?.takeIf { isValidDocumentOid(it) }?.let { oid ->
                item {
                    DetailSection(
                        title = "Allegati",
                        icon = Icons.Default.Description
                    ) {
                        val titleEnc = Uri.encode(prettifyTitle(seminarTitle(seminar.titolo)))
                        TextButton(
                            onClick = { navController.navigate("document_viewer/$oid/$titleEnc") }
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Visualizza allegato")
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
            
            // Fallback: se parsing non ha estratto nulla ma c'è descrizioneEstesa (es. "Prova del testo - A.A. 0-0")
            if (details.docente == null && details.dateLines.isEmpty() && details.aula == null && details.allievi == null && details.cfa == null) {
                val raw = seminar.descrizioneEstesa?.trim()
                if (!raw.isNullOrEmpty()) {
                    item {
                        DetailSection(
                            title = "Descrizione",
                            icon = Icons.Default.Info
                        ) {
                            Text(
                                text = plainText(raw),
                                style = MaterialTheme.typography.bodyMedium
                            )
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
            
            // Assenze consentite (come iOS)
            details.assenzeMax?.let { max ->
                item {
                    DetailSection(
                        title = "Assenze consentite",
                        icon = Icons.Default.Warning,
                        iconTint = if (max == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (max == 0) {
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
                                        text = "Nessuna assenza consentita",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Text(
                                    text = "Alla prima assenza o se si superano le assenze consentite non vengono assegnati i CFA.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text(
                                    text = "Numero massimo: $max assenze",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
            
            // Stato (come iOS)
            item {
                DetailSection(
                    title = "Stato",
                    icon = Icons.Default.Event
                ) {
                    when {
                        details.completed -> Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Hai frequentato il seminario!",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        seminar.dataRichiesta != null -> Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Richiesta inviata il ${formatDateDDMMYYYY(seminar.dataRichiesta)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        seminar.richiedibile -> Button(
                            onClick = { showBookingAlert = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Prenota partecipazione")
                        }
                        else -> Text(
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

private fun formatDateDDMMYYYY(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return "—"
    return try {
        val iso = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val out = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val d = iso.parse(dateStr)
        if (d != null) out.format(d) else dateStr
    } catch (_: Exception) {
        try {
            val inFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val d = inFmt.parse(dateStr)
            if (d != null) inFmt.format(d) else dateStr
        } catch (_: Exception) {
            dateStr
        }
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

