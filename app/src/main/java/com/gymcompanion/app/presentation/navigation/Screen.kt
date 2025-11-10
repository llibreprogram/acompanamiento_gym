package com.gymcompanion.app.presentation.navigation

/**
 * Define las rutas de navegación de la aplicación
 */
sealed class Screen(val route: String) {
    // Main screens
    object Home : Screen("home")
    object Routines : Screen("routines")
    object Exercises : Screen("exercises")
    object Progress : Screen("progress")
    object Profile : Screen("profile")
    
    // Detail screens
    object ExerciseDetail : Screen("exercise_detail/{exerciseId}") {
        fun createRoute(exerciseId: Long) = "exercise_detail/$exerciseId"
    }
    
    object RoutineDetail : Screen("routine_detail/{routineId}") {
        fun createRoute(routineId: Long) = "routine_detail/$routineId"
    }
    
    object WorkoutSession : Screen("workout_session/{routineId}") {
        fun createRoute(routineId: Long) = "workout_session/$routineId"
    }
    
    // Onboarding & Setup
    object Welcome : Screen("welcome")
    object BodyMetricsSetup : Screen("body_metrics_setup")
    object BodyMetricsEdit : Screen("body_metrics_edit")
}
