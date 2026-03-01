package com.laba.firenze.ui.regolamenti

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.TopAppBar
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegolamentiScreen(
    navController: NavController,
    viewModel: RegolamentiViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Funzione per aprire PDF nel browser
    fun openPDF(url: String, regolamentoId: String? = null) {
        // Track achievement when reading regulation
        regolamentoId?.let { id ->
            viewModel.achievementManager.trackRegolamentoRead(id)
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Se non riesce ad aprire, prova con il browser di default
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.setPackage("com.android.chrome")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e2: Exception) {
                // Ultimo tentativo con qualsiasi browser
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }
    }

    // Sorgente: link pubblici dal sito LABA (allineati a iOS, dic 2025)
    val allDocuments = remember {
        listOf(
            RegolamentoDocument(
                title = "Statuto dell'istituzione",
                url = "https://laba.biz/wp-content/uploads/2025/12/Statuto-dellIstituzione.pdf",
                category = RegolamentoCategory.ISTITUZIONE,
                icon = Icons.Default.Description,
                note = "Finalità e principi dell'accademia"
            ),
            RegolamentoDocument(
                title = "Norme generali",
                url = "https://laba.biz/wp-content/uploads/2025/12/Regolamento-Generale.pdf",
                category = RegolamentoCategory.DIDATTICA,
                icon = Icons.AutoMirrored.Filled.List,
                note = "Approfondimenti e integrazioni al Regolamento Didattico"
            ),
            RegolamentoDocument(
                title = "Regolamento didattico",
                url = "https://laba.biz/wp-content/uploads/2025/12/Regolamento-Didattico.pdf",
                category = RegolamentoCategory.DIDATTICA,
                icon = Icons.Default.Book,
                note = "Approvato dal MUR"
            ),
            RegolamentoDocument(
                title = "Regolamento tesi",
                url = "https://laba.biz/wp-content/uploads/2025/12/Regolamento-Tesi.pdf",
                category = RegolamentoCategory.DIDATTICA,
                icon = Icons.Default.School,
                note = "Scadenze, consegne e voto finale"
            ),
            RegolamentoDocument(
                title = "Supporto per allievi",
                url = "https://laba.biz/wp-content/uploads/2025/12/Supporto-alla-Didattica.pdf",
                category = RegolamentoCategory.INCLUSIONE,
                icon = Icons.Default.Person,
                note = "Tutor dedicati e modalità d'esame DSA/BES/ADHD"
            ),
            RegolamentoDocument(
                title = "Consulta degli Studenti",
                url = "https://laba.biz/wp-content/uploads/2025/12/Consulta-degli-studenti-1.pdf",
                category = RegolamentoCategory.STUDENTI,
                icon = Icons.Default.Group,
                note = "Elezioni, funzioni e durata"
            ),
            RegolamentoDocument(
                title = "Qualità LABA",
                url = "https://laba.biz/wp-content/uploads/2025/12/Qualita-LABA.pdf",
                category = RegolamentoCategory.QUALITA,
                icon = Icons.Default.Verified,
                note = "Comitato qualità e processi AQ"
            ),
            RegolamentoDocument(
                title = "Parità di genere",
                url = "https://laba.biz/wp-content/uploads/2025/12/Parita-di-Genere.pdf",
                category = RegolamentoCategory.QUALITA,
                icon = Icons.Default.Equalizer,
                note = "Obiettivi e misure adottate"
            )
        )
    }

    // Filtro testuale
    val filteredDocuments = remember(searchQuery, allDocuments) {
        val query = searchQuery.trim()
        if (query.isEmpty()) {
            allDocuments
        } else {
            allDocuments.filter { document ->
                val haystack = "${document.title} ${document.note ?: ""} ${document.category.displayName}"
                    .lowercase()
                haystack.contains(query.lowercase())
            }
        }
    }

    // Raggruppa per categoria
    val groupedDocuments = remember(filteredDocuments) {
        val order = listOf(
            RegolamentoCategory.ISTITUZIONE,
            RegolamentoCategory.DIDATTICA,
            RegolamentoCategory.STUDENTI,
            RegolamentoCategory.QUALITA,
            RegolamentoCategory.INCLUSIONE
        )
        
        order.mapNotNull { category ->
            val items = filteredDocuments.filter { it.category == category }
            if (items.isNotEmpty()) {
                RegolamentoSection(
                    category = category,
                    items = items.sortedBy { it.title }
                )
            } else null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Regolamenti") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
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
            // Header Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Regolamenti e Documenti Ufficiali",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Raccolta dei testi ufficiali LABA: statuto, regolamenti didattici e generali, tesi, consulta studenti, qualità e inclusione.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp, vertical = 8.dp),
                    placeholder = { Text("Cerca nei regolamenti") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    }
                )
            }

            // Documents by Category
            items(groupedDocuments) { section ->
                RegolamentoSectionView(
                    section = section,
                    onDocumentClick = { url ->
                        // Extract regolamento ID from URL (use URL as ID for now)
                        val regolamentoId = url.substringAfterLast("/").substringBefore(".")
                        openPDF(url, regolamentoId)
                    }
                )
            }
        }
    }
}

@Composable
private fun RegolamentoSectionView(
    section: RegolamentoSection,
    onDocumentClick: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section Title
        Text(
            text = section.category.displayName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Documents in Section
        section.items.forEach { document ->
            RegolamentoDocumentCard(document = document, onDocumentClick = onDocumentClick)
        }
    }
}

@Composable
private fun RegolamentoDocumentCard(
    document: RegolamentoDocument,
    onDocumentClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                onDocumentClick(document.url)
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = document.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (document.note != null) {
                    Text(
                        text = document.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Data Classes
enum class RegolamentoCategory(val displayName: String) {
    ISTITUZIONE("Istituzione"),
    DIDATTICA("Didattica"),
    STUDENTI("Studenti"),
    QUALITA("Qualità"),
    INCLUSIONE("Inclusione")
}

data class RegolamentoDocument(
    val title: String,
    val url: String,
    val category: RegolamentoCategory,
    val icon: ImageVector,
    val note: String?
)

data class RegolamentoSection(
    val category: RegolamentoCategory,
    val items: List<RegolamentoDocument>
)
