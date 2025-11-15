package com.gymcompanion.app.domain.util

import kotlin.math.max

/**
 * Utilidad para calcular métricas de salud personalizadas
 * 
 * Incluye:
 * - TMB (Tasa Metabólica Basal) usando Mifflin-St Jeor
 * - TDEE (Total Daily Energy Expenditure)
 * - Frecuencia cardíaca máxima y zonas de entrenamiento
 * - Calorías recomendadas según objetivo
 */
object HealthCalculator {
    
    /**
     * Calcula TMB usando la ecuación de Mifflin-St Jeor
     * Esta es la fórmula más precisa y actualizada
     * 
     * Hombres: TMB = 10 × peso(kg) + 6.25 × altura(cm) - 5 × edad(años) + 5
     * Mujeres: TMB = 10 × peso(kg) + 6.25 × altura(cm) - 5 × edad(años) - 161
     * 
     * @param weight Peso en kilogramos
     * @param height Altura en centímetros
     * @param age Edad en años
     * @param gender "male", "female", o "other" (usa promedio)
     * @return TMB en calorías/día
     */
    fun calculateBMR(
        weight: Double,
        height: Double,
        age: Int,
        gender: String
    ): Double {
        val baseBMR = (10 * weight) + (6.25 * height) - (5 * age)
        
        return when (gender) {
            "male" -> baseBMR + 5
            "female" -> baseBMR - 161
            else -> baseBMR - 78 // Promedio entre male y female
        }
    }
    
    /**
     * Niveles de actividad física según el Harris-Benedict
     */
    enum class ActivityLevel(val multiplier: Double, val description: String) {
        SEDENTARY(1.2, "Sedentario (poco o ningún ejercicio)"),
        LIGHTLY_ACTIVE(1.375, "Ligeramente activo (ejercicio ligero 1-3 días/semana)"),
        MODERATELY_ACTIVE(1.55, "Moderadamente activo (ejercicio moderado 3-5 días/semana)"),
        VERY_ACTIVE(1.725, "Muy activo (ejercicio intenso 6-7 días/semana)"),
        EXTREMELY_ACTIVE(1.9, "Extremadamente activo (ejercicio muy intenso, trabajo físico)")
    }
    
    /**
     * Calcula TDEE (Total Daily Energy Expenditure)
     * 
     * @param bmr Tasa metabólica basal
     * @param activityLevel Nivel de actividad física
     * @return TDEE en calorías/día
     */
    fun calculateTDEE(
        bmr: Double,
        activityLevel: ActivityLevel
    ): Double {
        return bmr * activityLevel.multiplier
    }
    
    /**
     * Objetivos de fitness y su ajuste calórico
     */
    enum class FitnessGoal(val calorieAdjustment: Double, val description: String) {
        WEIGHT_LOSS_AGGRESSIVE(-750.0, "Pérdida de peso agresiva (-750 kcal/día, ~0.75kg/semana)"),
        WEIGHT_LOSS_MODERATE(-500.0, "Pérdida de peso moderada (-500 kcal/día, ~0.5kg/semana)"),
        WEIGHT_LOSS_SLOW(-250.0, "Pérdida de peso lenta (-250 kcal/día, ~0.25kg/semana)"),
        MAINTENANCE(0.0, "Mantenimiento (mantener peso actual)"),
        MUSCLE_GAIN_LEAN(200.0, "Ganancia muscular magra (+200 kcal/día)"),
        MUSCLE_GAIN_MODERATE(350.0, "Ganancia muscular moderada (+350 kcal/día)"),
        MUSCLE_GAIN_BULK(500.0, "Volumen agresivo (+500 kcal/día)")
    }
    
