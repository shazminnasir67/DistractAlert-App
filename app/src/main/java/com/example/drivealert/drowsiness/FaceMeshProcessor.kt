package com.example.drivealert.drowsiness

import android.content.Context
import android.graphics.*
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import kotlin.math.abs

/**
 * MediaPipe Face Mesh processor for extracting facial landmarks
 * Uses ML Kit Face Detection as a simplified alternative to full MediaPipe integration
 */
class FaceMeshProcessor(private val context: Context) {
    
    companion object {
        private const val TAG = "FaceMeshProcessor"
    }
    
    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.15f)
            .enableTracking()
            .build()
    )
    
    /**
     * Convert ImageProxy to Bitmap
     */
    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
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
        
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 100, out)
        val jpegArray = out.toByteArray()
        
        return BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.size)
    }
    
    /**
     * Extract facial landmarks from image
     */
    suspend fun extractLandmarks(imageProxy: ImageProxy): Array<FloatArray>? {
        return try {
            val inputImage = InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
            val faces = faceDetector.process(inputImage).await()
            
            if (faces.isNotEmpty()) {
                val face = faces[0] // Use the first detected face
                convertFaceToLandmarks(face, imageProxy.width, imageProxy.height)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting landmarks", e)
            null
        }
    }
    
    /**
     * Convert ML Kit Face to landmark array format compatible with MediaPipe indices
     * This is a simplified mapping since ML Kit doesn't provide all 468 MediaPipe landmarks
     */
    private fun convertFaceToLandmarks(face: Face, width: Int, height: Int): Array<FloatArray> {
        // Create a landmarks array with 468 positions (MediaPipe standard)
        val landmarks = Array(468) { FloatArray(3) }
        
        // Map available ML Kit landmarks to approximate MediaPipe positions
        val boundingBox = face.boundingBox
        
        // Estimate eye positions based on face bounding box and available landmarks
        val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)
        val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)
        val leftEar = face.getLandmark(FaceLandmark.LEFT_EAR)
        val rightEar = face.getLandmark(FaceLandmark.RIGHT_EAR)
        val noseBase = face.getLandmark(FaceLandmark.NOSE_BASE)
        val leftMouth = face.getLandmark(FaceLandmark.MOUTH_LEFT)
        val rightMouth = face.getLandmark(FaceLandmark.MOUTH_RIGHT)
        val bottomMouth = face.getLandmark(FaceLandmark.MOUTH_BOTTOM)
        
        // Initialize all landmarks to center of face (fallback)
        val centerX = boundingBox.centerX().toFloat()
        val centerY = boundingBox.centerY().toFloat()
        
        for (i in landmarks.indices) {
            landmarks[i][0] = centerX
            landmarks[i][1] = centerY
            landmarks[i][2] = 0f
        }
        
        // Map key landmarks to MediaPipe indices (approximated)
        
        // Right eye landmarks (from MediaPipe perspective, which is mirrored)
        rightEye?.let { eye ->
            val eyeX = eye.position.x
            val eyeY = eye.position.y
            
            // MediaPipe right eye indices: 33, 133, 160, 144, 159, 145, 158, 153
            landmarks[33] = floatArrayOf(eyeX + 10, eyeY, 0f)     // right corner
            landmarks[133] = floatArrayOf(eyeX - 10, eyeY, 0f)   // left corner
            landmarks[160] = floatArrayOf(eyeX, eyeY - 5, 0f)    // top
            landmarks[144] = floatArrayOf(eyeX, eyeY + 5, 0f)    // bottom
            landmarks[159] = floatArrayOf(eyeX - 3, eyeY - 3, 0f) // top-left
            landmarks[145] = floatArrayOf(eyeX - 3, eyeY + 3, 0f) // bottom-left
            landmarks[158] = floatArrayOf(eyeX + 3, eyeY - 3, 0f) // top-right
            landmarks[153] = floatArrayOf(eyeX + 3, eyeY + 3, 0f) // bottom-right
        }
        
        // Left eye landmarks
        leftEye?.let { eye ->
            val eyeX = eye.position.x
            val eyeY = eye.position.y
            
            // MediaPipe left eye indices: 263, 362, 387, 373, 386, 374, 385, 380
            landmarks[263] = floatArrayOf(eyeX - 10, eyeY, 0f)   // left corner
            landmarks[362] = floatArrayOf(eyeX + 10, eyeY, 0f)   // right corner
            landmarks[387] = floatArrayOf(eyeX, eyeY - 5, 0f)    // top
            landmarks[373] = floatArrayOf(eyeX, eyeY + 5, 0f)    // bottom
            landmarks[386] = floatArrayOf(eyeX + 3, eyeY - 3, 0f) // top-right
            landmarks[374] = floatArrayOf(eyeX + 3, eyeY + 3, 0f) // bottom-right
            landmarks[385] = floatArrayOf(eyeX - 3, eyeY - 3, 0f) // top-left
            landmarks[380] = floatArrayOf(eyeX - 3, eyeY + 3, 0f) // bottom-left
        }
        
        // Mouth landmarks
        leftMouth?.let { left ->
            rightMouth?.let { right ->
                bottomMouth?.let { bottom ->
                    val mouthCenterX = (left.position.x + right.position.x) / 2
                    val mouthCenterY = (left.position.y + bottom.position.y) / 2
                    
                    // MediaPipe mouth indices: 61, 291, 39, 181, 0, 17, 269, 405
                    landmarks[61] = floatArrayOf(left.position.x, left.position.y, 0f)      // left corner
                    landmarks[291] = floatArrayOf(right.position.x, right.position.y, 0f)   // right corner
                    landmarks[39] = floatArrayOf(mouthCenterX - 5, mouthCenterY - 5, 0f)    // top-left
                    landmarks[181] = floatArrayOf(mouthCenterX + 5, mouthCenterY - 5, 0f)   // top-right
                    landmarks[0] = floatArrayOf(mouthCenterX, mouthCenterY - 8, 0f)         // top center
                    landmarks[17] = floatArrayOf(mouthCenterX, bottom.position.y, 0f)       // bottom center
                    landmarks[269] = floatArrayOf(mouthCenterX - 8, mouthCenterY, 0f)       // left
                    landmarks[405] = floatArrayOf(mouthCenterX + 8, mouthCenterY, 0f)       // right
                }
            }
        }
        
        return landmarks
    }
    
    /**
     * Release resources
     */
    fun close() {
        // ML Kit handles resource cleanup automatically
    }
}
