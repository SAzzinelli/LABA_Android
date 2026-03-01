package com.laba.firenze.ui.common

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.laba.firenze.R
import com.laba.firenze.ui.auth.AuthViewModel
import com.laba.firenze.ui.common.DevOptionsSheet
import kotlinx.coroutines.delay

/** Login Screen 2.0 - allineata a iOS LoginView2. */
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
    var startAnimations by remember { mutableStateOf(false) }
    var cardOffsetY by remember { mutableStateOf(50f) }
    var cardOpacity by remember { mutableFloatStateOf(0f) }

    val authState by authViewModel.authState.collectAsState()
    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    val primaryColor = MaterialTheme.colorScheme.primary

    var tapCount by remember { mutableStateOf(0) }
    var showDevMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        startAnimations = true
    }
    LaunchedEffect(startAnimations) {
        if (startAnimations) {
            animate(
                initialValue = 50f,
                targetValue = 0f,
                animationSpec = tween(600, easing = FastOutSlowInEasing)
            ) { value, _ -> cardOffsetY = value }
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(600, easing = FastOutSlowInEasing)
            ) { value, _ -> cardOpacity = value }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (startAnimations) {
            LoginAnimatedPatternBackground()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Image(
                painter = painterResource(id = if (isDarkTheme) R.drawable.bianco else R.drawable.blu),
                contentDescription = "LABA Logo",
                modifier = Modifier
                    .size(180.dp)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        tapCount++
                        if (tapCount >= 5) {
                            tapCount = 0
                            showDevMenu = true
                        }
                    }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Benvenuto",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Accedi al tuo account LABA",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = cardOffsetY.dp)
                    .alpha(cardOpacity),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Email", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                TextField(
                                    value = userBase,
                                    onValueChange = { userBase = it },
                                    placeholder = { Text("nome.cognome", fontSize = 16.sp) },
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() }),
                                    singleLine = true
                                )
                                Text("@labafirenze.com", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Password", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                TextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    placeholder = { Text("Password", fontSize = 16.sp) },
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
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Go),
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
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) "Nascondi" else "Mostra"
                                    )
                                }
                            }
                        }
                    }

                    if (authState.error != null && authState.error!!.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(authState.error!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        PrivacyPolicyText(
                            onPrivacyClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.laba.biz/privacy-policy"))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                    )
                    }

                    val isDisabled = userBase.isEmpty() || password.isEmpty() || authState.isLoading || isLoading
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (!isDisabled) {
                                isLoading = true
                                authViewModel.signIn(userBase, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !isDisabled,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDisabled) MaterialTheme.colorScheme.surfaceContainerHighest else primaryColor,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    ) {
                        if (authState.isLoading || isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(22.dp), tint = if (isDisabled) MaterialTheme.colorScheme.onSurfaceVariant else Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Accedi", fontWeight = FontWeight.SemiBold, color = if (isDisabled) MaterialTheme.colorScheme.onSurfaceVariant else Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "© 2026 LABA Firenze - with 💙 by Simone Azzinelli",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, end = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { showingInfo = true }) {
                Text("?", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    if (showDevMenu) {
        DevOptionsSheet(
            currentVersion = authViewModel.getApiVersion(),
            onSelectVersion = { authViewModel.setApiVersion(it); showDevMenu = false },
            onDismiss = { showDevMenu = false }
        )
    }

    LaunchedEffect(authState.isLoggedIn) { if (authState.isLoggedIn) isLoading = false }
    LaunchedEffect(authState.error) { if (authState.error != null) isLoading = false }

    if (showingInfo) {
        AccessHelpSheet(onDismiss = { showingInfo = false })
    }
}

@Composable
private fun PrivacyPolicyText(onPrivacyClick: () -> Unit) {
    val annotatedString = buildAnnotatedString {
        append("Accedendo accetti la ")
        pushStringAnnotation(tag = "privacy", annotation = "https://www.laba.biz/privacy-policy")
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
            append("Privacy Policy")
        }
        pop()
        append(".")
    }
    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier.fillMaxWidth(),
        onClick = { offset ->
            annotatedString.getStringAnnotations("privacy", offset, offset + 1).firstOrNull()?.let {
                onPrivacyClick()
            }
        }
    )
}

@Composable
private fun LoginAnimatedPatternBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "loginPattern")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.04f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    val primaryColor = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryColor.copy(alpha = alpha))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccessHelpSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.heightIn(max = 600.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Assistenza", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Guida rapida", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            HelpItem(icon = Icons.Default.AlternateEmail, text = "Accedi utilizzando nome.cognome")
            HelpItem(icon = Icons.Default.TextFields, text = "Controlla maiuscole/minuscole")
            HelpItem(icon = Icons.Default.Edit, text = "Evita spazi iniziali/finali nei campi")

            Spacer(modifier = Modifier.height(24.dp))
            Text("Recupero credenziali", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Hai problemi d'accesso? Possiamo aiutarti:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = {
                    val uri = Uri.parse("mailto:info@laba.biz?subject=Assistenza%20accesso&body=Nome%20e%20cognome%3A%0AMatricola%3A%0ADispositivo%3A%20Android%0A%0ADescrizione%20problema%3A%0A")
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            ) {
                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Richiedi reset password")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Link utili", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.laba.biz"))) }) {
                Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sito LABA")
            }
            TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:0556530786"))) }) {
                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chiama la segreteria didattica")
            }
            TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:3343824934"))) }) {
                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Contatta il supporto IT")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HelpItem(icon: ImageVector, text: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
