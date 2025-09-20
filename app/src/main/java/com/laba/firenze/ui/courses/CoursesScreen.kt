package com.laba.firenze.ui.courses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
    
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Corsi") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                }
            }
        )
        
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::updateSearchQuery,
            label = { Text("Cerca corsi") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { keyboardController?.hide() }
            )
        )
        
        // Year Filter
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf("Tutti", "1° Anno", "2° Anno", "3° Anno")) { year ->
                FilterChip(
                    onClick = { viewModel.updateYearFilter(year) },
                    label = { Text(year) },
                    selected = year == uiState.selectedYear
                )
            }
        }
        
        // Courses List
        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Regular Courses Section
            item {
                Text(
                    text = "Corsi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = MaterialTheme.typography.titleMedium.fontWeight,
                    color = LABA_Blue
                )
            }
            
            items(uiState.courses.filter { isRegularCourse(it.corso) }) { course ->
                CourseCard(
                    course = course,
                    onClick = { 
                        // TODO: Navigate to course detail
                    }
                )
            }
            
            // Workshop/Seminars Section
            item {
                Text(
                    text = "Workshop / Seminari / Tirocinio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = MaterialTheme.typography.titleMedium.fontWeight,
                    color = LABA_Blue,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            
            items(uiState.courses.filter { isWorkshopCourse(it.corso) }) { course ->
                CourseCard(
                    course = course,
                    onClick = { 
                        // TODO: Navigate to course detail
                    }
                )
            }
            
            // Thesis Section
            item {
                Text(
                    text = "Tesi Finale",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = MaterialTheme.typography.titleMedium.fontWeight,
                    color = LABA_Blue,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            
            items(uiState.courses.filter { isThesisCourse(it.corso) }) { course ->
                CourseCard(
                    course = course,
                    onClick = { 
                        // TODO: Navigate to course detail
                    }
                )
            }
        }
        }
    }
}

@Composable
private fun CourseCard(
    course: com.laba.firenze.domain.model.Esame,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp,
            hoveredElevation = 4.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (course.cfa != null && course.cfa.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "${course.cfa} CFA",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                if (course.anno != null && course.anno.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "${course.anno}° anno",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// Helper functions
private fun isRegularCourse(title: String): Boolean {
    val lowerTitle = title.lowercase()
    return !lowerTitle.contains("workshop") && 
           !lowerTitle.contains("seminario") && 
           !lowerTitle.contains("tirocinio") && 
           !lowerTitle.contains("tesi")
}

private fun isWorkshopCourse(title: String): Boolean {
    val lowerTitle = title.lowercase()
    return lowerTitle.contains("workshop") || 
           lowerTitle.contains("seminario") || 
           lowerTitle.contains("tirocinio")
}

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

private fun getGradeColor(grade: String): androidx.compose.ui.graphics.Color {
    return when {
        grade.contains("30") || grade.contains("lode") -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        grade.contains("27") || grade.contains("28") || grade.contains("29") -> androidx.compose.ui.graphics.Color(0xFF8BC34A)
        grade.contains("24") || grade.contains("25") || grade.contains("26") -> androidx.compose.ui.graphics.Color(0xFFFFC107)
        grade.contains("18") -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        else -> androidx.compose.ui.graphics.Color(0xFF9E9E9E)
    }
}

private fun getYearTint(year: String): androidx.compose.ui.graphics.Color {
    return when {
        year.contains("1") -> androidx.compose.ui.graphics.Color(0xFFE3F2FD)
        year.contains("2") -> androidx.compose.ui.graphics.Color(0xFFE8F5E8)
        year.contains("3") -> androidx.compose.ui.graphics.Color(0xFFFFF3E0)
        else -> androidx.compose.ui.graphics.Color(0xFFF3E5F5)
    }
}

@Composable
fun CoursesViewModel(): CoursesViewModel {
    return hiltViewModel()
}
