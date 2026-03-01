package com.laba.firenze.ui.gamification

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
private enum class YearRecapPageType {
    INTRO, EXAMS_STATS, AVERAGE, BEST_GRADE, WORST_GRADE, LODI, SESSIONS, CFA, ACHIEVEMENTS, OUTRO
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun YearRecapScreen(
    navController: NavController,
    viewModel: YearRecapViewModel = hiltViewModel()
) {
    val recapData by viewModel.recapData.collectAsStateWithLifecycle()

    val data = recapData ?: return

    val pages = remember(data.lodeCount) {
        val list = mutableListOf<YearRecapPageType>()
        list.add(YearRecapPageType.INTRO)
        list.add(YearRecapPageType.EXAMS_STATS)
        list.add(YearRecapPageType.AVERAGE)
        list.add(YearRecapPageType.BEST_GRADE)
        list.add(YearRecapPageType.WORST_GRADE)
        if (data.lodeCount > 0) list.add(YearRecapPageType.LODI)
        list.add(YearRecapPageType.SESSIONS)
        list.add(YearRecapPageType.CFA)
        list.add(YearRecapPageType.ACHIEVEMENTS)
        list.add(YearRecapPageType.OUTRO)
        list
    }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    var animate by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animate = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = true
        ) { index ->
            when (pages.getOrElse(index) { YearRecapPageType.INTRO }) {
                YearRecapPageType.INTRO -> IntroPage(year = data.year, animate = animate)
                YearRecapPageType.EXAMS_STATS -> ExamsStatsPage(data = data, animate = animate)
                YearRecapPageType.AVERAGE -> AverageGradePage(data = data, animate = animate)
                YearRecapPageType.BEST_GRADE -> BestGradePage(data = data, animate = animate)
                YearRecapPageType.WORST_GRADE -> WorstGradePage(data = data, animate = animate)
                YearRecapPageType.LODI -> LodiPage(data = data, animate = animate)
                YearRecapPageType.SESSIONS -> SessionsPage(data = data, animate = animate)
                YearRecapPageType.CFA -> CFAProgressPage(data = data, animate = animate)
                YearRecapPageType.ACHIEVEMENTS -> AchievementsSummaryPage(data = data, animate = animate)
                YearRecapPageType.OUTRO -> OutroPage(data = data, onDismiss = { navController.popBackStack() }, animate = animate)
            }
        }

        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(20.dp)
                .background(Color.White.copy(alpha = 0.3f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
        }

        if (pages.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (isSelected) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun IntroPage(year: Int, animate: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF2196F3),
                        Color(0xFF9C27B0),
                        Color(0xFFE91E63)
                    ).map { it.copy(alpha = 0.25f) }
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = year.toString(),
                fontSize = 100.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF2196F3)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Il tuo anno in LABA",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Scopri i traguardi che hai raggiunto",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExamsStatsPage(data: YearRecapData, animate: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF2196F3),
                        Color(0xFF00BCD4),
                        Color(0xFF9C27B0)
                    ).map { it.copy(alpha = 0.2f) }
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🎓", fontSize = 72.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = data.totalExams.toString(),
                fontSize = 80.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF2196F3)
            )
            Text("Esami superati", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = data.totalCFA.toString(),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        Text("CFA", style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (data.lodeCount > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = data.lodeCount.toString(),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9800)
                            )
                            Text(if (data.lodeCount == 1) "Lode" else "Lodi", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AverageGradePage(data: YearRecapData, animate: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF4CAF50), Color(0xFF00BCD4)).map { it.copy(alpha = 0.2f) }
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("📊", fontSize = 72.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "%.1f".format(data.averageGrade),
                fontSize = 72.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF4CAF50)
            )
            Text("Media voti", style = MaterialTheme.typography.titleMedium)
            when {
                data.averageGrade >= 28 -> Text("Eccellente! 🌟", style = MaterialTheme.typography.titleMedium, color = Color(0xFFFF9800))
                data.averageGrade >= 25 -> Text("Ottimo lavoro! 👏", style = MaterialTheme.typography.titleMedium, color = Color(0xFF4CAF50))
                data.averageGrade >= 22 -> Text("Buon risultato! 💪", style = MaterialTheme.typography.titleMedium, color = Color(0xFF2196F3))
            }
        }
    }
}

