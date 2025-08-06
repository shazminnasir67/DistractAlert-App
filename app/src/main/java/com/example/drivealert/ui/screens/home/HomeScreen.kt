package com.example.drivealert.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drivealert.ui.theme.*

@Composable
fun HomeScreen(
    onStartTrip: () -> Unit,
    userName: String = "Driver"
) {
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
            IconButton(onClick = { /* TODO: Add menu functionality */ }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = NeonGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Welcome Message
            Text(
                text = "Welcome back,",
                fontSize = 18.sp,
                color = LightGray2,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = userName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Start Trip Button
            Button(
                onClick = onStartTrip,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        tint = DarkBlack
                    )
                    Text(
                        text = "Start Trip",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkBlack
                    )
                }
            }
        }
    }
}
