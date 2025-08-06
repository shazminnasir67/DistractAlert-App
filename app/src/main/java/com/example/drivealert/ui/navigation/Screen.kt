package com.example.drivealert.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object StartTrip : Screen("start_trip")
    object Trips : Screen("trips")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Dashboard : Screen("dashboard")
    object TripHistory : Screen("trip_history")
    object Alerts : Screen("alerts")
    object Help : Screen("help")
    object DrowsinessDetection : Screen("drowsiness_detection")
    object FaceRecognition : Screen("face_recognition")
} 