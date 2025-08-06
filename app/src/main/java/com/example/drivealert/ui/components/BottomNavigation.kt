package com.example.drivealert.ui.components

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drivealert.ui.theme.*

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun DriveAlertBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, "home"),
        BottomNavItem("Trips", Icons.Default.Schedule, "trips"),
        BottomNavItem("Profile", Icons.Default.Person, "profile"),
        BottomNavItem("Settings", Icons.Default.Settings, "settings")
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp), // Reduced vertical padding
        colors = CardDefaults.cardColors(
            containerColor = DarkGray2
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 12.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp), // Reduced vertical padding
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                BottomNavItem(
                    item = item,
                    isSelected = currentRoute == item.route,
                    onNavigate = onNavigate
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onNavigate: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onNavigate(item.route) }
            .padding(8.dp)
    ) {
        // No background block - just clean icon and text
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = if (isSelected) NeonGreen else LightGray2,
            modifier = Modifier.size(24.dp)
        )
        
        Text(
            text = item.title,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) NeonGreen else LightGray2
        )
    }
} 