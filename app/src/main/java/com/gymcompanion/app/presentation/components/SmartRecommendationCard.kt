package com.gymcompanion.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gymcompanion.app.domain.usecase.OvertrainingRisk
import com.gymcompanion.app.domain.usecase.RiskLevel
import com.gymcompanion.app.domain.usecase.SessionPrediction

/**
 * Tarjeta de recomendación inteligente
 * Prioriza alertas de sobreentrenamiento sobre predicciones de rendimiento
 */
@Composable
fun SmartRecommendationCard(
    overtrainingRisk: OvertrainingRisk?,
    sessionPrediction: SessionPrediction?,
    modifier: Modifier = Modifier
) {
    val isRiskHigh = overtrainingRisk?.level == RiskLevel.HIGH || 
                     overtrainingRisk?.level == RiskLevel.MEDIUM
    
    val (icon, color, title, message) = when {
        isRiskHigh -> {
            val riskColor = if (overtrainingRisk?.level == RiskLevel.HIGH) 
                Color(0xFFF44336) else Color(0xFFFF9800)
            
            Quadruple(
                Icons.Default.Warning,
                riskColor,
                "Atención Requerida",
                overtrainingRisk?.message ?: ""
            )
        }
        else -> {
            Quadruple(
                Icons.Default.AutoAwesome,
                MaterialTheme.colorScheme.primary,
                "Insight Inteligente",
                sessionPrediction?.recommendation ?: "Sigue entrenando para obtener predicciones."
            )
        }
    }

    ModernCard(
        modifier = modifier,
        containerColor = color.copy(alpha = 0.1f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
