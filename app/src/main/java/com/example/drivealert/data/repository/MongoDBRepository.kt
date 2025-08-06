package com.example.drivealert.data.repository

import com.example.drivealert.data.model.FaceRecognitionResult
import com.example.drivealert.data.model.LoginResult
import com.example.drivealert.data.model.User
import com.example.drivealert.data.service.MongoDBServiceImpl
import java.text.SimpleDateFormat
import java.util.*

interface MongoDBRepository {
    suspend fun authenticateUser(faceEmbedding: List<Float>): FaceRecognitionResult
    suspend fun updateUserLastLogin(userId: String): Boolean
    suspend fun getUserById(userId: String): User?
    suspend fun getAllUsers(): List<User>
}

class MongoDBRepositoryImpl : MongoDBRepository {
    
    private val mongoDBService = MongoDBServiceImpl()
    
    // Helper function to convert timestamp to ISO string format (compatible with API 24+)
    private fun timestampToIsoString(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(timestamp))
    }
    
    override suspend fun authenticateUser(faceEmbedding: List<Float>): FaceRecognitionResult {
        return try {
            // Use the real MongoDB service
            val result = mongoDBService.authenticateDriver(faceEmbedding)
            
            if (result.isSuccess) {
                val driver = result.getOrNull()!!
                val user = User(
                    id = driver._id ?: "",
                    userId = driver.user_id,
                    fleetManagerId = driver.fleet_manager_id,
                    firstName = driver.first_name,
                    lastName = driver.last_name,
                    email = driver.email,
                    phone = driver.phone,
                    licenseNumber = driver.license_number,
                    username = driver.username,
                    accountStatus = driver.account_status,
                    faceEmbeddingUrl = driver.face_embedding_url,
                    createdAt = driver.created_at,
                    updatedAt = driver.updated_at,
                    lastLogin = driver.last_login?.let { timestamp ->
                        // Convert timestamp to ISO string format
                        timestampToIsoString(timestamp)
                    }
                )
                
                FaceRecognitionResult(
                    isSuccess = true,
                    user = user,
                    confidence = 0.95f // High confidence since it passed MongoDB matching
                )
            } else {
                FaceRecognitionResult(
                    isSuccess = false,
                    error = result.exceptionOrNull()?.message ?: "Authentication failed"
                )
            }
        } catch (e: Exception) {
            FaceRecognitionResult(
                isSuccess = false,
                error = "Authentication failed: ${e.message}"
            )
        }
    }
    
    override suspend fun updateUserLastLogin(userId: String): Boolean {
        return try {
            val result = mongoDBService.updateDriverLastLogin(userId)
            result.isSuccess && result.getOrNull() == true
        } catch (e: Exception) {
            println("Failed to update last login: ${e.message}")
            false
        }
    }
    
    override suspend fun getUserById(userId: String): User? {
        return try {
            val driversResult = mongoDBService.getAllActiveDrivers()
            if (driversResult.isSuccess) {
                val drivers = driversResult.getOrNull()!!
                val driver = drivers.find { it.user_id == userId }
                driver?.let {
                    User(
                        id = it._id ?: "",
                        userId = it.user_id,
                        fleetManagerId = it.fleet_manager_id,
                        firstName = it.first_name,
                        lastName = it.last_name,
                        email = it.email,
                        phone = it.phone,
                        licenseNumber = it.license_number,
                        username = it.username,
                        accountStatus = it.account_status,
                        faceEmbeddingUrl = it.face_embedding_url,
                        createdAt = it.created_at,
                        updatedAt = it.updated_at,
                        lastLogin = it.last_login?.let { timestamp ->
                            timestampToIsoString(timestamp)
                        }
                    )
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getAllUsers(): List<User> {
        return try {
            val driversResult = mongoDBService.getAllActiveDrivers()
            if (driversResult.isSuccess) {
                val drivers = driversResult.getOrNull()!!
                drivers.map { driver ->
                    User(
                        id = driver._id ?: "",
                        userId = driver.user_id,
                        fleetManagerId = driver.fleet_manager_id,
                        firstName = driver.first_name,
                        lastName = driver.last_name,
                        email = driver.email,
                        phone = driver.phone,
                        licenseNumber = driver.license_number,
                        username = driver.username,
                        accountStatus = driver.account_status,
                        faceEmbeddingUrl = driver.face_embedding_url,
                        createdAt = driver.created_at,
                        updatedAt = driver.updated_at,
                        lastLogin = driver.last_login?.let { timestamp ->
                            timestampToIsoString(timestamp)
                        }
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
} 