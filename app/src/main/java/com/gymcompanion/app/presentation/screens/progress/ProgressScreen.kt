package com.gymcompanion.app.presentation.screens.progress

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla de progreso
 * Visualiza el progreso del usuario con gráficos y estadísticas
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
        topBar = {
            TopAppBar(
                title = { Text("Mi Progreso") }
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Completa tu primer entrenamiento",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Registra métricas corporales para ver tu progreso aquí",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                // Estadísticas totales
                item {
                    Text(
                        text = "Estadísticas Totales",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    TotalStatsCards(
                        totalStats = totalStats,
                        viewModel = viewModel
                    )
                }
                
                // Gráfico de peso
                if (recentBodyMetrics.isNotEmpty()) {
                    item {
                        Text(
                            text = "Peso Corporal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    item {
                        WeightChartCard(
                            data = viewModel.getWeightChartData(),
                            weightChange = weightChange
                        )
                    }
                    
                    // Gráfico de IMC
                    item {
                        Text(
                            text = "Índice de Masa Corporal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    item {
                        BmiChartCard(
                            data = viewModel.getBmiChartData()
                        )
                    }
                }
                
                // Gráfico de volumen
                if (recentSessions.isNotEmpty()) {
                    item {
                        Text(
                            text = "Volumen de Entrenamiento",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    item {
                        VolumeChartCard(
                            data = viewModel.getVolumeChartData()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Cards con estadísticas totales
 */
@Composable
fun TotalStatsCards(
    totalStats: TotalStats,
    viewModel: ProgressViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Entrenamientos",
            value = totalStats.totalWorkouts.toString(),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Volumen Total",
            value = viewModel.formatVolume(totalStats.totalVolume),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Tiempo Total",
            value = viewModel.formatTime(totalStats.totalTime),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Card con gráfico de peso
 */
@Composable
fun WeightChartCard(
    data: List<Pair<Long, Float>>,
    weightChange: WeightChange?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header con cambio
            weightChange?.let { change ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Último mes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (change.change >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (change.change >= 0) Color(0xFFF44336) else Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = String.format("%.1f kg (%.1f%%)", 
                                kotlin.math.abs(change.change), 
                                kotlin.math.abs(change.percentage)
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (change.change >= 0) Color(0xFFF44336) else Color(0xFF4CAF50)
                        )
                    }
                }
            }
            
            if (data.size >= 2) {
                SimpleLineChart(
                    data = data,
                    label = "Peso (kg)",
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "Necesitas al menos 2 registros para ver el gráfico",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        }
    }
}

/**
 * Card con gráfico de volumen
 */
@Composable
fun VolumeChartCard(data: List<Pair<Long, Float>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (data.size >= 2) {
                SimpleLineChart(
                    data = data,
                    label = "Volumen (kg)",
                    color = Color(0xFF4CAF50)
                )
            } else {
                Text(
                    text = "Completa al menos 2 entrenamientos para ver el gráfico",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        }
    }
}

/**
 * Card con gráfico de IMC
 */
@Composable
fun BmiChartCard(data: List<Pair<Long, Float>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (data.size >= 2) {
                SimpleLineChart(
                    data = data,
                    label = "IMC",
                    color = Color(0xFFFF9800)
                )
            } else {
                Text(
                    text = "Necesitas al menos 2 registros para ver el gráfico",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        }
    }
}

/**
 * Gráfico de línea simple con Vico
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
