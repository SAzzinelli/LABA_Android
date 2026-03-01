package com.laba.firenze.ui.exams

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.laba.firenze.domain.model.Esame
import com.laba.firenze.ui.theme.*
import kotlinx.coroutines.delay

enum class StatusFilter { ALL, PASSED, PENDING }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamsScreen(
    navController: NavController,
    viewModel: ExamsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val exams by viewModel.exams.collectAsState()
    
    // Track section visit
    LaunchedEffect(Unit) {
        viewModel.trackSectionVisit("exams")
    }
    
    var selectedYear by remember { mutableIntStateOf(1) }
    var statusFilter by remember { mutableStateOf(StatusFilter.ALL) }
    var queryRaw by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    
    // Determina se è biennio o triennio - usa la stessa logica di HomeScreen
    // Osserva il profilo reattivamente per aggiornare i filtri quando viene caricato
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isBiennio = isBiennioLevel(profile)
    
    val years = if (isBiennio) listOf(1, 2) else listOf(1, 2, 3)
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Debounce per la ricerca (identico a iOS)
    LaunchedEffect(queryRaw) {
        delay(250)
        query = queryRaw.trim()
    }
    
    // Filtri identici a iOS
    val filteredByYear = exams.filter { it.anno == selectedYear.toString() }
    val filteredByStatus = when (statusFilter) {
        StatusFilter.ALL -> filteredByYear
        StatusFilter.PASSED -> filteredByYear.filter { exam ->
            val votoTrim = exam.voto?.trim() ?: ""
            !votoTrim.isEmpty() || isIdoneitaVote(exam.voto) || exam.data != null
        }
        StatusFilter.PENDING -> filteredByYear.filter { exam ->
            val votoTrim = exam.voto?.trim() ?: ""
            votoTrim.isEmpty() && !isIdoneitaVote(exam.voto) && exam.data == null
        }
    }
    
    val filtered = filteredByStatus.filter { exam ->
        val q = query.trim()
        if (q.isEmpty()) return@filter true
        
        val qLower = q.lowercase()
        val numericQuery = q.filter { it.isDigit() }.toIntOrNull()
        
        // Ricerca per titolo corso o docente
        if (prettifyTitle(exam.corso).contains(q, ignoreCase = true)) return@filter true
        if ((exam.docente ?: "").contains(q, ignoreCase = true)) return@filter true
        
        // Ricerca per voto numerico
        if (numericQuery != null && voteNumber(exam.voto) == numericQuery) return@filter true
        
        // Ricerca per idoneità
        if (qLower.contains("idone") && isIdoneitaVote(exam.voto)) return@filter true
        
        false
    }
    
    val regularExams = filtered.filter { !isOther(it) }
    val workshops = filtered.filter { it.corso.uppercase().contains("ATTIVITA' A SCELTA") }
    val thesis = filtered.filter { it.corso.uppercase().contains("TESI FINALE") }
    
    LaunchedEffect(Unit) {
        viewModel.loadExams()
        selectedYear = viewModel.getCurrentYear() ?: 1
    }

    var statusMenuExpanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header con ricerca e filtri di status in alto a destra
        TopAppBar(
            title = { Text("Esami") },
            actions = {
                Box {
                    IconButton(onClick = { statusMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = "Filtri status"
                        )
                    }
                    DropdownMenu(
                        expanded = statusMenuExpanded,
                        onDismissRequest = { statusMenuExpanded = false }
                    ) {
                        StatusFilter.entries.forEach { filter ->
                            DropdownMenuItem(
                                text = { Text(getStatusTitle(filter)) },
                                onClick = {
                                    statusFilter = filter
                                    statusMenuExpanded = false
                                },
                                leadingIcon = {
                                    if (statusFilter == filter) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = null
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        )
        
        // Barra di ricerca
        OutlinedTextField(
            value = queryRaw,
            onValueChange = { queryRaw = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Cerca esami") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(28.dp), // Forma capsula
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { keyboardController?.hide() }
            )
        )
        
        // Year Filters (come in Corsi, in una riga orizzontale)
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(years) { year ->
                FilterChip(
                    onClick = { selectedYear = year },
                    label = { 
                        Text(
                            text = getItalianOrdinalYear(year),
                            textAlign = TextAlign.Center
                        )
                    },
                    selected = selectedYear == year,
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Esami regolari
                items(regularExams) { exam ->
                    ExamCard(
                        exam = exam,
                        onClick = { 
                            val examId = exam.oid ?: "index_${exams.indexOf(exam)}"
                            navController.navigate("exam_detail/$examId")
                        }
                    )
                }
                
                // Workshop / Seminari / Tirocinio
                if (workshops.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Workshop / Seminari / Tirocinio",
                            icon = Icons.Filled.Workspaces
                        )
                    }
                    items(workshops) { exam ->
                        ExamCard(
                            exam = exam,
                            onClick = { 
                            val examId = exam.oid ?: "index_${exams.indexOf(exam)}"
                            navController.navigate("exam_detail/$examId")
                        }
                        )
                    }
                }
                
                // Tesi Finale
                if (thesis.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Tesi Finale",
                            icon = Icons.Filled.School
                        )
                    }
                    items(thesis) { exam ->
                        ExamCard(
                            exam = exam,
                            onClick = { 
                            val examId = exam.oid ?: "index_${exams.indexOf(exam)}"
                            navController.navigate("exam_detail/$examId")
                        }
                        )
                    }
                }
                
                if (regularExams.isEmpty() && workshops.isEmpty() && thesis.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nessun esame trovato",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExamCard(
    exam: Esame,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExamGradeBadge(voto = exam.voto)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = prettifyTitle(exam.corso),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                exam.docente?.let { docente ->
                    if (docente.isNotEmpty()) {
                        Text(
                            text = docente,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

/** Badge circolare voto esame (come iOS ExamGradeBadgeView): 28, 30L, ID, — */
@Composable
private fun ExamGradeBadge(voto: String?, size: Int = 32) {
    val label = displayGradeForBadge(voto)
    val isPending = voto.isNullOrBlank() || voto.trim().isEmpty()
    val isLode = isLodeGrade(voto)
    val accentColor = MaterialTheme.colorScheme.primary
    
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(
                if (isPending) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else accentColor
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isPending) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    else Color.White
        )
    }
}

private fun displayGradeForBadge(voto: String?): String {
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

private fun isLodeGrade(voto: String?): Boolean {
    val v = voto?.trim() ?: return false
    if (v.isEmpty()) return false
    val lower = v.lowercase()
    return lower.contains("lode") && (lower.contains("30") || lower.startsWith("30"))
}

// Helper functions identiche a iOS
private fun isOther(exam: Esame): Boolean {
    val t = exam.corso.lowercase()
    return t.contains("attivit") || t.contains("tesi")
}

private fun voteNumber(voto: String?): Int? {
    val v = voto?.trim() ?: return null
    if (v.isEmpty()) return null
    val comps = v.split("/")
    return comps.firstOrNull()?.trim()?.toIntOrNull()
}

private fun isIdoneitaVote(voto: String?): Boolean {
    val v = voto?.lowercase() ?: return false
    return v.contains("idoneo") || v.contains("idonea") || v.contains("idoneità")
}

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

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 12.dp, start = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun getStatusTitle(filter: StatusFilter): String {
    return when (filter) {
        StatusFilter.ALL -> "Tutti"
        StatusFilter.PASSED -> "Sostenuti"
        StatusFilter.PENDING -> "Non sostenuti"
    }
}


/**
 * Determina se lo studente è del biennio basandosi sul pianoStudi e sulla matricola
 * (stessa logica di HomeScreen.isBiennioLevel)
 */
private fun isBiennioLevel(profile: com.laba.firenze.domain.model.StudentProfile?): Boolean {
    if (profile == null) return false
    
    val pianoStudi = profile.pianoStudi?.lowercase() ?: ""
    val matricola = profile.matricola?.lowercase() ?: ""
    
    // Controlla nel pianoStudi
    val pianoContainsBiennio = pianoStudi.contains("biennio") || 
                               pianoStudi.contains("ii livello") || 
                               pianoStudi.contains("2° livello") || 
                               pianoStudi.contains("secondo livello")
    
    // Controlla nella matricola (se contiene "biennio" e non "triennio", è biennio)
    val hasOnlyBiennio = matricola.contains("biennio") && !matricola.contains("triennio")
    
    val result = pianoContainsBiennio || hasOnlyBiennio
    android.util.Log.d("ExamsScreen", "isBiennioLevel - pianoStudi: '$pianoStudi', matricola: '$matricola', result: $result")
    return result
}

private fun getGradeColor(grade: String?): Color {
    return when {
        grade?.contains("30") == true -> Grade_Excellent
        grade?.contains("2[6-9]") == true -> Grade_Good
        grade?.contains("1[8-9]") == true -> Grade_Pass
        grade?.contains("1[0-7]") == true -> Grade_Fail
        else -> Neutral_Gray
    }
}