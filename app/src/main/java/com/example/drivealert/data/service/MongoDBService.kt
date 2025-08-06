package com.example.drivealert.data.service

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// Data models matching your MongoDB structure
data class Driver(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("user_id") val userId: String,
    @SerializedName("fleet_manager_id") val fleetManagerId: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val email: String,
    val phone: String,
    @SerializedName("license_number") val licenseNumber: String,
    val username: String,
    @SerializedName("account_status") val accountStatus: String,
    @SerializedName("face_embedding_url") val faceEmbeddingUrl: String, // JSON string array
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("updated_at") val updatedAt: Long,
    @SerializedName("last_login") val lastLogin: Long?
) {
    fun getFaceEmbedding(): List<Float> {
        return try {
            val gson = Gson()
            val floatArray = gson.fromJson(faceEmbeddingUrl, Array<Float>::class.java)
            floatArray.toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

// API Request/Response models
data class AuthenticationRequest(
    @SerializedName("face_embedding") val faceEmbedding: List<Float>
)

data class AuthenticationResponse(
    val success: Boolean,
    val driver: Driver? = null,
    val message: String? = null,
    val confidence: Float? = null
)

data class UpdateLastLoginRequest(
    @SerializedName("user_id") val userId: String
)

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)

// Retrofit API interface for your backend service
interface DriverAuthAPI {
    
    @POST("/api/auth/login")
    suspend fun authenticateDriver(@Body request: AuthenticationRequest): AuthenticationResponse
    
    @GET("/api/drivers/active")
    suspend fun getActiveDrivers(): ApiResponse<List<Driver>>
    
    @PUT("/api/drivers/last-login")
    suspend fun updateLastLogin(@Body request: UpdateLastLoginRequest): ApiResponse<Boolean>
    
    @GET("/api/drivers/{userId}")
    suspend fun getDriverById(@Path("userId") userId: String): ApiResponse<Driver>
    
    @GET("/api/health")
    suspend fun healthCheck(): ApiResponse<String>
}

// Service interface
interface DriverAuthService {
    suspend fun authenticateDriver(faceEmbedding: List<Float>): Result<Driver>
    suspend fun updateDriverLastLogin(userId: String): Result<Boolean>
    suspend fun getAllActiveDrivers(): Result<List<Driver>>
    suspend fun checkBackendHealth(): Result<Boolean>
}

class DriverAuthServiceImpl : DriverAuthService {
    
    // Use backend configuration from BackendConfig
    private val baseUrl = com.example.drivealert.utils.BackendConfig.CURRENT_BASE_URL
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .client(
            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        )
        .build()
    
    private val api = retrofit.create(DriverAuthAPI::class.java)
    
    companion object {
        private const val SIMILARITY_THRESHOLD = 0.85f
    }
    
    override suspend fun authenticateDriver(faceEmbedding: List<Float>): Result<Driver> = withContext(Dispatchers.IO) {
        try {
            val response = api.authenticateDriver(AuthenticationRequest(faceEmbedding))
            
            if (response.success && response.driver != null) {
                Result.success(response.driver)
            } else {
                Result.failure(Exception(response.message ?: "Authentication failed"))
            }
        } catch (e: Exception) {
            // Fallback to mock data for development when backend is not available
            handleBackendUnavailable(faceEmbedding)
        }
    }
    
    override suspend fun getAllActiveDrivers(): Result<List<Driver>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getActiveDrivers()
            
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch drivers"))
            }
        } catch (e: Exception) {
            // Fallback to mock data for development
            Result.success(getMockDrivers())
        }
    }
    
    override suspend fun updateDriverLastLogin(userId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateLastLogin(UpdateLastLoginRequest(userId))
            Result.success(response.success)
        } catch (e: Exception) {
            // Return true for mock purposes when backend is unavailable
            Result.success(true)
        }
    }
    
    override suspend fun checkBackendHealth(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.healthCheck()
            Result.success(response.success)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun handleBackendUnavailable(faceEmbedding: List<Float>): Result<Driver> {
        // Local face recognition when backend is not available
        val mockDrivers = getMockDrivers()
        
        var bestMatch: Driver? = null
        var bestSimilarity = 0f
        
        for (driver in mockDrivers) {
            val driverEmbedding = driver.getFaceEmbedding()
            if (driverEmbedding.isNotEmpty()) {
                val similarity = calculateCosineSimilarity(faceEmbedding, driverEmbedding)
                if (similarity > bestSimilarity && similarity >= SIMILARITY_THRESHOLD) {
                    bestSimilarity = similarity
                    bestMatch = driver
                }
            }
        }
        
        return if (bestMatch != null) {
            Result.success(bestMatch)
        } else {
            Result.failure(Exception("No matching driver found with sufficient similarity"))
        }
    }
    
    private fun getMockDrivers(): List<Driver> {
        return listOf(
            Driver(
                id = "68933f184a78e8e1e4bc1be8",
                userId = "9e211492-f704-4343-8f67-c9b438621e5d",
                fleetManagerId = "dd255d70-4dce-4368-bf1f-553b9591ad55",
                firstName = "shazmin",
                lastName = "Nasir",
                email = "alviawan97@gmail.com",
                phone = "0312527122",
                licenseNumber = "123456789",
                username = "shazmin34",
                accountStatus = "Active",
                faceEmbeddingUrl = "[-0.09834085404872894,0.11783138662576675,0.13707754015922546,0.02797902747988701,-0.03228609636425972,0.09501434117555618,-0.08029597252607346,-0.0622604638338089,0.20644114911556244,-0.1323203295469284,0.2458396553993225,0.04394255951046944,-0.17177675664424896,-0.05712488293647766,-0.029764723032712936,0.10989474505186081,-0.07415200769901276,-0.15377925336360931,-0.06087956577539444,-0.06169208139181137,0.009081654250621796,-0.0006946372450329363,0.01326451264321804,-0.031153827905654907,-0.21901454031467438,-0.33191004395484924,-0.1551847904920578,-0.1450297236442566,-0.04931991174817085,-0.13254988193511963,-0.04590509086847305,-0.051555272191762924,-0.1150595024228096,-0.020522328093647957,0.005001645535230637,0.0948665663599968,-0.03560461103916168,-0.0008378866477869451,0.1647413671016693,0.030452100560069084,-0.13024291396141052,0.07655229419469833,0.07913310080766678,0.3063906729221344,0.17120061814785004,0.04857795313000679,0.04730496183037758,-0.07343095541000366,0.17016513645648956,-0.23140527307987213,0.08515385538339615,0.12444861978292465,0.15600647032260895,0.11863899230957031,0.15992088615894318,-0.10776940733194351,0.014770329929888248,0.12158510833978653,-0.11380749195814133,0.12302374094724655,0.021068217232823372,0.02025976963341236,-0.0679074227809906,-0.00552736921235919,0.27187052369117737,0.18177837133407593,-0.15848329663276672,-0.1552143692970276,0.1299312561750412,-0.08367230743169785,-0.07125630974769592,0.10792309045791626,-0.18010640144348145,-0.1434440016746521,-0.30088135600090027,0.05827130749821663,0.5407305955886841,0.06181129068136215,-0.19185686111450195,0.011811494827270508,-0.10945548862218857,-0.0064972466789186,-0.02997695282101631,0.07792920619249344,-0.13778425753116608,0.02216741442680359,-0.06863357126712799,0.03900708630681038,0.22937342524528503,0.03785522282123566,-0.045360956341028214,0.11700747907161713,-0.011894028633832932,0.08073078095912933,0.023117762058973312,0.06797611713409424,-0.1262803077697754,-0.08267057687044144,-0.14302238821983337,-0.08804334700107574,-0.011712574400007725,-0.05734122544527054,-0.03160306066274643,0.09893038123846054,-0.23868800699710846,0.20952744781970978,0.010747644118964672,-0.01664113625884056,-0.008658597245812416,0.10399802029132843,-0.10111366212368011,0.08191155642271042,0.17208899557590485,-0.32462432980537415,0.16157497465610504,0.12616032361984253,0.02581685408949852,0.08928381651639938,0.08923950791358948,0.048977047204971313,-0.06518929451704025,0.05025038123130798,-0.14892755448818207,-0.08886446803808212,-0.04651416838169098,-0.060249291360378265,0.05184382200241089,0.022104516625404358]",
                createdAt = 1747236528669,
                updatedAt = 1747236528669,
                lastLogin = null
            )
        )
    }
    
    private fun calculateCosineSimilarity(embedding1: List<Float>, embedding2: List<Float>): Float {
        if (embedding1.size != embedding2.size) return 0f
        
        var dotProduct = 0f
        var norm1 = 0f
        var norm2 = 0f
        
        for (i in embedding1.indices) {
            dotProduct += embedding1[i] * embedding2[i]
            norm1 += embedding1[i] * embedding1[i]
            norm2 += embedding2[i] * embedding2[i]
        }
        
        val denominator = kotlin.math.sqrt(norm1) * kotlin.math.sqrt(norm2)
        return if (denominator != 0f) dotProduct / denominator else 0f
    }
}

