package com.example.drivealert.drowsiness

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

/**
 * Full MediaPipe Face Landmarker for 478 facial landmarks
 * Direct equivalent to Python MediaPipe FaceMesh
 */
class MediaPipeFaceLandmarker(private val context: Context) {
    
    companion object {
        private const val TAG = "MediaPipeFaceLandmarker"
        private const val MODEL_FILE = "face_landmarker.task"
    }
    
    private var faceLandmarker: FaceLandmarker? = null
    private var isInitialized = false
    
    /**
     * Initialize MediaPipe Face Landmarker
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_FILE)
                .build()
            
            val options = FaceLandmarker.FaceLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .setNumFaces(1)
                .setMinFaceDetectionConfidence(0.3f)
                .setMinFacePresenceConfidence(0.3f)
                .setMinTrackingConfidence(0.8f)
                .setOutputFaceBlendshapes(false)
                .setOutputFacialTransformationMatrixes(false)
                .build()
            
            faceLandmarker = FaceLandmarker.createFromOptions(context, options)
            isInitialized = true
            Log.d(TAG, "MediaPipe Face Landmarker initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MediaPipe Face Landmarker", e)
            false
        }
    }
    
    /**
     * Extract 478 facial landmarks from ImageProxy
     */
    suspend fun extractLandmarks(imageProxy: ImageProxy): Array<FloatArray>? = withContext(Dispatchers.Default) {
        try {
            if (!isInitialized || faceLandmarker == null) {
                Log.w(TAG, "Face Landmarker not initialized")
                return@withContext null
            }
            
            // Convert ImageProxy to Bitmap
            val bitmap = imageProxyToBitmap(imageProxy)
            
            // Convert to MediaPipe format
            val mpImage = BitmapImageBuilder(bitmap).build()
            
            // Detect landmarks
            val result = faceLandmarker!!.detect(mpImage)
            
            // Convert result to landmarks array
            convertResultToLandmarks(result, bitmap.width, bitmap.height)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting landmarks", e)
            null
        }
    }
    
    /**
     * Convert ImageProxy to Bitmap efficiently
     */
    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val buffer: ByteBuffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        
        // For YUV format, we need proper conversion
        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer
        
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        
        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)
        
        val yuvImage = android.graphics.YuvImage(
            nv21, 
            android.graphics.ImageFormat.NV21, 
            imageProxy.width, 
            imageProxy.height, 
            null
        )
        
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(
            android.graphics.Rect(0, 0, imageProxy.width, imageProxy.height), 
            100, 
            out
        )
        val jpegArray = out.toByteArray()
        
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.size)
        
        // Handle rotation if needed
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        return if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }
    
    /**
     * Convert MediaPipe result to landmarks array compatible with original algorithm
     */
    private fun convertResultToLandmarks(
        result: FaceLandmarkerResult, 
        imageWidth: Int, 
        imageHeight: Int
    ): Array<FloatArray>? {
        
        if (result.faceLandmarks().isEmpty()) {
            return null
        }
        
        val landmarks = result.faceLandmarks()[0] // Get first face
        
        // MediaPipe returns 478 landmarks - exactly what we need!
        val landmarkArray = Array(478) { FloatArray(3) }
        
        for (i in landmarks.indices) {
            val landmark = landmarks[i]
            landmarkArray[i][0] = landmark.x() * imageWidth   // X coordinate
            landmarkArray[i][1] = landmark.y() * imageHeight  // Y coordinate
            landmarkArray[i][2] = landmark.z()                // Z coordinate (depth)
        }
        
        Log.d(TAG, "Extracted ${landmarks.size} landmarks from MediaPipe")
        return landmarkArray
    }
    
    /**
     * Release resources
     */
    fun close() {
        faceLandmarker?.close()
        isInitialized = false
    }
}
