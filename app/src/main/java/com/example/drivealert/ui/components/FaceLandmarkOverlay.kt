package com.example.drivealert.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.drivealert.ui.theme.*

/**
 * Face Landmark Overlay for real-time visualization
 * Draws 478 MediaPipe face landmarks on camera preview
 */
@Composable
fun FaceLandmarkOverlay(
    landmarks: Array<FloatArray>?,
    imageWidth: Int,
    imageHeight: Int,
    previewWidth: Float,
    previewHeight: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        landmarks?.let { landmarkArray ->
            drawFaceLandmarks(
                landmarks = landmarkArray,
                imageWidth = imageWidth,
                imageHeight = imageHeight,
                canvasWidth = size.width,
                canvasHeight = size.height
            )
        }
    }
}

/**
 * Draw face landmarks with different colors for different face regions
 */
private fun DrawScope.drawFaceLandmarks(
    landmarks: Array<FloatArray>,
    imageWidth: Int,
    imageHeight: Int,
    canvasWidth: Float,
    canvasHeight: Float
) {
    if (landmarks.isEmpty() || imageWidth <= 0 || imageHeight <= 0) return
    
    // Scale factors to map from image coordinates to canvas coordinates
    val scaleX = canvasWidth / imageWidth
    val scaleY = canvasHeight / imageHeight
    
    // Define landmark groups with colors (MediaPipe FaceMesh indices)
    val landmarkGroups = mapOf(
        // Face contour (0-16)
        "faceContour" to Pair(0..16, NeonGreen.copy(alpha = 0.8f)),
        
        // Left eyebrow (17-21)
        "leftEyebrow" to Pair(17..21, NeonBlue.copy(alpha = 0.8f)),
        
        // Right eyebrow (22-26)
        "rightEyebrow" to Pair(22..26, NeonBlue.copy(alpha = 0.8f)),
        
        // Nose bridge (27-30)
        "noseBridge" to Pair(27..30, NeonYellow.copy(alpha = 0.8f)),
        
        // Nose tip (31-35)
        "noseTip" to Pair(31..35, NeonYellow.copy(alpha = 0.8f)),
        
        // Left eye (36-41)
        "leftEye" to Pair(36..41, ErrorRed.copy(alpha = 0.9f)),
        
        // Right eye (42-47)
        "rightEye" to Pair(42..47, ErrorRed.copy(alpha = 0.9f)),
        
        // Mouth (48-67)
        "mouth" to Pair(48..67, NeonPink.copy(alpha = 0.8f))
    )
    
    // Draw landmarks for each group
    landmarkGroups.forEach { (_, groupData) ->
        val (range, color) = groupData
        range.forEach { index ->
            if (index < landmarks.size) {
                val landmark = landmarks[index]
                val x = landmark[0] * scaleX
                val y = landmark[1] * scaleY
                
                // Draw landmark point
                drawCircle(
                    color = color,
                    radius = 2.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            }
        }
    }
    
    // Draw all 478 landmarks as small dots (for complete MediaPipe coverage)
    landmarks.forEachIndexed { index, landmark ->
        val x = landmark[0] * scaleX
        val y = landmark[1] * scaleY
        
        // Different colors for key facial features
        val color = when {
            // Key eye landmarks (MediaPipe specific indices)
            index in listOf(33, 133, 160, 144, 159, 145, 158, 153) -> ErrorRed // Right eye
            index in listOf(263, 362, 387, 373, 386, 374, 385, 380) -> ErrorRed // Left eye
            
            // Key mouth landmarks
            index in listOf(61, 291, 39, 181, 0, 17, 269, 405) -> NeonPink
            
            // General face mesh
            else -> LightGray2.copy(alpha = 0.4f)
        }
        
        drawCircle(
            color = color,
            radius = 1.5.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(x, y)
        )
    }
    
    // Draw eye contours for better visualization
    drawEyeContours(landmarks, scaleX, scaleY)
    drawMouthContour(landmarks, scaleX, scaleY)
}

/**
 * Draw eye contours for enhanced visualization
 */
private fun DrawScope.drawEyeContours(
    landmarks: Array<FloatArray>,
    scaleX: Float,
    scaleY: Float
) {
    // Right eye contour (MediaPipe indices)
    val rightEyeIndices = listOf(33, 7, 163, 144, 145, 153, 154, 155, 133, 173, 157, 158, 159, 160, 161, 246)
    // Left eye contour
    val leftEyeIndices = listOf(362, 382, 381, 380, 374, 373, 390, 249, 263, 466, 388, 387, 386, 385, 384, 398)
    
    // Draw right eye
    drawContour(landmarks, rightEyeIndices, scaleX, scaleY, ErrorRed.copy(alpha = 0.7f))
    
    // Draw left eye  
    drawContour(landmarks, leftEyeIndices, scaleX, scaleY, ErrorRed.copy(alpha = 0.7f))
}

/**
 * Draw mouth contour for enhanced visualization
 */
private fun DrawScope.drawMouthContour(
    landmarks: Array<FloatArray>,
    scaleX: Float,
    scaleY: Float
) {
    // Mouth outer contour (MediaPipe indices)
    val mouthIndices = listOf(61, 84, 17, 314, 405, 320, 307, 375, 321, 308, 324, 318)
    
    drawContour(landmarks, mouthIndices, scaleX, scaleY, NeonPink.copy(alpha = 0.7f))
}

/**
 * Helper function to draw contour lines
 */
private fun DrawScope.drawContour(
    landmarks: Array<FloatArray>,
    indices: List<Int>,
    scaleX: Float,
    scaleY: Float,
    color: Color
) {
    if (indices.size < 2) return
    
    for (i in 0 until indices.size - 1) {
        val currentIndex = indices[i]
        val nextIndex = indices[i + 1]
        
        if (currentIndex < landmarks.size && nextIndex < landmarks.size) {
            val start = landmarks[currentIndex]
            val end = landmarks[nextIndex]
            
            drawLine(
                color = color,
                start = androidx.compose.ui.geometry.Offset(start[0] * scaleX, start[1] * scaleY),
                end = androidx.compose.ui.geometry.Offset(end[0] * scaleX, end[1] * scaleY),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
    
    // Close the contour by connecting last point to first
    val lastIndex = indices.last()
    val firstIndex = indices.first()
    
    if (lastIndex < landmarks.size && firstIndex < landmarks.size) {
        val start = landmarks[lastIndex]
        val end = landmarks[firstIndex]
        
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(start[0] * scaleX, start[1] * scaleY),
            end = androidx.compose.ui.geometry.Offset(end[0] * scaleX, end[1] * scaleY),
            strokeWidth = 2.dp.toPx()
        )
    }
}
