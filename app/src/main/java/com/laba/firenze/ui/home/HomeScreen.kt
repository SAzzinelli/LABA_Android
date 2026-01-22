package com.laba.firenze.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import android.content.Context
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.laba.firenze.domain.model.Esame
import com.laba.firenze.data.repository.SessionRepository
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import android.content.SharedPreferences
import kotlin.math.*
import kotlin.random.Random
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) { 
        viewModel.refreshOnAppear() 
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp, 
            end = 20.dp, 
            top = 60.dp, // Aumentato per evitare che il punch hole copra l'hero
            bottom = 140.dp
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
                // HERO SECTION
                item {
                    val heroInfo = viewModel.getHeroInfo()
                    HeroSection(
                        heroInfo = heroInfo,
                        statusPills = uiState.statusPills,
                        isGraduated = uiState.isGraduated
                    )
                }
        
        // KPI CARDS
        item {
            KpiCardsSection(
                passedExams = uiState.passedExamsCount,
                missingExams = uiState.missingExamsCount,
                cfaEarned = uiState.cfaEarned,
                totalExams = uiState.totalExamsCount
            )
        }
        
        
        // YEAR PROGRESS + CAREER AVERAGE
        item {
            val profile = viewModel.getUserProfile()
            YearProgressAndAverageSection(
                yearProgress = uiState.yearProgress,
                careerAverage = uiState.careerAverage,
                isBiennio = isBiennioLevel(profile),
                onNavigateToGrades = { navController.navigate("grades/trend") }
            )
        }
        
        // ESAMI PRENOTATI (solo se non laureato e ha currentYear, identico a iOS)
        val profile = viewModel.getUserProfile()
        val exams = uiState.bookedExams
        val allExams = viewModel.getAllExams()
        val shouldShowBooked = viewModel.shouldShowBookedExams(profile, allExams)
        
        if (!uiState.isGraduated && profile?.currentYear != null && shouldShowBooked && exams.isNotEmpty()) {
            item {
                BookedExamsSection(
                    exams = exams.take(3), // Mostra solo i primi 3
                    onNavigateToAll = { navController.navigate("esami-prenotati") }
                )
            }
        }
        
        // LESSONS TODAY (sempre presente, come iOS)
        item {
            LessonsTodayCard(lessons = uiState.lessonsToday)
        }
        
        // PER TE SECTION (come iOS)
        item {
            PerTeSection(
                viewModel = viewModel,
                onNavigate = { route -> 
                    when (route) {
                        "Voto di laurea" -> navController.navigate("calcola-voto-laurea")
                        "Simula la tua media" -> navController.navigate("simula-media")
                    }
                }
            )
        }
        
        // SERVIZI SECTION (separata, come iOS) - senza "Gestione servizi"
        item {
            ServiziSection(
                onNavigate = { route ->
                    when (route) {
                        "Service LABA" -> navController.navigate("service-laba")
                        "Prenotazione Aule" -> navController.navigate("prenotazione-aule")
                        "Biblioteca" -> navController.navigate("biblioteca")
                    }
                },
                showGestioneServizi = false
            )
        }
    }
}

// MARK: - Hero Section
@Composable
private fun HeroSection(
    heroInfo: HeroInfo,
    statusPills: List<String>,
    isGraduated: Boolean
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "hero_animation")
    val animationPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hero_phase"
    )
    
    // Load pattern from preferences (dynamic reading)
    val sharedPrefs = context.getSharedPreferences("LABA_PREFS", Context.MODE_PRIVATE)
    var selectedPattern by remember { mutableStateOf("wave") }
    
    LaunchedEffect(Unit) {
        selectedPattern = sharedPrefs.getString("hero_background_pattern", "wave") ?: "wave"
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                )
            )
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            )
    ) {
        // Animated pattern overlay based on selection
        with(density) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            )             {
                // Convert animationPhase (0-1) to time scale (60s duration)
                val time = animationPhase * 60f
                
                when (selectedPattern) {
                    "wave" -> drawWavePatternHero(size, time)
                    "dots" -> drawDotsPatternHero(size, time)
                    "grid" -> drawGridPatternHero(size, time)
                    "particles" -> drawParticlesPatternHero(size, time)
                    "circles" -> drawCirclesPatternHero(size, time)
                    "rays" -> drawRaysPatternHero(size, time)
                    "ripple" -> drawRipplePatternHero(size, time)
                    else -> drawWavePatternHero(size, time)
                }
            }
        }
        
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Ciao, ${heroInfo.displayName}! 👋",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            StatusPillsRow(
                heroInfo = heroInfo,
                pills = statusPills, 
                isGraduated = isGraduated
            )
        }
    }
}

