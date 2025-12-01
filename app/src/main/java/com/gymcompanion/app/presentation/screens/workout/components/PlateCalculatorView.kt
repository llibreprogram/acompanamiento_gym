package com.gymcompanion.app.presentation.screens.workout.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymcompanion.app.domain.usecase.PlateCalculator

/**
 * Visualización de la calculadora de discos
 */
@Composable
fun PlateCalculatorView(
    targetWeight: Double,
    equipmentType: String,
    plateCalculator: PlateCalculator,
    modifier: Modifier = Modifier
) {
    val equipment = remember(equipmentType) {
        plateCalculator.getEquipmentType(equipmentType)
    }
    
    val loadout = remember(targetWeight, equipment) {
        plateCalculator.calculatePlates(targetWeight, equipment)
    }
    
    // Solo mostrar para barbell y dumbbell
    val shouldShow = equipment == PlateCalculator.EquipmentType.BARBELL || 
                     equipment == PlateCalculator.EquipmentType.DUMBBELL
    
    AnimatedVisibility(
        visible = shouldShow && targetWeight > 0,
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (equipment) {
                            PlateCalculator.EquipmentType.BARBELL -> "Configuración de Discos"
                            PlateCalculator.EquipmentType.DUMBBELL -> "Peso por Mancuerna"
                            else -> "Peso"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (equipment) {
                            PlateCalculator.EquipmentType.DUMBBELL -> "${loadout.weightPerHand}kg c/u"
                            else -> "${loadout.totalWeight}kg"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Visual representation - solo para barbell
                if (equipment == PlateCalculator.EquipmentType.BARBELL) {
                    if (loadout.platesPerSide.isNotEmpty()) {
                        BarVisualization(loadout = loadout)
                    } else {
                        // Just show the bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(8.dp)
                                    .background(MaterialTheme.colorScheme.outline),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    
                    // Text breakdown
                    if (loadout.platesPerSide.isNotEmpty()) {
                        Text(
                            text = "Por lado: ${plateCalculator.formatPlates(loadout.platesPerSide)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    } else {
                        Text(
                            text = "Solo barra (${loadout.barWeight}kg)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    
                    if (!loadout.isExact && loadout.difference > 0.1) {
                        Text(
                            text = "⚠️ Faltan ${String.format("%.2f", loadout.difference)}kg para alcanzar el objetivo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else if (equipment == PlateCalculator.EquipmentType.DUMBBELL) {
                    // Para mancuernas, mostrar iconos de mancuernas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "${loadout.weightPerHand}kg",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Peso total: ${loadout.totalWeight}kg (${loadout.weightPerHand}kg × 2)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Visualización de la barra con los discos
 */
@Composable
fun BarVisualization(loadout: PlateCalculator.PlateLoadout) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Discos lado izquierdo
        PlateStack(plates = loadout.platesPerSide.reversed())
        
        // Barra central
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(8.dp)
                .background(MaterialTheme.colorScheme.outline),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Discos lado derecho
        PlateStack(plates = loadout.platesPerSide)
    }
}

/**
 * Stack de discos en un lado de la barra
 */
@Composable
fun PlateStack(plates: List<Double>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy((-4).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        plates.forEach { weight ->
            PlateVisual(weight = weight)
        }
    }
}

/**
 * Representación visual de un disco individual
 */
@Composable
fun PlateVisual(weight: Double) {
    val (height, color) = when {
        weight >= 20.0 -> 56.dp to Color(0xFFE53935) // Rojo - discos grandes
        weight >= 10.0 -> 48.dp to Color(0xFF1E88E5) // Azul - discos medianos
        weight >= 5.0 -> 40.dp to Color(0xFFFDD835)  // Amarillo - discos pequeños
        weight >= 2.0 -> 32.dp to Color(0xFF43A047)  // Verde - discos muy pequeños
        else -> 24.dp to Color(0xFF757575)           // Gris - discos mínimos
    }
    
    Box(
        modifier = Modifier
            .width(12.dp)
            .height(height)
            .clip(RoundedCornerShape(2.dp))
            .background(color)
            .border(1.dp, Color.Black.copy(alpha = 0.3f), RoundedCornerShape(2.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (weight >= 5.0) {
            Text(
                text = weight.toString().replace(".0", ""),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(1.dp)
            )
        }
    }
}
