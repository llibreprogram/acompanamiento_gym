package com.gymcompanion.app.domain.util

object UnitConverter {
    
    // Weight conversions
    fun kgToLb(kg: Double): Double = kg * 2.20462
    fun lbToKg(lb: Double): Double = lb / 2.20462
    
    fun formatWeight(value: Double, unit: String): String {
        return when (unit) {
            "lb" -> "%.1f lb".format(value)
            else -> "%.1f kg".format(value)
        }
    }
    
    fun convertWeight(value: Double, fromUnit: String, toUnit: String): Double {
        if (fromUnit == toUnit) return value
        return when {
            fromUnit == "kg" && toUnit == "lb" -> kgToLb(value)
            fromUnit == "lb" && toUnit == "kg" -> lbToKg(value)
            else -> value
        }
    }
    
    // Height conversions
    fun cmToFeet(cm: Double): Pair<Int, Int> {
        val totalInches = cm / 2.54
        val feet = (totalInches / 12).toInt()
        val inches = (totalInches % 12).toInt()
        return Pair(feet, inches)
    }
    
    fun feetToCm(feet: Int, inches: Int): Double {
        return (feet * 12 + inches) * 2.54
    }
    
    fun formatHeight(value: Double, unit: String): String {
        return when (unit) {
            "ft" -> {
                val (feet, inches) = cmToFeet(value)
                "$feet' $inches\""
            }
            else -> "%.0f cm".format(value)
        }
    }
    
    fun convertHeight(value: Double, fromUnit: String, toUnit: String): Double {
        if (fromUnit == toUnit) return value
        return when {
            fromUnit == "cm" && toUnit == "ft" -> value // Keep in cm internally
            fromUnit == "ft" && toUnit == "cm" -> value // Keep in cm internally
            else -> value
        }
    }
    
    // Distance conversions
    fun kmToMiles(km: Double): Double = km * 0.621371
    fun milesToKm(miles: Double): Double = miles / 0.621371
    
    fun formatDistance(value: Double, unit: String): String {
        return when (unit) {
            "mi" -> "%.2f mi".format(value)
            else -> "%.2f km".format(value)
        }
    }
    
    fun convertDistance(value: Double, fromUnit: String, toUnit: String): Double {
        if (fromUnit == toUnit) return value
        return when {
            fromUnit == "km" && toUnit == "mi" -> kmToMiles(value)
            fromUnit == "mi" && toUnit == "km" -> milesToKm(value)
            else -> value
        }
    }
    
    // BMI calculation (always uses kg and cm internally)
    fun calculateBMI(weightKg: Double, heightCm: Double): Double {
        val heightM = heightCm / 100.0
        return weightKg / (heightM * heightM)
    }
    
    fun getBMICategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Bajo peso"
            bmi < 25.0 -> "Normal"
            bmi < 30.0 -> "Sobrepeso"
            else -> "Obesidad"
        }
    }
}
