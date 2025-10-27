package com.laba.firenze.ui.courses

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.laba.firenze.domain.model.Esame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    navController: NavController,
    courseId: String,
    viewModel: CoursesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    // Trova il corso specifico
    val course = uiState.courses.find { it.oid == courseId }
    
    if (course == null) {
        // Corso non trovato
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Corso non trovato") },
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
                Text("Corso non trovato")
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dettagli corso") },
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
            // Dettagli corso
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Dettagli corso",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        DetailRow("Materia", prettifyTitle(course.corso))
                        course.docente?.let { docente ->
                            DetailRow("Docente", docente)
                        }
                        course.anno?.let { anno ->
                            DetailRow("Anno", italianOrdinalYear(anno.toIntOrNull() ?: 0))
                        }
                        course.cfa?.let { cfa ->
                            DetailRow("CFA", cfa)
                        }
                    }
                }
            }
            
            // Corso precedente richiesto
            val previousRequired = findPreviousRequired(course, uiState.courses)
            if (previousRequired != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Precedente richiesto",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val passed = !course.voto.isNullOrBlank()
                                Icon(
                                    imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (passed) Color.Green else Color(0xFFFF9800)
                                )
                                
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = prettifyTitle(previousRequired.corso),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    
                                    previousRequired.voto?.let { voto ->
                                        if (voto.isNotBlank()) {
                                            Text(
                                                text = "Voto: $voto",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    
                                    if (previousRequired.voto.isNullOrBlank()) {
                                        Surface(
                                            modifier = Modifier.padding(top = 4.dp),
                                            color = MaterialTheme.colorScheme.errorContainer,
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = "Da sostenere",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Text(
                                text = "Devi aver superato questo esame per poter prenotare ${prettifyTitle(course.corso)}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Propedeuticità
            val nextCourses = extractNextCourses(course)
            if (nextCourses.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Propedeuticità",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            nextCourses.forEach { courseName ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFFF9800)
                                    )
                                    
                                    Text(
                                        text = prettifyTitle(courseName),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            
                            Text(
                                text = "Superando ${prettifyTitle(course.corso)} potrai prenotare gli esami elencati.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Email docente
            val teacherEmail = getTeacherEmail(course.docente)
            if (teacherEmail != null) {
                item {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$teacherEmail")
                                putExtra(Intent.EXTRA_SUBJECT, "Richiesta informazioni - ${prettifyTitle(course.corso)}")
                            }
                            
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            } else {
                                // Fallback: copia email negli appunti
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Email docente", teacherEmail)
                                clipboard.setPrimaryClip(clip)
                                // Mostra toast
                                android.widget.Toast.makeText(context, "Email copiata negli appunti", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Invia mail al docente",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Trova il corso precedente richiesto per questo corso
 */
private fun findPreviousRequired(course: Esame, allCourses: List<Esame>): Esame? {
    val target = course.corso.uppercase()
    return allCourses.firstOrNull { exam ->
        val propedeutico = exam.propedeutico?.uppercase() ?: ""
        propedeutico.contains(target)
    }
}

/**
 * Estrae i corsi per cui questo corso è propedeutico
 */
private fun extractNextCourses(course: Esame): List<String> {
    val propedeutico = course.propedeutico
    if (propedeutico.isNullOrBlank()) return emptyList()
    
    val clean = propedeutico.replace("Corso propedeutico per ", "").trim()
    return if (clean.isBlank()) emptyList() else listOf(clean)
}

/**
 * Ottiene l'email del docente
 */
private fun getTeacherEmail(docente: String?): String? {
    if (docente.isNullOrBlank()) return null
    
    // Logica semplificata per ottenere email docente
    // In una implementazione reale, dovresti avere una mappa docenti -> email
    val emailMap = mapOf(
        "docente1" to "docente1@laba.biz",
        "docente2" to "docente2@laba.biz",
        // Aggiungi altri docenti qui
    )
    
    return emailMap[docente.lowercase()] ?: "${docente.lowercase().replace(" ", ".")}@laba.biz"
}

/**
 * Formatta il titolo in proper case
 */
private fun prettifyTitle(title: String): String {
    return title.replace("_", " ")
        .split(" ")
        .joinToString(" ") { word ->
            if (word.isBlank()) word
            else word.lowercase().replaceFirstChar { it.uppercase() }
        }
}

/**
 * Converte numero anno in ordinale italiano
 */
private fun italianOrdinalYear(year: Int): String {
    return when (year) {
        1 -> "1° anno"
        2 -> "2° anno"
        3 -> "3° anno"
        else -> "${year}° anno"
    }
}
