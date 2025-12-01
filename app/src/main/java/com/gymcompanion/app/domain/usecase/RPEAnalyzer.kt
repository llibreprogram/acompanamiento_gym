package com.gymcompanion.app.domain.usecase

import javax.inject.Inject

/**
 * Analizador de RPE (Rate of Perceived Exertion) para autoregulación.
 * Determina cómo ajustar la carga de trabajo basándose en la percepción de esfuerzo del usuario.
 */
class RPEAnalyzer @Inject constructor() {

    enum class Adjustment {
        INCREASE_WEIGHT,
        DECREASE_WEIGHT,
        KEEP_SAME,
        REST_MORE
    }

    data class RPEFeedback(
        val adjustment: Adjustment,
        val message: String
    )

    /**
     * Analiza el RPE de una serie y sugiere ajustes.
     * 
     * @param rpe El RPE reportado (1-10).
     * @param targetRpe El RPE objetivo (por defecto 8 para hipertrofia).
     */
    fun analyzeSet(rpe: Int, targetRpe: Int = 8): RPEFeedback {
        return when {
            // Muy fácil (RPE 1-4) -> Subir peso significativamente
            rpe <= targetRpe - 4 -> RPEFeedback(
                Adjustment.INCREASE_WEIGHT,
                "¡Muy fácil! Sube el peso un 5-10% para la próxima serie."
            )
            // Fácil (RPE 5-6) -> Subir peso ligeramente
            rpe <= targetRpe - 2 -> RPEFeedback(
                Adjustment.INCREASE_WEIGHT,
                "Fácil. Intenta subir un poco el peso (2.5kg - 5kg)."
            )
            // Zona óptima (RPE 7-9) -> Mantener
            rpe in (targetRpe - 1)..(targetRpe + 1) -> RPEFeedback(
                Adjustment.KEEP_SAME,
                "¡Perfecto! Mantén este peso y enfócate en la técnica."
            )
            // Fallo o muy difícil (RPE 10) -> Bajar peso o descansar más
            rpe >= 10 -> RPEFeedback(
                Adjustment.DECREASE_WEIGHT,
                "Al fallo. Descansa un poco más o baja el peso si pierdes técnica."
            )
            else -> RPEFeedback(Adjustment.KEEP_SAME, "Buen trabajo.")
        }
    }
}
