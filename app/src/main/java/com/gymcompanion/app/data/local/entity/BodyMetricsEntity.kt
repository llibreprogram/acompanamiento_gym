package com.gymcompanion.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que almacena las métricas corporales del usuario
 * Permite seguimiento histórico de cambios en composición corporal
 */
@Entity(
    tableName = "body_metrics",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"]), Index(value = ["recordedAt"])]
)
data class BodyMetricsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    
    // Datos básicos obligatorios
    val weight: Double, // en kg
    val height: Double, // en cm
    val experienceLevel: String, // "beginner", "intermediate", "advanced"
    
    // IMC calculado automáticamente
    val bmi: Double, // Calculado: weight / (height/100)^2
    
    // Composición corporal (opcional pero recomendado)
    val bodyFatPercentage: Double? = null,
    
    // Medidas corporales en cm (opcionales)
    val chestMeasurement: Double? = null,
    val waistMeasurement: Double? = null,
    val hipsMeasurement: Double? = null,
    val thighMeasurement: Double? = null,
    val armMeasurement: Double? = null,
    val calfMeasurement: Double? = null,
    
    // Metadata
    val recordedAt: Long = System.currentTimeMillis(),
    val notes: String? = null
) {
    companion object {
        /**
         * Calcula el IMC basándose en peso y altura
         */
        fun calculateBMI(weightKg: Double, heightCm: Double): Double {
            val heightM = heightCm / 100.0
            return weightKg / (heightM * heightM)
        }
        
        /**
         * Interpreta el IMC según rangos estándar OMS
         */
        fun interpretBMI(bmi: Double): String {
            return when {
                bmi < 18.5 -> "Bajo peso"
                bmi < 25.0 -> "Normal"
                bmi < 30.0 -> "Sobrepeso"
                bmi < 35.0 -> "Obesidad Clase I"
                bmi < 40.0 -> "Obesidad Clase II"
                else -> "Obesidad Clase III"
            }
        }
    }
}
