package com.example.drivealert.drowsiness

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*

/**
 * Drowsiness Detection Engine based on MediaPipe FaceMesh + PyTorch LSTM
 * Implements the exact algorithm from inference.py for Android
 */
class DrowsinessDetectionEngine(private val context: Context) {
    
    companion object {
        private const val TAG = "DrowsinessDetection"
        private const val CALIBRATION_FRAMES = 25
        private const val DETECTION_FRAMES = 20
        private const val ANALYSIS_INTERVAL = 15
        private const val DECAY_FACTOR = 0.9f
    }
    
    // Detection engines
    private val lstmClassifier = LSTMDrowsinessClassifier(context)
    private var useLSTMModel = false
    
    // Eye landmark positions (MediaPipe FaceMesh indices)
    private val rightEye = arrayOf(
        intArrayOf(33, 133),   // horizontal
        intArrayOf(160, 144),  // vertical 1
        intArrayOf(159, 145),  // vertical 2
        intArrayOf(158, 153)   // vertical 3
    )
    
    private val leftEye = arrayOf(
        intArrayOf(263, 362),  // horizontal
        intArrayOf(387, 373),  // vertical 1
        intArrayOf(386, 374),  // vertical 2
        intArrayOf(385, 380)   // vertical 3
    )
    
    // Mouth landmark positions
    private val mouth = arrayOf(
        intArrayOf(61, 291),   // horizontal
        intArrayOf(39, 181),   // vertical 1
        intArrayOf(0, 17),     // vertical 2
        intArrayOf(269, 405)   // vertical 3
    )
    
    // Detection state
    private var isCalibrated = false
    private var calibrationFrameCount = 0
    private var framesSinceLastAnalysis = 0
    
    // Calibration values
    private var earsNorm = FloatArray(2) // [mean, std]
    private var marsNorm = FloatArray(2)
    private var pucsNorm = FloatArray(2)
    private var moesNorm = FloatArray(2)
    
    // Current feature values (smoothed)
    private var earMain = 0f
    private var marMain = 0f
    private var pucMain = 0f
    private var moeMain = 0f
    
    // Feature history for classification
    private val featureHistory = mutableListOf<FloatArray>()
    
    // Calibration data collection
    private val calibrationEars = mutableListOf<Float>()
    private val calibrationMars = mutableListOf<Float>()
    private val calibrationPucs = mutableListOf<Float>()
    private val calibrationMoes = mutableListOf<Float>()
    
    /**
     * Calculate Euclidean distance between two 2D points
     */
    private fun distance(p1: FloatArray, p2: FloatArray): Float {
        val dx = p1[0] - p2[0]
        val dy = p1[1] - p2[1]
        return sqrt(dx * dx + dy * dy)
    }
    
    /**
     * Calculate Eye Aspect Ratio (EAR) with NaN protection
     */
    private fun eyeAspectRatio(landmarks: Array<FloatArray>, eye: Array<IntArray>): Float {
        val n1 = distance(landmarks[eye[1][0]], landmarks[eye[1][1]])
        val n2 = distance(landmarks[eye[2][0]], landmarks[eye[2][1]])
        val n3 = distance(landmarks[eye[3][0]], landmarks[eye[3][1]])
        val d = distance(landmarks[eye[0][0]], landmarks[eye[0][1]])
        
        // Prevent division by zero and invalid values
        return if (d > 0.001f) {
            val result = (n1 + n2 + n3) / (3 * d)
            if (result.isNaN() || result.isInfinite()) 0.25f else result
        } else {
            0.25f // Default EAR value
        }
    }
    
    /**
     * Calculate average eye feature with validation
     */
    private fun eyeFeature(landmarks: Array<FloatArray>): Float {
        val leftEAR = eyeAspectRatio(landmarks, leftEye)
        val rightEAR = eyeAspectRatio(landmarks, rightEye)
        val result = (leftEAR + rightEAR) / 2f
        return if (result.isNaN() || result.isInfinite()) 0.25f else result
    }
    
    /**
     * Calculate mouth feature (Mouth Aspect Ratio) with NaN protection
     */
    private fun mouthFeature(landmarks: Array<FloatArray>): Float {
        val n1 = distance(landmarks[mouth[1][0]], landmarks[mouth[1][1]])
        val n2 = distance(landmarks[mouth[2][0]], landmarks[mouth[2][1]])
        val n3 = distance(landmarks[mouth[3][0]], landmarks[mouth[3][1]])
        val d = distance(landmarks[mouth[0][0]], landmarks[mouth[0][1]])
        
        // Prevent division by zero and invalid values
        return if (d > 0.001f) {
            val result = (n1 + n2 + n3) / (3 * d)
            if (result.isNaN() || result.isInfinite()) 0.5f else result
        } else {
            0.5f // Default MAR value
        }
    }
    
