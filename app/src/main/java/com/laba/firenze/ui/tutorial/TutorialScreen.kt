package com.laba.firenze.ui.tutorial

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.laba.firenze.ui.tutorial.viewmodel.TutorialViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

data class TutorialPage(
    val id: Int,
    val title: String,
    val subtitle: String,
    val content: String,
    val iconId: String, // SF Symbol name
    val color: Color,
    val showSkip: Boolean,
    val isOptional: Boolean = false
)

@Composable
fun TutorialScreen(
    onDismiss: () -> Unit,
    viewModel: TutorialViewModel = hiltViewModel()
) {
    val pages = remember {
        listOf(
            TutorialPage(
                id = 0,
                title = "Benvenuto! 🎓",
                subtitle = "La tua app accademica completa",
                content = "Questa è la tua app LABA, progettata per accompagnarti durante tutto il tuo percorso universitario.",
                iconId = "school",
                color = Color(0xFF2196F3),
                showSkip = false
            ),
            TutorialPage(
                id = 1,
                title = "Home Dashboard 🏠",
                subtitle = "Il tuo centro di controllo",
                content = "Qui trovi tutto quello che ti serve: la sezione 'Per Te' con calcolo media e voto di laurea, i tuoi progressi CFA, e una previsione della tua media futura se non hai ancora sostenuto esami.",
                iconId = "home",
                color = Color(0xFF9C27B0),
                showSkip = true
            ),
            TutorialPage(
                id = 2,
                title = "Sezione Esami 📚",
                subtitle = "Gestisci i tuoi esami",
                content = "Visualizza tutti i tuoi esami, calcola la media con il simulatore, controlla il calendario delle sessioni e prenota i tuoi appelli. Il simulatore ti permette di vedere come cambierebbe la tua media aggiungendo esami.",
                iconId = "book",
                color = Color(0xFF4CAF50),
                showSkip = true
            ),
            TutorialPage(
                id = 3,
                title = "Corsi e Piano Studi 📋",
                subtitle = "Organizza il tuo percorso",
                content = "Consulta il tuo piano di studi, visualizza i corsi disponibili e gestisci il tuo percorso accademico. Tieni traccia dei crediti e delle materie da sostenere.",
                iconId = "description",
                color = Color(0xFFFF9800),
                showSkip = true
            ),
            TutorialPage(
                id = 4,
                title = "Seminari 📅",
                subtitle = "Partecipa e cresci",
                content = "Richiedi partecipazione ai seminari disponibili e visualizza quelli che hai già frequentato. I seminari sono un'ottima opportunità per arricchire il tuo percorso formativo.",
                iconId = "event",
                color = Color(0xFFF44336),
                showSkip = true
            ),
            TutorialPage(
                id = 5,
                title = "Documenti 📄",
                subtitle = "Tutto in un posto",
                content = "Accedi ai documenti ufficiali, consulta i regolamenti, trova informazioni sulla tesi di laurea e gestisci i tuoi dati personali. Qui hai tutto quello che serve per la burocrazia universitaria.",
                iconId = "folder",
                color = Color(0xFF3F51B5),
                showSkip = true
            ),
            TutorialPage(
                id = 6,
                title = "Prenotazioni 🏢",
                subtitle = "Aule e attrezzature",
                content = "Prenota aule studio, laboratori e attrezzature tramite il servizio integrato. Organizza il tuo tempo di studio in modo efficiente utilizzando le risorse disponibili.",
                iconId = "local_hospital",
                color = Color(0xFF009688),
                showSkip = true
            ),
            TutorialPage(
                id = 7,
                title = "Traguardi e Punti 🏆",
                subtitle = "La chicca finale!",
                content = "Scopri il sistema di achievement e punti! Sblocca traguardi completando azioni nell'app, guadagna punti e scopri il tuo 'Year Recap' annuale. È un modo divertente per tracciare i tuoi progressi!",
                iconId = "emoji_events",
                color = Color(0xFFFFEB3B),
                showSkip = true,
                isOptional = true
            ),
            TutorialPage(
                id = 8,
                title = "Pronto? Iniziamo! 🚀",
                subtitle = "La tua avventura inizia qui",
                content = "Ora hai tutto quello che ti serve per iniziare il tuo percorso universitario con LABA. Buono studio!",
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
                // Page indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    repeat(pages.size) { index ->
                        val animatedSize = remember { Animatable(if (index == currentPage) 1.2f else 1f) }
                        
                        LaunchedEffect(index == currentPage) {
                            animatedSize.animateTo(
                                targetValue = if (index == currentPage) 1.2f else 1f,
                                animationSpec = tween(durationMillis = 300)
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(8.dp)
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
                            text = if (currentPage == pages.size - 1) "Inizia!" else "Avanti",
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        // Icon placeholder (in a real app, you'd use a proper icon library)
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(page.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = getIconEmoji(page.iconId),
                fontSize = 56.sp
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Title
        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Subtitle
        Text(
            text = page.subtitle,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = page.color,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Content
        Text(
            text = page.content,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Optional badge
        if (page.isOptional) {
            Surface(
                color = Color(0xFFFFEB3B).copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp),
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

private fun getIconEmoji(iconId: String): String {
    return when (iconId) {
        "school" -> "🎓"
        "home" -> "🏠"
        "book" -> "📚"
        "description" -> "📋"
        "event" -> "📅"
        "folder" -> "📄"
        "local_hospital" -> "🏢"
        "emoji_events" -> "🏆"
        "check_circle" -> "✅"
        else -> "📱"
    }
}

