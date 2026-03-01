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
fun ProgrammiScreen(
    navController: NavController,
    viewModel: DocumentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    var searchQuery by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        try {
            android.util.Log.d("ProgrammiScreen", "LaunchedEffect started")
            viewModel.loadDocuments()
            android.util.Log.d("ProgrammiScreen", "LaunchedEffect completed")
        } catch (e: Exception) {
            // Gestisce eventuali errori di inizializzazione
            android.util.Log.e("ProgrammiScreen", "Error in LaunchedEffect: ${e.message}", e)
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Programmi") },
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
                label = { Text("Cerca programmi") },
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
                shape = RoundedCornerShape(28.dp), // Forma capsula
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
                    val programDocs = uiState.documents.filter { isProgramDoc(it) }
                    val filteredDocs = if (searchQuery.isBlank()) {
                        programDocs
                    } else {
                        val queryLower = searchQuery.lowercase().trim()
                        programDocs.filter { doc ->
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
                                    text = "Nessun programma",
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
                                        text = "Programmi didattici",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Qui trovi i programmi dei corsi: obiettivi, contenuti, modalità d'esame e bibliografia. I documenti sono aggiornati dai docenti/Segreteria.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            
                            // Documents list - flat list like iOS, sorted by course name
                            val sortedDocs = filteredDocs.sortedBy { prettifyTitle(it.titolo) }
                            items(sortedDocs) { doc ->
                                DocumentListItem(
                                    document = doc,
                                    onClick = {
                                        try {
                                            // Naviga al visualizzatore documenti
                                            val encodedTitle = Uri.encode(prettifyTitle(doc.titolo))
                                            navController.navigate("document_viewer/${doc.oid}/$encodedTitle")
                                        } catch (e: Exception) {
                                            println("ProgrammiScreen: Error navigating to document viewer: ${e.message}")
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
private fun DocumentListItem(
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
            Text(
                text = prettifyTitle(document.titolo),
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
 * Determina se un documento è un programma didattico
 * Cerca "programma" o "program" in tipo, descrizione e url
 */
private fun isProgramDoc(doc: LogosDoc): Boolean {
    val haystack = listOf(
        doc.tipo ?: "",
        doc.descrizione ?: "",
        doc.url ?: ""
    ).joinToString(" ").lowercase()
    
    // match sia "program" che "programma"
    return haystack.contains("programma") || haystack.contains("program")
}

/**
 * Formatta il titolo in proper case (prima lettera maiuscola, resto minuscolo)
 * Gestisce i valori null restituendo un fallback
 */
private fun prettifyTitle(title: String?): String {
    if (title.isNullOrBlank()) {
        return "Programma didattico"
    }
    return title.replace("_", " ")
        .split(" ")
        .joinToString(" ") { word ->
            if (word.isBlank()) word
            else word.lowercase().replaceFirstChar { it.uppercase() }
        }
}
