package com.example.drivealert.ui.screens.drowsinessdetection

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drivealert.utils.CameraManager
import com.example.drivealert.drowsiness.DrowsinessDetectionEngine
import com.example.drivealert.drowsiness.FaceMeshProcessor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.core.ExperimentalGetImage

@OptIn(ExperimentalGetImage::class)
class DrowsinessDetectionViewModel : ViewModel() {
    
    private var cameraManager: CameraManager? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    
    // Detection engines
    private var drowsinessEngine: DrowsinessDetectionEngine? = null
    private var faceMeshProcessor: FaceMeshProcessor? = null
    
    private val _uiState = MutableStateFlow(DrowsinessDetectionState())
    val uiState: StateFlow<DrowsinessDetectionState> = _uiState.asStateFlow()
    
    private var tripStartTime = 0L
    private var drowsinessAlertCount = 0
    
    fun checkCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun updateCameraPermission(hasPermission: Boolean) {
        _uiState.value = _uiState.value.copy(hasCameraPermission = hasPermission)
        if (hasPermission) {
            startDetection()
        }
    }
    
    fun setupCamera(
        context: Context,
        lifecycleOwner: androidx.lifecycle.LifecycleOwner,
        previewView: PreviewView
    ) {
        if (cameraManager?.isCameraStarted() == true) {
            return // Camera already started
        }
        
        // Initialize detection engines
        drowsinessEngine = DrowsinessDetectionEngine(context)
        faceMeshProcessor = FaceMeshProcessor(context)
        
        viewModelScope.launch {
            try {
                cameraManager = CameraManager(context, lifecycleOwner, cameraExecutor)
                
                val result = cameraManager?.startCamera(previewView) { imageProxy ->
                    processImageForDrowsinessDetection(imageProxy)
                }
                
                result?.getOrThrow()
                
                _uiState.value = _uiState.value.copy(
                    cameraReady = true,
                    isDetecting = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Camera setup failed: ${e.message}"
                )
            }
        }
    }
    
    private fun startDetection() {
        tripStartTime = System.currentTimeMillis()
        _uiState.value = _uiState.value.copy(isDetecting = true)
        
        // Start trip duration timer
        viewModelScope.launch {
            while (_uiState.value.isDetecting) {
                delay(1000) // Update every second
                val duration = (System.currentTimeMillis() - tripStartTime) / 1000
                val minutes = duration / 60
                val seconds = duration % 60
                _uiState.value = _uiState.value.copy(
                    tripDuration = String.format("%02d:%02d", minutes, seconds)
                )
            }
        }
    }
    
    private fun processImageForDrowsinessDetection(imageProxy: ImageProxy) {
        viewModelScope.launch {
            try {
                val processor = faceMeshProcessor
                val engine = drowsinessEngine
                
                if (processor != null && engine != null) {
                    // Extract facial landmarks
                    val landmarks = processor.extractLandmarks(imageProxy)
                    
                    if (landmarks != null) {
                        // Process landmarks for drowsiness detection
                        val result = engine.processImage(landmarks, imageProxy.width, imageProxy.height)
                        
                        // Update UI state based on detection result
                        when {
                            result.isCalibrating -> {
                                _uiState.value = _uiState.value.copy(
                                    isCalibrating = true,
                                    calibrationProgress = result.calibrationProgress,
                                    currentStatus = "Calibrating... ${(result.calibrationProgress * 100).toInt()}%",
                                    faceLandmarks = result.faceLandmarks,
                                    imageWidth = result.imageWidth,
                                    imageHeight = result.imageHeight
                                )
                            }
                            result.error != null -> {
                                _uiState.value = _uiState.value.copy(
                                    errorMessage = "Detection error: ${result.error}"
                                )
                            }
                            else -> {
                                // Update features display
                                result.features?.let { features ->
                                    _uiState.value = _uiState.value.copy(
                                        isCalibrating = false,
                                        isDrowsy = result.isDrowsy,
                                        currentStatus = if (result.isDrowsy) "DROWSY DETECTED" else "Alert",
                                        eyeAspectRatio = features[0],
                                        mouthAspectRatio = features[1],
                                        pupilCircularity = features[2],
                                        mouthOverEye = features[3],
                                        faceLandmarks = result.faceLandmarks,
                                        imageWidth = result.imageWidth,
                                        imageHeight = result.imageHeight
                                    )
                                    
                                    // Count drowsiness alerts
                                    if (result.isDrowsy) {
                                        drowsinessAlertCount++
                                        _uiState.value = _uiState.value.copy(
                                            alertCount = drowsinessAlertCount
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // No face detected
                        _uiState.value = _uiState.value.copy(
                            currentStatus = "No face detected"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Detection engines not initialized"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Processing error: ${e.message}"
                )
            } finally {
                imageProxy.close()
            }
        }
    }
    
    fun resetCalibration() {
        drowsinessEngine?.resetCalibration()
        _uiState.value = _uiState.value.copy(
            isCalibrating = false,
            calibrationProgress = 0f,
            currentStatus = "Ready to calibrate"
        )
    }
    
    fun stopDetection() {
        _uiState.value = _uiState.value.copy(isDetecting = false)
        cameraManager?.stopCamera()
    }
    
    override fun onCleared() {
        super.onCleared()
        stopDetection()
        faceMeshProcessor?.close()
        cameraExecutor.shutdown()
    }
}

data class DrowsinessDetectionState(
    val hasCameraPermission: Boolean = false,
    val cameraReady: Boolean = false,
    val isDetecting: Boolean = false,
    val isCalibrating: Boolean = false,
    val calibrationProgress: Float = 0f,
    val isDrowsy: Boolean = false,
    val currentStatus: String = "Initializing...",
    val tripDuration: String = "00:00",
    val alertCount: Int = 0,
    val eyeAspectRatio: Float = 0f,
    val mouthAspectRatio: Float = 0f,
    val pupilCircularity: Float = 0f,
    val mouthOverEye: Float = 0f,
    val errorMessage: String? = null,
    // Face landmark data for real-time visualization
    val faceLandmarks: Array<FloatArray>? = null,
    val imageWidth: Int = 0,
    val imageHeight: Int = 0
)
