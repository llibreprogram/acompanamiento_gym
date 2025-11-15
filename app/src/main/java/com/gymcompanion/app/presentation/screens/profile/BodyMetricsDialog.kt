package com.gymcompanion.app.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gymcompanion.app.data.local.entity.BodyMetricsEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dialog para capturar/editar datos corporales del usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyMetricsDialog(
    currentMetrics: BodyMetricsEntity?,
    currentUser: com.gymcompanion.app.data.local.entity.UserEntity?,
    onDismiss: () -> Unit,
    onSave: (
        weight: Double,
        height: Double,
        experienceLevel: String,
        bodyFatPercentage: Double?,
        chestMeasurement: Double?,
        waistMeasurement: Double?,
        hipsMeasurement: Double?,
        thighMeasurement: Double?,
        armMeasurement: Double?,
        calfMeasurement: Double?,
        notes: String?
    ) -> Unit,
    onUserDataUpdated: (name: String, gender: String, dateOfBirth: Long) -> Unit
) {
    // Estados del formulario - Datos de usuario
    var userName by remember { mutableStateOf(currentUser?.name ?: "Usuario") }
    var userGender by remember { mutableStateOf(currentUser?.gender ?: "other") }
    var userDateOfBirth by remember { mutableStateOf(currentUser?.dateOfBirth ?: System.currentTimeMillis() - (25 * 365 * 24 * 60 * 60 * 1000L)) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Estados del formulario - Métricas corporales
    var weight by remember { mutableStateOf(currentMetrics?.weight?.toString() ?: "") }
    var height by remember { mutableStateOf(currentMetrics?.height?.toString() ?: "") }
    var bodyFat by remember { mutableStateOf(currentMetrics?.bodyFatPercentage?.toString() ?: "") }
    var experienceLevel by remember { mutableStateOf(currentMetrics?.experienceLevel ?: "beginner") }
    var showAdvancedMeasures by remember { mutableStateOf(false) }
    
    // Medidas avanzadas
    var chest by remember { mutableStateOf(currentMetrics?.chestMeasurement?.toString() ?: "") }
    var waist by remember { mutableStateOf(currentMetrics?.waistMeasurement?.toString() ?: "") }
    var hips by remember { mutableStateOf(currentMetrics?.hipsMeasurement?.toString() ?: "") }
    var thigh by remember { mutableStateOf(currentMetrics?.thighMeasurement?.toString() ?: "") }
    var arm by remember { mutableStateOf(currentMetrics?.armMeasurement?.toString() ?: "") }
    var calf by remember { mutableStateOf(currentMetrics?.calfMeasurement?.toString() ?: "") }
    var notes by remember { mutableStateOf(currentMetrics?.notes ?: "") }
    
    // Validaciones
    var weightError by remember { mutableStateOf(false) }
    var heightError by remember { mutableStateOf(false) }
    
    // IMC calculado
    val calculatedBMI = remember(weight, height) {
        try {
            val w = weight.toDoubleOrNull()
            val h = height.toDoubleOrNull()
            if (w != null && h != null && h > 0) {
                BodyMetricsEntity.calculateBMI(w, h)
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (currentMetrics == null) "Configurar Datos Corporales" else "Actualizar Datos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Datos personales
                Text(
                    text = "👤 Datos Personales",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Nombre
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Género
                Text(
                    text = "Género",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = userGender == "male",
                        onClick = { userGender = "male" },
                        label = { Text("Hombre") },
                        modifier = Modifier.weight(1f),
                        leadingIcon = if (userGender == "male") {
                            { Text("♂️") }
                        } else null
                    )
                    FilterChip(
                        selected = userGender == "female",
                        onClick = { userGender = "female" },
                        label = { Text("Mujer") },
                        modifier = Modifier.weight(1f),
                        leadingIcon = if (userGender == "female") {
                            { Text("♀️") }
                        } else null
                    )
                    FilterChip(
                        selected = userGender == "other",
                        onClick = { userGender = "other" },
                        label = { Text("Otro") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Fecha de nacimiento
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fecha de Nacimiento: ${formatDate(userDateOfBirth)}")
                }
                
                val calculatedAge = calculateAge(userDateOfBirth)
                Text(
                    text = "Edad: $calculatedAge años",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Divider()
                
                // Datos básicos obligatorios
                Text(
                    text = "📊 Datos Corporales (Obligatorios)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Peso
                OutlinedTextField(
                    value = weight,
                    onValueChange = { 
                        weight = it
                        weightError = false
                    },
                    label = { Text("Peso (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = weightError,
                    supportingText = if (weightError) {
                        { Text("Ingresa un peso válido (30-300 kg)") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Altura
                OutlinedTextField(
                    value = height,
                    onValueChange = { 
                        height = it
                        heightError = false
                    },
                    label = { Text("Altura (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = heightError,
                    supportingText = if (heightError) {
                        { Text("Ingresa una altura válida (100-250 cm)") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // IMC calculado
                calculatedBMI?.let { bmi ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "IMC Calculado: ${String.format("%.1f", bmi)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = BodyMetricsEntity.interpretBMI(bmi),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                // Nivel de experiencia
                Text(
                    text = "Nivel de Experiencia",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExperienceLevelOption(
                        level = "beginner",
                        label = "Principiante",
                        description = "Menos de 6 meses entrenando",
                        selected = experienceLevel == "beginner",
                        onClick = { experienceLevel = "beginner" }
                    )
                    ExperienceLevelOption(
                        level = "intermediate",
                        label = "Intermedio",
                        description = "6 meses a 2 años de experiencia",
                        selected = experienceLevel == "intermediate",
                        onClick = { experienceLevel = "intermediate" }
                    )
                    ExperienceLevelOption(
                        level = "advanced",
                        label = "Avanzado",
                        description = "Más de 2 años entrenando",
                        selected = experienceLevel == "advanced",
                        onClick = { experienceLevel = "advanced" }
                    )
                }
                
                Divider()
                
                // Datos opcionales
                Text(
                    text = "Datos Opcionales",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Porcentaje de grasa corporal
                OutlinedTextField(
                    value = bodyFat,
                    onValueChange = { bodyFat = it },
                    label = { Text("% Grasa Corporal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = { Text("Ej: 15.5") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Toggle para medidas avanzadas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Medidas Corporales Detalladas",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = showAdvancedMeasures,
                        onCheckedChange = { showAdvancedMeasures = it }
                    )
                }
                
                // Medidas avanzadas
                if (showAdvancedMeasures) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = chest,
                            onValueChange = { chest = it },
                            label = { Text("Pecho (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = waist,
                            onValueChange = { waist = it },
                            label = { Text("Cintura (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = hips,
                            onValueChange = { hips = it },
                            label = { Text("Cadera (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = thigh,
                            onValueChange = { thigh = it },
                            label = { Text("Muslos (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = arm,
                            onValueChange = { arm = it },
                            label = { Text("Brazos (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = calf,
                            onValueChange = { calf = it },
                            label = { Text("Pantorrillas (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Notas
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas (opcional)") },
                    placeholder = { Text("Ej: Lesión en rodilla derecha") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validar campos obligatorios
                    val w = weight.toDoubleOrNull()
                    val h = height.toDoubleOrNull()
                    
                    weightError = w == null || w < 30 || w > 300
                    heightError = h == null || h < 100 || h > 250
                    
                    if (!weightError && !heightError && w != null && h != null) {
                        // Guardar datos de usuario
                        onUserDataUpdated(userName, userGender, userDateOfBirth)
                        
                        // Guardar métricas corporales
                        onSave(
                            w,
                            h,
                            experienceLevel,
                            bodyFat.toDoubleOrNull(),
                            chest.toDoubleOrNull(),
                            waist.toDoubleOrNull(),
                            hips.toDoubleOrNull(),
                            thigh.toDoubleOrNull(),
                            arm.toDoubleOrNull(),
                            calf.toDoubleOrNull(),
                            notes.takeIf { it.isNotBlank() }
                        )
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
    
    // DatePicker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = userDateOfBirth
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            userDateOfBirth = millis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun ExperienceLevelOption(
    level: String,
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = if (selected) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RadioButton(
                selected = selected,
                onClick = onClick
            )
        }
    }
}

/**
 * Formatea un timestamp en formato de fecha legible
 */
private fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    return format.format(date)
}

/**
 * Calcula la edad a partir de un timestamp de fecha de nacimiento
 */
private fun calculateAge(dateOfBirthMillis: Long): Int {
    val birthDate = java.util.Date(dateOfBirthMillis)
    val today = java.util.Date()
    
    val birthYear = birthDate.year + 1900
    val currentYear = today.year + 1900
    
    var age = currentYear - birthYear
    
    // Ajustar si aún no ha cumplido años este año
    val birthMonth = birthDate.month
    val currentMonth = today.month
    if (currentMonth < birthMonth || (currentMonth == birthMonth && today.date < birthDate.date)) {
        age--
    }
    
    return age
}
