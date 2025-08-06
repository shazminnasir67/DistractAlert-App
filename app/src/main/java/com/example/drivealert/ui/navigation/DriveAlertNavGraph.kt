package com.example.drivealert.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.drivealert.ui.components.DriveAlertBottomNavigation
import com.example.drivealert.ui.screens.login.LoginScreen
import com.example.drivealert.ui.screens.facerecognition.FaceRecognitionScreen
import com.example.drivealert.ui.screens.home.HomeScreen
import com.example.drivealert.ui.screens.drowsinessdetection.DrowsinessDetectionScreen
import com.example.drivealert.ui.screens.profile.ProfileScreen
import com.example.drivealert.ui.screens.starttrip.StartTripScreen
import com.example.drivealert.ui.viewmodel.SharedViewModel

@Composable
fun DriveAlertNavGraph(
    navController: NavHostController,
    sharedViewModel: SharedViewModel
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Login.route
    
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(route = Screen.Login.route) {
            LoginScreen(
                onNavigateToFaceRecognition = {
                    navController.navigate(Screen.FaceRecognition.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                sharedViewModel = sharedViewModel
            )
        }
        
        composable(route = Screen.FaceRecognition.route) {
            FaceRecognitionScreen(
                onNavigateToStartTrip = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.FaceRecognition.route) { inclusive = true }
                    }
                },
                sharedViewModel = sharedViewModel
            )
        }
        
        composable(route = Screen.Home.route) {
            MainScreen(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            ) {
                HomeScreen(
                    onStartTrip = {
                        navController.navigate(Screen.DrowsinessDetection.route)
                    },
                    userName = sharedViewModel.getCurrentUser()?.firstName ?: "Driver"
                )
            }
        }
        
        composable(route = Screen.DrowsinessDetection.route) {
            DrowsinessDetectionScreen(
                onStopTrip = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(route = Screen.Profile.route) {
            MainScreen(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            ) {
                ProfileScreen(
                    user = sharedViewModel.getCurrentUser(),
                    onBackClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }
        
        composable(route = Screen.Trips.route) {
            MainScreen(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            ) {
                // TODO: Add TripsScreen
                StartTripScreen()
            }
        }
        
        composable(route = Screen.Settings.route) {
            MainScreen(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            ) {
                // TODO: Add SettingsScreen
                StartTripScreen()
            }
        }
    }
}

@Composable
private fun MainScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            DriveAlertBottomNavigation(
                currentRoute = currentRoute,
                onNavigate = onNavigate
            )
        }
    ) { paddingValues ->
        content()
    }
} 