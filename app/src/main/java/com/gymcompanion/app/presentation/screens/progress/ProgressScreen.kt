package com.gymcompanion.app.presentation.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.gymcompanion.app.presentation.components.*
import com.gymcompanion.app.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 📊 PROGRESS SCREEN — Premium Dark Neon Design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val recentBodyMetrics by viewModel.recentBodyMetrics.collectAsState()
    val recentSessions by viewModel.recentSessions.collectAsState()
    val totalStats by viewModel.totalStats.collectAsState()
    val weightChange = viewModel.getWeightChange()

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mi Progreso",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = DarkSurface.copy(alpha = 0.95f)
                )
            )
        }
    ) { paddingValues ->
        if (recentSessions.isEmpty() && recentBodyMetrics.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    // Neon icon
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        NeonGreen.copy(alpha = 0.15f),
                                        NeonBlue.copy(alpha = 0.15f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Text(
                        text = "Completa tu primer entrenamiento",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = TextPrimary
                    )
                    Text(
                        text = "Registra métricas corporales para ver tu progreso aquí",
                        textAlign = TextAlign.Center,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Total stats
                item {
                    AnimatedEntrance(index = 0) {
                        SectionHeader(title = "📈 Estadísticas Totales")
                    }
                }

                item {
                    AnimatedEntrance(index = 1) {
                        TotalStatsCards(
                            totalStats = totalStats,
                            viewModel = viewModel
                        )
                    }
                }

                // Weight chart
                if (recentBodyMetrics.isNotEmpty()) {
                    item {
                        AnimatedEntrance(index = 2) {
                            SectionHeader(title = "⚖️ Peso Corporal")
                        }
                    }

                    item {
                        AnimatedEntrance(index = 3) {
                            WeightChartCard(
                                data = viewModel.getWeightChartData(),
                                weightChange = weightChange
                            )
                        }
                    }

                    // BMI chart
                    item {
                        AnimatedEntrance(index = 4) {
                            SectionHeader(title = "📐 Índice de Masa Corporal")
                        }
                    }

                    item {
                        AnimatedEntrance(index = 5) {
                            BmiChartCard(
                                data = viewModel.getBmiChartData()
                            )
                        }
                    }
                }

                // Volume chart
                if (recentSessions.isNotEmpty()) {
                    item {
                        AnimatedEntrance(index = 6) {
                            SectionHeader(title = "💪 Volumen de Entrenamiento")
                        }
                    }

                    item {
                        AnimatedEntrance(index = 7) {
                            VolumeChartCard(
                                data = viewModel.getVolumeChartData()
                            )
                        }
                    }
                }

                // Bottom spacing
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

/**
 * Total stats with glassmorphic mini-cards
 */
@Composable
fun TotalStatsCards(
    totalStats: TotalStats,
    viewModel: ProgressViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        NeonStatMiniCard(
            title = "Sesiones",
            value = totalStats.totalWorkouts.toString(),
            accentColor = NeonBlue,
            modifier = Modifier.weight(1f)
        )
        NeonStatMiniCard(
            title = "Volumen",
            value = viewModel.formatVolume(totalStats.totalVolume),
            accentColor = NeonGreen,
            modifier = Modifier.weight(1f)
        )
        NeonStatMiniCard(
            title = "Tiempo",
            value = viewModel.formatTime(totalStats.totalTime),
            accentColor = NeonPurple,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun NeonStatMiniCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                fontSize = 16.sp
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Keep old name as alias
@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) =
    NeonStatMiniCard(title = title, value = value, accentColor = NeonBlue, modifier = modifier)

/**
 * Weight chart card with glassmorphic styling
 */
@Composable
fun WeightChartCard(
    data: List<Pair<Long, Float>>,
    weightChange: WeightChange?
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Weight change header
            weightChange?.let { change ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Último mes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (change.change >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (change.change >= 0) NeonPink else NeonGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = String.format("%.1f lbs (%.1f%%)",
                                kotlin.math.abs(change.change),
                                kotlin.math.abs(change.percentage)
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (change.change >= 0) NeonPink else NeonGreen
                        )
                    }
                }
            }

            if (data.size >= 2) {
                SimpleLineChart(
                    data = data,
                    label = "Peso (lbs)",
                    color = NeonBlue
                )
            } else {
                Text(
                    text = "Necesitas al menos 2 registros para ver el gráfico",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        }
    }
}

/**
 * Volume chart card
 */
@Composable
fun VolumeChartCard(data: List<Pair<Long, Float>>) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (data.size >= 2) {
                SimpleLineChart(
                    data = data,
                    label = "Volumen (lbs)",
                    color = NeonGreen
                )
            } else {
                Text(
                    text = "Completa al menos 2 entrenamientos para ver el gráfico",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        }
    }
}

/**
 * BMI chart card
 */
@Composable
fun BmiChartCard(data: List<Pair<Long, Float>>) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (data.size >= 2) {
                SimpleLineChart(
                    data = data,
                    label = "IMC",
                    color = NeonOrange
                )
            } else {
                Text(
                    text = "Necesitas al menos 2 registros para ver el gráfico",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        }
    }
}

/**
 * Simple line chart (Vico)
 */
@Composable
fun SimpleLineChart(
    data: List<Pair<Long, Float>>,
    label: String,
    color: Color
) {
    if (data.isEmpty()) return

    val entries = data.mapIndexed { index, pair ->
        index.toFloat() to pair.second
    }

    val chartEntryModel = entryModelOf(*entries.toTypedArray())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Chart(
            chart = lineChart(),
            model = chartEntryModel,
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis()
        )
    }
}
