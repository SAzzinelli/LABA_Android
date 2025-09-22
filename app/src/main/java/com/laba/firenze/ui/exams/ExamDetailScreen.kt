package com.laba.firenze.ui.exams

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
            // Logica corretta: un esame ha un prerequisito solo se è il "2" o "3" di una materia
            // Esempio: "Architettura Interni 2" richiede "Architettura Interni 1"
            val prerequisite = uiState.allExams.firstOrNull { prerequisiteExam ->
                val examCourse = exam.corso.uppercase()
                val prerequisiteCourse = prerequisiteExam.corso.uppercase()
                
                // Estrai il numero dall'esame attuale (se presente)
                val examNumber = examCourse.filter { it.isDigit() }.toIntOrNull()
                val prerequisiteNumber = prerequisiteCourse.filter { it.isDigit() }.toIntOrNull()
                
                // L'esame attuale deve essere 2 o 3, e il prerequisito deve essere il numero precedente
                examNumber != null && prerequisiteNumber != null && 
                examNumber > 1 && prerequisiteNumber == examNumber - 1 &&
                // Controlla che sia la stessa materia (stesso nome senza numero)
                examCourse.replace(Regex("\\d+"), "").trim() == prerequisiteCourse.replace(Regex("\\d+"), "").trim()
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
                                        // Badge senza bordo, solo sfondo colorato
                                        Surface(
                                            color = MaterialTheme.colorScheme.errorContainer,
                                            shape = MaterialTheme.shapes.small,
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Text(
                                                text = "Da sostenere",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Text(
                                text = "È necessario aver superato ${prettifyTitle(prerequisite.corso)} per poter prenotare ${prettifyTitle(exam.corso)}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
            
            // Sezione Propedeuticità - mostra quali esami questo esame sblocca
            val unlockedExams = uiState.allExams.filter { potentialExam ->
                val examCourse = exam.corso.uppercase()
                val potentialCourse = potentialExam.corso.uppercase()
                
                // Estrai i numeri
                val examNumber = examCourse.filter { it.isDigit() }.toIntOrNull()
                val potentialNumber = potentialCourse.filter { it.isDigit() }.toIntOrNull()
                
                // L'esame attuale deve essere 1 o 2, e l'esame sbloccato deve essere il numero successivo
                examNumber != null && potentialNumber != null && 
                potentialNumber == examNumber + 1 &&
                // Controlla che sia la stessa materia
                examCourse.replace(Regex("\\d+"), "").trim() == potentialCourse.replace(Regex("\\d+"), "").trim()
            }
            
            if (unlockedExams.isNotEmpty()) {
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
                            
                            unlockedExams.forEach { unlockedExam ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    
                                    Text(
                                        text = prettifyTitle(unlockedExam.corso),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            
                            Text(
                                text = "Superando ${prettifyTitle(exam.corso)} potrai prenotare questi esami.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
            
            // Sezione Prenotazione - Pulsante
            item {
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
                    // Pulsante disattivato per prenotazione non disponibile
                    Button(
                        onClick = { },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Prenotazione non disponibile")
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
