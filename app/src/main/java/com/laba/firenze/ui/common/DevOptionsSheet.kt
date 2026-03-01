package com.laba.firenze.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laba.firenze.ui.theme.*

// Helper for Dev Options
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevOptionsSheet(
    currentVersion: String,
    onSelectVersion: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Developer Options 🛠️",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text("Select API Environment:", style = MaterialTheme.typography.titleSmall)
            
            Row(
                modifier = Modifier.fillMaxWidth().clickable { 
                    onSelectVersion("v2")
                    restartApp(context)
                }.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = currentVersion == "v2", onClick = null)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("v2 (Stable)", fontWeight = FontWeight.Bold)
                    Text("api/api", style = MaterialTheme.typography.bodySmall)
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().clickable { 
                    onSelectVersion("v3")
                    restartApp(context)
                }.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = currentVersion == "v3", onClick = null)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("v3 (Test)", fontWeight = FontWeight.Bold)
                    Text("api-test/api", style = MaterialTheme.typography.bodySmall)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            Text("App will restart upon selection.", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

fun restartApp(context: android.content.Context) {
    val packageManager = context.packageManager
    val intent = packageManager.getLaunchIntentForPackage(context.packageName)
    val componentName = intent?.component
    val mainIntent = android.content.Intent.makeRestartActivityTask(componentName)
    context.startActivity(mainIntent)
    Runtime.getRuntime().exit(0)
}
