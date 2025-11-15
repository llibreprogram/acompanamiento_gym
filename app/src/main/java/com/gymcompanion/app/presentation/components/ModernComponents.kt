package com.gymcompanion.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Card moderno con glassmorphism y animación
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: Dp = 4.dp,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    gradient: Brush? = null,
    glassEffect: Boolean = false,
    animateOnClick: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed && animateOnClick) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_scale"
    )
    
    val cardModifier = modifier
        .scale(scale)
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false
        )
        .clip(shape)
        .then(
            if (gradient != null) {
                Modifier.background(gradient)
            } else if (glassEffect) {
                Modifier.background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            containerColor.copy(alpha = 0.7f),
                            containerColor.copy(alpha = 0.5f)
                        )
                    )
                )
            } else {
                Modifier.background(containerColor)
            }
        )
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    isPressed = false
                    onClick()
                }
            } else {
                Modifier
            }
        )
    
    Card(
        modifier = cardModifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

/**
 * Botón moderno con animación y gradiente opcional
 */
@Composable
fun ModernButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    icon: ImageVector? = null,
    gradient: Brush? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    loading: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "button_scale"
    )
    
    Button(
        onClick = {
            if (!loading) {
                isPressed = false
                onClick()
            }
        },
        modifier = modifier
            .scale(scale)
            .height(54.dp),
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (gradient != null) Color.Transparent else containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (gradient != null) {
                        Modifier.background(gradient, RoundedCornerShape(16.dp))
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = contentColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Card con estadística animada
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    iconBackgroundColor: Color = MaterialTheme.colorScheme.primary,
    iconColor: Color = MaterialTheme.colorScheme.onPrimary,
    animate: Boolean = true
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        if (animate) {
            kotlinx.coroutines.delay(100)
            visible = true
        }
    }
    
    AnimatedVisibility(
        visible = !animate || visible,
        enter = fadeIn(animationSpec = tween(500)) + 
                slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
    ) {
        ModernCard(
            modifier = modifier,
            containerColor = containerColor,
            contentColor = contentColor,
            elevation = 3.dp,
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    subtitle?.let {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(iconBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

/**
 * Card de acción rápida con ícono grande
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradient: Brush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    )
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "quick_action_scale"
    )
    
    Card(
        onClick = {
            isPressed = false
            onClick()
        },
        modifier = modifier
            .scale(scale)
            .height(120.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

/**
 * Divider moderno con gradiente
 */
@Composable
fun ModernDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    gradient: Boolean = true
) {
    if (gradient) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(thickness)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    )
                )
        )
    } else {
        Divider(
            modifier = modifier,
            thickness = thickness,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    }
}

/**
 * Badge moderno con animación
 */
@Composable
fun ModernBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }
}

/**
 * Progress indicator circular moderno con texto interno
 */
@Composable
fun ModernCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
    value: String? = null,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeWidth: Dp = 8.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "progress"
    )
    
    Box(
        modifier = modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background circle
        CircularProgressIndicator(
            progress = 1f,
            modifier = Modifier.fillMaxSize(),
            color = backgroundColor,
            strokeWidth = strokeWidth
        )
        
        // Progress circle
        CircularProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier.fillMaxSize(),
            color = color,
            strokeWidth = strokeWidth
        )
        
        // Center text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            value?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            label?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
