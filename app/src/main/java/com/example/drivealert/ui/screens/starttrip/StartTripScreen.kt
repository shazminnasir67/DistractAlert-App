package com.example.drivealert.ui.screens.starttrip

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drivealert.ui.components.AppHeader
import com.example.drivealert.ui.theme.*

@Composable
fun StartTripScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlack)
    ) {
        // Professional Header
        AppHeader(
            title = "Start Trip",
            showMenu = true,
            onBackClick = { /* TODO: Navigate back */ }
        )
        
        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Success Animation
            SuccessAnimation()
            
            // Welcome Message
            WelcomeSection()
            
            // Trip Options
            TripOptionsSection()
            
            // Phase 2 Info
            Phase2InfoSection()
        }
    }
}

@Composable
private fun SuccessAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "success_animation")
    val checkmarkScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "checkmark_scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkGray2
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 12.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Animated Success Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(checkmarkScale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(SuccessGreen, DarkGray2)
                        ),
                        shape = RoundedCornerShape(40.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = DarkBlack,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            // Success Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "✅ Authentication Successful!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Face recognition completed with high confidence",
                    fontSize = 16.sp,
                    color = LightGray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun WelcomeSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkGray2
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = NeonPurple,
                    modifier = Modifier.size(32.dp)
                )
                
                Column {
                    Text(
                        text = "Welcome back, Driver!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightGray
                    )
                    Text(
                        text = "Ready to start your journey safely",
                        fontSize = 14.sp,
                        color = LightGray2
                    )
                }
            }
            
            Divider(
                color = DarkGray3,
                thickness = 1.dp
            )
            
            // Status indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusIndicator(
                    icon = Icons.Default.Face,
                    label = "Face Verified",
                    color = SuccessGreen
                )
                StatusIndicator(
                    icon = Icons.Default.Security,
                    label = "System Ready",
                    color = NeonGreen
                )
                StatusIndicator(
                    icon = Icons.Default.Sensors,
                    label = "Sensors Active",
                    color = NeonBlue
                )
            }
        }
    }
}

@Composable
private fun StatusIndicator(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = LightGray2,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TripOptionsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkGray2
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Trip Options",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NeonGreen
            )
            
            // Start Trip Button
            Button(
                onClick = { /* TODO: Start trip */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    contentColor = DarkBlack
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Start Trip",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Quick Settings Button
            OutlinedButton(
                onClick = { /* TODO: Open settings */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = NeonBlue
                ),
                border = androidx.compose.foundation.BorderStroke(2.dp, NeonBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Quick Settings",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun Phase2InfoSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkGray2
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = InfoBlue,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Phase 2 Complete",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = InfoBlue
                )
            }
            
            Text(
                text = "✅ Face Recognition with MongoDB",
                fontSize = 14.sp,
                color = SuccessGreen,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "✅ User Authentication System",
                fontSize = 14.sp,
                color = SuccessGreen,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "✅ Professional UI/UX Design",
                fontSize = 14.sp,
                color = SuccessGreen,
                fontWeight = FontWeight.Medium
            )
            
            Divider(
                color = DarkGray3,
                thickness = 1.dp
            )
            
            Text(
                text = "Ready for Phase 3: Trip Start with Preconditions Check",
                fontSize = 12.sp,
                color = LightGray2,
                textAlign = TextAlign.Center
            )
        }
    }
} 