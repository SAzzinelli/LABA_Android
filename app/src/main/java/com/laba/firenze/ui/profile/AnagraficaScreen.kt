package com.laba.firenze.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.text.selection.SelectionContainer
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.laba.firenze.ui.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnagraficaScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userProfile = uiState.userProfile
    var showChangePassword by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anagrafica") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Dati Anagrafici",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Questi dati provengono dal gestionale LOGOS.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Generalità Section
            ProfileDataSection(
                title = "Generalità",
                items = listOf(
                    ProfileDataItem("Nome", userProfile?.nome ?: "-"),
                    ProfileDataItem("Cognome", userProfile?.cognome ?: "-"),
                    ProfileDataItem("Sesso", userProfile?.sesso ?: "-"),
                    ProfileDataItem("Matricola", userProfile?.matricola ?: "-")
                )
            )
            
            // Contatti Section
            ProfileDataSection(
                title = "Contatti",
                items = buildList {
                    userProfile?.emailLABA?.takeIf { it.isNotEmpty() }?.let {
                        add(ProfileDataItem("Email Istituzionale", it, isSelectable = true))
                    }
                    userProfile?.emailPersonale?.takeIf { it.isNotEmpty() }?.let {
                        add(ProfileDataItem("Email Personale", it, isSelectable = true))
                    }
                    userProfile?.cellulare?.takeIf { it.isNotEmpty() }?.let {
                        add(ProfileDataItem("Cellulare", it))
                    }
                    userProfile?.telefono?.takeIf { it.isNotEmpty() }?.let {
                        add(ProfileDataItem("Telefono", it))
                    }
                }
            )
            
            // Accademico Section
            ProfileDataSection(
                title = "Accademico",
                items = listOf(
                    ProfileDataItem("Stato Pagamenti", userProfile?.pagamenti ?: "-"),
                    ProfileDataItem("Piano di Studi", userProfile?.pianoStudi ?: "-"),
                    ProfileDataItem("Anno Attuale", userProfile?.currentYear ?: "-"),
                    ProfileDataItem("Status", userProfile?.status ?: "-")
                )
            )
            
            // Cambia Password Button
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { showChangePassword = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Lock, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cambia Password")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    if (showChangePassword) {
        ChangePasswordDialog(
            onDismiss = {
                viewModel.clearChangePasswordState()
                showChangePassword = false
            },
            onSuccess = { showChangePassword = false },
            viewModel = viewModel
        )
    }
}

@Composable
private fun ProfileDataSection(
    title: String,
    items: List<ProfileDataItem>
) {
    if (items.isEmpty()) return
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    ProfileDataRow(
                        label = item.label,
                        value = item.value,
                        isSelectable = item.isSelectable
                    )
                    if (index < items.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileDataRow(
    label: String,
    value: String,
    isSelectable: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (isSelectable) {
            SelectionContainer {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )
            }
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

private data class ProfileDataItem(
    val label: String,
    val value: String,
    val isSelectable: Boolean = false
)

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: ProfileViewModel
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    val uiState by viewModel.uiState.collectAsState()
    val changePasswordState = uiState.changePasswordState
    val isLoading = changePasswordState == com.laba.firenze.ui.profile.ChangePasswordState.Loading
    
    LaunchedEffect(changePasswordState) {
        when (changePasswordState) {
            is com.laba.firenze.ui.profile.ChangePasswordState.Success -> {
                successMessage = "Password cambiata con successo!"
                viewModel.clearChangePasswordState()
                delay(1500)
                onSuccess()
            }
            is com.laba.firenze.ui.profile.ChangePasswordState.Error -> {
                errorMessage = (changePasswordState as com.laba.firenze.ui.profile.ChangePasswordState.Error).message
                viewModel.clearChangePasswordState()
            }
            else -> {}
        }
    }
    
    val isValid = oldPassword.isNotEmpty() && 
                  newPassword.isNotEmpty() && 
                  confirmPassword.isNotEmpty() &&
                  newPassword == confirmPassword &&
                  newPassword.length >= 8
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambia Password") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (successMessage != null) {
                    Text(
                        text = successMessage!!,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Vecchia password") },
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nuova password") },
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Text(
                            "Minimo 8 caratteri",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                )
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Conferma password") },
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword,
                    supportingText = {
                        if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                            Text(
                                "Le password non corrispondono",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    errorMessage = null
                    successMessage = null
                    viewModel.changePassword(oldPassword, newPassword)
                },
                enabled = isValid && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Cambia Password")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}
