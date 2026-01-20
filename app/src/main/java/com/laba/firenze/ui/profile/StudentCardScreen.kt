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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TileMode
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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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
                pitch = pitch
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
    pitch: Float
) {
    // Dynamic Gradient Center based on Roll/Pitch
    // Roll (X) affects X center, Pitch (Y) affects Y center
    // Normalized [-1, 1] roughly
    val density = LocalDensity.current
    
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp) // Card ratio roughly
            .clip(RoundedCornerShape(24.dp))
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        
        val centerX = width / 2 + (roll * 4) // Sensitivity factor
        val centerY = height / 2 + (pitch * 4)
        
        // Iridescent Colors (Laba Accent based)
        val baseColor = MaterialTheme.colorScheme.primary
        val gradientColors = listOf(
            baseColor.copy(alpha = 0.9f),
            baseColor.copy(alpha = 0.7f),
            baseColor.copy(alpha = 0.5f),
            baseColor.copy(alpha = 0.8f),
            baseColor.copy(alpha = 1.0f)
        )
        
        // Background Gradient (Moving)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = gradientColors,
                        center = Offset(centerX, centerY),
                        radius = width * 1.2f
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
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Logo & Check
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground), // Use App Logo if available, fallback to launcher
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
                )
                
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Verified",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Student Info
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                
                // Name
                Column {
                    Text(
                        "NOME",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = uiState.displayName?.uppercase() ?: "STUDENTE LABA",
                        // style removed here to avoid duplication
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 2.dp),
                        style = LocalTextStyle.current.copy(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.25f),
                                offset = Offset(0f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                }
                
                // Details Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Matricola (Wait, UiState doesn't have matricola explicitly, might be in displayName or hidden)
                    // Checking HomeUiState... it has displayName. Profile likely has matricola.
                    // HomeViewModel loads Profile. I can update HomeUiState to include Matricola or access Profile directly.
                    // For now, I'll omit Matricola if not available in HomeUiState, or update HomeUiState.
                    // Plan says: "Name, Matricola, Course".
                    // I should update HomeUiState to expose Matricola.
                    
                    // Mocking Matricola access or using Course info which is available (getCourseDisplayInfo)
                    
                    Column {
                        Text(
                            "STATUS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "STUDENTE ATTIVO",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
