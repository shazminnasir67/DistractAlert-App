package com.example.drivealert.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id")
    val id: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("fleet_manager_id")
    val fleetManagerId: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("license_number")
    val licenseNumber: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("account_status")
    val accountStatus: String,
    @SerializedName("face_embedding_url")
    val faceEmbeddingUrl: String,
    @SerializedName("created_at")
    val createdAt: Long,
    @SerializedName("updated_at")
    val updatedAt: Long,
    @SerializedName("last_login")
    val lastLogin: String? = null
) {
    val fullName: String
        get() = "$firstName $lastName"
    
    val faceEmbedding: List<Float>
        get() = parseFaceEmbedding(faceEmbeddingUrl)
    
    private fun parseFaceEmbedding(embeddingString: String): List<Float> {
        return try {
            embeddingString
                .removeSurrounding("[", "]")
                .split(",")
                .map { it.trim().toFloat() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

data class FaceRecognitionResult(
    val isSuccess: Boolean,
    val user: User? = null,
    val confidence: Float = 0f,
    val error: String? = null
)

data class LoginResult(
    val isSuccess: Boolean,
    val user: User? = null,
    val error: String? = null
) 