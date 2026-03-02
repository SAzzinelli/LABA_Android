package com.laba.firenze.ui.profile

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.laba.firenze.R
import com.laba.firenze.ui.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCardScreen(
    navController: androidx.navigation.NavController? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Sensor Data for Iridescent Effect
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    var roll by remember { mutableStateOf(0f) }
    var pitch by remember { mutableStateOf(0f) }
    
    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    // Accelerometer: x, y, z
                    // Simple approximation for roll/pitch impact on visual
                    // Limit values to avoid extreme movement
                    val newRoll = (it.values[0] * 2).coerceIn(-45f, 45f) // X axis
                    val newPitch = (it.values[1] * 2).coerceIn(-45f, 45f) // Y axis
                    
                    // Smooth transition could be added here, but direct mapping is responsive
                    roll = newRoll
                    pitch = newPitch
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Tessera Studente") },
                navigationIcon = {
                    if (navController != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                "Badge Studente",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // The Card
            StudentVerificationBadge(
                uiState = uiState,
                roll = roll,
                pitch = pitch,
                viewModel = viewModel
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Info text
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Text(
                    "Mostra questa schermata all'esercente insieme al tuo documento. L'agevolazione è personale.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun StudentVerificationBadge(
    uiState: com.laba.firenze.ui.home.HomeUiState,
    roll: Float,
    pitch: Float,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // Ottieni i dati completi dal profilo
    val profile = viewModel.getUserProfile()
    val studentName = if (profile?.nome != null && profile?.cognome != null) {
        "${profile.nome} ${profile.cognome}"
    } else {
        profile?.displayName ?: uiState.displayName ?: "STUDENTE LABA"
    }
    
    val matricola = profile?.matricola
    val courseInfo = profile?.pianoStudi?.let { piano ->
        // Rimuovi "A.A." e pulisci
        piano.split("A.A.").firstOrNull()?.trim()?.replace(" - ", " ") ?: piano.trim()
    }
    val yearInfo = profile?.currentYear?.toIntOrNull()?.let { year ->
        when (year) {
            1 -> "1° ANNO"
            2 -> "2° ANNO"
            3 -> "3° ANNO"
            else -> "$year° ANNO"
        }
    }
    // Dynamic Gradient Center based on Roll/Pitch
    // Roll (X) affects X center, Pitch (Y) affects Y center
    // Normalized [-1, 1] roughly
    val density = LocalDensity.current
    
        BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(270.dp) // Altezza sufficiente per evitare testo tagliato (CORSO, MULTIMEDIA, ecc.)
            .clip(RoundedCornerShape(24.dp))
    ) {
        // Usa maxWidth e maxHeight dallo scope di BoxWithConstraints
        val width = with(density) { this@BoxWithConstraints.maxWidth.toPx() }
        val height = with(density) { this@BoxWithConstraints.maxHeight.toPx() }
        
        // Calcola il centro del gradiente in base a roll e pitch (come iOS)
        val centerX = width / 2 + (roll / 45.0f) * (width * 0.3f) // Sposta il centro in base a roll
        val centerY = height / 2 + (pitch / 45.0f) * (height * 0.3f) // Sposta il centro in base a pitch
        val scale = 2.0f // Scala del gradiente (come iOS)
        
        // Iridescent Colors (come iOS - basati su labaAccent con variazioni)
        val baseColor = MaterialTheme.colorScheme.primary
        // Variazioni più sottili e sobrie, mantenendo l'accent come base (come iOS)
        val gradientColors = listOf(
            baseColor.copy(alpha = 0.95f),
            baseColor.copy(alpha = 0.90f),
            baseColor.copy(alpha = 0.85f),
            baseColor.copy(alpha = 0.92f),
            baseColor.copy(alpha = 0.88f),
            baseColor.copy(alpha = 0.95f)
        )
        
        // Background Gradient (Moving - come iOS)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = gradientColors,
                        center = Offset(centerX, centerY),
                        radius = maxOf(width, height) * scale
                    )
                )
        )
        
        // Shiny Overlay (Specular reflection simulation)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.0f),
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.0f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(width, height)
                    )
                )
        )
        
        // Content (come iOS - padding orizzontale 16, top 12, bottom 20)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            // Header: Logo LABA & Checkmark (come iOS)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Logo LABA: solo il logo senza sfondo (bianco per badge blu)
                Image(
                    painter = painterResource(id = R.drawable.bianco),
                    contentDescription = "LABA Logo",
                    modifier = Modifier.size(52.dp),
                    contentScale = ContentScale.Fit
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Checkmark seal in alto a destra (come iOS)
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Verified",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Student Info (come iOS - verticale con tutti i campi)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Nome completo (nome + cognome)
                Column {
                    Text(
                        text = "NOME",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = studentName.uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 2,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 24.sp,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.3f),
                                offset = Offset(0f, 1f),
                                blurRadius = 2f
                            )
                        )
                    )
                }
                
                // Matricola
                Column {
                    Text(
                        text = "MATRICOLA",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = matricola?.takeIf { it.isNotBlank() } ?: "—",
                        fontWeight = FontWeight.SemiBold,
                        color = if (matricola?.isNotBlank() == true) Color.White else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 2.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.3f),
                                offset = Offset(0f, 1f),
                                blurRadius = 2f
                            )
                        )
                    )
                }
                
                // Corso e Anno (stessa riga)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Corso
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CORSO",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = courseInfo?.uppercase()?.takeIf { it.isNotBlank() } ?: "—",
                            fontWeight = FontWeight.SemiBold,
                            color = if (courseInfo?.isNotBlank() == true) Color.White else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 2.dp),
                            maxLines = 2,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    offset = Offset(0f, 1f),
                                    blurRadius = 2f
                                )
                            )
                        )
                    }
                    
                    // Anno
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ANNO",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = yearInfo?.uppercase()?.takeIf { it.isNotBlank() } ?: "—",
                            fontWeight = FontWeight.SemiBold,
                            color = if (yearInfo?.isNotBlank() == true) Color.White else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 2.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    offset = Offset(0f, 1f),
                                    blurRadius = 2f
                                )
                            )
                        )
                    }
                }
                
                // Status
                Column {
                    Text(
                        text = "STATUS",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "STUDENTE LABA",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 2.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.3f),
                                offset = Offset(0f, 1f),
                                blurRadius = 2f
                            )
                        )
                    )
                }
            }
        }
    }
}
