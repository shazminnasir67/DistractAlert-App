package com.example.drivealert.drowsiness

import android.content.Context
import android.util.Log
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.exp

/**
 * PyTorch LSTM Model Classifier for Drowsiness Detection
 * Direct port of the Python model inference logic
 */
class LSTMDrowsinessClassifier(private val context: Context) {
    
    companion object {
        private const val TAG = "LSTMClassifier"
        private const val MODEL_FILE = "clf_lstm_jit6.ptl"  // PyTorch Lite format
        private const val SEQUENCE_LENGTH = 5  // 5-frame sequences
        private const val FEATURE_SIZE = 4     // EAR, MAR, PUC, MOE
        private const val NUM_SEQUENCES = 6    // 6 overlapping sequences
        private const val DROWSY_THRESHOLD = 5 // Same as Python: preds.sum() >= 5
    }
    
    private var module: Module? = null
    private var isInitialized = false
    
    /**
     * Initialize PyTorch model from assets
     */
    suspend fun initialize(): Boolean {
        return try {
            val modelFile = assetFilePath(MODEL_FILE)
            module = LiteModuleLoader.load(modelFile)
            isInitialized = true
            Log.d(TAG, "PyTorch LSTM model loaded successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load PyTorch model", e)
            false
        }
    }
    
    /**
     * Classify drowsiness using LSTM model
     * Implements the exact same logic as Python get_classification()
     */
    fun classifyDrowsiness(inputData: List<FloatArray>): Int {
        if (!isInitialized || module == null || inputData.size < 20) {
            return 0 // Alert by default
        }
        
        try {
            // Create 6 overlapping sequences of 5 frames each
            // Exactly matching Python: input_data[:5], input_data[3:8], etc.
            val modelInput = Array(NUM_SEQUENCES) { Array(SEQUENCE_LENGTH) { FloatArray(FEATURE_SIZE) } }
            
            modelInput[0] = inputData.subList(0, 5).toTypedArray()      // frames 0-4
            modelInput[1] = inputData.subList(3, 8).toTypedArray()      // frames 3-7
            modelInput[2] = inputData.subList(6, 11).toTypedArray()     // frames 6-10
            modelInput[3] = inputData.subList(9, 14).toTypedArray()     // frames 9-13
            modelInput[4] = inputData.subList(12, 17).toTypedArray()    // frames 12-16
            modelInput[5] = inputData.subList(15, 20).toTypedArray()    // frames 15-19
            
            // Convert to PyTorch tensor [6, 5, 4]
            val inputTensor = createTensorFromArray(modelInput)
            
            // Run inference
            val outputIValue = module!!.forward(IValue.from(inputTensor))
            val outputTensor = outputIValue.toTensor()
            val outputData = outputTensor.dataAsFloatArray
            
            // Apply sigmoid and threshold (> 0.5)
            // Equivalent to: torch.sigmoid(model(model_input)).gt(0.5).int()
            var drowsyPredictions = 0
            for (prediction in outputData) {
                val sigmoidValue = 1.0f / (1.0f + exp(-prediction))
                if (sigmoidValue > 0.5f) {
                    drowsyPredictions++
                }
            }
            
            // Return 1 (drowsy) if >= 5 predictions are positive
            // Equivalent to: int(preds.sum() >= 5)
            return if (drowsyPredictions >= DROWSY_THRESHOLD) 1 else 0
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during LSTM inference", e)
            return 0 // Default to alert
        }
    }
    
    /**
     * Convert feature array to PyTorch tensor
     */
    private fun createTensorFromArray(data: Array<Array<FloatArray>>): Tensor {
        val flatArray = FloatArray(NUM_SEQUENCES * SEQUENCE_LENGTH * FEATURE_SIZE)
        var index = 0
        
        for (sequence in data) {
            for (frame in sequence) {
                for (feature in frame) {
                    flatArray[index++] = feature
                }
            }
        }
        
        return Tensor.fromBlob(
            flatArray,
            longArrayOf(NUM_SEQUENCES.toLong(), SEQUENCE_LENGTH.toLong(), FEATURE_SIZE.toLong())
        )
    }
    
    /**
     * Copy asset file to internal storage for PyTorch loading
     */
    private fun assetFilePath(assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }
        
        try {
            context.assets.open(assetName).use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(4 * 1024)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.flush()
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error copying asset file: $assetName", e)
        }
        
        return file.absolutePath
    }
    
    /**
     * Check if model is ready
     */
    fun isReady(): Boolean = isInitialized && module != null
    
    /**
     * Release model resources
     */
    fun close() {
        module?.destroy()
        module = null
        isInitialized = false
    }
}
