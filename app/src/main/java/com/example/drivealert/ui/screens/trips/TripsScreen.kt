package com.example.drivealert.ui.screens.trips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drivealert.ui.components.AppHeader
import com.example.drivealert.ui.theme.*

@Composable
fun TripsScreen(
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlack)
    ) {
        // Header
        AppHeader(
            title = "Trips",
            showMenu = false,
            onBackClick = onBackClick
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "No trips",
                tint = LightGray2,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Your Recent Trips",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Trip history will appear here once you complete your first journey",
                fontSize = 16.sp,
                color = LightGray2,
                textAlign = TextAlign.Center
            )
        }
    }
}
