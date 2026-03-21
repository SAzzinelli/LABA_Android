package com.laba.firenze.ui.seminars

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.laba.firenze.domain.model.InternshipPayload
import com.laba.firenze.domain.model.Seminario
import com.laba.firenze.ui.common.prettifyTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeminarsScreen(
    navController: NavController,
    viewModel: SeminarsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    
    LaunchedEffect(Unit) {
        viewModel.trackSectionVisit("seminari")
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Attività a scelta") })
        
        // Tab segmentato (come iOS: Seminari | Attività integrative)
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            SegmentedButton(
                selected = uiState.selectedTab == AttivitaSceltaTab.SEMINARI,
                onClick = { viewModel.setSelectedTab(AttivitaSceltaTab.SEMINARI) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                label = { Text("Seminari") }
            )
            SegmentedButton(
                selected = uiState.selectedTab == AttivitaSceltaTab.ATTIVITA_INTEGRATIVE,
                onClick = { viewModel.setSelectedTab(AttivitaSceltaTab.ATTIVITA_INTEGRATIVE) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                label = { Text("Attività integrative") }
            )
        }
        
        when (uiState.selectedTab) {
            AttivitaSceltaTab.SEMINARI -> SeminariTabContent(
                uiState = uiState,
                viewModel = viewModel,
                keyboardController = keyboardController,
                navController = navController
            )
            AttivitaSceltaTab.ATTIVITA_INTEGRATIVE -> AttivitaIntegrativeTabContent(internships = uiState.internships)
        }
    }
}

@Composable
private fun SeminariTabContent(
    uiState: SeminarsUiState,
    viewModel: SeminarsViewModel,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?,
    navController: NavController
) {
    var selectedFilter by remember { mutableStateOf(uiState.filter) }
    LaunchedEffect(uiState.filter) { selectedFilter = uiState.filter }
    
    OutlinedTextField(
        value = uiState.searchQuery,
        onValueChange = viewModel::updateSearchQuery,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Cerca seminari") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        trailingIcon = {
            if (uiState.searchQuery.isNotEmpty()) {
                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SeminariFilter.entries.forEach { filter ->
            val isSelected = selectedFilter == filter
            FilterChip(
                onClick = {
                    selectedFilter = filter
                    viewModel.setFilter(filter)
                },
                label = { Text(filter.label) },
                selected = isSelected,
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(20.dp)),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
    
    if (uiState.seminars.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyFilterState(filter = uiState.filter)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.seminars) { seminar ->
                SeminarCard(
                    seminar = seminar,
                    onClick = { navController.navigate("seminar-detail/${seminar.oid}") }
                )
            }
        }
    }
}

@Composable
private fun AttivitaIntegrativeTabContent(internships: List<InternshipPayload>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Section(
                title = "Esperienze formative",
                content = {
                    if (internships.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            internships.forEach { t ->
                                InternshipCard(internship = t)
                            }
                        }
                    } else {
                        TirociniPlaceholder()
                    }
                }
            )
        }
    }
}

@Composable
private fun InternshipCard(internship: InternshipPayload) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = internship.descrizione ?: "Tirocinio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                internship.annoAccademicoInizio?.let { anno ->
                    Text(
                        text = "Anno $anno${internship.annoAccademicoFine?.let { "–$it" } ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                internship.periodoTirocinioDal?.let { dal ->
                    Text(
                        text = "Dal ${dal.take(10)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                internship.periodoTirocinioAl?.let { al ->
                    Text(
                        text = "Al ${al.take(10)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                internship.cfa?.let { cfa ->
                    Text(
                        text = "$cfa CFA",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (internship.relazioneFinale == true) {
                    Text(
                        text = "Relazione finale inviata",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (internship.moduloOre == true) {
                    Text(
                        text = "Modulo ore inviato",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                if (internship.biennio == true || internship.triennio == true) {
                    Text(
                        text = listOfNotNull(
                            internship.biennio?.takeIf { it }?.let { "Biennio" },
                            internship.triennio?.takeIf { it }?.let { "Triennio" }
                        ).joinToString(" / "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        content()
    }
}

@Composable
private fun TirociniPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Work,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Tirocini",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Quando inizierai un tirocinio, lo troverai qui insieme alle ore registrate e ai CFA ottenuti.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SeminarCard(
    seminar: Seminario,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = prettifyTitle(seminarTitle(seminar.titolo)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                SeminarRowStatusIcon(seminar = seminar)
            }
            
            if (seminar.docente != null) {
                Text(
                    text = "Docente: ${seminar.docente}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (seminar.dataInizio != null) {
                    Text(
                        text = seminar.dataInizio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (seminar.aula != null) {
                    Text(
                        text = seminar.aula,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** Icona stato seminario (ordine come iOS): partecipato → check verde; non convalidato → info arancione; prenotato in attesa → calendar blu; richiedibile → pallino blu pulsante; else → cerchio grigio con —. */
@Composable
private fun SeminarRowStatusIcon(seminar: Seminario) {
    when {
        seminar.partecipato -> Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Frequentato",
            modifier = Modifier.size(20.dp),
            tint = Color(0xFF4CAF50)
        )
        isSeminarioNonConvalidato(seminar) -> Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Non convalidato",
            modifier = Modifier.size(20.dp),
            tint = Color(0xFFFF9800)
        )
        isSeminarioPrenotatoInAttesa(seminar) -> Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = "Prenotato",
            modifier = Modifier.size(20.dp),
            tint = Color(0xFF2196F3)
        )
        seminar.richiedibile -> BlinkingBlueDot()
        else -> SeminarDaSostenereBadge()
    }
}

@Composable
private fun BlinkingBlueDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val opacity by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "opacity"
    )
    Box(
        modifier = Modifier
            .size(18.dp)
            .drawBehind {
                drawCircle(color = Color(0xFF2196F3).copy(alpha = opacity))
            }
    )
}

@Composable
private fun SeminarDaSostenereBadge() {
    Box(
        modifier = Modifier
            .size(18.dp)
            .background(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "—",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptyFilterState(filter: SeminariFilter) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = if (filter == SeminariFilter.FREQUENTATI) Icons.Default.CalendarToday else Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = when (filter) {
                    SeminariFilter.FREQUENTATI -> "Nessuna attività frequentata e convalidata"
                    SeminariFilter.PRENOTABILI -> "Nessuna attività prenotabile al momento"
                    SeminariFilter.TUTTI -> "Nessuna attività disponibile"
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SeminarsViewModel(): SeminarsViewModel {
    return hiltViewModel()
}

