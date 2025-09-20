package com.laba.firenze.ui.exams

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.laba.firenze.domain.model.Esame
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamDetailScreen(
    examId: String,
    navController: NavController,
    viewModel: ExamsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val exam = viewModel.getExamById(examId)
    var showBookingAlert by remember { mutableStateOf(false) }
    
    // Mostra loading se i dati non sono ancora caricati
    if (uiState.isLoading && exam == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    if (exam == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Esame non trovato")
        }
        return
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Dettagli esame") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sezione Dettagli
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Dettagli",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        DetailRow("Materia", prettifyTitle(exam.corso))
                        
                        exam.docente?.let { docente ->
                            if (docente.isNotEmpty()) {
                                DetailRow("Docente", docente)
                            }
                        }
                        
                        exam.anno?.let { anno ->
                            DetailRow("Anno", getItalianOrdinalYear(anno.toIntOrNull() ?: 1))
                        }
                        
                        exam.cfa?.let { cfa ->
                            DetailRow("CFA", cfa)
                        }
                        
                        exam.data?.let { data ->
                            DetailRow("Data", formatDate(data))
                        }
                        
                        if (!exam.voto.isNullOrEmpty()) {
                            DetailRow("Voto", exam.voto)
                        }
                    }
                }
            }
            
            // Sezione Propedeuticità (se presente)
            val prerequisite = uiState.allExams.firstOrNull { prerequisiteExam ->
                (exam.propedeutico ?: "").uppercase().contains(prerequisiteExam.corso.uppercase())
            }
            
            if (prerequisite != null) {
                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Precedente richiesto",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val passed = !(prerequisite.voto ?: "").isEmpty()
                                Icon(
                                    imageVector = if (passed) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = if (passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                
                                Column {
                                    Text(
                                        text = prettifyTitle(prerequisite.corso),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    if (!prerequisite.voto.isNullOrEmpty()) {
                                        Text(
                                            text = "Voto: ${prerequisite.voto}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        AssistChip(
                                            onClick = { },
                                            label = { Text("Da sostenere") },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                                labelColor = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        )
                                    }
                                }
                            }
                            
                            Text(
                                text = "È necessario aver superato questo esame per poter prenotare ${prettifyTitle(exam.corso)}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
            
            // Sezione Propedeutico per (se presente)
            if (!exam.propedeutico.isNullOrEmpty()) {
                val clean = exam.propedeutico.replace("Corso propedeutico per ", "")
                
                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Propedeuticità",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                
                                Text(
                                    text = prettifyTitle(clean),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Text(
                                text = "Superando ${prettifyTitle(exam.corso)} potrai prenotare i corsi indicati di seguito.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
            
            // Sezione Prenotazione
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Prenotazione",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        if (exam.richiedibile) {
                            Button(
                                onClick = { showBookingAlert = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Event,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
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
    }
    
    // Alert per prenotazione
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
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// Helper functions
private fun prettifyTitle(title: String): String {
    return title.replace("_", " ")
        .split(" ")
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase() else it.toString() 
            }
        }
}

private fun getItalianOrdinalYear(year: Int): String {
    return when (year) {
        1 -> "Primo anno"
        2 -> "Secondo anno"
        3 -> "Terzo anno"
        else -> "Anno $year"
    }
}

private fun formatDate(dateString: String): String {
    return try {
        // Prova a parsare la data se è in formato ISO
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString // Ritorna la stringa originale se non riesce a parsarla
    }
}
