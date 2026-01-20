package com.laba.firenze.ui.courses

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

/**
 * CourseDetailScreen completa (identica a iOS CourseDetailView)
 * Include: header card, propedeuticità, email docente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    navController: NavController,
    courseId: String,
    viewModel: CoursesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    // Trova il corso specifico (cerca in allCourses per essere sicuri di trovarlo)
    val course = uiState.allCourses.find { it.oid == courseId } ?: uiState.courses.find { it.oid == courseId }
    
    if (course == null) {
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
    
    val docenteEmail = getTeacherEmail(course.docente)
    val previousRequired = findPreviousRequired(course, uiState.allCourses)
    val nextCourses = extractNextCourses(course)
    val isCurrentCoursePassed = !course.voto.isNullOrEmpty()

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
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Card con Titolo
            item {
                Text(
                    text = prettifyTitle(course.corso),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            
            // Card Informazioni sul corso
            item {
                ExamInfoCard(
                    exam = course,
                    title = "Informazioni sul corso",
                    titleColor = MaterialTheme.colorScheme.primary
                )
            }
            
            // Precedente richiesto (Propedeuticità)
            previousRequired?.let { prev ->
                item {
                    PrerequisiteCardForCourse(
                        prerequisite = prev,
                        currentCourse = course
                    )
                }
            }
            
            // Propedeuticità (esami sbloccati da questo corso)
            if (nextCourses.isNotEmpty()) {
                item {
                    if (!isCurrentCoursePassed) {
                        // Mostra "Esami successivi bloccati"
                        BlockedExamsCard(
                            unlockedExams = nextCourses,
                            currentExam = course.corso
                        )
                    } else {
                        // Mostra "Esami sbloccati"
                        UnlockedExamsCard(
                            unlockedExams = nextCourses,
                            currentExam = course.corso
                        )
                    }
                }
            }
            
            // Invia mail al docente (centrato)
            docenteEmail?.let { email ->
                item {
                    ContactCard(
                        email = email,
                        courseName = course.corso,
                        onEmailClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$email")
                                putExtra(Intent.EXTRA_SUBJECT, "Richiesta informazioni - ${prettifyTitle(course.corso)}")
                            }
                            
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            } else {
                                // Fallback: copia email negli appunti
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Email docente", email)
                                clipboard.setPrimaryClip(clip)
                                android.widget.Toast.makeText(context, "Email copiata negli appunti", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

// MARK: - Card Informazioni Esame (riutilizzabile)
@Composable
private fun ExamInfoCard(
    exam: Esame,
    title: String,
    titleColor: Color
) {
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
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = titleColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                exam.docente?.let { docente ->
                    ExamDetailRow(
                        label = "Docente",
                        value = docente,
                        icon = Icons.Default.Person,
                        iconColor = titleColor
                    )
                }
                
                exam.anno?.let { anno ->
                    ExamDetailRow(
                        label = "Anno",
                        value = italianOrdinalYear(anno.toIntOrNull() ?: 1),
                        icon = Icons.Default.CalendarMonth,
                        iconColor = titleColor
                    )
                }
                
                exam.cfa?.let { cfa ->
                    ExamDetailRow(
                        label = "Crediti formativi (CFA)",
                        value = cfa,
                        icon = Icons.Default.School,
                        iconColor = titleColor
                    )
                }
            }
        }
    }
}

@Composable
private fun ExamDetailRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Row(
            modifier = Modifier.weight(1f),
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
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End
            )
        }
    }
}

// MARK: - Card Propedeuticità per Corso
@Composable
private fun PrerequisiteCardForCourse(
    prerequisite: Esame,
    currentCourse: Esame
) {
    val passed = !prerequisite.voto.isNullOrEmpty()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Esame propedeutico richiesto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = "Per poter sostenere l'esame di ${prettifyTitle(currentCourse.corso)} è necessario aver superato questo esame:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .background(
                        if (passed) Color(0xFF4CAF50).copy(alpha = 0.1f)
                        else Color(0xFFFF9800).copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (passed) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    modifier = Modifier.size(32.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = prettifyTitle(prerequisite.corso),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    prerequisite.voto?.let { voto ->
                        if (voto.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Superato",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "Voto: $voto",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    if (prerequisite.voto.isNullOrEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "Da sostenere",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// MARK: - Card Esami Bloccati (riutilizzabile)
@Composable
private fun BlockedExamsCard(
    unlockedExams: List<String>,
    currentExam: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF9800).copy(alpha = 0.1f) // Arancione chiaro
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Esami successivi bloccati",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
            }
            
            unlockedExams.forEach { unlockedExam ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = prettifyTitle(unlockedExam),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Non disponibile",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Text(
                text = "Devi prima sostenere ${prettifyTitle(currentExam)} prima di poter prenotare ${unlockedExams.joinToString(" e ") { prettifyTitle(it) }}.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// MARK: - Card Esami Sbloccati (riutilizzabile)
@Composable
private fun UnlockedExamsCard(
    unlockedExams: List<String>,
    currentExam: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Esami sbloccati",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            unlockedExams.forEach { unlockedExam ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = prettifyTitle(unlockedExam),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Text(
                text = "Superando ${prettifyTitle(currentExam)} sarà possibile prenotare ${unlockedExams.joinToString(" e ") { prettifyTitle(it) }}.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// MARK: - Card Contatti
@Composable
private fun ContactCard(
    email: String,
    courseName: String,
    onEmailClick: () -> Unit
) {
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
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Contatti",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Button(
                onClick = onEmailClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Invia mail al docente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
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

private fun italianOrdinalYear(year: Int): String {
    return when (year) {
        1 -> "1° anno"
        2 -> "2° anno"
        3 -> "3° anno"
        else -> "${year}° anno"
    }
}

/**
 * Trova il corso precedente richiesto per questo corso
 */
private fun findPreviousRequired(course: Esame, allCourses: List<Esame>): Esame? {
    val target = course.corso.uppercase()
    return allCourses.firstOrNull { exam ->
        val propedeutico = exam.propedeutico?.uppercase() ?: ""
        propedeutico.contains(target) && exam.oid != course.oid
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
 * Ottiene l'email del docente (identica a iOS teacherEmails)
 */
private fun getTeacherEmail(docente: String?): String? {
    if (docente.isNullOrBlank()) return null
    
    // Logica identica a iOS teacherEmails
    val parts = docente.split("/").map { it.trim() }
    return parts.firstOrNull()?.let { full ->
        val comps = full.split(" ").filter { comp -> comp.isNotEmpty() }
        if (comps.size >= 2) {
            val first = comps[0]
            val last = comps.drop(1).joinToString("") // Unisce cognomi composti
            var base = "$first.$last"
            base = base.lowercase()
                .replace("'", "")
                .replace(" ", "")
                .replace("-", "")
                .normalizeDiacritics()
            "$base@labafirenze.com"
        } else {
            null
        }
    }
}

/**
 * Normalizza caratteri accentati (identica a iOS folding)
 */
private fun String.normalizeDiacritics(): String {
    return this.lowercase()
        .replace("à", "a").replace("è", "e").replace("é", "e")
        .replace("ì", "i").replace("ò", "o").replace("ù", "u")
}
