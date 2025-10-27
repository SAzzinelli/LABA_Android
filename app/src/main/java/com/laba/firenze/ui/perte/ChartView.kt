package com.laba.firenze.ui.perte

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun ChartView(
    avgPoints: List<Double>,
    examTitles: List<String>,
    examDates: List<String>,
    examGrades: List<Int>,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableIntStateOf(-1) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (avgPoints.isEmpty()) {
                EmptyChartContent()
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Chart title and current average
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Andamento della media",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (avgPoints.isNotEmpty()) {
                            Text(
                                text = "Media attuale: ${String.format("%.2f", avgPoints.lastOrNull() ?: 0.0)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Chart canvas
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val secondaryColor = MaterialTheme.colorScheme.secondary
                    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .pointerInput(avgPoints) {
                                detectTapGestures { offset ->
                                    val tappedIndex = getTappedIndex(offset, Size(size.width.toFloat(), size.height.toFloat()), avgPoints.size)
                                    selectedIndex = if (tappedIndex == selectedIndex) -1 else tappedIndex
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawGradeChart(
                                avgPoints = avgPoints,
                                selectedIndex = selectedIndex,
                                canvasSize = Size(size.width.toFloat(), size.height.toFloat()),
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                gridColor = gridColor,
                                labelColor = labelColor
                            )
                        }
                    }
                    
                    // Selected point info
                    if (selectedIndex >= 0 && selectedIndex < avgPoints.size) {
                        SelectedPointInfo(
                            avgPoints = avgPoints,
                            examTitles = examTitles,
                            examDates = examDates,
                            selectedIndex = selectedIndex
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedPointInfo(
    avgPoints: List<Double>,
    examTitles: List<String>,
    examDates: List<String>,
    selectedIndex: Int
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Esame ${selectedIndex + 1}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            if (selectedIndex < examTitles.size) {
                Text(
                    text = examTitles[selectedIndex],
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 2
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Media: ${String.format("%.2f", avgPoints[selectedIndex])}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                if (selectedIndex < examDates.size) {
                    Text(
                        text = examDates[selectedIndex],
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

private fun getTappedIndex(offset: Offset, canvasSize: Size, pointCount: Int): Int {
    if (pointCount <= 1) return -1
    
    val chartWidth = canvasSize.width
    val pointSpacing = chartWidth / (pointCount - 1)
    val tappedX = offset.x
    
    // Find closest point
    var closestIndex = 0
    var minDistance = Float.MAX_VALUE
    
    for (i in 0 until pointCount) {
        val pointX = i * pointSpacing
        val distance = abs(tappedX - pointX)
        if (distance < minDistance) {
            minDistance = distance
            closestIndex = i
        }
    }
    
    // Only select if tap is close enough (within 50dp equivalent)
    return if (minDistance < 50f) closestIndex else -1
}

private fun DrawScope.drawGradeChart(
    avgPoints: List<Double>,
    selectedIndex: Int,
    canvasSize: Size,
    primaryColor: Color,
    secondaryColor: Color,
    gridColor: Color,
    labelColor: Color
) {
    if (avgPoints.isEmpty()) return
    
    val padding = 40f
    val chartWidth = canvasSize.width - (padding * 2)
    val chartHeight = canvasSize.height - (padding * 2)
    
    // Calculate min/max for Y axis
    val minAvg = avgPoints.minOrNull() ?: 0.0
    val maxAvg = avgPoints.maxOrNull() ?: 30.0
    val avgRange = maxAvg - minAvg
    val yPadding = if (avgRange > 0) avgRange * 0.1 else 1.0 // 10% padding
    
    val yMin = (minAvg - yPadding).toFloat()
    val yMax = (maxAvg + yPadding).toFloat()
    
    // Draw grid lines
    drawGridLines(canvasSize, padding, chartWidth, chartHeight, yMin, yMax, gridColor)
    
    // Draw data points and line
    drawDataLine(avgPoints, padding, chartWidth, chartHeight, yMin, yMax, selectedIndex, primaryColor, secondaryColor)
    
    // Draw Y-axis labels
    drawYAxisLabels(padding, chartHeight, yMin, yMax, labelColor)
}

private fun DrawScope.drawGridLines(
    canvasSize: Size,
    padding: Float,
    chartWidth: Float,
    chartHeight: Float,
    yMin: Float,
    yMax: Float,
    gridColor: Color
) {
    
    // Horizontal grid lines
    val horizontalLines = 4
    for (i in 0..horizontalLines) {
        val y = padding + (i * chartHeight / horizontalLines)
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(padding + chartWidth, y),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
        )
    }
    
    // Vertical grid lines
    val verticalLines = 5
    for (i in 0..verticalLines) {
        val x = padding + (i * chartWidth / verticalLines)
        drawLine(
            color = gridColor,
            start = Offset(x, padding),
            end = Offset(x, padding + chartHeight),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
        )
    }
}

private fun DrawScope.drawDataLine(
    avgPoints: List<Double>,
    padding: Float,
    chartWidth: Float,
    chartHeight: Float,
    yMin: Float,
    yMax: Float,
    selectedIndex: Int,
    primaryColor: Color,
    secondaryColor: Color
) {
    if (avgPoints.size < 2) return
    
    val pointSpacing = chartWidth / (avgPoints.size - 1)
    
    // Create path for the line
    val path = Path()
    val points = mutableListOf<Offset>()
    
    // Calculate all points
    for (i in avgPoints.indices) {
        val x = padding + (i * pointSpacing)
        val y = padding + chartHeight - ((avgPoints[i].toFloat() - yMin) / (yMax - yMin) * chartHeight)
        points.add(Offset(x, y))
    }
    
    // Draw gradient fill under the line
    val fillPath = Path().apply {
        moveTo(points.first().x, padding + chartHeight)
        points.forEach { point ->
            lineTo(point.x, point.y)
        }
        lineTo(points.last().x, padding + chartHeight)
        close()
    }
    
    val gradient = Brush.verticalGradient(
        colors = listOf(
            primaryColor.copy(alpha = 0.3f),
            primaryColor.copy(alpha = 0.1f)
        ),
        startY = padding,
        endY = padding + chartHeight
    )
    
    drawPath(fillPath, gradient)
    
    // Draw the line
    path.moveTo(points.first().x, points.first().y)
    for (i in 1 until points.size) {
        path.lineTo(points[i].x, points[i].y)
    }
    
    drawPath(
        path = path,
        color = primaryColor,
        style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    
    // Draw points
    points.forEachIndexed { index, point ->
        val isSelected = index == selectedIndex
        val pointColor = if (isSelected) secondaryColor else primaryColor
        val pointRadius = if (isSelected) 8f else 5f
        
        drawCircle(
            color = pointColor,
            radius = pointRadius,
            center = point
        )
        
        // Draw white center for selected point
        if (isSelected) {
            drawCircle(
                color = Color.White,
                radius = 3f,
                center = point
            )
        }
    }
}

private fun DrawScope.drawYAxisLabels(
    padding: Float,
    chartHeight: Float,
    yMin: Float,
    yMax: Float,
    labelColor: Color
) {
    val horizontalLines = 4
    
    for (i in 0..horizontalLines) {
        val y = padding + (i * chartHeight / horizontalLines)
        val value = yMax - (i * (yMax - yMin) / horizontalLines)
        
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#${Integer.toHexString(labelColor.toArgb()).substring(2)}")
                textSize = 24f // 12sp equivalent
                textAlign = android.graphics.Paint.Align.RIGHT
                isAntiAlias = true
            }
            
            drawText(
                String.format("%.1f", value),
                padding - 8f,
                y + 4f, // Adjust for text baseline
                paint
            )
        }
    }
}

@Composable
private fun EmptyChartContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Nessun voto disponibile",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Quando avrai esami con voto registrato, vedrai qui l'andamento.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

