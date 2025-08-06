package com.example.drivealert.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object NetworkUtils {
    
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
    
    suspend fun checkDatabaseConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check backend service health endpoint instead of MongoDB Data API
            val url = URL("${BackendConfig.CURRENT_BASE_URL}${BackendConfig.HEALTH_CHECK_ENDPOINT}")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val responseCode = connection.responseCode
            connection.disconnect()
            
            responseCode in 200..299
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun checkBackendService(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check if your backend service is running
            val url = URL("${BackendConfig.CURRENT_BASE_URL}${BackendConfig.HEALTH_CHECK_ENDPOINT}")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val responseCode = connection.responseCode
            connection.disconnect()
            
            responseCode == 200
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun simulateDatabaseConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            // First try to connect to actual backend service
            val isBackendAvailable = checkBackendService()
            if (isBackendAvailable) {
                return@withContext true
            }
            
            // If backend is not available, simulate connection for development
            kotlinx.coroutines.delay(2000)
            true // Return true for simulation - allows development without backend
        } catch (e: Exception) {
            false
        }
    }
}
