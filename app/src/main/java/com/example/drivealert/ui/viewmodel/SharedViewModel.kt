package com.example.drivealert.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.drivealert.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedViewModel : ViewModel() {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _context = MutableStateFlow<Context?>(null)
    val context: StateFlow<Context?> = _context.asStateFlow()
    
    fun setCurrentUser(user: User?) {
        _currentUser.value = user
    }
    
    fun updateCurrentUser(user: User?) {
        _currentUser.value = user
    }
    
    fun getCurrentUser(): User? = _currentUser.value
    
    fun setContext(context: Context) {
        _context.value = context
    }
} 