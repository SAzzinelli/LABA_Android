package com.laba.firenze.ui.perte

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.laba.firenze.domain.model.RoomCategory
import com.laba.firenze.domain.model.SuperSaasAppointment
import com.laba.firenze.domain.model.SuperSaasAvailabilitySlot
import com.laba.firenze.domain.model.SuperSaasRoom
import com.laba.firenze.domain.model.SuperSaasDateParser
import android.webkit.WebView
import android.webkit.WebViewClient
import java.text.SimpleDateFormat
import java.util.*

/** Prenotazione Aule - SuperSaas integration: login, rooms with slots, WebView fallback. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrenotazioneAuleScreen(
    navController: NavController,
    viewModel: PrenotazioneAuleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val token by viewModel.token.collectAsState(initial = "")
    val isAuthenticated = token.isNotEmpty()
    val user by viewModel.user.collectAsState()
    var showWebView by remember { mutableStateOf(false) }
    var selectedRoomForSlots by remember { mutableStateOf<SuperSaasRoom?>(null) }
    var selectedDate by remember { mutableStateOf(Date()) }

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            viewModel.loadRooms()
        }
    }

    // Booking success dialog
    if (uiState.showBookingSuccess && uiState.lastCreatedBooking != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissBookingSuccess() },
            title = { Text("Prenotazione confermata") },
            text = {
                Text("La tua aula è stata prenotata con successo.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissBookingSuccess()
                    selectedRoomForSlots = null
                }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            selectedRoomForSlots != null -> selectedRoomForSlots!!.name
                            showWebView -> "Calendario prenotazioni"
                            else -> "Prenotazione Aule"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when {
                            selectedRoomForSlots != null -> {
                                selectedRoomForSlots = null
                                viewModel.clearSlotsSelection()
                            }
                            showWebView -> showWebView = false
                            else -> navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (showWebView) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.setSupportZoom(true)
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false
                        loadUrl("https://prenotazioni.laba.biz")
                    }
                },
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            )
        } else if (!isAuthenticated) {
            SuperSaasLoginContent(
                modifier = Modifier.padding(paddingValues),
                labaEmail = viewModel.labaEmail,
                isLoading = uiState.isLoading,
                error = uiState.error,
                onLogin = { email, password -> viewModel.login(email, password) },
                onClearError = { viewModel.clearError() }
            )
        } else if (selectedRoomForSlots != null) {
            SuperSaasSlotsContent(
                modifier = Modifier.padding(paddingValues),
                room = selectedRoomForSlots!!,
                selectedDate = selectedDate,
                onDateChange = { selectedDate = it },
                slots = uiState.selectedRoomSlots,
                isLoading = uiState.selectedRoomSlotsLoading,
                currentUserEmail = user?.email,
                onSlotSelect = { slot ->
                    if (slot.available) {
                        viewModel.createBooking(selectedRoomForSlots!!, slot)
                    }
                },
                onRefresh = { viewModel.loadSlots(selectedRoomForSlots!!, selectedDate) },
                isCreatingBooking = uiState.isCreatingBooking
            )
            LaunchedEffect(selectedRoomForSlots, selectedDate) {
                viewModel.loadSlots(selectedRoomForSlots!!, selectedDate)
            }
        } else {
            SuperSaasRoomsContent(
                modifier = Modifier.padding(paddingValues),
                rooms = uiState.rooms.ifEmpty { com.laba.firenze.domain.model.SuperSaasRooms.list },
                userAppointments = uiState.userAppointments,
                onRoomSlotsClick = { selectedRoomForSlots = it },
                onOpenWebView = { showWebView = true },
                onLogout = { viewModel.logout() },
                onRefresh = { viewModel.loadUserAppointments() },
                isLoading = uiState.isLoading
            )
        }
    }
}

@Composable
private fun SuperSaasLoginContent(
    modifier: Modifier = Modifier,
    labaEmail: String?,
    isLoading: Boolean,
    error: String?,
    onLogin: (email: String, password: String) -> Unit,
    onClearError: () -> Unit
) {
    var email by remember { mutableStateOf(labaEmail ?: "") }
    var password by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val isValidEmail = email.lowercase().contains("@labafirenze.com")

    LaunchedEffect(labaEmail) {
        if (labaEmail != null && email.isEmpty()) {
            email = labaEmail
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(64.dp).align(Alignment.CenterHorizontally),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Accedi per prenotare le aule",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Usa le tue credenziali LABA (email @labafirenze.com) per accedere al servizio SuperSaaS.",
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
            onValueChange = { email = it.lowercase() },
            label = { Text("Email LABA") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onLogin(email, password) },
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && isValidEmail,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Accedi")
            }
        }
        if (!isValidEmail && email.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Inserisci un'email @labafirenze.com",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun SuperSaasRoomsContent(
    modifier: Modifier = Modifier,
    rooms: List<SuperSaasRoom>,
    userAppointments: List<SuperSaasAppointment>,
    onRoomSlotsClick: (SuperSaasRoom) -> Unit,
    onOpenWebView: () -> Unit,
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
    isLoading: Boolean
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM", Locale("it", "IT")) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale("it", "IT")) }
    val upcomingAppointments = remember(userAppointments) {
        val now = Date()
        userAppointments
            .filter { SuperSaasDateParser.parseDate(it.start)?.after(now) == true }
            .sortedBy { SuperSaasDateParser.parseDate(it.start)?.time ?: 0L }
            .take(5)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (upcomingAppointments.isNotEmpty()) {
            item {
                Text("Le Tue Prenotazioni", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp))
            }
            items(upcomingAppointments) { apt ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = roomNameForScheduleId(apt.schedule_id), style = MaterialTheme.typography.titleSmall)
                            Text(text = apt.formattedTimeRange, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(text = SuperSaasDateParser.parseDate(apt.start)?.let { dateFormat.format(it) } ?: "", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        item {
            Text("Aule disponibili", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp))
        }
        RoomCategory.entries.forEach { category ->
            val categoryRooms = rooms.filter { it.category == category }
            if (categoryRooms.isNotEmpty()) {
                items(categoryRooms) { room ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        onClick = { onRoomSlotsClick(room) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = room.name, style = MaterialTheme.typography.titleSmall)
                                Text(text = category.displayName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(text = "Vedi slot", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                onClick = onOpenWebView
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Apri calendario prenotazioni", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Accedi e prenota sul sito", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Esci dalle Aule")
            }
        }
    }
}

@Composable
private fun SuperSaasSlotsContent(
    modifier: Modifier = Modifier,
    room: SuperSaasRoom,
    selectedDate: Date,
    onDateChange: (Date) -> Unit,
    slots: List<SuperSaasAvailabilitySlot>?,
    isLoading: Boolean,
    currentUserEmail: String?,
    onSlotSelect: (SuperSaasAvailabilitySlot) -> Unit,
    onRefresh: () -> Unit,
    isCreatingBooking: Boolean
) {
    val showDatePicker = remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Seleziona data", style = MaterialTheme.typography.titleSmall)
            TextButton(onClick = { showDatePicker.value = true }) {
                Text(SimpleDateFormat("dd/MM/yyyy", Locale("it", "IT")).format(selectedDate))
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
        if (showDatePicker.value) {
            SuperSaasDatePickerDialog(
                selectedDate = selectedDate,
                onDateSelected = {
                    onDateChange(it)
                    showDatePicker.value = false
                },
                onDismiss = { showDatePicker.value = false }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Slot disponibili", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val slotsForDate = slots?.filter { slot ->
                val d = SuperSaasDateParser.parseDate(slot.start) ?: return@filter false
                calendar.time = d
                val slotDay = calendar.get(Calendar.DAY_OF_YEAR)
                val slotYear = calendar.get(Calendar.YEAR)
                calendar.time = selectedDate
                slotDay == calendar.get(Calendar.DAY_OF_YEAR) && slotYear == calendar.get(Calendar.YEAR)
            } ?: emptyList()
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (slotsForDate.isEmpty()) {
                    item {
                        Text("Nessuno slot disponibile per questa data.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    items(slotsForDate) { slot ->
                        val isUserSlot = !slot.available && (slot.bookedByEmail?.lowercase() == currentUserEmail?.lowercase())
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    slot.available -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    isUserSlot -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }
                            ),
                            onClick = {
                                if (slot.available && !isCreatingBooking) onSlotSelect(slot)
                            }
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (slot.available) Icons.Default.Schedule else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (slot.available) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = slot.timeRange, style = MaterialTheme.typography.titleSmall)
                                    if (!slot.available) {
                                        Text(text = "Prenotato da: ${slot.bookedBy ?: "Sconosciuto"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                if (slot.available && !isCreatingBooking) {
                                    Text("Disponibile", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                } else if (isCreatingBooking && slot.available) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuperSaasDatePickerDialog(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.time,
        yearRange = 2024..2030
    )
    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { onDateSelected(Date(it)) }
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        }
    ) {
        androidx.compose.material3.DatePicker(state = datePickerState)
    }
}

private fun roomNameForScheduleId(scheduleId: Int): String = when (scheduleId) {
    519294 -> "Camera Oscura"
    190289 -> "Photo LAB 1"
    190222 -> "Photo LAB 2"
    621475 -> "Scanner"
    548608 -> "Design LAB"
    397143 -> "Open LAB Fashion"
    702367 -> "Servizi Streaming"
    817857 -> "Movie Hall"
    else -> "Aula"
}
