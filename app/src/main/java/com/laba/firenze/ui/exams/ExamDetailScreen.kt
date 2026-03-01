package com.laba.firenze.ui.exams

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.laba.firenze.domain.model.Esame
import java.text.SimpleDateFormat
import java.util.*

/**
 * ExamDetailScreen completa (identica a iOS ExamDetailView)
 * Include: header card, banner dinamico, propedeuticità, prenotazione
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamDetailScreen(
    examId: String,
    navController: NavController,
    viewModel: ExamsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val allExams by viewModel.exams.collectAsState()
    val exam = viewModel.getExamById(examId)
    var showBookingAlert by remember { mutableStateOf(false) }
    var propedeuticoExpanded by remember { mutableStateOf(false) }
    
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
    
    val liveExam = exam // Per coerenza con iOS "liveEsame"
    val isBooked = liveExam.dataRichiesta != null
    val infoTitle = if (isBooked) "Informazioni sull'esame" else "Informazioni sul corso"
    val infoColor = if (isBooked) Color(0xFFFF3B30) else MaterialTheme.colorScheme.primary // Rosso se prenotato
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dettagli esame") },
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
            // Header Card con Titolo e Voto/Banner
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Titolo esame prominente
                    Text(
                        text = prettifyTitle(liveExam.corso),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Voto o Status Banner (come iOS: displayGrade 30L, ID, —)
                    when {
                        !liveExam.voto.isNullOrEmpty() -> {
                            ExamPassedBanner(displayGrade = displayGradeForBanner(liveExam.voto))
                        }
                        liveExam.dataRichiesta != null -> {
                            // Prenotato - banner rosso con glow
                            BookingGlowBanner(date = liveExam.dataRichiesta!!)
                        }
                    }
                }
            }
            
            // Card Dettagli (con colori dinamici)
            item {
                ExamInfoCard(
                    exam = liveExam,
                    title = infoTitle,
                    titleColor = infoColor
                )
            }
            
            // Propedeuticità (come iOS: usa campo propedeutico dall'API)
            val prerequisite = allExams.firstOrNull { prerequisiteExam ->
                prerequisiteExam.oid != liveExam.oid &&
                (prerequisiteExam.propedeutico?.uppercase() ?: "").contains(liveExam.corso.uppercase())
            }
            
            prerequisite?.let { prev ->
                item {
                    PrerequisiteCard(
                        prerequisite = prev,
                        currentExam = liveExam,
                        isExpanded = propedeuticoExpanded,
                        onExpandedChange = { propedeuticoExpanded = it },
                        isNotBooked = liveExam.dataRichiesta == null && liveExam.voto.isNullOrEmpty()
                    )
                }
            }
            
            // Propedeuticità inversa (come iOS: usa campo propedeutico "Corso propedeutico per X")
            val propedeuticoText = liveExam.propedeutico?.trim() ?: ""
            val nextCourseNames = if (propedeuticoText.isEmpty()) emptyList()
                else listOf(propedeuticoText.replace("Corso propedeutico per ", "").trim()).filter { it.isNotEmpty() }
            val unlockedExams = allExams.filter { potentialExam ->
                potentialExam.oid != liveExam.oid && nextCourseNames.any { name ->
                    potentialExam.corso.uppercase().contains(name.uppercase())
                }
            }
            
            if (nextCourseNames.isNotEmpty() || unlockedExams.isNotEmpty()) {
                item {
                    val isCurrentExamPassed = !liveExam.voto.isNullOrEmpty()
                    val displayUnlocked = if (unlockedExams.isNotEmpty()) unlockedExams.map { it.corso }
                        else nextCourseNames
                    
                    if (!isCurrentExamPassed) {
                        BlockedExamsCard(
                            unlockedExams = displayUnlocked,
                            currentExam = liveExam.corso
                        )
                    } else {
                        UnlockedExamsCard(
                            unlockedExams = displayUnlocked,
                            currentExam = liveExam.corso
                        )
                    }
                }
            }
            
            // Prenotazione (solo se non superato e non già prenotato)
            if (liveExam.voto.isNullOrEmpty() && liveExam.dataRichiesta == null) {
                item {
                    BookingSection(
                        exam = liveExam,
                        onBookClick = { showBookingAlert = true }
                    )
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

// MARK: - Banner Esame Superato (verde, come iOS: testo sx, voto in cerchio dx)
@Composable
private fun ExamPassedBanner(displayGrade: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50) // Verde
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "Esame Superato",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayGrade,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

private fun displayGradeForBanner(voto: String?): String {
    val v = voto?.trim() ?: return "—"
    if (v.isEmpty()) return "—"
    val lower = v.lowercase()
    if (lower.contains("lode") && (lower.contains("30") || lower.startsWith("30"))) return "30L"
    if (lower.contains("idoneo") || lower.contains("idonea") || lower.contains("idoneità")) return "ID"
    val slashIdx = v.indexOf("/")
    if (slashIdx >= 0) {
        val numPart = v.substring(0, slashIdx).trim()
        if (numPart.isNotEmpty()) return numPart
    }
    return v
}

// MARK: - Banner Prenotazione (rosso con glow)
@Composable
private fun BookingGlowBanner(date: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    val pastelRed = Color(0xFFFF3B30)
    val formattedDate = try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val parsedDate = dateFormat.parse(date)
        if (parsedDate != null) {
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN)
            outputFormat.format(parsedDate)
        } else {
            date
        }
    } catch (e: Exception) {
        date
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(16.dp))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = pastelRed
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Prenotazione Effettuata",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "Prenotato il: $formattedDate",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

// MARK: - Card Informazioni Esame
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
                ExamDetailRow(
                    label = "Docente",
                    value = exam.docente ?: "—",
                    icon = Icons.Default.Person,
                    iconColor = titleColor
                )
                
                exam.anno?.let { anno ->
                    ExamDetailRow(
                        label = "Anno",
                        value = getItalianOrdinalYear(anno.toIntOrNull() ?: 1),
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
                
                exam.data?.let { data ->
                    ExamDetailRow(
                        label = "Sostenuto il:",
                        value = formatDate(data),
                        icon = Icons.Default.CalendarMonth,
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
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// MARK: - Card Propedeuticità
@Composable
private fun PrerequisiteCard(
    prerequisite: Esame,
    currentExam: Esame,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    isNotBooked: Boolean
) {
    LaunchedEffect(isNotBooked) {
        onExpandedChange(isNotBooked)
    }
    
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
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandedChange(!isExpanded) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
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
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Per poter sostenere l'esame di ${prettifyTitle(currentExam.corso)} è necessario aver superato questo esame:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    val passed = !prerequisite.voto.isNullOrEmpty()
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
                            
                            if (!prerequisite.voto.isNullOrEmpty()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Superato",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "Voto: ${prerequisite.voto}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(8.dp),
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
    }
}

// MARK: - Card Esami Bloccati
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

// MARK: - Card Esami Sbloccati
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

// MARK: - Sezione Prenotazione
@Composable
private fun BookingSection(
    exam: Esame,
    onBookClick: () -> Unit
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
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Prenotazione",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            if (exam.richiedibile) {
                Text(
                    text = "Clicca sul pulsante sottostante per prenotarti all'appello. Assicurati di poter essere presente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Button(
                    onClick = onBookClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Prenota esame",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
        1 -> "1° anno"
        2 -> "2° anno"
        3 -> "3° anno"
        else -> "$year° anno"
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}
