package com.laba.firenze.ui.thesis

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.compose.foundation.clickable
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThesisScreen(
    navController: NavController,
    viewModel: ThesisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    

    // Funzione per scaricare e aprire documenti LOGOS internamente
    fun openLogosDocument(allegatoOid: String) {
        coroutineScope.launch {
            try {
                // Scarica il documento dall'API
                val documentData = viewModel.downloadDocument(allegatoOid)
                if (documentData != null) {
                    // Salva temporaneamente il file e aprilo
                    val fileName = "thesis_document_$allegatoOid.pdf"
                    val file = java.io.File(context.cacheDir, fileName)
                    file.writeBytes(documentData)
                    
                    // Apri il file con un'app PDF
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(
                            androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            ),
                            "application/pdf"
                        )
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Fallback: apri con qualsiasi app
                        val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(
                                androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                ),
                                "*/*"
                            )
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(fallbackIntent)
                    }
                } else {
                    // Se il download fallisce, mostra messaggio di errore
                    android.widget.Toast.makeText(context, "Documento non disponibile al momento", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Mostra errore invece di aprire URL inesistente
                android.widget.Toast.makeText(context, "Errore nel caricamento del documento", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Funzione per aprire PDF nel browser o documento LOGOS
    fun openPDF(url: String) {
        if (url.startsWith("logos://document/")) {
            // Documento LOGOS - scarica e apri internamente
            val allegatoOid = url.removePrefix("logos://document/")
            openLogosDocument(allegatoOid)
        } else {
            // URL normale - apri nel browser
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
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tesi di laurea") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Tesi di laurea",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Qui trovi regolamenti, modelli e scadenze per preparare la prova finale. Usa questa sezione come guida rapida insieme alle indicazioni del relatore e della segreteria.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // KPI Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Esami completati
                    KpiCard(
                        title = "Esami",
                        value = uiState.examsCompletedStatus,
                        icon = Icons.Default.CheckCircle,
                        isCompleted = uiState.allExamsCompleted,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Voto di presentazione o stato laurea
                    if (uiState.canGraduate) {
                        KpiCard(
                            title = "Voto di presentazione",
                            value = uiState.presentationGrade,
                            icon = Icons.Default.School,
                            isCompleted = false,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        KpiCard(
                            title = "Stato laurea",
                            value = "Non disponibile",
                            icon = Icons.Default.Info,
                            isCompleted = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Voto d'ingresso info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Voto d'ingresso: La tua stima in tempo reale",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (uiState.canGraduate) {
                            Text(
                                text = "Il Voto di presentazione è un calcolo automatico basato sulla media aritmetica dei tuoi esami verbalizzati. Il voto finale è convertito su una scala di 110.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Text(
                                text = "Il voto di presentazione sarà disponibile quando avrai completato almeno l'80% degli esami del tuo corso di studi.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = "Come lo verifichi e lo ricalcoli?",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "• Dalla Home: tocca la sezione \"Voto d'ingresso\".\n• Dalla sezione Esami: accedi alla lista dei tuoi voti.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Checklist Section
            item {
                ChecklistSection(minPages = uiState.minPages)
            }

            // Documenti Section
            item {
                DocumentsSection(
                    documents = uiState.documents,
                    isLoading = uiState.isLoading,
                    onDocumentClick = { document ->
                        document.url?.let { url ->
                            openPDF(url)
                        }
                    }
                )
            }

            // Pergamena Section
            item {
                PergamenaSection(
                    onNavigateToPergamena = { navController.navigate("pergamena") }
                )
            }

            // Contatti Section
            item {
                ContactsSection(
                    onOpenRegulations = { openPDF("https://laba.biz/wp-content/uploads/2025/03/REGOLAMENTO-TESI-da-feb-2024.pdf") },
                    onOpenEmail = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:info@laba.biz")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isCompleted) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isCompleted) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = if (isCompleted) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ChecklistSection(minPages: Int) {
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
            Text(
                text = "Checklist rapida",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            val checklistItems = listOf(
                Icons.Default.Person to "Scegli argomento e relatore",
                Icons.Default.Schedule to "Concorda revisioni periodiche con il relatore",
                Icons.AutoMirrored.Filled.Send to "Alla scadenza delle consegne invia al relatore una versione PDF (almeno l'80% dell'elaborato)",
                Icons.Default.Description to "Imposta il frontespizio seguendo le indicazioni sul file che trovi qua",
                Icons.Default.TextFields to "Almeno $minPages pagine; 2/3 ricerca e 1/3 progetto",
                Icons.Default.Print to "Stampa 4 copie fisiche e consegnale in segreteria entro la scadenza",
                Icons.Default.Storage to "Il giorno della discussione porta una USB con materiali e PDF della tesi",
                Icons.Default.Euro to "Pagamenti: dopo la tesi, solo per il ritiro pergamena (se previsto)"
            )
            
            checklistItems.forEach { (icon, text) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DocumentsSection(
    documents: List<ThesisDocument>,
    isLoading: Boolean,
    onDocumentClick: (ThesisDocument) -> Unit
) {
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
            Text(
                text = "Moduli, pagamenti e modelli",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (documents.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Nessun documento disponibile al momento",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                }
            } else {
                documents.forEach { document ->
                    DocumentItem(
                        document = document,
                        onClick = { onDocumentClick(document) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DocumentItem(
    document: ThesisDocument,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                if (document.type.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = document.type,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
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

@Composable
private fun PergamenaSection(
    onNavigateToPergamena: () -> Unit
) {
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
            Text(
                text = "Pergamena ufficiale",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = onNavigateToPergamena,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Newspaper,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Come richiederla e ritirarla")
            }
        }
    }
}

@Composable
private fun ContactsSection(
    onOpenRegulations: () -> Unit,
    onOpenEmail: () -> Unit
) {
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
            Text(
                text = "Hai dubbi?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onOpenRegulations,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Regolamento", style = MaterialTheme.typography.labelMedium)
                }
                
                Button(
                    onClick = onOpenEmail,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Segreteria", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

data class ThesisDocument(
    val id: String,
    val title: String,
    val type: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val url: String?
)

data class ThesisUiState(
    val isLoading: Boolean = false,
    val documents: List<ThesisDocument> = emptyList(),
    val examsCompletedStatus: String = "Da verificare",
    val allExamsCompleted: Boolean = false,
    val presentationGrade: String = "—",
    val minPages: Int = 80,
    val canGraduate: Boolean = false
)