    /**
     * Calcula calorías diarias recomendadas según objetivo
     * 
     * @param tdee Total Daily Energy Expenditure
     * @param goal Objetivo de fitness
     * @return Calorías diarias recomendadas
     */
    fun calculateTargetCalories(
        tdee: Double,
        goal: FitnessGoal
    ): Double {
        return max(1200.0, tdee + goal.calorieAdjustment) // Mínimo 1200 kcal para salud
    }
    
    /**
     * Calcula distribución de macronutrientes según objetivo
     * 
     * @param targetCalories Calorías objetivo diarias
     * @param goal Objetivo de fitness
     * @return Triple(proteína en gramos, carbohidratos en gramos, grasas en gramos)
     */
    fun calculateMacros(
        targetCalories: Double,
        weight: Double,
        goal: FitnessGoal
    ): Triple<Double, Double, Double> {
        // Proteína: base en peso corporal
        val proteinGrams = when (goal) {
            FitnessGoal.WEIGHT_LOSS_AGGRESSIVE,
            FitnessGoal.WEIGHT_LOSS_MODERATE,
            FitnessGoal.WEIGHT_LOSS_SLOW -> weight * 2.2 // Alta proteína en déficit
            FitnessGoal.MUSCLE_GAIN_LEAN,
            FitnessGoal.MUSCLE_GAIN_MODERATE,
            FitnessGoal.MUSCLE_GAIN_BULK -> weight * 2.0 // Alta proteína en superávit
            else -> weight * 1.8 // Mantenimiento
        }
        
        val proteinCalories = proteinGrams * 4
        
        // Grasa: 25-30% de calorías totales
        val fatPercentage = when (goal) {
            FitnessGoal.WEIGHT_LOSS_AGGRESSIVE,
            FitnessGoal.WEIGHT_LOSS_MODERATE -> 0.25 // Menos grasa en déficit
            else -> 0.30
        }
        val fatCalories = targetCalories * fatPercentage
        val fatGrams = fatCalories / 9
        
        // Carbohidratos: resto de calorías
        val carbCalories = targetCalories - proteinCalories - fatCalories
        val carbGrams = max(50.0, carbCalories / 4) // Mínimo 50g para función cerebral
        
        return Triple(proteinGrams, carbGrams, fatGrams)
    }
    
    /**
     * Calcula frecuencia cardíaca máxima usando fórmula de Tanaka
     * Más precisa que la tradicional 220-edad
     * 
     * @param age Edad en años
     * @return Frecuencia cardíaca máxima en bpm
     */
    fun calculateMaxHeartRate(age: Int): Int {
        return (208 - (0.7 * age)).toInt()
    }
    
    /**
     * Zonas de entrenamiento cardiovascular
     */
    enum class HeartRateZone(
        val minPercent: Double,
        val maxPercent: Double,
        val zoneName: String,
        val description: String,
        val color: String
    ) {
        ZONE_1(0.5, 0.6, "Zona 1: Recuperación", "Muy ligero - Recuperación activa", "#4CAF50"),
        ZONE_2(0.6, 0.7, "Zona 2: Quema de Grasa", "Ligero - Resistencia aeróbica", "#8BC34A"),
        ZONE_3(0.7, 0.8, "Zona 3: Aeróbico", "Moderado - Mejora cardiovascular", "#FFC107"),
        ZONE_4(0.8, 0.9, "Zona 4: Anaeróbico", "Intenso - Umbral láctico", "#FF9800"),
        ZONE_5(0.9, 1.0, "Zona 5: Máximo", "Muy intenso - Esfuerzo máximo", "#F44336")
    }
    
    /**
     * Calcula rangos de frecuencia cardíaca por zona
     * 
     * @param maxHeartRate Frecuencia cardíaca máxima
     * @return Mapa de zonas con rangos (min, max)
     */
    fun calculateHeartRateZones(maxHeartRate: Int): Map<HeartRateZone, Pair<Int, Int>> {
        return HeartRateZone.values().associateWith { zone ->
            val min = (maxHeartRate * zone.minPercent).toInt()
            val max = (maxHeartRate * zone.maxPercent).toInt()
            Pair(min, max)
        }
    }
    
