package com.example.drivealert.ui.screens.login


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drivealert.data.model.LoginState
import com.example.drivealert.data.model.User
import com.example.drivealert.data.repository.MongoDBRepository
import com.example.drivealert.data.repository.MongoDBRepositoryImpl
import com.example.drivealert.ui.viewmodel.SharedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class LoginViewModel(
    private val sharedViewModel: SharedViewModel
) : ViewModel() {
    private val mongoDBRepository: MongoDBRepository = MongoDBRepositoryImpl()
    
    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()
    

    
    fun startFaceRecognition() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Simulate face detection and recognition process
                kotlinx.coroutines.delay(2000) // Simulate processing time
                
                // Generate mock face embedding
                val mockEmbedding = List(512) { (Math.random() * 2 - 1).toFloat() }
                
                // Authenticate with MongoDB
                val recognitionResult = mongoDBRepository.authenticateUser(mockEmbedding)
                
                if (recognitionResult.isSuccess) {
                    recognitionResult.user?.let { user ->
                        // Update last login timestamp on server
                        mongoDBRepository.updateUserLastLogin(user.userId)
                        
                        // Create updated user with current timestamp as lastLogin
                        val currentTimeIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                            timeZone = java.util.TimeZone.getTimeZone("UTC")
                        }.format(java.util.Date())
                        
                        val updatedUser = user.copy(lastLogin = currentTimeIso)
                        sharedViewModel.setCurrentUser(updatedUser)
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = recognitionResult.error ?: "Face recognition failed"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Face recognition failed"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun getCurrentUser(): User? = sharedViewModel.getCurrentUser()
} 