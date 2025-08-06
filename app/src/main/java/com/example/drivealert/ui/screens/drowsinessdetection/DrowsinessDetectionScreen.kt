package com.example.drivealert.ui.screens.drowsinessdetection

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drivealert.ui.theme.*
import com.example.drivealert.ui.components.FaceLandmarkOverlay

@Composable
fun DrowsinessDetectionScreen(
    onStopTrip: () -> Unit,
    viewModel: DrowsinessDetectionViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    
    // Camera permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.updateCameraPermission(isGranted)
    }
    
    // Check camera permission on startup
    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasPermission) {
            viewModel.updateCameraPermission(true)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlack)
    ) {
        // Custom Header Bar (avoiding experimental TopAppBar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onStopTrip) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = NeonGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Drowsiness Detection",
                color = White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        
        if (!uiState.hasCameraPermission) {
            // Permission Required Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Camera Permission Required",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = White,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "We need camera access to detect drowsiness and keep you safe",
                    fontSize = 16.sp,
                    color = LightGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = DarkBlack
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Grant Permission",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Camera Preview with Face Landmark Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).also { previewView ->
                            viewModel.setupCamera(ctx, lifecycleOwner, previewView)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // Real-time Face Landmark Overlay
                if (uiState.faceLandmarks != null && uiState.isDetecting) {
                    FaceLandmarkOverlay(
                        landmarks = uiState.faceLandmarks,
                        imageWidth = uiState.imageWidth,
                        imageHeight = uiState.imageHeight,
                        previewWidth = 0f, // Will be calculated in the overlay
                        previewHeight = 0f,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Status Overlay
                if (uiState.isDetecting) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                uiState.isDrowsy -> ErrorRed.copy(alpha = 0.9f)
                                uiState.isCalibrating -> WarningOrange.copy(alpha = 0.9f)
                                else -> SuccessGreen.copy(alpha = 0.9f)
                            }
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = uiState.currentStatus,
                                color = DarkBlack,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            
                            if (uiState.isCalibrating) {
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Calibration Progress with Countdown
                                val remainingFrames = (25 - (uiState.calibrationProgress * 25).toInt())
                                Text(
                                    text = "Stay still... $remainingFrames frames left",
                                    color = DarkBlack,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                LinearProgressIndicator(
                                    progress = uiState.calibrationProgress,
                                    modifier = Modifier.fillMaxWidth(),
                                    color = DarkBlack,
                                    trackColor = DarkBlack.copy(alpha = 0.3f)
                                )
                                
                                if (uiState.calibrationProgress > 0.8f) {
                                    Text(
                                        text = "Almost done!",
                                        color = DarkBlack,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else if (!uiState.isCalibrating && uiState.eyeAspectRatio != 0f) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "EAR: %.2f | MAR: %.2f".format(
                                        uiState.eyeAspectRatio,
                                        uiState.mouthAspectRatio
                                    ),
                                    color = DarkBlack,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "PUC: %.2f | MOE: %.2f".format(
                                        uiState.pupilCircularity,
                                        uiState.mouthOverEye
                                    ),
                                    color = DarkBlack,
                                    fontSize = 12.sp
                                )
                                if (uiState.alertCount > 0) {
                                    Text(
                                        text = "Alerts: ${uiState.alertCount}",
                                        color = DarkBlack,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Bottom Controls
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkGray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Trip Duration: ${uiState.tripDuration}",
                        color = White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Reset Calibration Button
                    if (!uiState.isCalibrating) {
                        Button(
                            onClick = { viewModel.resetCalibration() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WarningOrange,
                                contentColor = DarkBlack
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Reset Calibration",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // Stop Trip Button
                    Button(
                        onClick = {
                            viewModel.stopDetection()
                            onStopTrip()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ErrorRed,
                            contentColor = White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Stop Trip",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
