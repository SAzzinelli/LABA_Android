package com.laba.firenze.ui.documents

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentViewerScreen(
    navController: NavController,
    allegatoOid: String,
    title: String,
    viewModel: DocumentsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var tempFileUri by remember { mutableStateOf<Uri?>(null) }
    
    LaunchedEffect(allegatoOid) {
        isLoading = true
        errorMessage = null
        
        try {
            val documentData = viewModel.downloadDocument(allegatoOid)
            if (documentData != null) {
                // Salva il file temporaneo
                val tempFile = File(context.cacheDir, "LABA_${allegatoOid}_${sanitizeFileName(title)}.pdf")
                FileOutputStream(tempFile).use { fos ->
                    fos.write(documentData)
                }
                
                tempFileUri = Uri.fromFile(tempFile)
                isLoading = false
            } else {
                errorMessage = "Impossibile scaricare il documento"
                isLoading = false
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Errore sconosciuto"
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    if (tempFileUri != null) {
                        IconButton(
                            onClick = {
                                shareDocument(context, tempFileUri!!, title)
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Condividi")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Caricamento documento...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                errorMessage != null -> {
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
                                text = "Errore",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                tempFileUri != null -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header con informazioni documento
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Documento pronto",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Il documento è stato scaricato e salvato. Puoi aprirlo con un'applicazione PDF o condividerlo.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        // Pulsanti azione
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    openDocument(context, tempFileUri!!)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Apri PDF")
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    shareDocument(context, tempFileUri!!, title)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Condividi")
                            }
                        }
                        
                        // Informazioni aggiuntive
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Informazioni",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "• Il documento è salvato nella cache dell'app",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "• Puoi aprire il PDF con qualsiasi app compatibile",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "• Il file verrà eliminato automaticamente quando non più necessario",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun sanitizeFileName(fileName: String): String {
    val invalid = Regex("[\\\\/:*?\"<>|\\n]")
    val sanitized = fileName.replace(invalid, " ")
    return if (sanitized.isBlank()) "documento" else sanitized
}

private fun openDocument(context: Context, fileUri: Uri) {
    try {
        // Prova prima con Chrome specificamente
        val chromeIntent = Intent(Intent.ACTION_VIEW, fileUri).apply {
            setPackage("com.android.chrome")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        if (chromeIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(chromeIntent)
            return
        }
        
        // Fallback: qualsiasi browser
        val browserIntent = Intent(Intent.ACTION_VIEW, fileUri).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        if (browserIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(browserIntent)
        } else {
            // Ultimo fallback: qualsiasi app che può gestire PDF
            val pdfIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            if (pdfIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(pdfIntent)
            } else {
                android.widget.Toast.makeText(context, "Nessuna app disponibile per aprire PDF", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Errore nell'apertura del documento: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
    }
}

private fun shareDocument(context: Context, fileUri: Uri, title: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooserIntent = Intent.createChooser(intent, "Condividi documento")
        if (chooserIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooserIntent)
        } else {
            android.widget.Toast.makeText(context, "Nessuna app disponibile per condividere", android.widget.Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Errore nella condivisione del documento", android.widget.Toast.LENGTH_SHORT).show()
    }
}

