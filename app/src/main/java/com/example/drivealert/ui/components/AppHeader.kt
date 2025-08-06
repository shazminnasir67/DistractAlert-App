package com.example.drivealert.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drivealert.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    title: String,
    showMenu: Boolean = true,
    onMenuClick: () -> Unit = {},
    onBackClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val menuRotation by animateFloatAsState(
        targetValue = if (menuExpanded) 90f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "menu_rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBlack)
    ) {
        // Main Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Back button or logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = DarkGray2,
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = NeonGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    // App Logo/Icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = NeonGreen,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "DriveAlert",
                            tint = DarkBlack,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                // Title
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen
                )
            }
            
            // Right side - Menu and actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Settings button
                onSettingsClick?.let {
                    IconButton(
                        onClick = it,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = DarkGray2,
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = NeonBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                // Profile button
                onProfileClick?.let {
                    IconButton(
                        onClick = it,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = DarkGray2,
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = NeonPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                // Menu button
                if (showMenu) {
                    IconButton(
                        onClick = { 
                            menuExpanded = !menuExpanded
                            onMenuClick()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = DarkGray2,
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = NeonGreen,
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(menuRotation)
                        )
                    }
                }
            }
        }
        
        // Animated Menu Dropdown
        AnimatedVisibility(
            visible = menuExpanded,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        ) {
            MenuDropdown(
                onDismiss = { menuExpanded = false }
            )
        }
        
        // Bottom border with neon effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            NeonGreen,
                            NeonBlue,
                            NeonGreen,
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun MenuDropdown(
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkGray2
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            MenuItem(
                icon = Icons.Default.Analytics,
                title = "Dashboard",
                subtitle = "View trip statistics",
                onClick = { /* TODO */ }
            )
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = DarkGray3,
                thickness = 1.dp
            )
            
            MenuItem(
                icon = Icons.Default.Schedule,
                title = "Trip History",
                subtitle = "View past trips",
                onClick = { /* TODO */ }
            )
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = DarkGray3,
                thickness = 1.dp
            )
            
            MenuItem(
                icon = Icons.Default.Notifications,
                title = "Alerts",
                subtitle = "Manage notifications",
                onClick = { /* TODO */ }
            )
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = DarkGray3,
                thickness = 1.dp
            )
            
            MenuItem(
                icon = Icons.Default.Info,
                title = "Help & Support",
                subtitle = "Get assistance",
                onClick = { /* TODO */ }
            )
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = DarkGray3,
                thickness = 1.dp
            )
            
            MenuItem(
                icon = Icons.Default.ExitToApp,
                title = "Logout",
                subtitle = "Sign out of account",
                onClick = { /* TODO */ }
            )
        }
    }
}

@Composable
private fun MenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = NeonGreen,
            modifier = Modifier.size(24.dp)
        )
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = LightGray
            )
            
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = LightGray2
            )
        }
        
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            tint = DarkGray3,
            modifier = Modifier.size(20.dp)
        )
    }
} 