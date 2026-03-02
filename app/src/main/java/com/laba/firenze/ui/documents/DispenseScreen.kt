package com.laba.firenze.ui.documents

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.laba.firenze.domain.model.LogosDoc
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DispenseScreen(
    navController: NavController,
    viewModel: DocumentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    var searchQuery by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        try {
            println("DispenseScreen: LaunchedEffect started")
            viewModel.loadDocuments()
            println("DispenseScreen: LaunchedEffect completed")
        } catch (e: Exception) {
            println("DispenseScreen: Error in LaunchedEffect: ${e.message}")
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Dispense e materiali") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Cerca dispense") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { keyboardController?.hide() }
                )
            )
            
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Caricamento...")
                        }
                    }
                }
                
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Impossibile caricare",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = uiState.error!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                else -> {
                    val dispensaDocs = uiState.documents.filter { isDispensaDoc(it) }
                    val filteredDocs = if (searchQuery.isBlank()) {
                        dispensaDocs
                    } else {
                        val queryLower = searchQuery.lowercase().trim()
                        dispensaDocs.filter { doc ->
                            prettifyTitle(doc.titolo).lowercase().contains(queryLower) ||
                            (doc.descrizione?.lowercase()?.contains(queryLower) == true) ||
                            (doc.tipo?.lowercase()?.contains(queryLower) == true)
                        }
                    }
                    
                    if (filteredDocs.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Nessuna dispensa",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Quando disponibili, appariranno qui.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 140.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Header info
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Description,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Dispense e materiali",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Materiali caricati e condivisi dai docenti come PDF, slide o riferimenti didattici.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    
                                    // Warning about external platforms
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Warning,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                            Text(
                                                text = "Alcuni corsi usano piattaforme esterne, chiedi sempre conferma al docente (es. Google Drive, Classroom)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Documents list - flat list like Programmi
                            val sortedDocs = filteredDocs.sortedBy { it.descrizione ?: "" }
                            items(sortedDocs) { doc ->
                                DispensaListItem(
                                    document = doc,
                                    onClick = {
                                        try {
                                            viewModel.trackDispenseOpen(doc.oid)
                                            val title = if (!doc.descrizione.isNullOrBlank()) {
                                                doc.descrizione
                                            } else {
                                                doc.tipo ?: "Dispensa"
                                            }
                                            val encodedTitle = Uri.encode(prettifyTitle(title))
                                            val directUrlParam = doc.url?.takeIf { it.startsWith("http") }?.let { Uri.encode(it) } ?: "_"
                                            navController.navigate("document_viewer/${doc.oid}/$encodedTitle/$directUrlParam")
                                        } catch (e: Exception) {
                                            println("DispenseScreen: Error navigating to document viewer: ${e.message}")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DispensaListItem(
    document: LogosDoc,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val title = if (!document.descrizione.isNullOrBlank()) {
                document.descrizione
            } else {
                document.tipo ?: "Dispensa"
            }
            
            Text(
                text = prettifyTitle(title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Determina se un documento è una dispensa
 * Cerca "dispens" o "handout" in tipo, descrizione e url
 */
private fun isDispensaDoc(doc: LogosDoc): Boolean {
    val haystack = listOf(
        doc.tipo ?: "",
        doc.descrizione ?: "",
        doc.url ?: ""
    ).joinToString(" ").lowercase()
    
    // intercetta "dispensa/dispense/dispens" e possibili termini inglesi
    return haystack.contains("dispens") || haystack.contains("handout")
}

/**
 * Formatta il titolo in proper case (prima lettera maiuscola, resto minuscolo)
 * Gestisce i valori null restituendo un fallback
 */
private fun prettifyTitle(title: String?): String {
    if (title.isNullOrBlank()) {
        return "Dispensa"
    }
    return title.replace("_", " ")
        .split(" ")
        .joinToString(" ") { word ->
            if (word.isBlank()) word
            else word.lowercase().replaceFirstChar { it.uppercase() }
        }
}
