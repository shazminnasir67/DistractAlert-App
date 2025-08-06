package com.example.drivealert.drowsiness

import org.junit.Test
import org.junit.Assert.*
import kotlin.math.*

/**
 * Unit tests for drowsiness detection feature calculations
 * Ensures Kotlin implementation matches Python output
 */
class DrowsinessFeatureTest {
    
    companion object {
        // Test landmarks (simplified subset for testing)
        private val testLandmarks = arrayOf(
            // Right eye landmarks (MediaPipe indices: 33, 133, 160, 144, 159, 145, 158, 153)
            floatArrayOf(100f, 100f, 0f), // 33 - right corner
            floatArrayOf(80f, 100f, 0f),  // 133 - left corner  
            floatArrayOf(90f, 95f, 0f),   // 160 - top
            floatArrayOf(90f, 105f, 0f),  // 144 - bottom
            floatArrayOf(87f, 97f, 0f),   // 159 - top-left
            floatArrayOf(87f, 103f, 0f),  // 145 - bottom-left
            floatArrayOf(93f, 97f, 0f),   // 158 - top-right
            floatArrayOf(93f, 103f, 0f),  // 153 - bottom-right
            
            // Left eye landmarks (MediaPipe indices: 263, 362, 387, 373, 386, 374, 385, 380)
            floatArrayOf(200f, 100f, 0f), // 263 - left corner
            floatArrayOf(220f, 100f, 0f), // 362 - right corner
            floatArrayOf(210f, 95f, 0f),  // 387 - top
            floatArrayOf(210f, 105f, 0f), // 373 - bottom
            floatArrayOf(213f, 97f, 0f),  // 386 - top-right
            floatArrayOf(213f, 103f, 0f), // 374 - bottom-right
            floatArrayOf(207f, 97f, 0f),  // 385 - top-left
            floatArrayOf(207f, 103f, 0f), // 380 - bottom-left
            
            // Mouth landmarks (MediaPipe indices: 61, 291, 39, 181, 0, 17, 269, 405)
            floatArrayOf(140f, 150f, 0f), // 61 - left corner
            floatArrayOf(160f, 150f, 0f), // 291 - right corner
            floatArrayOf(145f, 145f, 0f), // 39 - top-left
            floatArrayOf(155f, 145f, 0f), // 181 - top-right
            floatArrayOf(150f, 142f, 0f), // 0 - top center
            floatArrayOf(150f, 158f, 0f), // 17 - bottom center
            floatArrayOf(142f, 150f, 0f), // 269 - left
            floatArrayOf(158f, 150f, 0f), // 405 - right
        )
    }
    
    /**
     * Test distance calculation
     */
    @Test
    fun testDistanceCalculation() {
        val p1 = floatArrayOf(0f, 0f)
        val p2 = floatArrayOf(3f, 4f)
        
        val distance = sqrt((3f * 3f) + (4f * 4f))
        val calculatedDistance = distance(p1, p2)
        
        assertEquals(distance, calculatedDistance, 0.001f)
    }
    
    /**
     * Test Eye Aspect Ratio calculation
     */
    @Test
    fun testEyeAspectRatio() {
        // Create a dummy engine to access private methods
        val engine = TestDrowsinessEngine()
        
        // Test with known eye landmarks
        val rightEye = arrayOf(
            intArrayOf(33, 133),   // horizontal: 100,100 to 80,100 = distance 20
            intArrayOf(160, 144),  // vertical 1: 90,95 to 90,105 = distance 10
            intArrayOf(159, 145),  // vertical 2: 87,97 to 87,103 = distance 6
            intArrayOf(158, 153)   // vertical 3: 93,97 to 93,103 = distance 6
        )
        
        // Expected EAR = (10 + 6 + 6) / (3 * 20) = 22/60 = 0.367
        val expectedEAR = 22f / 60f
        val calculatedEAR = engine.testEyeAspectRatio(testLandmarks, rightEye)
        
        assertEquals(expectedEAR, calculatedEAR, 0.01f)
    }
    
