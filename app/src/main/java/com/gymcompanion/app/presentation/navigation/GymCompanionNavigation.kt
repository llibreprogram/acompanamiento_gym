package com.gymcompanion.app.presentation.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                    },
                    onNavigateToProgress = {
                        navController.navigate(Screen.Progress.route)
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
                    },
                    onViewDetails = { routineId ->
                        navController.navigate(Screen.RoutineDetail.createRoute(routineId))
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
            composable(
                route = Screen.RoutineDetail.route,
                arguments = listOf(
                    navArgument("routineId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val routineId = backStackEntry.arguments?.getLong("routineId") ?: 0L
                com.gymcompanion.app.presentation.screens.routines.RoutineDetailScreen(
                    routineId = routineId,
                    onNavigateBack = { navController.popBackStack() }
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
 * Barra de navegación inferior moderna con animaciones
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        BottomNavItem("Inicio", Screen.Home.route, Icons.Filled.Home, Icons.Filled.Home),
        BottomNavItem("Rutinas", Screen.Routines.route, Icons.Filled.FitnessCenter, Icons.Filled.FitnessCenter),
        BottomNavItem("Ejercicios", Screen.Exercises.route, Icons.Filled.List, Icons.Filled.List),
        BottomNavItem("Progreso", Screen.Progress.route, Icons.Filled.TrendingUp, Icons.Filled.TrendingUp),
        BottomNavItem("Perfil", Screen.Profile.route, Icons.Filled.Person, Icons.Filled.Person)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp
    ) {
        items.forEachIndexed { index, item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            val scale by animateFloatAsState(
                targetValue = if (selected) 1.1f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "nav_item_scale_$index"
            )

            NavigationBarItem(
                icon = {
                    Box(
                        modifier = Modifier.scale(scale),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (selected) item.selectedIcon else item.icon,
                            contentDescription = item.title,
                            modifier = Modifier.size(if (selected) 28.dp else 24.dp)
                        )
                    }
                },
                label = {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            )
        }
    }
}

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)