    /**
     * Calculate pupil circularity for one eye with validation
     */
    private fun pupilCircularity(landmarks: Array<FloatArray>, eye: Array<IntArray>): Float {
        val perimeter = distance(landmarks[eye[0][0]], landmarks[eye[1][0]]) +
                distance(landmarks[eye[1][0]], landmarks[eye[2][0]]) +
                distance(landmarks[eye[2][0]], landmarks[eye[3][0]]) +
                distance(landmarks[eye[3][0]], landmarks[eye[0][1]]) +
                distance(landmarks[eye[0][1]], landmarks[eye[3][1]]) +
                distance(landmarks[eye[3][1]], landmarks[eye[2][1]]) +
                distance(landmarks[eye[2][1]], landmarks[eye[1][1]]) +
                distance(landmarks[eye[1][1]], landmarks[eye[0][0]])
        
        val radius = distance(landmarks[eye[1][0]], landmarks[eye[3][1]]) * 0.5f
        val area = PI.toFloat() * radius * radius
        
        // Prevent division by zero and invalid values
        return if (perimeter > 0.001f && area > 0.001f) {
            val result = (4 * PI.toFloat() * area) / (perimeter * perimeter)
            if (result.isNaN() || result.isInfinite()) 0.8f else result
        } else {
            0.8f // Default PUC value
        }
    }
    
    /**
     * Calculate average pupil feature with validation
     */
    private fun pupilFeature(landmarks: Array<FloatArray>): Float {
        val leftPUC = pupilCircularity(landmarks, leftEye)
        val rightPUC = pupilCircularity(landmarks, rightEye)
        val result = (leftPUC + rightPUC) / 2f
        return if (result.isNaN() || result.isInfinite()) 0.8f else result
    }
    
    /**
     * Process face landmarks and extract features with validation
     */
    private fun extractFeatures(landmarks: Array<FloatArray>): FloatArray {
        val ear = eyeFeature(landmarks)
        val mar = mouthFeature(landmarks)
        val puc = pupilFeature(landmarks)
        val moe = if (ear > 0.001f) {
            val result = mar / ear
            if (result.isNaN() || result.isInfinite()) 2.0f else result
        } else {
            2.0f // Default MOE value
        }
        return floatArrayOf(ear, mar, puc, moe)
    }
    
    /**
     * Perform calibration with current frame
     */
    private suspend fun calibrate(landmarks: Array<FloatArray>): Boolean = withContext(Dispatchers.Default) {
        val features = extractFeatures(landmarks)
        
        calibrationEars.add(features[0])
        calibrationMars.add(features[1])
        calibrationPucs.add(features[2])
        calibrationMoes.add(features[3])
        
        calibrationFrameCount++
        
        if (calibrationFrameCount >= CALIBRATION_FRAMES) {
            // Calculate normalization values
            earsNorm[0] = calibrationEars.average().toFloat()
            earsNorm[1] = calculateStd(calibrationEars, earsNorm[0])
            
            marsNorm[0] = calibrationMars.average().toFloat()
            marsNorm[1] = calculateStd(calibrationMars, marsNorm[0])
            
            pucsNorm[0] = calibrationPucs.average().toFloat()
            pucsNorm[1] = calculateStd(calibrationPucs, pucsNorm[0])
            
            moesNorm[0] = calibrationMoes.average().toFloat()
            moesNorm[1] = calculateStd(calibrationMoes, moesNorm[0])
            
            isCalibrated = true
            Log.d(TAG, "Calibration completed: EAR(${earsNorm[0]}, ${earsNorm[1]})")
            return@withContext true
        }
        
        return@withContext false
    }
    
    /**
     * Calculate standard deviation
     */
    private fun calculateStd(values: List<Float>, mean: Float): Float {
        val variance = values.map { (it - mean) * (it - mean) }.average()
        return sqrt(variance).toFloat()
    }
    
    /**
     * Normalize features using calibration values
     */
    private fun normalizeFeatures(features: FloatArray): FloatArray {
        return floatArrayOf(
            (features[0] - earsNorm[0]) / earsNorm[1],
            (features[1] - marsNorm[0]) / marsNorm[1],
            (features[2] - pucsNorm[0]) / pucsNorm[1],
            (features[3] - moesNorm[0]) / moesNorm[1]
        )
    }
    
    /**
     * Smooth features using decay factor
     */
    private fun smoothFeatures(newFeatures: FloatArray) {
        if (earMain == -1000f) {
            // First valid frame
            earMain = newFeatures[0]
            marMain = newFeatures[1]
            pucMain = newFeatures[2]
            moeMain = newFeatures[3]
        } else {
            // Apply exponential smoothing
            earMain = earMain * DECAY_FACTOR + (1 - DECAY_FACTOR) * newFeatures[0]
            marMain = marMain * DECAY_FACTOR + (1 - DECAY_FACTOR) * newFeatures[1]
            pucMain = pucMain * DECAY_FACTOR + (1 - DECAY_FACTOR) * newFeatures[2]
            moeMain = moeMain * DECAY_FACTOR + (1 - DECAY_FACTOR) * newFeatures[3]
        }
    }
    