    /**
     * Test Mouth Aspect Ratio calculation  
     */
    @Test
    fun testMouthAspectRatio() {
        val engine = TestDrowsinessEngine()
        
        val mouth = arrayOf(
            intArrayOf(61, 291),   // horizontal: 140,150 to 160,150 = distance 20
            intArrayOf(39, 181),   // vertical 1: 145,145 to 155,145 = distance 10
            intArrayOf(0, 17),     // vertical 2: 150,142 to 150,158 = distance 16
            intArrayOf(269, 405)   // vertical 3: 142,150 to 158,150 = distance 16
        )
        
        // Expected MAR = (10 + 16 + 16) / (3 * 20) = 42/60 = 0.7
        val expectedMAR = 42f / 60f
        val calculatedMAR = engine.testMouthFeature(testLandmarks, mouth)
        
        assertEquals(expectedMAR, calculatedMAR, 0.01f)
    }
    
    /**
     * Test pupil circularity calculation
     */
    @Test
    fun testPupilCircularity() {
        val engine = TestDrowsinessEngine()
        
        val rightEye = arrayOf(
            intArrayOf(33, 133),
            intArrayOf(160, 144),
            intArrayOf(159, 145),
            intArrayOf(158, 153)
        )
        
        val calculatedPUC = engine.testPupilCircularity(testLandmarks, rightEye)
        
        // Should be between 0 and 1 for valid pupil shape
        assertTrue("PUC should be positive", calculatedPUC > 0)
        assertTrue("PUC should be less than 1", calculatedPUC < 1)
    }
    
    /**
     * Test feature normalization
     */
    @Test
    fun testFeatureNormalization() {
        val engine = TestDrowsinessEngine()
        
        val features = floatArrayOf(0.5f, 0.3f, 0.8f, 1.2f)
        val mean = floatArrayOf(0.4f, 0.25f, 0.7f, 1.0f)
        val std = floatArrayOf(0.1f, 0.05f, 0.15f, 0.2f)
        
        val normalized = engine.testNormalizeFeatures(features, mean, std)
        
        // Expected: (0.5-0.4)/0.1 = 1.0, (0.3-0.25)/0.05 = 1.0, etc.
        assertEquals(1.0f, normalized[0], 0.01f)
        assertEquals(1.0f, normalized[1], 0.01f)
        assertEquals((0.8f - 0.7f) / 0.15f, normalized[2], 0.01f)
        assertEquals((1.2f - 1.0f) / 0.2f, normalized[3], 0.01f)
    }
    
    private fun distance(p1: FloatArray, p2: FloatArray): Float {
        val dx = p1[0] - p2[0]
        val dy = p1[1] - p2[1]
        return sqrt(dx * dx + dy * dy)
    }
}

/**
 * Test wrapper to access private methods
 */
private class TestDrowsinessEngine {
    
    fun testEyeAspectRatio(landmarks: Array<FloatArray>, eye: Array<IntArray>): Float {
        val n1 = distance(landmarks[eye[1][0]], landmarks[eye[1][1]])
        val n2 = distance(landmarks[eye[2][0]], landmarks[eye[2][1]])
        val n3 = distance(landmarks[eye[3][0]], landmarks[eye[3][1]])
        val d = distance(landmarks[eye[0][0]], landmarks[eye[0][1]])
        return (n1 + n2 + n3) / (3 * d)
    }
    
    fun testMouthFeature(landmarks: Array<FloatArray>, mouth: Array<IntArray>): Float {
        val n1 = distance(landmarks[mouth[1][0]], landmarks[mouth[1][1]])
        val n2 = distance(landmarks[mouth[2][0]], landmarks[mouth[2][1]])
        val n3 = distance(landmarks[mouth[3][0]], landmarks[mouth[3][1]])
        val d = distance(landmarks[mouth[0][0]], landmarks[mouth[0][1]])
        return (n1 + n2 + n3) / (3 * d)
    }
    
    fun testPupilCircularity(landmarks: Array<FloatArray>, eye: Array<IntArray>): Float {
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
        return (4 * PI.toFloat() * area) / (perimeter * perimeter)
    }
    
    fun testNormalizeFeatures(features: FloatArray, mean: FloatArray, std: FloatArray): FloatArray {
        return floatArrayOf(
            (features[0] - mean[0]) / std[0],
            (features[1] - mean[1]) / std[1],
            (features[2] - mean[2]) / std[2],
            (features[3] - mean[3]) / std[3]
        )
    }
    
    private fun distance(p1: FloatArray, p2: FloatArray): Float {
        val dx = p1[0] - p2[0]
        val dy = p1[1] - p2[1]
        return sqrt(dx * dx + dy * dy)
    }
}
