package com.gymcompanion.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gymcompanion.app.presentation.screens.home.HomeScreen
import com.gymcompanion.app.presentation.screens.exercises.ExerciseDetailScreen
import com.gymcompanion.app.presentation.screens.exercises.ExercisesScreen
import com.gymcompanion.app.presentation.screens.progress.ProgressScreen
import com.gymcompanion.app.presentation.screens.profile.ProfileScreen
import com.gymcompanion.app.presentation.screens.routines.RoutinesScreen
import com.gymcompanion.app.presentation.screens.workout.WorkoutScreen

/**
 * Configuración principal de navegación de la aplicación
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymCompanionNavigation() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onStartWorkout = { routineId ->
                        navController.navigate(Screen.WorkoutSession.createRoute(routineId))
                    },
                    onNavigateToRoutines = {
                        navController.navigate(Screen.Routines.route)
                    }
                )
            }
            composable(Screen.Routines.route) {
                RoutinesScreen(
                    onRoutineClick = { routineId ->
                        navController.navigate(Screen.WorkoutSession.createRoute(routineId))
                    },
                    onCreateRoutine = {
                        navController.navigate(Screen.RoutineGenerator.route)
                    }
                )
            }
            composable(Screen.RoutineGenerator.route) {
                com.gymcompanion.app.presentation.screens.routine_generator.RoutineGeneratorScreen(
                    onRoutineGenerated = {
                        navController.popBackStack()
                        navController.navigate(Screen.Routines.route)
                    }
                )
            }
            composable(Screen.Exercises.route) {
                ExercisesScreen(
                    onExerciseClick = { exerciseId ->
                        navController.navigate(Screen.ExerciseDetail.createRoute(exerciseId))
                    }
                )
            }
            composable(
                route = Screen.ExerciseDetail.route,
                arguments = listOf(
                    navArgument("exerciseId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getLong("exerciseId") ?: 0L
                ExerciseDetailScreen(
                    exerciseId = exerciseId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.WorkoutSession.route,
                arguments = listOf(
                    navArgument("routineId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val routineId = backStackEntry.arguments?.getLong("routineId") ?: 0L
                WorkoutScreen(
                    routineId = routineId,
                    onWorkoutComplete = {
                        navController.popBackStack(Screen.Home.route, false)
                    },
                    onWorkoutCancel = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Progress.route) {
                ProgressScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen(navController = navController)
            }
        }
    }
}

/**
 * Barra de navegación inferior
 */
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val items = listOf(
        BottomNavItem("Inicio", Screen.Home.route, Icons.Filled.Home),
        BottomNavItem("Rutinas", Screen.Routines.route, Icons.Filled.FitnessCenter),
        BottomNavItem("Ejercicios", Screen.Exercises.route, Icons.Filled.List),
        BottomNavItem("Progreso", Screen.Progress.route, Icons.Filled.TrendingUp),
        BottomNavItem("Perfil", Screen.Profile.route, Icons.Filled.Person)
    )
    
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)
