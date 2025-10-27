package com.laba.firenze.ui.perte

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.laba.firenze.ui.LABANavigation
import com.laba.firenze.ui.seminars.prettifyTitle
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeTrendScreen(
    onNavigateBack: () -> Unit,
    onNavigateToExams: () -> Unit,
    viewModel: GradeTrendViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Andamento voti") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showKPIInfo() }) {
                        Icon(Icons.Default.Info, contentDescription = "Info")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // KPI Section
            item {
                KPISection(
                    currentAvg = uiState.currentAvg,
                    deltaAvg = uiState.deltaAvg,
                    examCount = uiState.exams.size
                )
            }
            
            // Chart Section
            if (uiState.exams.isEmpty()) {
                item {
                    EmptyChartView()
                }
            } else {
                item {
                    ChartSection(
                        exams = uiState.exams,
                        avgPoints = uiState.avgPoints,
                        selectedIndex = uiState.selectedIndex,
                        onIndexSelected = viewModel::selectIndex,
                        examTitles = uiState.examTitles,
                        examDates = uiState.examDates,
                        examGrades = uiState.examGrades
                    )
                }
            }
            
            // Statistics Grid
            if (uiState.exams.isNotEmpty()) {
                item {
                    StatisticsGrid(
                        examCount = uiState.exams.size,
                        bestGrade = uiState.bestGrade,
                        worstGrade = uiState.worstGrade,
                        lodiCount = uiState.lodiCount,
                        onLodiInfoClick = viewModel::showLodiInfo
                    )
                }
            }
            
            // Recent Exams
            if (uiState.exams.isNotEmpty()) {
                item {
                    RecentExamsSection(
                        exams = uiState.exams,
                        sortAscending = uiState.sortAscending,
                        onSortChanged = viewModel::toggleSort
                    )
                }
            }
            
            // Footer Link
            if (uiState.exams.isNotEmpty()) {
                item {
                    FooterLink(onNavigateToExams = onNavigateToExams)
                }
            }
        }
    }
    
    // Info Dialogs
    if (uiState.showKPIInfo) {
        KPIInfoDialog(
            onDismiss = viewModel::hideKPIInfo,
            deltaAvg = uiState.deltaAvg
        )
    }
    
    if (uiState.showLodiInfo) {
        LodiInfoDialog(
            onDismiss = viewModel::hideLodiInfo,
            lodiCount = uiState.lodiCount,
            totalExams = uiState.totalEligibleExams
        )
    }
}

