package com.example.drivealert.utils

object BackendConfig {
    // Backend Service Configuration
    // TODO: Replace with your actual backend service URL
    
    // For Android Emulator (localhost backend)
    const val BACKEND_BASE_URL_EMULATOR = "http://10.0.2.2:8080"
    
    // For Real Device (same network as your computer)
    // Replace 192.168.1.100 with your computer's actual IP address
    const val BACKEND_BASE_URL_DEVICE = "http://192.168.1.100:8080"
    
    // For Production
    const val BACKEND_BASE_URL_PRODUCTION = "https://your-backend-domain.com"
    
    // Current environment - change this based on your setup
    const val CURRENT_BASE_URL = BACKEND_BASE_URL_EMULATOR
    
    // API Endpoints
    const val AUTH_LOGIN_ENDPOINT = "/api/auth/login"
    const val DRIVERS_ACTIVE_ENDPOINT = "/api/drivers/active"
    const val UPDATE_LAST_LOGIN_ENDPOINT = "/api/drivers/last-login"
    const val HEALTH_CHECK_ENDPOINT = "/api/health"
    
    // MongoDB connection details (for your backend service, not mobile app)
    const val MONGODB_URI = "mongodb+srv://alviawan97:SMSzRZrR6nsUSwyV@cluster0.syz8muv.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0"
    const val DATABASE_NAME = "distract_alert"
    const val COLLECTION_NAME = "drivers"
    
    // Face recognition threshold
    const val FACE_SIMILARITY_THRESHOLD = 0.85f
    
    // Connection timeouts
    const val CONNECTION_TIMEOUT = 30000L // 30 seconds
    const val READ_TIMEOUT = 30000L // 30 seconds
    const val WRITE_TIMEOUT = 30000L // 30 seconds
}

// Legacy support - keeping for backward compatibility
object MongoDBConfig {
    const val MONGODB_URI = BackendConfig.MONGODB_URI
    const val DATABASE_NAME = BackendConfig.DATABASE_NAME
    const val COLLECTION_NAME = BackendConfig.COLLECTION_NAME
    const val FACE_SIMILARITY_THRESHOLD = BackendConfig.FACE_SIMILARITY_THRESHOLD
    const val CONNECTION_TIMEOUT = BackendConfig.CONNECTION_TIMEOUT
    const val READ_TIMEOUT = BackendConfig.READ_TIMEOUT
    
    // Deprecated - no longer needed
    @Deprecated("MongoDB Data API is deprecated. Use backend service instead.")
    const val DATA_API_APP_ID = ""
    @Deprecated("MongoDB Data API is deprecated. Use backend service instead.")
    const val DATA_API_URL = ""
    @Deprecated("MongoDB Data API is deprecated. Use backend service instead.")
    const val API_KEY = ""
}
