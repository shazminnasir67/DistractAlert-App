package com.example.drivealert.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drivealert.R
import com.example.drivealert.data.model.LoginState
import com.example.drivealert.ui.viewmodel.SharedViewModel
import kotlinx.coroutines.delay
@Composable
fun LoginScreen(
    onNavigateToFaceRecognition: () -> Unit,
    sharedViewModel: SharedViewModel,
    viewModel: LoginViewModel = viewModel { LoginViewModel(sharedViewModel) }
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Navigate to face recognition when login is clicked
    LaunchedEffect(uiState.isLoading) {
        if (uiState.isLoading) {
            delay(2000) // Show loading for 2 seconds
            onNavigateToFaceRecognition()
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background Image (Full opacity)
        Image(
            painter = painterResource(id = R.drawable.app_login_bg),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Main Content
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title at the top
            Text(
                text = "DISTRACT ALERT",
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDEFB3C), // Neon color
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Default,
                lineHeight = 60.sp,
                modifier = Modifier.padding(top = 80.dp, bottom = 40.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // White Container at 40% bottom (increased height)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 24.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    // Subheading
                    Text(
                        text = "Real-Time Drowsiness & Distraction Detection",
                        fontSize = 18.sp,
                        color = Color(0xFF1A1A1A),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 24.sp
                    )
                    

                    
                    // Login Button
                    Button(
                        onClick = {
                            viewModel.startFaceRecognition()
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDEFB3C), // Neon color
                            contentColor = Color(0xFF1A1A1A)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFF1A1A1A),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Login",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Loading message
                    if (uiState.isLoading) {
                        Text(
                            text = "Analyzing Face...",
                            color = Color(0xFF1A1A1A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        }
    }
}

 