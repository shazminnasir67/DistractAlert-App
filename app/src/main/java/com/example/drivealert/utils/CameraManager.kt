package com.example.drivealert.utils

import android.content.Context
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService

class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val cameraExecutor: ExecutorService
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageAnalysis: ImageAnalysis? = null
    
    suspend fun startCamera(
        previewView: PreviewView,
        onImageAnalysis: (ImageProxy) -> Unit
    ): Result<Unit> {
        return try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProvider = cameraProviderFuture.get()
            
            // Build the preview use case
            preview = Preview.Builder()
                .setTargetResolution(Size(1280, 720)) // Set fixed resolution to prevent flickering
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
            
            // Build the image analysis use case
            imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480)) // Lower resolution for analysis
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setImageQueueDepth(1) // Process only latest frame
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor, onImageAnalysis)
                }
            
            // Select front camera
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            
            // Unbind all use cases before rebinding
            cameraProvider?.unbindAll()
            
            // Bind use cases to camera
            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun stopCamera() {
        try {
            cameraProvider?.unbindAll()
            camera = null
            preview = null
            imageAnalysis = null
        } catch (e: Exception) {
            // Log error but don't crash
        }
    }
    
    fun isCameraStarted(): Boolean {
        return camera != null
    }
    
    fun toggleTorch(enable: Boolean) {
        camera?.cameraControl?.enableTorch(enable)
    }
}
