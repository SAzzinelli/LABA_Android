package com.laba.firenze.ui.benefits

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laba.firenze.domain.model.PartnerCategory
import com.laba.firenze.domain.model.StudentBenefitPartner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgevolazioniScreen(navController: androidx.navigation.NavController) {
    val allPartners = StudentBenefitPartner.samples
    var selectedCategory by remember { mutableStateOf<PartnerCategory?>(null) }
    var searchText by remember { mutableStateOf("") }
    
    val filteredPartners = remember(selectedCategory, searchText) {
        allPartners.filter { partner ->
            val categoryMatch = selectedCategory == null || partner.category == selectedCategory
            val searchMatch = if (searchText.isBlank()) true else {
                partner.name.contains(searchText, ignoreCase = true) ||
                partner.address.contains(searchText, ignoreCase = true) ||
                partner.category.displayName.contains(searchText, ignoreCase = true)
            }
            categoryMatch && searchMatch
        }.sortedBy { it.name }
    }
    
    // Group by category if no category selected, otherwise just list
    val groupedPartners = remember(filteredPartners, selectedCategory) {
        if (selectedCategory != null) {
            mapOf(selectedCategory!! to filteredPartners)
        } else {
            filteredPartners.groupBy { it.category }.toSortedMap { a, b -> a.displayName.compareTo(b.displayName) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agevolazioni") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Cerca convenzione") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 140.dp)
            ) {
                // Intro Card
                item {
                    IntroCard()
                }
                
                // Filter Chips
                item {
                    CategoryFilterBar(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it }
                    )
                }
                
                // Partners List
                groupedPartners.forEach { (category, partners) ->
                    if (partners.isNotEmpty()) {
                        item {
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                        }
                        
                        items(partners) { partner ->
                            PartnerCard(partner = partner)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
                
                if (groupedPartners.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Nessuna convenzione trovata", color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IntroCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Stars, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    "Convenzioni dedicate",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                "Mostra il tuo badge LABA nei locali convenzionati per ottenere sconti e condizioni dedicate su stampa, ristorazione, abbigliamento e molto altro.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun CategoryFilterBar(
    selectedCategory: PartnerCategory?,
    onCategorySelected: (PartnerCategory?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("Tutte") }
            )
        }
        items(PartnerCategory.allCases()) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { 
                    onCategorySelected(if (selectedCategory == category) null else category) 
                },
                label = { Text(category.displayName) }
            )
        }
    }
}

@Composable
fun PartnerCard(partner: StudentBenefitPartner) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = partner.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = partner.category.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (partner.mapLink != null) {
                        SmallIconButton(Icons.Default.Map, Color.Blue) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(partner.mapLink))
                            context.startActivity(intent)
                        }
                    }
                    if (partner.phone != null) {
                        SmallIconButton(Icons.Default.Phone, Color.Green) {
                             val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${partner.phone}"))
                             context.startActivity(intent)
                        }
                    }
                }
            }
            
            // Address
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Text(
                    text = partner.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Highlight Box / Expand Button
            if (partner.highlight != null || partner.additionalNotes != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFA500).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .clickable { expanded = !expanded }
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocalOffer, 
                            contentDescription = null, 
                            tint = Color(0xFFF57C00),
                            modifier = Modifier
                                .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                                .padding(6.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (expanded) "Nascondi dettagli" else (partner.highlight ?: "Scopri l'agevolazione"),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!expanded) {
                                Text(
                                    "Mostra dettagli",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    AnimatedVisibility(visible = expanded) {
                        Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (partner.highlight != null) {
                                Text(
                                    partner.highlight!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (partner.additionalNotes != null) {
                                Text(
                                    partner.additionalNotes!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            // "Usa agevolazione" Button
                            Button(
                                onClick = { 
                                    // Navigate to Student Card (or BenefitRedeemView if implemented separate)
                                    // For now, we don't have direct nav access here unless passed
                                    // But implementing visual logic first
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Default.Stars, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Usa agevolazione")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmallIconButton(icon: ImageVector, tint: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(tint.copy(alpha = 0.1f), CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
    }
}