@Composable
private fun StatusPillsRow(heroInfo: HeroInfo, pills: List<String>, isGraduated: Boolean) {
    // pills parameter not used but kept for API consistency
    @Suppress("UNUSED_PARAMETER")
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isGraduated) {
            // Se laureato: mostra solo pillola di status
            Pill("Laureato")
            Text(
                text = "ma perché usi ancora l'app?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.95f)
            )
        } else {
            // Se non laureato: mostra anno di corso e corso compatto
            heroInfo.studyYear?.let { year ->
                Pill("${getItalianOrdinalYear(year)}")
            }
            
            // Corso compatto + A.A. (se disponibile)
            val courseDisplay = if (heroInfo.academicYear.isNotEmpty()) {
                "${heroInfo.courseName} ${heroInfo.academicYear}"
            } else {
                heroInfo.courseName
            }
            
            Text(
                text = courseDisplay,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.95f)
            )
        }
    }
}

private fun getItalianOrdinalYear(year: Int): String {
    return when (year) {
        1 -> "1° anno"
        2 -> "2° anno"
        3 -> "3° anno"
        4 -> "4° anno"
        5 -> "5° anno"
        else -> "$year° anno" // Fuoricorso
    }
}

@Composable
private fun Pill(text: String) {
    val lighterAccent = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
    // val outlineAccent = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) // Non utilizzata
    
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(lighterAccent)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        style = MaterialTheme.typography.labelMedium,
        color = Color.White,
        fontWeight = FontWeight.Medium
    )
}

// MARK: - KPI Cards Section
@Composable
private fun KpiCardsSection(
    passedExams: Int,
    missingExams: Int,
    cfaEarned: Int,
    totalExams: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
        // Esami sostenuti
        KpiCard(
            title = "Esami\nsostenuti",
            value = passedExams.toString(),
            emphasizeGlow = false,
            modifier = Modifier.weight(1f)
        )
        
        // Esami mancanti (con glow quando completato)
        KpiCard(
            title = "Esami\nmancanti",
            value = missingExams.toString(),
            emphasizeGlow = true,
            isComplete = missingExams == 0 && totalExams > 0,
            modifier = Modifier.weight(1f)
        )
        
        // CFA acquisiti
        KpiCard(
            title = "CFA \nacquisiti",
            value = cfaEarned.toString(),
            emphasizeGlow = false,
            modifier = Modifier.weight(1f)
        )
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    emphasizeGlow: Boolean, // Non utilizzato ma mantenuto per coerenza API
    isComplete: Boolean = false,
    modifier: Modifier = Modifier
) {
    @Suppress("UNUSED_PARAMETER")
    
    Box(
        modifier = modifier.height(96.dp)
    ) {
        // Main card con depth
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isComplete) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isComplete) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Hai sostenuto tutti gli esami!",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

/**
 * Determina se lo studente è del biennio basandosi sul pianoStudi
 */
private fun isBiennioLevel(profile: com.laba.firenze.domain.model.StudentProfile?): Boolean {
    val pianoStudi = profile?.pianoStudi?.lowercase() ?: ""
    return pianoStudi.contains("biennio") || 
           pianoStudi.contains("ii livello") || 
           pianoStudi.contains("2° livello") || 
           pianoStudi.contains("secondo livello")
}

