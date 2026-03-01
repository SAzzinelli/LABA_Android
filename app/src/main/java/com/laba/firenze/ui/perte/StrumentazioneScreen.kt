package com.laba.firenze.ui.perte

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.laba.firenze.data.api.GestionaleUser
import com.laba.firenze.domain.model.EquipmentLoan
import com.laba.firenze.domain.model.EquipmentReport
import com.laba.firenze.domain.model.EquipmentRequest
import com.laba.firenze.domain.model.UserEquipment

/** Service LABA (Gestionale attrezzatura) - dashboard allineata a iOS UserSimpleDashboard. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrumentazioneScreen(
    navController: NavController,
    viewModel: StrumentazioneViewModel = hiltViewModel()
) {
    val token by viewModel.token.collectAsState()
    val user by viewModel.user.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isAuthenticated = token.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isAuthenticated) "Service LABA" else "Accesso Service LABA") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    if (isAuthenticated) {
                        var showMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Aggiorna") },
                                onClick = {
                                    showMenu = false
                                    viewModel.refreshData()
                                },
                                leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Esci", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    viewModel.logout()
                                },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (!isAuthenticated) {
            GestionaleLoginContent(
                modifier = Modifier.padding(paddingValues),
                isLoading = uiState.isLoading,
                error = uiState.error,
                onLogin = { email, password -> viewModel.login(email, password) },
                onClearError = { viewModel.clearError() }
            )
        } else {
            GestionaleDashboardContent(
                modifier = Modifier.padding(paddingValues),
                user = user,
                equipment = uiState.equipment,
                requests = uiState.requests,
                loans = uiState.loans,
                reports = uiState.reports,
                isLoading = uiState.isLoading,
                onRefresh = { viewModel.refreshData() },
                onLogout = { viewModel.logout() },
                onCreateReport = { loan -> viewModel.openCreateReport(loan) }
            )
        }
    }

    uiState.createReportLoan?.let { loan ->
        SegnalaProblemaSheet(
            loan = loan,
            onDismiss = { viewModel.dismissCreateReport() },
            onSubmit = { report -> viewModel.submitReport(report) },
            isSubmitting = uiState.isSubmittingReport
        )
    }
}

@Composable
private fun GestionaleLoginContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    error: String?,
    onLogin: (email: String, password: String) -> Unit,
    onClearError: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(64.dp).align(Alignment.CenterHorizontally),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Accedi al Service LABA", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Per accedere alle attrezzature LABA effettua il login con le tue credenziali del gestionale.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (error != null) {
            Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))
        }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onLogin(email, password) },
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Accedi")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GestionaleDashboardContent(
    modifier: Modifier = Modifier,
    user: GestionaleUser?,
    equipment: List<UserEquipment>,
    requests: List<EquipmentRequest>,
    loans: List<EquipmentLoan>,
    reports: List<EquipmentReport>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onCreateReport: (EquipmentLoan) -> Unit
) {
    val activeLoans = remember(loans) { loans.filter { it.stato == "attivo" } }
    val availableCount = remember(equipment) { equipment.count { it.isAvailable } }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var showAllLoans by remember { mutableStateOf(false) }
    var showAllRequests by remember { mutableStateOf(false) }

    val catalogoIndex = remember(activeLoans, requests, equipment, showAllLoans, showAllRequests) {
        var idx = 2
        if (activeLoans.isNotEmpty()) {
            idx += 1
            idx += if (showAllLoans) activeLoans.size else minOf(3, activeLoans.size)
            if (activeLoans.size > 3) idx += 1
        }
        if (requests.isNotEmpty()) {
            idx += 1
            idx += if (showAllRequests) requests.size else minOf(3, requests.size)
            if (requests.size > 3) idx += 1
        }
        idx + 2
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Le Tue Attività",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KPIWidget(
                        modifier = Modifier.weight(1f),
                        value = "$availableCount",
                        label = "Disponibili",
                        icon = Icons.Default.CameraAlt,
                        color = Color(0xFF34C759)
                    )
                    KPIWidget(
                        modifier = Modifier.weight(1f),
                        value = "${activeLoans.size}",
                        label = "Prestiti Attivi",
                        icon = Icons.Default.Work,
                        color = Color(0xFF007AFF)
                    )
                    KPIWidget(
                        modifier = Modifier.weight(1f),
                        value = "${requests.size}",
                        label = "Richieste",
                        icon = Icons.Default.Description,
                        color = Color(0xFFFF9500)
                    )
                }
            }

            if (activeLoans.isNotEmpty()) {
                item {
                    Text(
                        "Prestiti Attivi",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                val loansToShow = if (showAllLoans) activeLoans else activeLoans.take(3)
                items(loansToShow) { loan ->
                    LoanRow(loan = loan)
                }
                if (activeLoans.size > 3) {
                    item {
                        TextButton(
                            onClick = { showAllLoans = !showAllLoans },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (showAllLoans) "Mostra meno" else "Vedi tutti i prestiti",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (requests.isNotEmpty()) {
                item {
                    Text(
                        "Richieste",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                val requestsToShow = if (showAllRequests) requests else requests.take(3)
                items(requestsToShow) { req ->
                    RequestRow(request = req)
                }
                if (requests.size > 3) {
                    item {
                        TextButton(
                            onClick = { showAllRequests = !showAllRequests },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (showAllRequests) "Mostra meno" else "Vedi tutte le richieste",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "Azioni",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                    ActionRow(
                        icon = Icons.AutoMirrored.Filled.List,
                        label = "Disponibili",
                        onClick = {
                            if (equipment.isNotEmpty()) {
                                scope.launch {
                                    listState.animateScrollToItem(catalogoIndex)
                                }
                            }
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    ActionRow(
                        icon = Icons.Default.Work,
                        label = "I Miei Prestiti",
                        onClick = { showAllLoans = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    ActionRow(
                        icon = Icons.Default.Description,
                        label = "Le Mie Richieste",
                        onClick = { showAllRequests = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    ActionRow(
                        icon = Icons.Default.Warning,
                        label = "Segnala Problema",
                        onClick = {
                            when {
                                activeLoans.isEmpty() -> { /* nessun prestito attivo per segnalare */ }
                                activeLoans.size == 1 -> onCreateReport(activeLoans.first())
                                else -> onCreateReport(activeLoans.first()) // usa il primo se multipli
                            }
                        },
                        tint = MaterialTheme.colorScheme.error
                    )
                    }
                }
            }

            if (equipment.isNotEmpty()) {
                item {
                    Text(
                        "Catalogo attrezzature",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                items(equipment.filter { it.isAvailable }.take(8)) { item ->
                    EquipmentCard(equipment = item)
                }
            }

            item {
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Esci dal Service")
                }
            }
        }

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun RowScope.KPIWidget(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoanRow(loan: EquipmentLoan) {
    val statusColor = when {
        loan.isExpired -> MaterialTheme.colorScheme.error
        loan.isExpiringSoon -> Color(0xFFFF9500)
        else -> Color(0xFF34C759)
    }
    val timeText = when (val d = loan.daysRemaining) {
        in Int.MIN_VALUE..<0 -> "Scaduto da ${-d} giorni"
        0 -> "Scade oggi"
        1 -> "Scade domani"
        else -> "Scade tra $d giorni"
    }
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
                if (loan.isExpired) Icons.Default.CheckCircle else Icons.Default.Schedule,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(loan.fullEquipmentName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(loan.dateRange, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(timeText, style = MaterialTheme.typography.labelSmall, color = statusColor)
            }
        }
    }
}

