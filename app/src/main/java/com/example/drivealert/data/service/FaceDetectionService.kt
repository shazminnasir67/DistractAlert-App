package com.example.drivealert.data.service

import android.graphics.Rect
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.tasks.await
import kotlin.math.max
import kotlin.math.min

interface FaceDetectionService {
    suspend fun detectFaces(imageProxy: ImageProxy): List<DetectedFace>
    fun release()
}

data class DetectedFace(
    val boundingBox: Rect,
    val confidence: Float,
    val headEulerAngleY: Float, // Head rotation left-right
    val headEulerAngleZ: Float, // Head rotation tilt
    val leftEyeOpenProbability: Float?,
    val rightEyeOpenProbability: Float?,
    val smilingProbability: Float?
)

class FaceDetectionServiceImpl : FaceDetectionService {
    
    private val detector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.15f) // Minimum face size relative to image
            .enableTracking() // Enable face tracking for smoother bounding boxes
            .build()
        
        FaceDetection.getClient(options)
    }
    
    override suspend fun detectFaces(imageProxy: ImageProxy): List<DetectedFace> {
        return try {
            val image = InputImage.fromMediaImage(
                imageProxy.image!!,
                imageProxy.imageInfo.rotationDegrees
            )
            
            val faces = detector.process(image).await()
            
            faces.mapNotNull { face ->
                // Filter faces with good confidence and proper size
                if (face.boundingBox.width() > 100 && face.boundingBox.height() > 100) {
                    DetectedFace(
                        boundingBox = face.boundingBox,
                        confidence = 1.0f, // ML Kit doesn't provide confidence, assume high
                        headEulerAngleY = face.headEulerAngleY,
                        headEulerAngleZ = face.headEulerAngleZ,
                        leftEyeOpenProbability = face.leftEyeOpenProbability,
                        rightEyeOpenProbability = face.rightEyeOpenProbability,
                        smilingProbability = face.smilingProbability
                    )
                } else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override fun release() {
        // ML Kit handles cleanup automatically
    }
}

// Extension function to convert ML Kit face bounds to normalized coordinates
fun Face.toNormalizedBounds(imageWidth: Int, imageHeight: Int): androidx.compose.ui.geometry.Rect {
    val left = boundingBox.left.toFloat() / imageWidth
    val top = boundingBox.top.toFloat() / imageHeight
    val right = boundingBox.right.toFloat() / imageWidth
    val bottom = boundingBox.bottom.toFloat() / imageHeight
    
    return androidx.compose.ui.geometry.Rect(
        left = max(0f, left),
        top = max(0f, top),
        right = min(1f, right),
        bottom = min(1f, bottom)
    )
}