    /**
     * Estima peso máximo en 1RM (One Rep Max) usando fórmula de Brzycki
     * 
     * @param weight Peso levantado
     * @param reps Repeticiones realizadas (1-10 para precisión)
     * @return Peso máximo estimado en 1RM
     */
    fun calculateOneRepMax(weight: Double, reps: Int): Double {
        if (reps == 1) return weight
        if (reps > 10) return weight // No confiable con más de 10 reps
        
        // Fórmula de Brzycki: 1RM = weight × (36 / (37 - reps))
        return weight * (36.0 / (37.0 - reps))
    }
    
    /**
     * Calcula porcentajes de entrenamiento basados en 1RM
     * 
     * @param oneRepMax Peso máximo en 1RM
     * @return Mapa de porcentajes comunes (60%, 70%, 80%, 85%, 90%, 95%)
     */
    fun calculateTrainingWeights(oneRepMax: Double): Map<Int, Double> {
        return mapOf(
            60 to oneRepMax * 0.60,
            70 to oneRepMax * 0.70,
            75 to oneRepMax * 0.75,
            80 to oneRepMax * 0.80,
            85 to oneRepMax * 0.85,
            90 to oneRepMax * 0.90,
            95 to oneRepMax * 0.95
        )
    }
    
    /**
     * Calcula el índice de masa corporal magra (FFMI - Fat-Free Mass Index)
     * Útil para estimar potencial genético de músculo
     * 
     * @param weight Peso en kg
     * @param height Altura en cm
     * @param bodyFatPercentage Porcentaje de grasa corporal
     * @return FFMI
     */
    fun calculateFFMI(
        weight: Double,
        height: Double,
        bodyFatPercentage: Double
    ): Double {
        val heightInMeters = height / 100.0
        val leanMass = weight * (1 - bodyFatPercentage / 100.0)
        val ffmi = leanMass / (heightInMeters * heightInMeters)
        
        // Ajuste normalizado por altura (6'0" / 180cm es la referencia)
        val heightAdjustment = 6.1 * (1.8 - heightInMeters)
        return ffmi + heightAdjustment
    }
    
    /**
     * Interpreta el FFMI
     * 
     * <18: Por debajo del promedio
     * 18-20: Promedio
     * 20-22: Por encima del promedio (buen desarrollo muscular)
     * 22-25: Excelente (atleta natural en límite genético)
     * >25: Sospecha de uso de esteroides (raro en naturales)
     */
    fun interpretFFMI(ffmi: Double): String {
        return when {
            ffmi < 18 -> "Por debajo del promedio"
            ffmi < 20 -> "Promedio"
            ffmi < 22 -> "Por encima del promedio"
            ffmi < 25 -> "Excelente desarrollo muscular"
            else -> "Nivel de élite"
        }
    }
    
    /**
     * Calcula consumo de agua recomendado en litros
     * 
     * Base: 35ml por kg de peso
     * + 500ml por hora de ejercicio moderado
     * + 1000ml por hora de ejercicio intenso
     * 
     * @param weight Peso en kg
     * @param exerciseMinutes Minutos de ejercicio al día
     * @param exerciseIntensity "low", "moderate", "high"
     * @return Litros de agua recomendados por día
     */
    fun calculateWaterIntake(
        weight: Double,
        exerciseMinutes: Int = 0,
        exerciseIntensity: String = "moderate"
    ): Double {
        val baseWater = weight * 0.035 // 35ml por kg
        
        val exerciseWater = when (exerciseIntensity) {
            "low" -> exerciseMinutes / 60.0 * 0.3
            "moderate" -> exerciseMinutes / 60.0 * 0.5
            "high" -> exerciseMinutes / 60.0 * 1.0
            else -> 0.0
        }
        
        return baseWater + exerciseWater
    }
}
