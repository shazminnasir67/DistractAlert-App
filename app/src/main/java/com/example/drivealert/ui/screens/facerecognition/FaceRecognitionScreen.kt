package com.example.drivealert.ui.screens.facerecognition

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drivealert.ui.components.DatabaseConnectionDialog
import com.example.drivealert.ui.viewmodel.SharedViewModel
import kotlinx.coroutines.delay

@Composable
fun FaceRecognitionScreen(
    onNavigateToStartTrip: () -> Unit,
    sharedViewModel: SharedViewModel,
    viewModel: FaceRecognitionViewModel = viewModel { FaceRecognitionViewModel(sharedViewModel) }
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.updateCameraPermission(isGranted)
    }
    
    LaunchedEffect(Unit) {
        if (!viewModel.checkCameraPermission(context)) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            viewModel.updateCameraPermission(true)
        }
    }
    
    // Navigate to Start Trip when face recognition is successful
    LaunchedEffect(uiState.faceRecognized) {
        if (uiState.faceRecognized) {
            delay(2000) // Show success message for 2 seconds
            onNavigateToStartTrip()
        }
    }
    
    // Database Connection Dialog
    DatabaseConnectionDialog(
        showDialog = uiState.showDatabaseDialog,
        connectionStatus = uiState.connectionStatus,
        onDismiss = { viewModel.dismissDatabaseDialog() },
        onRetry = { viewModel.retryDatabaseConnection() }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        // Camera Preview Section (80% of screen height)
        if (uiState.hasCameraPermission) {
            CameraPreviewSection(
                uiState = uiState,
                context = context,
                lifecycleOwner = lifecycleOwner,
                viewModel = viewModel
            )
        } else {
            // Show camera permission request
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Camera Permission Required",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Please grant camera permission to continue with face recognition",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDEFB3C),
                            contentColor = Color(0xFF1A1A1A)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Grant Permission",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        // Status and Controls Section (20% of screen height)
        StatusSection(
            uiState = uiState,
            onNavigateToStartTrip = onNavigateToStartTrip
        )
    }
}

@Composable
private fun CameraPreviewSection(
    uiState: FaceRecognitionState,
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    viewModel: FaceRecognitionViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
            .padding(16.dp)
    ) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp)),
            update = { previewView ->
                viewModel.setupCamera(context, lifecycleOwner, previewView)
            }
        )
        
        // Face Detection Bounding Box
        if (uiState.faceDetected) {
            FaceBoundingBox(
                faceBounds = uiState.faceBounds,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Loading Overlay
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFFDEFB3C),
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Composable
private fun FaceBoundingBox(
    faceBounds: androidx.compose.ui.geometry.Rect?,
    modifier: Modifier = Modifier
) {
    if (faceBounds != null) {
        Canvas(modifier = modifier) {
            val strokeWidth = 4f
            val cornerRadius = 8f
            
            // Convert normalized coordinates (0.0 to 1.0) to screen coordinates
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            val scaledBounds = androidx.compose.ui.geometry.Rect(
                left = faceBounds.left * canvasWidth,
                top = faceBounds.top * canvasHeight,
                right = faceBounds.right * canvasWidth,
                bottom = faceBounds.bottom * canvasHeight
            )
            
            // Draw bounding box with neon green color
            drawRoundRect(
                color = Color(0xFFDEFB3C),
                topLeft = scaledBounds.topLeft,
                size = scaledBounds.size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                style = Stroke(width = strokeWidth)
            )
            
            // Draw corner indicators
            val cornerSize = 20f
            val cornerColor = Color(0xFFDEFB3C)
            
            // Top-left corner
            drawLine(
                color = cornerColor,
                start = scaledBounds.topLeft,
                end = scaledBounds.topLeft + androidx.compose.ui.geometry.Offset(cornerSize, 0f),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = cornerColor,
                start = scaledBounds.topLeft,
                end = scaledBounds.topLeft + androidx.compose.ui.geometry.Offset(0f, cornerSize),
                strokeWidth = strokeWidth
            )
            
            // Top-right corner
            drawLine(
                color = cornerColor,
                start = scaledBounds.topRight,
                end = scaledBounds.topRight + androidx.compose.ui.geometry.Offset(-cornerSize, 0f),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = cornerColor,
                start = scaledBounds.topRight,
                end = scaledBounds.topRight + androidx.compose.ui.geometry.Offset(0f, cornerSize),
                strokeWidth = strokeWidth
            )
            
            // Bottom-left corner
            drawLine(
                color = cornerColor,
                start = scaledBounds.bottomLeft,
                end = scaledBounds.bottomLeft + androidx.compose.ui.geometry.Offset(cornerSize, 0f),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = cornerColor,
                start = scaledBounds.bottomLeft,
                end = scaledBounds.bottomLeft + androidx.compose.ui.geometry.Offset(0f, -cornerSize),
                strokeWidth = strokeWidth
            )
            
            // Bottom-right corner
            drawLine(
                color = cornerColor,
                start = scaledBounds.bottomRight,
                end = scaledBounds.bottomRight + androidx.compose.ui.geometry.Offset(-cornerSize, 0f),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = cornerColor,
                start = scaledBounds.bottomRight,
                end = scaledBounds.bottomRight + androidx.compose.ui.geometry.Offset(0f, -cornerSize),
                strokeWidth = strokeWidth
            )
        }
    }
}

@Composable
private fun StatusSection(
    uiState: FaceRecognitionState,
    onNavigateToStartTrip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.2f)
            .background(Color(0xFF1A1A1A))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Status Text
        Text(
            text = when {
                !uiState.hasCameraPermission -> "Camera permission not granted"
                uiState.isLoading -> "Analyzing Face..."
                uiState.faceDetected && uiState.faceRecognized -> "Face Recognized! ✅"
                uiState.faceDetected -> "Face Detected - Processing..."
                else -> "Position your face in the camera"
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        // Show confidence level when face is detected
        if (uiState.faceDetected && !uiState.faceRecognized && !uiState.isLoading) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Face detected - Checking similarity...",
                fontSize = 14.sp,
                color = Color(0xFFDEFB3C),
                textAlign = TextAlign.Center
            )
        }
        
        // Show confidence level when available
        if (uiState.confidence > 0f) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Similarity: ${(uiState.confidence * 100).toInt()}%",
                fontSize = 12.sp,
                color = if (uiState.confidence >= 0.85f) Color(0xFF4CAF50) else Color(0xFFFF9800),
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Success Message (when face is recognized)
        if (uiState.faceRecognized) {
            Text(
                text = "✅ Face Recognized Successfully!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDEFB3C),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Navigating to Start Trip screen...",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
        
        // Debug Info
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Camera: ${if (uiState.hasCameraPermission) "✅" else "❌"} | Face: ${if (uiState.faceDetected) "✅" else "❌"} | DB: ${if (uiState.faceRecognized) "✅" else "⏳"}",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        
        // Show detection count when face is detected
        if (uiState.faceDetected && !uiState.faceRecognized) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Face detected - Stabilizing...",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
        
        // Error Message
        uiState.errorMessage?.let { errorMsg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMsg,
                fontSize = 14.sp,
                color = Color.Red,
                textAlign = TextAlign.Center
            )
        }
    }
} 