// Legacy compatibility - keeping old interface
interface MongoDBService {
    suspend fun authenticateDriver(faceEmbedding: List<Float>): Result<MongoDBDriver>
    suspend fun updateDriverLastLogin(userId: String): Result<Boolean>
    suspend fun getAllActiveDrivers(): Result<List<MongoDBDriver>>
}

data class MongoDBDriver(
    val _id: String? = null,
    val user_id: String,
    val fleet_manager_id: String,
    val first_name: String,
    val last_name: String,
    val email: String,
    val phone: String,
    val license_number: String,
    val username: String,
    val account_status: String,
    val face_embedding_url: String,
    val created_at: Long,
    val updated_at: Long,
    val last_login: Long?
) {
    fun getFaceEmbedding(): List<Float> {
        return try {
            val gson = Gson()
            val floatArray = gson.fromJson(face_embedding_url, Array<Float>::class.java)
            floatArray.toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

class MongoDBServiceImpl : MongoDBService {
    private val driverAuthService = DriverAuthServiceImpl()
    
    override suspend fun authenticateDriver(faceEmbedding: List<Float>): Result<MongoDBDriver> {
        return try {
            val result = driverAuthService.authenticateDriver(faceEmbedding)
            if (result.isSuccess) {
                val driver = result.getOrNull()!!
                val mongoDriver = MongoDBDriver(
                    _id = driver.id,
                    user_id = driver.userId,
                    fleet_manager_id = driver.fleetManagerId,
                    first_name = driver.firstName,
                    last_name = driver.lastName,
                    email = driver.email,
                    phone = driver.phone,
                    license_number = driver.licenseNumber,
                    username = driver.username,
                    account_status = driver.accountStatus,
                    face_embedding_url = driver.faceEmbeddingUrl,
                    created_at = driver.createdAt,
                    updated_at = driver.updatedAt,
                    last_login = driver.lastLogin
                )
                Result.success(mongoDriver)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Authentication failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateDriverLastLogin(userId: String): Result<Boolean> {
        return driverAuthService.updateDriverLastLogin(userId)
    }
    
    override suspend fun getAllActiveDrivers(): Result<List<MongoDBDriver>> {
        return try {
            val result = driverAuthService.getAllActiveDrivers()
            if (result.isSuccess) {
                val drivers = result.getOrNull()!!.map { driver ->
                    MongoDBDriver(
                        _id = driver.id,
                        user_id = driver.userId,
                        fleet_manager_id = driver.fleetManagerId,
                        first_name = driver.firstName,
                        last_name = driver.lastName,
                        email = driver.email,
                        phone = driver.phone,
                        license_number = driver.licenseNumber,
                        username = driver.username,
                        account_status = driver.accountStatus,
                        face_embedding_url = driver.faceEmbeddingUrl,
                        created_at = driver.createdAt,
                        updated_at = driver.updatedAt,
                        last_login = driver.lastLogin
                    )
                }
                Result.success(drivers)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to fetch drivers"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Legacy compatibility types
data class RegistrationRequest(
    val name: String,
    val email: String,
    val faceEmbedding: List<Float>
) 