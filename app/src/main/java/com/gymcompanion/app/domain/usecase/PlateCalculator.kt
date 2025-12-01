package com.gymcompanion.app.domain.usecase

import javax.inject.Inject

/**
 * Calculadora de discos para barra olímpica
 * Calcula qué discos poner en cada lado de la barra para alcanzar el peso objetivo
 */
class PlateCalculator @Inject constructor() {
    
    companion object {
        const val STANDARD_BAR_WEIGHT = 20.0 // kg
        
        // Discos disponibles en orden descendente (en kg)
        val AVAILABLE_PLATES = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 2.0, 1.25, 1.0, 0.5)
    }
    
    /**
     * Tipo de equipo para el ejercicio
     */
    enum class EquipmentType {
        BARBELL,      // Barra olímpica - calcular discos
        DUMBBELL,     // Mancuernas - mostrar peso por mancuerna
        BODYWEIGHT,   // Peso corporal - no mostrar calculadora
        MACHINE,      // Máquina - no mostrar calculadora
        CABLE,        // Cable - no mostrar calculadora
        OTHER         // Otro - no mostrar calculadora
    }
    
    /**
     * Resultado del cálculo de discos
     */
    data class PlateLoadout(
        val totalWeight: Double,
        val barWeight: Double,
        val platesPerSide: List<Double>,
        val isExact: Boolean,
        val difference: Double = 0.0,
        val equipmentType: EquipmentType = EquipmentType.BARBELL,
        val weightPerHand: Double? = null // Para mancuernas
    )
    
    /**
     * Determina el tipo de equipo basado en el string
     */
    fun getEquipmentType(equipmentNeeded: String): EquipmentType {
        return when (equipmentNeeded.lowercase()) {
            "barbell" -> EquipmentType.BARBELL
            "dumbbell", "dumbbells" -> EquipmentType.DUMBBELL
            "bodyweight", "body weight" -> EquipmentType.BODYWEIGHT
            "machine" -> EquipmentType.MACHINE
            "cable" -> EquipmentType.CABLE
            else -> EquipmentType.OTHER
        }
    }
    
    /**
     * Calcula los discos necesarios para alcanzar el peso objetivo
     * @param targetWeight Peso total objetivo
     * @param equipmentType Tipo de equipo usado
     * @param barWeight Peso de la barra (por defecto 20kg)
     * @return PlateLoadout con la configuración de discos
     */
    fun calculatePlates(
        targetWeight: Double,
        equipmentType: EquipmentType = EquipmentType.BARBELL,
        barWeight: Double = STANDARD_BAR_WEIGHT
    ): PlateLoadout {
        // Para mancuernas, calcular peso por mancuerna
        if (equipmentType == EquipmentType.DUMBBELL) {
            val weightPerHand = targetWeight / 2.0
            return PlateLoadout(
                totalWeight = targetWeight,
                barWeight = 0.0,
                platesPerSide = emptyList(),
                isExact = true,
                equipmentType = equipmentType,
                weightPerHand = weightPerHand
            )
        }
        
        // Para otros equipos que no sean barra, no calcular
        if (equipmentType != EquipmentType.BARBELL) {
            return PlateLoadout(
                totalWeight = targetWeight,
                barWeight = 0.0,
                platesPerSide = emptyList(),
                isExact = true,
                equipmentType = equipmentType
            )
        }
        
        // Lógica para barra olímpica
        // Si el peso objetivo es menor o igual al peso de la barra, no se necesitan discos
        if (targetWeight <= barWeight) {
            return PlateLoadout(
                totalWeight = barWeight,
                barWeight = barWeight,
                platesPerSide = emptyList(),
                isExact = targetWeight == barWeight,
                difference = barWeight - targetWeight,
                equipmentType = equipmentType
            )
        }
        
        // Peso que necesitamos agregar (dividido entre 2 lados)
        var remainingWeight = (targetWeight - barWeight) / 2.0
        val platesPerSide = mutableListOf<Double>()
        
        // Algoritmo greedy: usar el disco más grande posible en cada paso
        for (plate in AVAILABLE_PLATES) {
            while (remainingWeight >= plate - 0.01) { // Tolerancia de 0.01kg para errores de redondeo
                platesPerSide.add(plate)
                remainingWeight -= plate
            }
        }
        
        val actualWeight = barWeight + (platesPerSide.sum() * 2)
        val isExact = Math.abs(actualWeight - targetWeight) < 0.01
        
        return PlateLoadout(
            totalWeight = actualWeight,
            barWeight = barWeight,
            platesPerSide = platesPerSide,
            isExact = isExact,
            difference = targetWeight - actualWeight,
            equipmentType = equipmentType
        )
    }
    
    /**
     * Formatea la lista de discos para mostrar
     * Ejemplo: [20, 20, 5, 2.5] -> "2×20kg + 5kg + 2.5kg"
     */
    fun formatPlates(plates: List<Double>): String {
        if (plates.isEmpty()) return "Sin discos"
        
        val grouped = plates.groupBy { it }
            .map { (weight, list) ->
                if (list.size > 1) {
                    "${list.size}×${weight}kg"
                } else {
                    "${weight}kg"
                }
            }
        
        return grouped.joinToString(" + ")
    }
}