// MARK: - Year Progress and Average Section
@Composable
private fun YearProgressAndAverageSection(
    yearProgress: YearProgress?,
    careerAverage: Double?,
    isBiennio: Boolean,
    onNavigateToGrades: () -> Unit
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
            Text(
                text = "Come stai andando?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Year progress (1st, 2nd, 3rd year - solo 1° e 2° per biennio)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val maxYear = if (isBiennio) 2 else 3
                for (year in 1..maxYear) {
                    YearProgressCard(
                        year = year,
                        progress = yearProgress?.getProgressForYear(year) ?: 0.0,
                        missingCount = yearProgress?.getMissingForYear(year) ?: 0,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            
            // Career Average (tappable)
            CareerAverageCard(
                average = careerAverage,
                onClick = onNavigateToGrades
            )
        }
    }
}

@Composable
private fun YearProgressCard(
    year: Int,
    progress: Double,
    missingCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Esami ${getItalianOrdinalYear(year)}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        // Thin progress bar
        ThinProgressBar(progress = progress)
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (progress >= 1.0) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Completati",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "$missingCount mancanti",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ThinProgressBar(progress: Double) {
    val clampedProgress = max(0.0, min(1.0, progress))
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(clampedProgress.toFloat())
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun CareerAverageCard(
    average: Double?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "La tua media",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = average?.let { String.format("%.2f", it) } ?: "—",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Progress bar for average
        val averageProgress = average?.let { max(0.0, min(1.0, it / 30.0)) } ?: 0.0
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(averageProgress.toFloat())
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                            )
                        )
                    )
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Clicca per scoprirla nel dettaglio",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// MARK: - Lessons Today Card (sempre presente, come iOS)
@Composable
private fun LessonsTodayCard(lessons: List<LessonUi>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
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
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Le tue lezioni",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (lessons.isEmpty()) {
                // Nessuna lezione - mostra messaggio come iOS
                Text(
                    text = "Oggi",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Nessuna lezione oggi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
                Text(
                    text = "Nessuna lezione in calendario nei prossimi giorni.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Ci sono lezioni - mostra elenco
                Text(
                    text = "Oggi",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                lessons.forEachIndexed { index, lesson ->
                    LessonRow(lesson = lesson)
                    if (index < lessons.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 2.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    }
                }
                
                // Se ci sono solo poche lezioni, mostra anche domani (come iOS)
                if (lessons.size <= 2) {
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                    Text(
                        text = "Domani",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Nessuna lezione in calendario.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonRow(lesson: LessonUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Date badge - GIORNO non orario!
        DayBadge(date = lesson.date)
        
        // Time column
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = lesson.time,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
        }
        
        // Course info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = lesson.title,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                lesson.room?.let { room ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = room,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                
                lesson.teacher?.let { teacher ->
                    if (lesson.room != null) {
                        Text("–", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = teacher,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
        
        if (lesson.isNow) {
            DayBadge(date = "", text = "ORA", isNow = true)
        }
    }
}

@Composable
private fun DayBadge(date: String, text: String = "", isNow: Boolean = false) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                if (isNow) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isNow) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Default
            )
        } else {
            // Mostra il GIORNO della settimana
            val dayOfWeek = try {
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dateObj = formatter.parse(date)
                val dayFormatter = SimpleDateFormat("EEE", Locale("it", "IT"))
                dayFormatter.format(dateObj ?: Date()).uppercase()
            } catch (e: Exception) {
                "OGG"
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(-2.dp)
            ) {
                Text(
                    text = dayOfWeek,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 9.sp
                )
                Text(
                    text = try {
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val dateObj = formatter.parse(date)
                        val dayFormatter = SimpleDateFormat("dd", Locale.getDefault())
                        dayFormatter.format(dateObj ?: Date())
                    } catch (e: Exception) {
                        "15"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// MARK: - Per Te Section (come iOS - solo Voto di laurea e Simula media)
@Composable
private fun PerTeSection(
    viewModel: HomeViewModel,
    onNavigate: (String) -> Unit
) {
    val profile = viewModel.getUserProfile()
    val allExams = viewModel.getAllExams()
    val isGraduated = profile?.status?.lowercase()?.contains("laureat") == true
    val hasCompletedAllExams = viewModel.hasCompletedAllGraduationExams()
    val currentYear = profile?.currentYear?.toIntOrNull()
    val canAccessGraduationGrade = isGraduated || hasCompletedAllExams || currentYear == 3
    
    var showGraduationGradeAlert by remember { mutableStateOf(false) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text("Per te", style = MaterialTheme.typography.titleMedium)
        }
        
        // Voto di laurea (con accesso condizionale)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { 
                    if (canAccessGraduationGrade) {
                        onNavigate("Voto di laurea")
                    } else {
                        showGraduationGradeAlert = true
                    }
                },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = if (canAccessGraduationGrade) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Voto di laurea",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (canAccessGraduationGrade) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Simula la tua media
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigate("Simula la tua media") },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Simula la tua media",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    // Alert per sezione non disponibile
    if (showGraduationGradeAlert) {
        AlertDialog(
            onDismissRequest = { showGraduationGradeAlert = false },
            title = { Text("Sezione non disponibile") },
            text = {
                Text(
                    "Il calcolo del voto di laurea sarà disponibile appena avrai completato tutti gli esami del tuo piano di studi. Continua con gli ultimi passi e torna qui per prepararti alla discussione!"
                )
            },
            confirmButton = {
                TextButton(onClick = { showGraduationGradeAlert = false }) {
                    Text("OK")
                }
            }
        )
    }
}

// MARK: - Servizi Section (separata, come iOS)
@Composable
private fun ServiziSection(
    onNavigate: (String) -> Unit,
    showGestioneServizi: Boolean = true
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Default.TabletMac,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text("Servizi", style = MaterialTheme.typography.titleMedium)
        }
        
        // Service LABA
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigate("Service LABA") },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Service LABA",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Prenotazione Aule
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigate("Prenotazione Aule") },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MeetingRoom,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Prenotazione Aule",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Biblioteca
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigate("Biblioteca") },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Biblioteca",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Servizi (gestione funzionalità) - solo se showGestioneServizi è true
        if (showGestioneServizi) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("Servizi") },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Gestione servizi",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// MARK: - Helper Functions

// MARK: - Hero Wave Pattern Drawing (animated like iOS)
private fun DrawScope.drawWavePatternHero(size: Size, time: Float) {
    val color = Color.White.copy(alpha = 0.8f)
    val waveCount = 8
    val spacing = size.height / waveCount
    val t = time * 0.6f
    
    for (i in 0 until waveCount) {
        val yPos = i * spacing
        val wavePhase = sin((i * 0.5 + t * 1.2).toDouble()) * 0.3
        val amplitude: Float = 8f + (i % 3) * 2
        
        val wavePath = Path()
        
        for (x in 0..size.width.toInt() step 2) {
            val normalizedX = x.toFloat() / size.width
            val wave = sin((normalizedX * 6.0 + i * 0.7 + t * 1.5).toDouble()).toFloat() * amplitude
            val y = yPos + wave + (wavePhase.toFloat() * 3)
            
            if (x == 0) {
                wavePath.moveTo(x.toFloat(), y)
            } else {
                wavePath.lineTo(x.toFloat(), y)
            }
        }
        
        val opacity = 0.4f + (i % 2) * 0.2f
        drawPath(
            path = wavePath,
            color = color.copy(alpha = opacity),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
        )
    }
}

// MARK: - Additional Hero Patterns

private fun DrawScope.drawDotsPatternHero(size: Size, time: Float) {
    val color = Color.White.copy(alpha = 0.8f)
    val step = 28f
    val radius = 3f
    val t = time * 0.6f
    
    var y = -step
    while (y <= size.height + step) {
        var x = -step
        while (x <= size.width + step) {
            val seed = (x * 13 + y * 7).toInt()
            val dx = sin((x + y) / 140f + t * (1.2f + 0.17f * sin(seed.toFloat()))) * 10f * (0.8f + 0.3f * cos(seed.toFloat()))
            val dy = cos((x - y) / 120f + t * (1.3f + 0.23f * cos((seed + 99).toFloat()))) * 10f * (0.8f + 0.3f * sin((seed + 42).toFloat()))
            
            drawCircle(
                color = color,
                radius = radius,
                center = Offset(x + dx, y + dy)
            )
            x += step
        }
        y += step
    }
}

private fun DrawScope.drawGridPatternHero(size: Size, time: Float) {
    val color = Color.White.copy(alpha = 0.8f)
    val t = time * 0.4f
    val spacing = 40f
    
    // Vertical lines
    for (x in 0..size.width.toInt() step spacing.toInt()) {
        val offset = sin(t * 0.8f + x * 0.01f) * 20f
        drawLine(
            color = color.copy(alpha = 0.2f),
            start = Offset(x.toFloat(), -offset),
            end = Offset(x.toFloat(), size.height + offset),
            strokeWidth = 1.5f
        )
    }
    
    // Horizontal lines
    for (y in 0..size.height.toInt() step spacing.toInt()) {
        val offset = cos(t * 0.6f + y * 0.01f) * 20f
        drawLine(
            color = color.copy(alpha = 0.2f),
            start = Offset(-offset, y.toFloat()),
            end = Offset(size.width + offset, y.toFloat()),
            strokeWidth = 1.5f
        )
    }
}

private fun DrawScope.drawParticlesPatternHero(size: Size, time: Float) {
    val color = Color.White.copy(alpha = 0.8f)
    val t = time * 0.5f
    val particleCount = 50
    
    for (i in 0 until particleCount) {
        val seed = i * 123.456f
        val sx = (seed * 7.891) % size.width
        val sy = (seed * 4.567) % size.height
        val x = ((sx + sin(t * 0.8f + seed * 0.1f) * 40f) % size.width).toFloat()
        val y = ((sy + cos(t * 0.6f + seed * 0.15f) * 40f) % size.height).toFloat()
        val size = 3f + (i % 3) * 2f
        val opacity = 0.4f + (i % 3) * 0.2f
        
        drawCircle(
            color = color.copy(alpha = opacity),
            radius = size,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawCirclesPatternHero(size: Size, time: Float) {
    val color = Color.White.copy(alpha = 0.8f)
    val t = time * 0.4f
    val circleCount = 20
    
    for (i in 0 until circleCount) {
        val seed = i * 45.678f
        val cx = ((seed * 3.141) % size.width).toFloat()
        val cy = ((seed * 2.718) % size.height).toFloat()
        val pulse = sin(t + i * 0.5f)
        val radius = 15f + pulse * 25f
        
        drawCircle(
            color = color.copy(alpha = 0.7f),
            radius = radius,
            center = Offset(cx, cy),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
        )
    }
}

private fun DrawScope.drawRaysPatternHero(size: Size, time: Float) {
    val color = Color.White.copy(alpha = 0.8f)
    val t = time * 0.3f
    
    val rayCount = 12
    for (i in 0 until rayCount) {
        val angle = (i * 2f * PI.toFloat() / rayCount) + t
        val length = size.height.coerceAtMost(size.width)
        
        val startX = size.width / 2f
        val startY = size.height / 2f
        val endX = startX + cos(angle) * length
        val endY = startY + sin(angle) * length
        
        drawLine(
            color = color.copy(alpha = 0.7f),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 1.5f
        )
    }
}

private fun DrawScope.drawRipplePatternHero(size: Size, time: Float) {
    val color = Color.White.copy(alpha = 0.8f)
    val t = time * 0.5f
    
    val rippleCount = 5
    for (i in 0 until rippleCount) {
        val phase = (t + i * 0.5f) % (2 * PI.toFloat())
        val radius = phase * 30f
        
        val centers = listOf(
            Offset(size.width * 0.3f, size.height * 0.3f),
            Offset(size.width * 0.7f, size.height * 0.7f),
            Offset(size.width * 0.5f, size.height * 0.2f)
        )
        
        centers.forEach { center ->
            if (phase < 2 * PI.toFloat()) {
                drawCircle(
                    color = color.copy(alpha = 0.7f),
                    radius = radius,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                )
            }
        }
    }
}

// MARK: - Booked Exams Section (identica a iOS)
@Composable
private fun BookedExamsSection(
    exams: List<Esame>,
    onNavigateToAll: () -> Unit
) {
    val pastelRed = Color(0xFFFF3B30) // Rosso vivace identico a iOS
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header rosso pastello
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = pastelRed
                )
                Text(
                    text = "Prossimi esami",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = pastelRed
                )
            }
            
            // Lista esami (massimo 3)
            exams.forEachIndexed { index, exam ->
                BookedExamRow(exam = exam, number = index + 1)
                if (index < exams.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                }
            }
            
            // Pulsante per vedere tutti gli esami prenotati
            TextButton(
                onClick = onNavigateToAll,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = pastelRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vedi esami prenotati",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = pastelRed
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = pastelRed.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BookedExamRow(
    exam: Esame,
    number: Int
) {
    val pastelRed = Color(0xFFFF3B30) // Rosso vivace identico a iOS
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Numero in cerchio rosso pastello
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(pastelRed),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
        
        // Info esame
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = prettifyTitle(exam.corso),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            exam.docente?.let { docente ->
                if (docente.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = surnamesOnly(docente),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Helper: formatta il titolo in proper case (identico a iOS prettifyTitle)
 */
private fun prettifyTitle(title: String): String {
    return title.replace("_", " ")
        .split(" ")
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase() else it.toString() 
            }
        }
}

/**
 * Helper: estrae solo i cognomi dal nome del docente (identico a iOS surnamesOnly)
 */
private fun surnamesOnly(docente: String): String {
    val parts = docente.split("/").firstOrNull()?.trim() ?: docente
    val components = parts.split(" ").filter { it.isNotEmpty() }
    return when {
        components.size >= 2 -> components.drop(1).joinToString(" ") // Tutti tranne il primo
        else -> parts
    }
}

// MARK: - Animated Glow Background
@Composable
private fun AnimatedGlowBackground(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    phase: Float
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        if (isActive) {
            drawAnimatedGradient(size, phase, primaryColor)
        }
    }
}

private fun DrawScope.drawAnimatedGradient(size: Size, phase: Float, primaryColor: Color) {
    val spotsCount = 6
    val seeds = listOf(123f, 456f, 789f, 321f, 654f, 987f)
    
    for (i in 0 until spotsCount) {
        val sx = seeds[i % seeds.size]
        val sy = seeds[(i + 1) % seeds.size]
        val px = 0.5f + 0.42f * sin((phase * 0.18f) + sx / 37f + i)
        val py = 0.5f + 0.42f * cos((phase * 0.15f) + sy / 41f + i)
        
        val baseRadius = min(size.width, size.height) * (0.20f + 0.10f * (i % 3))
        val radius = baseRadius * (0.92f + 0.55f)
        
        val center = Offset(px * size.width, py * size.height)
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.85f),
                    primaryColor.copy(alpha = 0.35f),
                    primaryColor.copy(alpha = 0f)
                ),
                center = center,
                radius = radius
            ),
            center = center,
            radius = radius
        )
    }
}

// MARK: - Data Classes
data class LessonUi(
    val title: String,
    val time: String,
    val room: String?,
    val teacher: String?,
    val date: String, // AGGIUNTO: data per il giorno
    val isNow: Boolean = false
)

data class YearProgress(
    val year1: Double = 0.0,
    val year1Total: Int = 0,
    val year1Missing: Int = 0,
    val year2: Double = 0.0,
    val year2Total: Int = 0,
    val year2Missing: Int = 0,
    val year3: Double = 0.0,
    val year3Total: Int = 0,
    val year3Missing: Int = 0
) {
    fun getProgressForYear(year: Int): Double {
        return when (year) {
            1 -> year1
            2 -> year2
            3 -> year3
            else -> 0.0
        }
    }
    
    fun getTotalForYear(year: Int): Int {
        return when (year) {
            1 -> year1Total
            2 -> year2Total
            3 -> year3Total
            else -> 0
        }
    }
    
    fun getMissingForYear(year: Int): Int {
        return when (year) {
            1 -> year1Missing
            2 -> year2Missing
            3 -> year3Missing
            else -> 0
        }
    }
}
