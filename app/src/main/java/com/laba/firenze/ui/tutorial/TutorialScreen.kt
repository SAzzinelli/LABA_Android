package com.laba.firenze.ui.tutorial

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.laba.firenze.ui.profile.ProfileViewModel
import com.laba.firenze.ui.tutorial.viewmodel.TutorialViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

data class TutorialPage(
    val id: Int,
    val title: String,
    val subtitle: String,
    val content: String,
    val iconId: String,
    val color: Color,
    val showSkip: Boolean,
    val isOptional: Boolean = false,
    val valueLine: String? = null
)

@Composable
fun TutorialScreen(
    onDismiss: () -> Unit,
    viewModel: TutorialViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel? = null
) {
    val pages = remember {
        listOf(
            TutorialPage(
                id = 0,
                title = "La tua carriera. In ordine.",
                subtitle = "",
                content = "Voti, media, esami, piano di studi e documenti.\nTutto organizzato. Sempre aggiornato.",
                iconId = "graduationcap",
                color = Color(0xFF2196F3),
                showSkip = false,
                valueLine = "Niente più calcoli manuali."
            ),
            TutorialPage(
                id = 1,
                title = "Voti e media",
                subtitle = "",
                content = "Monitora l'andamento del tuo percorso e valuta in anticipo l'impatto dei prossimi esami.",
                iconId = "home",
                color = Color(0xFF9C27B0),
                showSkip = true
            ),
            TutorialPage(
                id = 2,
                title = "Gestisci gli esami",
                subtitle = "",
                content = "Controlla la tua situazione accademica e pianifica gli appelli in base ai requisiti richiesti.",
                iconId = "book",
                color = Color(0xFF4CAF50),
                showSkip = true
            ),
            TutorialPage(
                id = 3,
                title = "Strumenti per te!",
                subtitle = "",
                content = "Accedi rapidamente ai servizi e alle risorse utili per la vita accademica.",
                iconId = "settings",
                color = Color(0xFFFF9800),
                showSkip = true
            ),
            TutorialPage(
                id = 4,
                title = "Seminari e workshop",
                subtitle = "",
                content = "Partecipa alle attività extra e amplia il tuo percorso formativo oltre le lezioni.",
                iconId = "event",
                color = Color(0xFFF44336),
                showSkip = true
            ),
            TutorialPage(
                id = 5,
                title = "Personalizza",
                subtitle = "",
                content = "Adatta l'app alle tue preferenze e tieni sempre a portata di mano ciò che ti serve.",
                iconId = "person",
                color = Color(0xFF673AB7),
                showSkip = true
            ),
            TutorialPage(
                id = 6,
                title = "Traguardi",
                subtitle = "",
                content = "Sblocca achievement completando azioni nell'app e scopri il tuo Year Recap annuale.",
                iconId = "emoji_events",
                color = Color(0xFF2196F3),
                showSkip = true,
                isOptional = true
            ),
            TutorialPage(
                id = 7,
                title = "La tua foto profilo",
                subtitle = "Mostrati in classifica",
                content = "Imposta la tua foto per apparire nel profilo e nelle classifiche.",
                iconId = "photo",
                color = Color(0xFF00BCD4),
                showSkip = true,
                valueLine = "La vedranno tutti."
            ),
            TutorialPage(
                id = 8,
                title = "Prova le novità",
                subtitle = "Funzionalità Beta",
                content = "Attiva le feature in fase di sviluppo per provare per prime le novità.",
                iconId = "sparkles",
                color = Color(0xFF9C27B0),
                showSkip = true
            ),
            TutorialPage(
                id = 9,
                title = "Iniziamo!",
                subtitle = "",
                content = "Ora hai tutto quello che ti serve. Buono studio!",
                iconId = "check_circle",
                color = Color(0xFF4CAF50),
                showSkip = false
            )
        )
    }
    
    var currentPage by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        pages[currentPage].color.copy(alpha = 0.1f),
                        pages[currentPage].color.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with skip button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (pages[currentPage].showSkip) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(
                            text = "Salta",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(3f)
            ) {
                LazyRow(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 0.dp, end = 0.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp)
                ) {
                    itemsIndexed(pages) { index, page ->
                        Box(
                            modifier = Modifier
                                .fillParentMaxWidth(0.999f)
                                .fillMaxHeight()
                        ) {
                            TutorialPageView(
                                page = page,
                                profileViewModel = profileViewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Bottom controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Page indicators (dimensioni maggiori per leggibilità)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    repeat(pages.size) { index ->
                        val animatedSize = remember { Animatable(if (index == currentPage) 1.2f else 1f) }
                        
                        LaunchedEffect(index == currentPage) {
                            animatedSize.animateTo(
                                targetValue = if (index == currentPage) 1.2f else 1f,
                                animationSpec = tween(durationMillis = 300)
                            )
                        }
                        
                        val dotSize = 12.dp * animatedSize.value
                        Box(
                            modifier = Modifier
                                .size(dotSize)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentPage) pages[currentPage].color
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
                
                // Navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentPage > 0) {
                        val scope = rememberCoroutineScope()
                        TextButton(
                            onClick = {
                                if (currentPage > 0) {
                                    currentPage--
                                    scope.launch {
                                        listState.animateScrollToItem(currentPage)
                                    }
                                }
                            }
                        ) {
                            Text(
                                text = "Indietro",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(80.dp))
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    val scope = rememberCoroutineScope()
                    Button(
                        onClick = {
                            if (currentPage < pages.size - 1) {
                                currentPage++
                                scope.launch {
                                    listState.animateScrollToItem(currentPage)
                                }
                            } else {
                                viewModel.markTutorialCompleted()
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = pages[currentPage].color
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .widthIn(min = 120.dp)
                            .height(50.dp)
                    ) {
                        Text(
                            text = if (currentPage == pages.size - 1) "Inizia" else "Avanti",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TutorialPageView(
    page: TutorialPage,
    profileViewModel: ProfileViewModel?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("laba_preferences", Context.MODE_PRIVATE)
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(page.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = getIconEmoji(page.iconId), fontSize = 56.sp)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        if (page.subtitle.isNotEmpty()) {
            Text(
                text = page.subtitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = page.color,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        Text(
            text = page.content,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(bottom = if (page.valueLine != null) 8.dp else 16.dp)
        )
        
        page.valueLine?.let { value ->
            Text(
                text = value,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        if (page.id == 7 && profileViewModel != null) {
            TutorialProfilePhotoSetup(profileViewModel = profileViewModel, color = page.color)
        } else if (page.id == 8) {
            TutorialBetaFeatures(sharedPrefs = sharedPrefs)
        }
        
        if (page.isOptional) {
            Surface(
                color = Color(0xFFFFEB3B).copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "⭐", fontSize = 16.sp)
                    Text(
                        text = "Funzionalità bonus",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TutorialProfilePhotoSetup(
    profileViewModel: ProfileViewModel,
    color: Color
) {
    val context = LocalContext.current
    val profilePhotoURL by profileViewModel.profilePhotoURL.collectAsState()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                profileViewModel.uploadProfilePhoto(stream.readBytes())
            }
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        onClick = { imagePickerLauncher.launch("image/*") }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (profilePhotoURL != null) {
                    Image(
                        painter = rememberAsyncImagePainter(profilePhotoURL),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("📷", fontSize = 24.sp)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Tocca per impostare la foto",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "Apparirà in classifica e profilo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TutorialBetaFeatures(sharedPrefs: android.content.SharedPreferences) {
    var timetableEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("laba.timetable.enabled", false)) }
    var achievementsEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("laba.achievements.enabled", false)) }
    var minigamesEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("laba.minigames.enabled", true)) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TutorialBetaToggle(
            icon = "calendar",
            title = "Orario delle lezioni",
            description = "Visualizza l'orario delle lezioni",
            isOn = timetableEnabled,
            onCheckedChange = {
                timetableEnabled = it
                sharedPrefs.edit().putBoolean("laba.timetable.enabled", it).apply()
            }
        )
        TutorialBetaToggle(
            icon = "trophy",
            title = "Traguardi",
            description = "Traguardi e trofei",
            isOn = achievementsEnabled,
            onCheckedChange = {
                achievementsEnabled = it
                sharedPrefs.edit().putBoolean("laba.achievements.enabled", it).apply()
            }
        )
        TutorialBetaToggle(
            icon = "game",
            title = "Minigiochi",
            description = "LABArola, classifiche",
            isOn = minigamesEnabled,
            onCheckedChange = {
                minigamesEnabled = it
                sharedPrefs.edit().putBoolean("laba.minigames.enabled", it).apply()
            }
        )
    }
}

@Composable
private fun TutorialBetaToggle(
    icon: String,
    title: String,
    description: String,
    isOn: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF9C27B0).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(getIconEmoji(icon), fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = isOn, onCheckedChange = onCheckedChange)
        }
    }
}

private fun getIconEmoji(iconId: String): String {
    return when (iconId) {
        "school", "graduationcap" -> "🎓"
        "home" -> "🏠"
        "book" -> "📚"
        "description" -> "📋"
        "event" -> "📅"
        "folder" -> "📄"
        "local_hospital" -> "🏢"
        "emoji_events" -> "🏆"
        "check_circle" -> "✅"
        "settings" -> "⚙️"
        "person" -> "👤"
        "photo" -> "📷"
        "sparkles" -> "✨"
        "calendar" -> "📅"
        "trophy" -> "🏆"
        "game" -> "🎮"
        else -> "📱"
    }
}

