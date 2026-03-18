package com.laba.firenze.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

/** Nomi sezioni per l'ordine Home (identico a iOS home.sectionOrder). */
private val SECTION_LABELS = mapOf(
    "hero" to "Hero / Benvenuto",
    "kpi" to "KPI (Esami, CFA)",
    "progress" to "Progresso anno e media",
    "lessons" to "Lezioni di oggi",
    "exams" to "Esami prenotati",
    "quickActions" to "Per te e Servizi"
)

/** Schermata ordine sezioni Home (identico a iOS personalizzazione). */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeSectionOrderScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val order by viewModel.sectionOrder.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ordine sezioni Bacheca") },
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Trascina per riordinare le sezioni nella Home.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            itemsIndexed(order) { index, sectionId ->
                val label = SECTION_LABELS[sectionId] ?: sectionId
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DragHandle,
                            contentDescription = "Trascina",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                if (index > 0) {
                                    val newOrder = order.toMutableList()
                                    newOrder.removeAt(index)
                                    newOrder.add(index - 1, sectionId)
                                    viewModel.saveSectionOrder(newOrder)
                                }
                            }
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Sposta su")
                        }
                        IconButton(
                            onClick = {
                                if (index < order.size - 1) {
                                    val newOrder = order.toMutableList()
                                    newOrder.removeAt(index)
                                    newOrder.add(index + 1, sectionId)
                                    viewModel.saveSectionOrder(newOrder)
                                }
                            }
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Sposta giù")
                        }
                    }
                }
            }
        }
    }
}
