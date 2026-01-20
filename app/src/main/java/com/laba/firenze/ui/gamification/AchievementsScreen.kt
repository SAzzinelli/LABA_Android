package com.laba.firenze.ui.gamification

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.laba.firenze.data.gamification.AchievementManager
import com.laba.firenze.domain.model.Achievement
import com.laba.firenze.domain.model.AchievementCategory
import com.laba.firenze.domain.model.AchievementRarity
import com.laba.firenze.ui.home.HomeViewModel
import com.laba.firenze.ui.gamification.AchievementIconHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.collectAsState
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    viewModel: AchievementsViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    navController: NavController
) {
    val achievements by viewModel.achievements.collectAsState()
    val totalPoints by viewModel.totalPoints.collectAsState()
    
    // Track section visit
    LaunchedEffect(Unit) {
        viewModel.trackSectionVisit("achievements")
    }
    
    // Update achievements from session data when screen appears
    LaunchedEffect(Unit) {
        val profile = homeViewModel.getUserProfile()
        val exams = homeViewModel.getAllExams()
        val seminars = homeViewModel.getAllSeminars()
        viewModel.updateAchievements(exams, seminars, profile)
    }
    
    var searchText by remember { mutableStateOf("") }
    var expandedCategories by remember { 
        mutableStateOf<Set<AchievementCategory>>(emptySet()) 
    }
    var selectedAchievement by remember { 
        mutableStateOf<Achievement?>(null) 
    }
    
    // Initialize expanded categories
    LaunchedEffect(Unit) {
        expandedCategories = AchievementCategory.values().toSet()
    }
    
    val filteredAchievements = remember(achievements, searchText) {
        if (searchText.isBlank()) {
            achievements
        } else {
            achievements.filter { achievement ->
                achievement.title.contains(searchText, ignoreCase = true) ||
                achievement.description.contains(searchText, ignoreCase = true)
            }
        }
    }
    
    val grouped: Map<AchievementCategory, List<Achievement>> = remember(filteredAchievements) {
        filteredAchievements.groupBy { achievement -> achievement.category }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Traguardi") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Overview Section
                item {
                    AchievementOverviewSection(
                        totalPoints = totalPoints,
                        unlockedCount = achievements.count { it.isUnlocked },
                        totalCount = achievements.size
                    )
                }
                
                // Categories (collapsible)
                AchievementCategory.values().forEach { category ->
                    val categoryAchievements: List<Achievement> = grouped[category] ?: emptyList()
                    if (categoryAchievements.isNotEmpty() || searchText.isEmpty()) {
                        item(key = "category_header_${category.id}") {
                            AchievementCategoryHeader(
                                category = category,
                                isExpanded = expandedCategories.contains(category),
                                onToggle = {
                                    expandedCategories = if (expandedCategories.contains(category)) {
                                        expandedCategories - category
                                    } else {
                                        expandedCategories + category
                                    }
                                }
                            )
                        }
                        
                        if (expandedCategories.contains(category)) {
                            items(
                                items = categoryAchievements,
                                key = { achievement -> achievement.id }
                            ) { achievement ->
                                AchievementRow(
                                    achievement = achievement,
                                    onClick = { selectedAchievement = achievement }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
        
        // Achievement Detail Dialog
        selectedAchievement?.let { achievement ->
            AchievementDetailDialog(
                achievement = achievement,
                onDismiss = { selectedAchievement = null },
                stats = viewModel.stats.value
            )
        }
    }
}

@Composable
fun AchievementOverviewSection(
    totalPoints: Int,
    unlockedCount: Int,
    totalCount: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Introduction text
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "I tuoi traguardi in App",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Traccia i tuoi progressi, sblocca achievement e scopri il tuo percorso in LABA attraverso punti e riconoscimenti.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Points and Achievements (2-column layout)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Punti Totali",
                icon = "⭐",
                value = totalPoints.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Sbloccati",
                icon = "🏆",
                value = "$unlockedCount",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    icon: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = icon,
                fontSize = 32.sp
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun AchievementCategoryHeader(
    category: AchievementCategory,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    TextButton(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.iconName,
                    fontSize = 20.sp
                )
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AchievementRow(
    achievement: Achievement,
    onClick: () -> Unit
) {
    val opacity = if (achievement.isUnlocked) 1f else 0.6f
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        if (achievement.isUnlocked) 
                            Color(achievement.category.colorHex).copy(alpha = 0.2f) 
                        else 
                            Color.Gray.copy(alpha = 0.1f),
                        MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (achievement.isUnlocked) {
                    Icon(
                        imageVector = AchievementIconHelper.getIconForSFSymbol(achievement.icon),
                        contentDescription = null,
                        tint = Color(achievement.category.colorHex),
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = achievement.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = opacity)
                    )
                    if (achievement.isUnlocked) {
                        Text(
                            text = achievement.rarity.emoji,
                            fontSize = 12.sp
                        )
                    }
                }
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = opacity),
                    maxLines = 2
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Points Badge
            if (achievement.isUnlocked) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${achievement.points}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(achievement.category.colorHex)
                    )
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// Achievement Unlocked Toast Banner
@Composable
fun AchievementUnlockedToast(
    achievement: Achievement,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(true) }
    var offset by remember { mutableStateOf(0f) }
    
    // Auto-dismiss after 4 seconds
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(4000)
        isVisible = false
        onDismiss()
    }
    
    if (isVisible) {
        androidx.compose.animation.AnimatedVisibility(
            visible = isVisible,
            enter = androidx.compose.animation.slideInVertically(
                initialOffsetY = { -it },
                animationSpec = androidx.compose.animation.core.spring()
            ) + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = androidx.compose.animation.core.spring()
            ) + androidx.compose.animation.fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon with glow
                    Box(
                        modifier = Modifier.size(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    Color(achievement.category.colorHex).copy(alpha = 0.3f),
                                    MaterialTheme.shapes.small
                                )
                        )
                        Icon(
                            imageVector = AchievementIconHelper.getIconForSFSymbol(achievement.icon),
                            contentDescription = null,
                            tint = Color(achievement.category.colorHex),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    // Content
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = achievement.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "+${achievement.points} punti",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(achievement.category.colorHex)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Achievement Detail Dialog
@Composable
fun AchievementDetailDialog(
    achievement: Achievement,
    onDismiss: () -> Unit,
    stats: com.laba.firenze.domain.model.UserStats
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .fillMaxWidth()
                        .background(
                            if (achievement.isUnlocked)
                                Color(achievement.category.colorHex).copy(alpha = 0.2f)
                            else
                                Color.Gray.copy(alpha = 0.1f),
                            MaterialTheme.shapes.large
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AchievementIconHelper.getIconForSFSymbol(achievement.icon),
                        contentDescription = null,
                        tint = Color(achievement.category.colorHex),
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                // Description
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700)
                        )
                        Text(
                            text = "${achievement.points}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Punti",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = achievement.rarity.emoji,
                            fontSize = 24.sp
                        )
                        Text(
                            text = achievement.rarity.displayName,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Rarità",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Progress (if applicable)
                if (achievement.maxProgress > 1) {
                    Column {
                        Text(
                            text = "Progresso",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        LinearProgressIndicator(
                            progress = { achievement.progressPercentage.toFloat() },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "${achievement.progress}/${achievement.maxProgress}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Unlock status
                if (achievement.isUnlocked) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50)
                        )
                        Text(
                            text = "Sbloccato!",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    achievement.unlockedDate?.let { timestamp: Long ->
                        val date = java.util.Date(timestamp)
                        val dateStr = java.text.SimpleDateFormat(
                            "dd MMMM yyyy, HH:mm",
                            java.util.Locale.getDefault()
                        ).format(date)
                        Text(
                            text = "il $dateStr",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column {
                        Text(
                            text = "Come sbloccare",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = achievement.hint ?: achievement.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (achievement.progress > 0) {
                            Text(
                                text = "Progresso: ${(achievement.progressPercentage * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
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

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val achievementManager: AchievementManager
) : androidx.lifecycle.ViewModel() {
    val achievements: StateFlow<List<Achievement>> = achievementManager.achievements
    val totalPoints: StateFlow<Int> = achievementManager.totalPoints
    val recentlyUnlocked: StateFlow<Achievement?> = achievementManager.recentlyUnlocked
    val stats: StateFlow<com.laba.firenze.domain.model.UserStats> = achievementManager.stats
    
    fun updateAchievements(
        exams: List<com.laba.firenze.domain.model.Esame>,
        seminars: List<com.laba.firenze.domain.model.Seminario>,
        profile: com.laba.firenze.domain.model.StudentProfile?
    ) {
        achievementManager.updateAchievements(exams, seminars, profile)
    }
    
    fun trackSectionVisit(section: String) {
        achievementManager.trackSectionVisit(section)
    }
    
    fun dismissUnlockedToast() {
        achievementManager.dismissUnlockedToast()
    }
}
