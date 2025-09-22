package com.laba.firenze.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.laba.firenze.R
import com.laba.firenze.ui.auth.AuthViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var userBase by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showingInfo by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Animation states
    var startAnimations by remember { mutableStateOf(false) }
    
    val authState by authViewModel.authState.collectAsState()
    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        delay(350)
        startAnimations = true
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Animated background pattern (simplified for Android)
        if (startAnimations) {
            AnimatedPatternBackground()
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            
            // LABA Logo - usa logo blu in light mode, bianco in dark mode
            val isDarkTheme = isSystemInDarkTheme()
            Image(
                painter = painterResource(
                    id = if (isDarkTheme) R.drawable.bianco else R.drawable.blu
                ),
                contentDescription = "LABA Logo",
                modifier = Modifier.size(160.dp)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Username field
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    TextField(
                        value = userBase,
                        onValueChange = { userBase = it },
                        placeholder = { 
                            Text(
                                "nome.cognome",
                                fontSize = 14.sp
                            ) 
                        },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { passwordFocusRequester.requestFocus() }
                        ),
                        singleLine = true
                    )
                    Text(
                        "@labafirenze.com",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Password field
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { 
                            Text(
                                "Password",
                                fontSize = 14.sp
                            ) 
                        },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(passwordFocusRequester),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Go
                        ),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                focusManager.clearFocus()
                                if (userBase.isNotEmpty() && password.isNotEmpty()) {
                                    isLoading = true
                                    authViewModel.signIn(userBase, password)
                                }
                            }
                        ),
                        singleLine = true
                    )
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible }
                    ) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Error message
            if (authState.error != null) {
                Text(
                    text = authState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Login button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (userBase.isNotEmpty() && password.isNotEmpty()) {
                        isLoading = true
                        authViewModel.signIn(userBase, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 12.dp),
                enabled = userBase.isNotEmpty() && password.isNotEmpty() && !authState.isLoading && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                if (authState.isLoading || isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Entra",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Footer
            Text(
                text = "© 2025 LABA Firenze - with 💙 by Simone Azzinelli",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            )
        }
        
        // Top bar with info button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, end = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = { showingInfo = true }
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    // Handle successful login
    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) {
            isLoading = false
        }
    }
    
    LaunchedEffect(authState.error) {
        if (authState.error != null) {
            isLoading = false
        }
    }
    
    // Info sheet
    if (showingInfo) {
        AccessHelpSheet(onDismiss = { showingInfo = false })
    }
}

@Composable
private fun AnimatedPatternBackground() {
    // Simplified animated background for Android
    val infiniteTransition = rememberInfiniteTransition(label = "pattern")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.02f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccessHelpSheet(
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.size(width = 36.dp, height = 5.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Assistenza",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Quick guide
            Text(
                text = "Guida rapida",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            HelpItem(
                icon = Icons.Default.AlternateEmail,
                text = "Accedi utilizzando nome.cognome"
            )
            HelpItem(
                icon = Icons.Default.TextFields,
                text = "Controlla maiuscole/minuscole"
            )
            HelpItem(
                icon = Icons.Default.Edit,
                text = "Evita spazi iniziali/finali nei campi"
            )
            HelpItem(
                icon = Icons.Default.Schedule,
                text = "Hai appena cambiato password? Attendi un po'"
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Contact info
            Text(
                text = "Contatti",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            HelpItem(
                icon = Icons.Default.Phone,
                text = "Segreteria Didattica: 055 653 0786"
            )
            HelpItem(
                icon = Icons.Default.Phone,
                text = "Reparto IT: 334 382 4934"
            )
            HelpItem(
                icon = Icons.Default.Email,
                text = "Email: info@laba.biz"
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HelpItem(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}