    /**
     * Initialize the detection engine and try to load LSTM model
     */
    suspend fun initialize(): Boolean {
        return try {
            useLSTMModel = lstmClassifier.initialize()
            if (useLSTMModel) {
                Log.d(TAG, "Using PyTorch LSTM model for classification")
            } else {
                Log.d(TAG, "Using heuristic classification (LSTM model not available)")
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize detection engine", e)
            useLSTMModel = false
            true // Still proceed with heuristic
        }
    }
    
    /**
     * Simple classification based on feature patterns (fallback)
     */
    private fun heuristicClassification(): Int {
        if (featureHistory.size < DETECTION_FRAMES) return 0
        
        // Simple heuristic: if EAR is consistently low, classify as drowsy
        val recentFrames = featureHistory.takeLast(10)
        val lowEarCount = recentFrames.count { it[0] < -1.0f }
        
        return if (lowEarCount >= 6) 1 else 0
    }
    
    /**
     * Enhanced classification using LSTM model or heuristic fallback
     */
    private fun classifyDrowsiness(): Int {
        if (featureHistory.size < DETECTION_FRAMES) return 0
        
        return if (useLSTMModel && lstmClassifier.isReady()) {
            // Use PyTorch LSTM model (exact same as Python)
            lstmClassifier.classifyDrowsiness(featureHistory)
        } else {
            // Use heuristic fallback
            heuristicClassification()
        }
    }
    
    /**
     * Process image for drowsiness detection
     */
    suspend fun processImage(landmarks: Array<FloatArray>, imageWidth: Int = 0, imageHeight: Int = 0): DrowsinessResult = withContext(Dispatchers.Default) {
        try {
            // If not calibrated, perform calibration
            if (!isCalibrated) {
                val calibrationComplete = calibrate(landmarks)
                return@withContext DrowsinessResult(
                    isCalibrating = true,
                    calibrationProgress = calibrationFrameCount.toFloat() / CALIBRATION_FRAMES,
                    features = extractFeatures(landmarks),
                    faceLandmarks = landmarks,
                    imageWidth = imageWidth,
                    imageHeight = imageHeight
                )
            }
            
            // Extract and normalize features
            val rawFeatures = extractFeatures(landmarks)
            val normalizedFeatures = normalizeFeatures(rawFeatures)
            
            // Smooth features
            smoothFeatures(normalizedFeatures)
            
            // Add to history
            if (featureHistory.size >= DETECTION_FRAMES) {
                featureHistory.removeAt(0)
            }
            featureHistory.add(floatArrayOf(earMain, marMain, pucMain, moeMain))
            
            // Perform classification every ANALYSIS_INTERVAL frames
            framesSinceLastAnalysis++
            var drowsinessState = 0
            
            if (framesSinceLastAnalysis >= ANALYSIS_INTERVAL && featureHistory.size == DETECTION_FRAMES) {
                framesSinceLastAnalysis = 0
                drowsinessState = classifyDrowsiness()
                Log.d(TAG, "Classification result: ${if (drowsinessState == 1) "DROWSY" else "ALERT"}")
            }
            
            DrowsinessResult(
                isCalibrating = false,
                isDrowsy = drowsinessState == 1,
                features = floatArrayOf(earMain, marMain, pucMain, moeMain),
                rawFeatures = rawFeatures,
                faceLandmarks = landmarks,
                imageWidth = imageWidth,
                imageHeight = imageHeight
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image", e)
            DrowsinessResult(error = e.message)
        }
    }
    
    /**
     * Reset calibration and models
     */
    fun resetCalibration() {
        isCalibrated = false
        calibrationFrameCount = 0
        calibrationEars.clear()
        calibrationMars.clear()
        calibrationPucs.clear()
        calibrationMoes.clear()
        featureHistory.clear()
        earMain = 0f
        marMain = 0f
        pucMain = 0f
        moeMain = 0f
        framesSinceLastAnalysis = 0
    }
    
    /**
     * Clean up resources
     */
    fun close() {
        lstmClassifier.close()
    }
}

/**
 * Result of drowsiness detection processing
 */
data class DrowsinessResult(
    val isCalibrating: Boolean = false,
    val calibrationProgress: Float = 0f,
    val isDrowsy: Boolean = false,
    val features: FloatArray? = null,
    val rawFeatures: FloatArray? = null,
    val faceLandmarks: Array<FloatArray>? = null,
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val error: String? = null
)
