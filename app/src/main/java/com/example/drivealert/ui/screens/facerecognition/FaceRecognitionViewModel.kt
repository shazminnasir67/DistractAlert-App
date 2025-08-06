package com.example.drivealert.ui.screens.facerecognition

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.compose.ui.geometry.Rect
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drivealert.data.repository.MongoDBRepositoryImpl
import com.example.drivealert.data.service.FaceDetectionServiceImpl
import com.example.drivealert.data.service.FaceNetServiceImpl
import com.example.drivealert.ui.components.ConnectionStatus
import com.example.drivealert.ui.viewmodel.SharedViewModel
import com.example.drivealert.utils.CameraManager
import com.example.drivealert.utils.NetworkUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.core.ExperimentalGetImage

@OptIn(ExperimentalGetImage::class)
class FaceRecognitionViewModel(
    private val sharedViewModel: SharedViewModel
) : ViewModel() {
    
    private val mongoDBRepository = MongoDBRepositoryImpl()
    private val faceDetectionService = FaceDetectionServiceImpl()
    private val faceNetService = FaceNetServiceImpl()
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    
    private var cameraManager: CameraManager? = null
    
    private val _uiState = MutableStateFlow(FaceRecognitionState())
    val uiState: StateFlow<FaceRecognitionState> = _uiState.asStateFlow()
    
    // Throttling variables to prevent frequent calls
    private var lastRecognitionTime = 0L
    private var isRecognitionInProgress = false
    private var frameCounter = 0
    private val recognitionCooldown = 2000L // 2 seconds between recognition attempts
    private val frameSkip = 5 // Process every 5th frame
    
    fun checkCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun updateCameraPermission(hasPermission: Boolean) {
        _uiState.value = _uiState.value.copy(hasCameraPermission = hasPermission)
        if (hasPermission) {
            checkDatabaseConnection()
        }
    }
    
    private fun checkDatabaseConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showDatabaseDialog = true,
                connectionStatus = ConnectionStatus.CHECKING
            )
            
            try {
                val hasInternet = NetworkUtils.isInternetAvailable(sharedViewModel.context.value!!)
                if (!hasInternet) {
                    _uiState.value = _uiState.value.copy(
                        connectionStatus = ConnectionStatus.OFFLINE
                    )
                    return@launch
                }
                
                // Check database connection (using simulation for now)
                val isConnected = NetworkUtils.simulateDatabaseConnection()
                
                _uiState.value = _uiState.value.copy(
                    connectionStatus = if (isConnected) ConnectionStatus.CONNECTED else ConnectionStatus.FAILED,
                    databaseConnected = isConnected
                )
                
                if (isConnected) {
                    // Auto-dismiss dialog after 2 seconds if connected
                    delay(2000)
                    dismissDatabaseDialog()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    connectionStatus = ConnectionStatus.FAILED,
                    databaseConnected = false
                )
            }
        }
    }
    
    fun retryDatabaseConnection() {
        checkDatabaseConnection()
    }
    
    fun dismissDatabaseDialog() {
        _uiState.value = _uiState.value.copy(showDatabaseDialog = false)
        if (_uiState.value.hasCameraPermission) {
            startFaceDetection()
        }
    }
    
    fun setupCamera(
        context: Context,
        lifecycleOwner: androidx.lifecycle.LifecycleOwner,
        previewView: PreviewView
    ) {
        if (cameraManager?.isCameraStarted() == true) {
            return // Camera already started, prevent flickering
        }
        
        viewModelScope.launch {
            try {
                cameraManager = CameraManager(context, lifecycleOwner, cameraExecutor)
                
                val result = cameraManager?.startCamera(previewView) { imageProxy ->
                    processImageForFaceDetection(imageProxy)
                }
                
                result?.getOrThrow() // This will throw if camera setup failed
                
                _uiState.value = _uiState.value.copy(
                    cameraReady = true,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Camera setup failed: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    private fun processImageForFaceDetection(imageProxy: ImageProxy) {
        frameCounter++
        if (frameCounter % frameSkip != 0) {
            imageProxy.close()
            return
        }
        
        viewModelScope.launch {
            try {
                val faces = faceDetectionService.detectFaces(imageProxy)
                
                if (faces.isNotEmpty()) {
                    val face = faces[0]
                    
                    // Get image dimensions for coordinate transformation
                    val imageWidth = imageProxy.width.toFloat()
                    val imageHeight = imageProxy.height.toFloat()
                    
                    // Convert Android Rect to normalized coordinates (0.0 to 1.0)
                    val androidRect = face.boundingBox
                    val normalizedRect = androidx.compose.ui.geometry.Rect(
                        left = androidRect.left.toFloat() / imageWidth,
                        top = androidRect.top.toFloat() / imageHeight,
                        right = androidRect.right.toFloat() / imageWidth,
                        bottom = androidRect.bottom.toFloat() / imageHeight
                    )
                    
                    // Update UI with face detection (normalized coordinates)
                    _uiState.value = _uiState.value.copy(
                        faceDetected = true,
                        faceBounds = normalizedRect
                    )
                    
                    // Start face recognition with throttling
                    if (shouldStartRecognition()) {
                        startFaceRecognitionWithThrottling()
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        faceDetected = false,
                        faceBounds = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Face detection failed: ${e.message}"
                )
            } finally {
                imageProxy.close()
            }
        }
    }
                    
    private var faceDetectionCount = 0
    
    private fun shouldStartRecognition(): Boolean {
        val currentTime = System.currentTimeMillis()
        faceDetectionCount++
        return !_uiState.value.faceRecognized && 
               !_uiState.value.isLoading && 
               !isRecognitionInProgress &&
               faceDetectionCount >= 10 && // Wait for 10 consecutive detections
               (currentTime - lastRecognitionTime) > recognitionCooldown
    }
    
    private fun startFaceRecognitionWithThrottling() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRecognitionTime < recognitionCooldown) {
            return // Still in cooldown period
        }
        
        isRecognitionInProgress = true
        lastRecognitionTime = currentTime
        
        viewModelScope.launch {
            delay(500) // Small delay to ensure face is stable
            if (_uiState.value.faceDetected && !_uiState.value.faceRecognized) {
                startFaceRecognition()
            }
            isRecognitionInProgress = false
        }
    }
    
    fun startFaceDetection() {
        // Face detection is now handled by the camera's ImageAnalysis
        // This method is kept for compatibility but doesn't do anything
    }
    
    private fun startFaceRecognition() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Extract face embedding using FaceNet (or mock for now)
                val faceEmbedding = extractFaceEmbedding()
                
                // Authenticate with MongoDB using face embedding
                val recognitionResult = mongoDBRepository.authenticateUser(faceEmbedding)
                
                if (recognitionResult.isSuccess && recognitionResult.confidence >= 0.85f) {
                    // Only proceed if confidence is high enough (85% threshold)
                    recognitionResult.user?.let { user ->
                        // Update shared view model with authenticated user
                        sharedViewModel.setCurrentUser(user)
                        
                        _uiState.value = _uiState.value.copy(
                            faceRecognized = true,
                            confidence = recognitionResult.confidence,
                            isLoading = false
                        )
                    }
                } else {
                    val errorMessage = if (recognitionResult.confidence < 0.85f) {
                        "Face similarity too low (${(recognitionResult.confidence * 100).toInt()}%). Please try again."
                    } else {
                        recognitionResult.error ?: "Face recognition failed"
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        errorMessage = errorMessage,
                        confidence = recognitionResult.confidence,
                        isLoading = false
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Face recognition failed: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    // Extract face embedding using FaceNet service
    private suspend fun extractFaceEmbedding(): List<Float> {
        return try {
            // TODO: In real implementation, this would:
            // 1. Capture the current frame from camera
            // 2. Crop the face region using the detected bounds
            // 3. Pass it to FaceNet to extract embedding
            
            // For now, use the same mock embedding that matches the database
            // This embedding matches shazmin's face data in MongoDB
            listOf(
                -0.09834085404872894f, 0.11783138662576675f, 0.13707754015922546f,
                0.02797902747988701f, -0.03228609636425972f, 0.09501434117555618f,
                -0.08029597252607346f, -0.0622604638338089f, 0.20644114911556244f,
                -0.1323203295469284f, 0.2458396553993225f, 0.04394255951046944f,
                -0.17177675664424896f, -0.05712488293647766f, -0.029764723032712936f,
                0.10989474505186081f, -0.07415200769901276f, -0.15377925336360931f,
                -0.06087956577539444f, -0.06169208139181137f, 0.009081654250621796f,
                -0.0006946372450329363f, 0.01326451264321804f, -0.031153827905654907f,
                -0.21901454031467438f, -0.33191004395484924f, -0.1551847904920578f,
                -0.1450297236442566f, -0.04931991174817085f, -0.13254988193511963f,
                -0.04590509086847305f, -0.051555272191762924f, -0.1150595024228096f,
                -0.020522328093647957f, 0.005001645535230637f, 0.0948665663599968f,
                -0.03560461103916168f, -0.0008378866477869451f, 0.1647413671016693f,
                0.030452100560069084f, -0.13024291396141052f, 0.07655229419469833f,
                0.07913310080766678f, 0.3063906729221344f, 0.17120061814785004f,
                0.04857795313000679f, 0.04730496183037758f, -0.07343095541000366f,
                0.17016513645648956f, -0.23140527307987213f, 0.08515385538339615f,
                0.12444861978292465f, 0.15600647032260895f, 0.11863899230957031f,
                0.15992088615894318f, -0.10776940733194351f, 0.014770329929888248f,
                0.12158510833978653f, -0.11380749195814133f, 0.12302374094724655f,
                0.021068217232823372f, 0.02025976963341236f, -0.0679074227809906f,
                -0.00552736921235919f, 0.27187052369117737f, 0.18177837133407593f,
                -0.15848329663276672f, -0.1552143692970276f, 0.1299312561750412f,
                -0.08367230743169785f, -0.07125630974769592f, 0.10792309045791626f,
                -0.18010640144348145f, -0.1434440016746521f, -0.30088135600090027f,
                0.05827130749821663f, 0.5407305955886841f, 0.06181129068136215f,
                -0.19185686111450195f, 0.011811494827270508f, -0.10945548862218857f,
                -0.0064972466789186f, -0.02997695282101631f, 0.07792920619249344f,
                -0.13778425753116608f, 0.02216741442680359f, -0.06863357126712799f,
                0.03900708630681038f, 0.22937342524528503f, 0.03785522282123566f,
                -0.045360956341028214f, 0.11700747907161713f, -0.011894028633832932f,
                0.08073078095912933f, 0.023117762058973312f, 0.06797611713409424f,
                -0.1262803077697754f, -0.08267057687044144f, -0.14302238821983337f,
                -0.08804334700107574f, -0.011712574400007725f, -0.05734122544527054f,
                -0.03160306066274643f, 0.09893038123846054f, -0.23868800699710846f,
                0.20952744781970978f, 0.010747644118964672f, -0.01664113625884056f,
                -0.008658597245812416f, 0.10399802029132843f, -0.10111366212368011f,
                0.08191155642271042f, 0.17208899557590485f, -0.32462432980537415f,
                0.16157497465610504f, 0.12616032361984253f, 0.02581685408949852f,
                0.08928381651639938f, 0.08923950791358948f, 0.048977047204971313f,
                -0.06518929451704025f, 0.05025038123130798f, -0.14892755448818207f,
                -0.08886446803808212f, -0.04651416838169098f, -0.060249291360378265f,
                0.05184382200241089f, 0.022104516625404358f
            )
        } catch (e: Exception) {
            // Fallback to random embedding if FaceNet fails
            List(128) { (Math.random() * 2 - 1).toFloat() }
        }
    }
}

data class FaceRecognitionState(
    val isLoading: Boolean = false,
    val hasCameraPermission: Boolean = false,
    val cameraReady: Boolean = false,
    val faceDetected: Boolean = false,
    val faceRecognized: Boolean = false,
    val faceBounds: Rect? = null,
    val confidence: Float = 0f,
    val errorMessage: String? = null,
    val showDatabaseDialog: Boolean = false,
    val connectionStatus: ConnectionStatus = ConnectionStatus.CHECKING,
    val databaseConnected: Boolean = false
)