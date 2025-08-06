package com.example.drivealert.utils

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object CameraUtils {
    
    fun getCameraProvider(context: Context): ListenableFuture<ProcessCameraProvider> {
        return ProcessCameraProvider.getInstance(context)
    }
    
    fun createPreview(): Preview {
        return Preview.Builder()
            .build()
    }
    
    fun createImageAnalysis(): ImageAnalysis {
        return ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }
    
    fun getFrontCameraSelector(): CameraSelector {
        return CameraSelector.DEFAULT_FRONT_CAMERA
    }
    
    fun bindCameraUseCases(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        cameraProvider: ProcessCameraProvider,
        preview: Preview,
        imageAnalysis: ImageAnalysis,
        cameraSelector: CameraSelector = getFrontCameraSelector()
    ) {
        try {
            cameraProvider.unbindAll()
            
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun createCameraExecutor(): ExecutorService {
        return Executors.newSingleThreadExecutor()
    }
} 