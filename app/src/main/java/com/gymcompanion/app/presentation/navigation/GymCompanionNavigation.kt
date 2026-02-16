package com.gymcompanion.app.presentation.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
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
            val enterTrans = fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.95f, animationSpec = tween(400))
            val exitTrans = fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 0.95f, animationSpec = tween(400))

            composable(
                route = Screen.Home.route,
                enterTransition = { enterTrans },
                exitTransition = { exitTrans },
                popEnterTransition = { enterTrans },
                popExitTransition = { exitTrans }
            ) {
                HomeScreen(
                    onStartWorkout = { routineId ->
                        navController.navigate(Screen.WorkoutSession.createRoute(routineId))
                    },
                    onNavigateToRoutines = {
                        navController.navigate(Screen.Routines.route)
                    },
                    onNavigateToProgress = {
                        navController.navigate(Screen.Progress.route)
                    },
                    onNavigateToAnalytics = {
                        navController.navigate(Screen.Progress.route)
                    }
                )
            }
            composable(
                route = Screen.Routines.route,
                enterTransition = { enterTrans },
                exitTransition = { exitTrans },
                popEnterTransition = { enterTrans },
                popExitTransition = { exitTrans }
            ) {
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
            composable(
                route = Screen.RoutineGenerator.route,
                enterTransition = { enterTrans },
                exitTransition = { exitTrans },
                popEnterTransition = { enterTrans },
                popExitTransition = { exitTrans }
            ) {
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
                ),
                enterTransition = { enterTrans },
                exitTransition = { exitTrans },
                popEnterTransition = { enterTrans },
                popExitTransition = { exitTrans }
            ) { backStackEntry ->
                val routineId = backStackEntry.arguments?.getLong("routineId") ?: 0L
                com.gymcompanion.app.presentation.screens.routines.RoutineDetailScreen(
                    routineId = routineId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.Exercises.route,
                enterTransition = { enterTrans },
                exitTransition = { exitTrans },
                popEnterTransition = { enterTrans },
                popExitTransition = { exitTrans }
            ) {
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
                ),
                enterTransition = { enterTrans },
                exitTransition = { exitTrans },
                popEnterTransition = { enterTrans },
                popExitTransition = { exitTrans }
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
                ),
                enterTransition = { enterTrans },
                exitTransition = { exitTrans },
                popEnterTransition = { enterTrans },
                popExitTransition = { exitTrans }
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
            composable(
                route = Screen.Progress.route,
                enterTransition = { enterTrans },
                exitTransition = { exitTrans },
                popEnterTransition = { enterTrans },
                popExitTransition = { exitTrans }
            ) {
                ProgressScreen()
            }
            composable(
                route = Screen.Profile.route,
                enterTransition = { enterTrans },
                exitTransition = { exitTrans },
                popEnterTransition = { enterTrans },
                popExitTransition = { exitTrans }
            ) {
                ProfileScreen(navController = navController)
            }
        }
    }
}

/**
 * Barra de navegación inferior moderna con animaciones y colores neón
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        BottomNavItem("Inicio", Screen.Home.route, Icons.Filled.Home, Icons.Filled.Home, com.gymcompanion.app.presentation.theme.NeonBlue),
        BottomNavItem("Rutinas", Screen.Routines.route, Icons.Filled.FitnessCenter, Icons.Filled.FitnessCenter, com.gymcompanion.app.presentation.theme.NeonPurple),
        BottomNavItem("Ejercicios", Screen.Exercises.route, Icons.Filled.List, Icons.Filled.List, com.gymcompanion.app.presentation.theme.NeonOrange),
        BottomNavItem("Progreso", Screen.Progress.route, Icons.Filled.TrendingUp, Icons.Filled.TrendingUp, com.gymcompanion.app.presentation.theme.NeonGreen),
        BottomNavItem("Perfil", Screen.Profile.route, Icons.Filled.Person, Icons.Filled.Person, com.gymcompanion.app.presentation.theme.NeonPink)
    )

    NavigationBar(
        containerColor = com.gymcompanion.app.presentation.theme.DarkSurface.copy(alpha = 0.95f),
        contentColor = com.gymcompanion.app.presentation.theme.TextSecondary,
        tonalElevation = 0.dp,
        modifier = Modifier,
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        items.forEachIndexed { index, item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            
            // Animated scale for selection
            val scale by animateFloatAsState(
                targetValue = if (selected) 1.2f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "nav_item_scale_$index"
            )

            NavigationBarItem(
                icon = {
                    Box(
                        modifier = Modifier.scale(scale),
                        contentAlignment = Alignment.Center
                    ) {
                        // Glow effect behind selected icon
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .blur(12.dp)
                                    .background(item.color.copy(alpha = 0.5f), CircleShape)
                            )
                        }
                        
                        Icon(
                            if (selected) item.selectedIcon else item.icon,
                            contentDescription = item.title,
                            modifier = Modifier.size(24.dp),
                            tint = if (selected) item.color else com.gymcompanion.app.presentation.theme.TextTertiary
                        )
                    }
                },
                label = {
                    if (selected) {
                        Text(
                            item.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = item.color,
                            fontSize = 10.sp
                        )
                    }
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
                    indicatorColor = Color.Transparent // No pill background, just the glow
                ),
                alwaysShowLabel = false
            )
        }
    }
}

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
    val color: androidx.compose.ui.graphics.Color = com.gymcompanion.app.presentation.theme.NeonBlue
)
