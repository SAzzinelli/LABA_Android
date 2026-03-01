package com.laba.firenze.ui.courses

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.laba.firenze.ui.theme.LABA_Blue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    navController: NavController,
    viewModel: CoursesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    @Suppress("UNUSED_VARIABLE")
    val context = LocalContext.current
    
    // Track section visit
    LaunchedEffect(Unit) {
        viewModel.trackSectionVisit("corsi")
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Corsi") }
        )
        
        // Barra di ricerca (identica a Esami)
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::updateSearchQuery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Cerca corsi") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(28.dp), // Forma capsula
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { keyboardController?.hide() }
            )
        )
        
        // Year Filter (pillole senza sfondo)
        // Determina se è biennio o triennio - usa la stessa logica di HomeScreen
        // Osserva il profilo reattivamente per aggiornare i filtri quando viene caricato
        val profile by viewModel.userProfile.collectAsStateWithLifecycle()
        val isBiennio = isBiennioLevel(profile)
        
        val yearFilters = if (isBiennio) {
            listOf("Tutti", "1° anno", "2° anno")
        } else {
            listOf("Tutti", "1° anno", "2° anno", "3° anno")
        }
        
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(yearFilters) { year ->
                FilterChip(
                    onClick = { viewModel.updateYearFilter(year) },
                    label = { Text(year) },
                    selected = year == uiState.selectedYear,
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
        
        val regularCourses = uiState.courses.filter { !isOther(it.corso) }
        val workshops = uiState.courses.filter { it.corso.uppercase().contains("ATTIVIT") && it.corso.uppercase().contains("SCELTA") }
        val thesis = uiState.courses.filter { it.corso.uppercase().contains("TESI FINALE") }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(regularCourses) { course ->
                CourseCard(
                    course = course,
                    onClick = { navController.navigate("course_detail/${course.oid ?: ""}") }
                )
            }
            if (workshops.isNotEmpty()) {
                item {
                    Text(
                        text = "Workshop / Seminari / Tirocinio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                items(workshops) { course ->
                    CourseCard(
                        course = course,
                        onClick = { navController.navigate("course_detail/${course.oid ?: ""}") }
                    )
                }
            }
            if (thesis.isNotEmpty()) {
                item {
                    Text(
                        text = "Tesi Finale",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                items(thesis) { course ->
                    CourseCard(
                        course = course,
                        onClick = { navController.navigate("course_detail/${course.oid ?: ""}") }
                    )
                }
            }
        }
    }
}

/**
 * Invia email al professore del corso
 */
private fun sendEmailToTeacher(context: android.content.Context, course: com.laba.firenze.domain.model.Esame) {
    val teacherEmail = getTeacherEmail(course.docente)
    if (teacherEmail != null) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:$teacherEmail".toUri()
            putExtra(Intent.EXTRA_SUBJECT, "Richiesta informazioni - ${prettifyTitle(course.corso)}")
        }
        
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback: copia email negli appunti
            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Email docente", teacherEmail)
            clipboard.setPrimaryClip(clip)
            android.widget.Toast.makeText(context, "Email copiata negli appunti", android.widget.Toast.LENGTH_SHORT).show()
        }
    } else {
        android.widget.Toast.makeText(context, "Email del docente non disponibile", android.widget.Toast.LENGTH_SHORT).show()
    }
}

/**
 * Ottiene l'email del docente
 */
private fun getTeacherEmail(docente: String?): String? {
    if (docente.isNullOrBlank()) return null
    
    // Logica semplificata per ottenere email docente
    val emailMap = mapOf(
        "docente1" to "docente1@laba.biz",
        "docente2" to "docente2@laba.biz",
        // Aggiungi altri docenti qui
    )
    
    return emailMap[docente.lowercase()] ?: "${docente.lowercase().replace(" ", ".")}@laba.biz"
}



@Composable
private fun CourseCard(
    course: com.laba.firenze.domain.model.Esame,
    onClick: () -> Unit
) {
    val yearTint = when (course.anno?.toIntOrNull()) {
        1 -> Color(0xFFE3F2FD)
        2 -> Color(0xFFE8F5E9)
        3 -> Color(0xFFFFF3E0)
        else -> Color(0xFFF3E5F5)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = prettifyTitle(course.corso),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = MaterialTheme.typography.titleMedium.fontWeight
            )
            
            if (course.docente != null) {
                Text(
                    text = course.docente,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                course.anno?.toIntOrNull()?.let { anno ->
                    com.laba.firenze.ui.common.Pill(
                        text = when (anno) {
                            1 -> "1° anno"
                            2 -> "2° anno"
                            3 -> "3° anno"
                            else -> "$anno° anno"
                        },
                        kind = com.laba.firenze.ui.common.PillKind.YEAR,
                        tintOverride = yearTint
                    )
                }
                course.cfa?.takeIf { it.isNotBlank() }?.let { cfa ->
                    com.laba.firenze.ui.common.Pill(
                        text = "$cfa CFA",
                        kind = com.laba.firenze.ui.common.PillKind.CFA
                    )
                }
            }
        }
    }
}

private fun isOther(title: String): Boolean {
    val t = title.lowercase()
    return t.contains("attivit") || t.contains("tesi")
}

@Suppress("UNUSED_FUNCTION")
private fun isWorkshopCourse(title: String): Boolean {
    val lowerTitle = title.lowercase()
    return lowerTitle.contains("workshop") || 
           lowerTitle.contains("seminario") || 
           lowerTitle.contains("tirocinio")
}

@Suppress("UNUSED_FUNCTION")
private fun isThesisCourse(title: String): Boolean {
    val lowerTitle = title.lowercase()
    return lowerTitle.contains("tesi")
}

private fun prettifyTitle(title: String): String {
    return title.replace("_", " ")
        .split(" ")
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
}

@Suppress("UNUSED_FUNCTION")
private fun getGradeColor(grade: String): Color {
    return when {
        grade.contains("30") || grade.contains("lode") -> Color(0xFF4CAF50)
        grade.contains("27") || grade.contains("28") || grade.contains("29") -> Color(0xFF8BC34A)
        grade.contains("24") || grade.contains("25") || grade.contains("26") -> Color(0xFFFFC107)
        grade.contains("18") -> Color(0xFFFF9800)
        else -> Color(0xFF9E9E9E)
    }
}

@Suppress("UNUSED_FUNCTION")
private fun getYearTint(year: String): Color {
    return when {
        year.contains("1") -> Color(0xFFE3F2FD)
        year.contains("2") -> Color(0xFFE8F5E8)
        year.contains("3") -> Color(0xFFFFF3E0)
        else -> Color(0xFFF3E5F5)
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
    android.util.Log.d("CoursesScreen", "isBiennioLevel - pianoStudi: '$pianoStudi', matricola: '$matricola', result: $result")
    return result
}

@Composable
@Suppress("UNUSED_FUNCTION", "FunctionName")
private fun coursesViewModel(): CoursesViewModel {
    return hiltViewModel()
}
