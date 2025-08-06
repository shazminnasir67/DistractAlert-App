package com.example.drivealert.data.repository

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface FaceDetectionRepository {
    suspend fun detectFace(image: Bitmap): Boolean
    fun startFaceDetection(): Flow<Boolean>
    fun stopFaceDetection()
    fun createImageAnalysis(): ImageAnalysis
    fun detectFaceInImage(image: InputImage, callback: (List<com.google.mlkit.vision.face.Face>, Exception?) -> Unit)
}

class FaceDetectionRepositoryImpl : FaceDetectionRepository {
    private var isDetecting = false
    private val faceDetector: FaceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setMinFaceSize(0.15f)
            .build()
        
        FaceDetection.getClient(options)
    }
    
    override suspend fun detectFace(image: Bitmap): Boolean {
        return try {
            val inputImage = InputImage.fromBitmap(image, 0)
            val faces = faceDetector.process(inputImage).await()
            faces.isNotEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    override fun startFaceDetection(): Flow<Boolean> = callbackFlow {
        isDetecting = true
        
        awaitClose {
            isDetecting = false
        }
    }
    
    override fun stopFaceDetection() {
        isDetecting = false
    }
    
    override fun createImageAnalysis(): ImageAnalysis {
        return ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { imageAnalysis ->
                imageAnalysis.setAnalyzer(
                    java.util.concurrent.Executors.newSingleThreadExecutor()
                ) { imageProxy ->
                    processImage(imageProxy)
                }
            }
    }
    
    override fun detectFaceInImage(image: InputImage, callback: (List<Face>, Exception?) -> Unit) {
        faceDetector.process(image)
            .addOnSuccessListener { faces ->
                callback(faces, null)
            }
            .addOnFailureListener { exception ->
                callback(emptyList(), exception)
            }
    }
    
    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null && isDetecting) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            faceDetector.process(image)
                .addOnSuccessListener { faces ->
                    // Face detected
                    if (faces.isNotEmpty()) {
                        // We could emit this to a flow if needed
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
} 