@Composable
private fun BestGradePage(data: YearRecapData, animate: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF4CAF50), Color(0xFFFFEB3B)).map { it.copy(alpha = 0.2f) }
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🏆", fontSize = 80.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Il tuo voto migliore", style = MaterialTheme.typography.titleMedium)
            Text(
                text = data.bestGrade,
                fontSize = 72.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF4CAF50)
            )
            Text("Eccellente risultato! 🎉", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun WorstGradePage(data: YearRecapData, animate: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFFF9800), Color(0xFFE91E63)).map { it.copy(alpha = 0.2f) }
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("📈", fontSize = 72.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Il voto più basso", style = MaterialTheme.typography.titleMedium)
            Text(
                text = data.worstGrade,
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFF5722)
            )
            Text(
                text = "Anche questo ha contribuito alla tua crescita ✨",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LodiPage(data: YearRecapData, animate: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFFFEB3B), Color(0xFFFF9800)).map { it.copy(alpha = 0.25f) }
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("⭐", fontSize = 80.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = data.lodeCount.toString(),
                fontSize = 80.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFF9800)
            )
            Text(if (data.lodeCount == 1) "Lode" else "Lodi", style = MaterialTheme.typography.titleMedium)
            Text("Eccellenza! 🌟", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun SessionsPage(data: YearRecapData, animate: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF00BCD4), Color(0xFF2196F3)).map { it.copy(alpha = 0.2f) }
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val session = data.mostProductiveSession
            val count = session?.let { data.examsPerSession[it] }
            val month = data.bestMonth
            when {
                session != null && count != null -> {
                    Text("📅", fontSize = 72.sp)
                    Text("Sessione più produttiva", style = MaterialTheme.typography.titleMedium)
                    Text(session, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                    Text("$count esami", fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color(0xFF2196F3))
                }
                month != null -> {
                    Text("📅", fontSize = 72.sp)
                    Text("Mese migliore", style = MaterialTheme.typography.titleMedium)
                    Text(month, fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color(0xFF2196F3))
                }
                else -> Text("Continua così!", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}

@Composable
private fun CFAProgressPage(data: YearRecapData, animate: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF2196F3), Color(0xFF00BCD4)).map { it.copy(alpha = 0.2f) }
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("📊", fontSize = 72.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = data.totalCFA.toString(),
                fontSize = 80.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF2196F3)
            )
            Text("Crediti Formativi", style = MaterialTheme.typography.titleMedium)
            Text("Continua così! 🚀", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun AchievementsSummaryPage(data: YearRecapData, animate: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFFFEB3B), Color(0xFFFF9800)).map { it.copy(alpha = 0.2f) }
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🏆", fontSize = 72.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = data.totalAchievements.toString(),
                fontSize = 72.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFF9800)
            )
            Text("Achievement sbloccati", style = MaterialTheme.typography.titleMedium)
            Text("${data.totalPoints} CFApp totali", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun OutroPage(data: YearRecapData, onDismiss: () -> Unit, animate: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF2196F3)).map { it.copy(alpha = 0.25f) }
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🎉", fontSize = 80.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Ottimo lavoro!", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (data.totalExams > 0)
                    "Hai superato ${data.totalExams} esami con una media di %.1f".format(data.averageGrade)
                else
                    "Hai iniziato il tuo percorso in LABA",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Text("Continua così!", style = MaterialTheme.typography.titleLarge, color = Color(0xFF2196F3))
            Spacer(modifier = Modifier.height(48.dp))
            Text("❤️", fontSize = 32.sp)
            Text("LABA Firenze", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Esci")
            }
        }
    }
}
