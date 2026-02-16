package com.gymcompanion.app.presentation.components

import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Gráfico interactivo moderno hecho 100% en Compose
 */
@Composable
fun InteractiveChart(
    dataPoints: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    gradientColors: List<Color> = listOf(
        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.primary.copy(alpha = 0.0f)
    )
) {
    if (dataPoints.isEmpty()) return

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    
    // Animation state
    var animationProgress by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        ) { value, _ ->
            animationProgress = value
        }
    }

    val density = LocalDensity.current
    val textPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.GRAY
            textAlign = Paint.Align.CENTER
            textSize = density.run { 10.sp.toPx() }
        }
    }

    Column(modifier = modifier) {
        // Header with selected value
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progreso de Volumen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (selectedIndex != null) {
                val (label, value) = dataPoints[selectedIndex!!]
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${value.toInt()} lbs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = lineColor
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val stepX = size.width / (dataPoints.size - 1)
                            val index = (offset.x / stepX).roundToInt().coerceIn(0, dataPoints.size - 1)
                            selectedIndex = index
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val spacing = width / (dataPoints.size - 1)
                
                val maxValue = dataPoints.maxOf { it.second } * 1.2f
                val minValue = 0f

                val points = dataPoints.mapIndexed { index, pair ->
                    val x = index * spacing
                    val y = height - ((pair.second - minValue) / (maxValue - minValue) * height) * animationProgress
                    Offset(x, y)
                }

                // Draw gradient path
                val path = Path().apply {
                    moveTo(points.first().x, height)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, height)
                    close()
                }

                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(gradientColors),
                )

                // Draw line
                val linePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    // Use cubic bezier for smooth curve
                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val controlPoint1 = Offset((p1.x + p2.x) / 2, p1.y)
                        val controlPoint2 = Offset((p1.x + p2.x) / 2, p2.y)
                        cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p2.x, p2.y)
                    }
                }

                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw points and selection
                points.forEachIndexed { index, point ->
                    // Draw selection indicator
                    if (selectedIndex == index) {
                        drawLine(
                            color = lineColor.copy(alpha = 0.5f),
                            start = Offset(point.x, 0f),
                            end = Offset(point.x, height),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )
                        
                        drawCircle(
                            color = Color.White,
                            radius = 6.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            color = lineColor,
                            radius = 4.dp.toPx(),
                            center = point
                        )
                    } else if (index == points.lastIndex) {
                        // Always show last point if nothing selected
                        drawCircle(
                            color = lineColor,
                            radius = 3.dp.toPx(),
                            center = point
                        )
                    }
                }
            }
        }
    }
}