@Composable
private fun RequestRow(request: EquipmentRequest) {
    val statusColor = when (request.stato.lowercase()) {
        "pending", "in_attesa", "in attesa" -> Color(0xFFFF9500)
        "approved", "approvata" -> Color(0xFF34C759)
        "rejected", "rifiutata" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
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
                when (request.stato.lowercase()) {
                    "pending", "in_attesa", "in attesa" -> Icons.Default.Schedule
                    "approved", "approvata" -> Icons.Default.CheckCircle
                    "rejected", "rifiutata" -> Icons.Default.Cancel
                    else -> Icons.Default.Description
                },
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(request.equipmentName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        "${request.statusDisplay} • ${request.shortDateRange}",
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge, color = tint)
        }
    }
}

@Composable
private fun EquipmentCard(equipment: UserEquipment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(equipment.nome, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${equipment.categoryDisplay} • ${equipment.availabilityText}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SegnalaProblemaSheet(
    loan: EquipmentLoan,
    onDismiss: () -> Unit,
    onSubmit: (com.laba.firenze.domain.model.CreateEquipmentReport) -> Unit,
    isSubmitting: Boolean
) {
    var tipo by remember { mutableStateOf("problema") }
    var urgenza by remember { mutableStateOf("media") }
    var messaggio by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text("Segnala Problema", style = MaterialTheme.typography.titleLarge)
            Text("Prestito: ${loan.equipmentName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = messaggio,
                onValueChange = { messaggio = it },
                label = { Text("Messaggio") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Annulla") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        onSubmit(
                            com.laba.firenze.domain.model.CreateEquipmentReport(
                                prestito_id = loan.id,
                                inventario_id = loan.inventario_id,
                                tipo = tipo,
                                urgenza = urgenza,
                                messaggio = messaggio.ifBlank { "Segnalazione" }
                            )
                        )
                    },
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                    else Text("Invia")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
