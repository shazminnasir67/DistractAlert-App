package com.example.drivealert.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

enum class ConnectionStatus {
    CHECKING,
    CONNECTED,
    FAILED,
    OFFLINE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseConnectionDialog(
    showDialog: Boolean,
    connectionStatus: ConnectionStatus,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    if (showDialog) {
        Dialog(
            onDismissRequest = { 
                if (connectionStatus != ConnectionStatus.CHECKING) {
                    onDismiss()
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = connectionStatus != ConnectionStatus.CHECKING,
                dismissOnClickOutside = connectionStatus != ConnectionStatus.CHECKING
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Status Icon
                    when (connectionStatus) {
                        ConnectionStatus.CHECKING -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = Color(0xFFDEFB3C)
                            )
                        }
                        ConnectionStatus.CONNECTED -> {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Connected",
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFF4CAF50)
                            )
                        }
                        ConnectionStatus.FAILED -> {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Connection Failed",
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFFF44336)
                            )
                        }
                        ConnectionStatus.OFFLINE -> {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = "Offline",
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFFFF9800)
                            )
                        }
                    }
                    
                    // Title
                    Text(
                        text = when (connectionStatus) {
                            ConnectionStatus.CHECKING -> "Connecting to Database"
                            ConnectionStatus.CONNECTED -> "Connection Successful"
                            ConnectionStatus.FAILED -> "Connection Failed"
                            ConnectionStatus.OFFLINE -> "No Internet Connection"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    
                    // Description
                    Text(
                        text = when (connectionStatus) {
                            ConnectionStatus.CHECKING -> "Please wait while we establish a secure connection to MongoDB Atlas..."
                            ConnectionStatus.CONNECTED -> "Successfully connected to the database. Face authentication is ready."
                            ConnectionStatus.FAILED -> "Unable to connect to the database. Check your internet connection or try again."
                            ConnectionStatus.OFFLINE -> "Please check your internet connection and try again."
                        },
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    
                    // Connection Details (when checking or connected)
                    if (connectionStatus == ConnectionStatus.CHECKING || connectionStatus == ConnectionStatus.CONNECTED) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2A2A2A)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ConnectionStatusItem(
                                    label = "Internet Connection",
                                    isSuccess = connectionStatus != ConnectionStatus.OFFLINE
                                )
                                ConnectionStatusItem(
                                    label = "MongoDB Atlas",
                                    isSuccess = connectionStatus == ConnectionStatus.CONNECTED
                                )
                                ConnectionStatusItem(
                                    label = "Face Recognition Service",
                                    isSuccess = connectionStatus == ConnectionStatus.CONNECTED
                                )
                            }
                        }
                    }
                    
                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (connectionStatus == ConnectionStatus.FAILED || connectionStatus == ConnectionStatus.OFFLINE) {
                            OutlinedButton(
                                onClick = onRetry,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFDEFB3C)
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                        
                        if (connectionStatus != ConnectionStatus.CHECKING) {
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (connectionStatus == ConnectionStatus.CONNECTED) 
                                        Color(0xFFDEFB3C) else Color(0xFF444444),
                                    contentColor = if (connectionStatus == ConnectionStatus.CONNECTED) 
                                        Color.Black else Color.White
                                )
                            ) {
                                Text(
                                    text = when (connectionStatus) {
                                        ConnectionStatus.CONNECTED -> "Continue"
                                        else -> "Continue Offline"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionStatusItem(
    label: String,
    isSuccess: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
        
        Icon(
            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Wifi,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (isSuccess) Color(0xFF4CAF50) else Color.Gray
        )
    }
}