@Composable
private fun KPISection(
    currentAvg: Double?,
    deltaAvg: Double,
    examCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Current Average
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = currentAvg?.let { String.format("%.2f", it) } ?: "—",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Exam count pill
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Media su",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = examCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = if (examCount == 1) "esame" else "esami",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            
            // Delta
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (deltaAvg >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = if (deltaAvg >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                    Text(
                        text = String.format("%+.2f", deltaAvg),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (deltaAvg >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
                
                // Status pill
                val statusText = when {
                    deltaAvg > 0.005 -> "In miglioramento"
                    deltaAvg < -0.005 -> "In peggioramento"
                    else -> "Andamento stabile"
                }
                val statusColor = when {
                    deltaAvg > 0 -> Color(0xFF4CAF50)
                    deltaAvg < 0 -> Color(0xFFF44336)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                
                Surface(
                    color = statusColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyChartView() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Nessun voto disponibile",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Quando avrai esami con voto registrato, vedrai qui l'andamento.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ChartSection(
    exams: List<ExamData>,
    avgPoints: List<AvgPoint>,
    selectedIndex: Int?,
    onIndexSelected: (Int?) -> Unit,
    examTitles: List<String>,
    examDates: List<String>,
    examGrades: List<Int>
) {
    @Suppress("UNUSED_PARAMETER")
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Andamento della media",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            // Chart
            ChartView(
                avgPoints = avgPoints.map { it.avg },
                examTitles = examTitles,
                examDates = examDates,
                examGrades = examGrades
            )
        }
    }
}

@Composable
private fun StatisticsGrid(
    examCount: Int,
    bestGrade: Int?,
    worstGrade: Int?,
    lodiCount: Int,
    onLodiInfoClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Sostenuti",
            value = examCount.toString(),
            modifier = Modifier.weight(1f)
        )
        
        StatCard(
            title = "Migliore",
            value = bestGrade?.let { "$it/30" } ?: "—",
            tint = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
        
        StatCard(
            title = "Peggiore",
            value = worstGrade?.let { "$it/30" } ?: "—",
            tint = Color(0xFFF44336),
            modifier = Modifier.weight(1f)
        )
        
        StatCard(
            title = "Lodi",
            value = lodiCount.toString(),
            tint = Color(0xFFFF9800),
            onInfoClick = onLodiInfoClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    tint: Color? = null,
    onInfoClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (tint != null) {
                tint.copy(alpha = 0.16f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (onInfoClick != null) {
                    IconButton(
                        onClick = onInfoClick,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = tint ?: MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RecentExamsSection(
    exams: List<ExamData>,
    sortAscending: Boolean,
    onSortChanged: () -> Unit
) {
    val recentExams = exams.takeLast(10).let { recent ->
        if (sortAscending) recent else recent.reversed()
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ultimi esami",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                TextButton(onClick = onSortChanged) {
                    Text(
                        text = if (sortAscending) "Data ↑" else "Data ↓",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                recentExams.forEachIndexed { index, exam ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = prettifyTitle(exam.title),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2
                            )
                            Text(
                                text = formatDate(exam.date),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "${exam.grade}/30",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                    
                    if (index < recentExams.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FooterLink(onNavigateToExams: () -> Unit) {
    Card(
        onClick = onNavigateToExams,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Per la lista completa vai alla sezione ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Esami",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun KPIInfoDialog(
    onDismiss: () -> Unit,
    deltaAvg: Double
) {
    val status = when {
        deltaAvg > 0.005 -> "Andamento positivo"
        deltaAvg < -0.005 -> "Andamento negativo"
        else -> "Andamento stabile"
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = status,
                color = when {
                    deltaAvg > 0 -> Color(0xFF4CAF50)
                    deltaAvg < 0 -> Color(0xFFF44336)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Cos'è questo numero",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text("Indica di quanto è cambiata la tua media con l'ultimo esame.")
                Text("• Se è positivo (es. +0,13) l'ultimo esame ha alzato la media.")
                Text("• Se è negativo (es. −0,13) l'ultimo esame l'ha abbassata.")
                Text("• Se è ≈ 0 la media è rimasta stabile.")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Chiudi")
            }
        }
    )
}

@Composable
private fun LodiInfoDialog(
    onDismiss: () -> Unit,
    lodiCount: Int,
    totalExams: Int
) {
    val needed = maxOf(1, (totalExams / 2) + 1)
    val missing = maxOf(0, needed - lodiCount)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lodi e menzione accademica") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Hai $lodiCount lodi su $totalExams esami considerati.")
                Text("Per ambire alla menzione accademica servono almeno $needed lodi (metà degli esami + 1).")
                
                LinearProgressIndicator(
                    progress = { minOf(lodiCount.toFloat(), needed.toFloat()) / needed.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFF9800)
                )
                
                if (missing > 0) {
                    Text("Ti mancano $missing lodi", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("Obiettivo raggiunto: puoi ambire alla menzione accademica!", color = Color(0xFF4CAF50))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Chiudi")
            }
        }
    )
}

// Data classes
data class ExamData(
    val id: Int,
    val title: String,
    val grade: Int,
    val date: Date
)

data class AvgPoint(
    val id: Int,
    val avg: Double,
    val date: Date
)

// Helper functions
private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale("it"))
    return formatter.format(date)
}
