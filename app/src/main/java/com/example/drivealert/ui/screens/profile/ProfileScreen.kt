package com.example.drivealert.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drivealert.data.model.User
import com.example.drivealert.ui.components.AppHeader
import com.example.drivealert.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    user: User?,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlack)
    ) {
        // Clean Header
        AppHeader(
            title = "Profile",
            showMenu = false,
            onBackClick = onBackClick
        )
        
        if (user != null) {
            ProfileContent(user = user)
        } else {
            LoadingProfile()
        }
    }
}

@Composable
private fun ProfileContent(user: User) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Clean Profile Header
        ProfileHeader(user = user)
        
        // Member Information
        MemberInfoSection(user = user)
        
        // Contact Information  
        ContactInfoSection(user = user)
    }
}

@Composable
private fun ProfileHeader(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkGray2
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Circular Profile Image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = NeonGreen,
                        shape = RoundedCornerShape(40.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = DarkBlack,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            // Name and Last Login Info
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = user.fullName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                
                Text(
                    text = "Last login: ${formatLastLogin(user.lastLogin)}",
                    fontSize = 14.sp,
                    color = LightGray2
                )
            }
        }
    }
}

@Composable
private fun MemberInfoSection(user: User) {
    InfoSection(
        title = "Member Since",
        icon = Icons.Default.Schedule,
        items = listOf(
            InfoItem("Joined", formatDate(user.createdAt)),
            InfoItem("Status", user.accountStatus)
        )
    )
}

@Composable
private fun ContactInfoSection(user: User) {
    InfoSection(
        title = "Contact Information",
        icon = Icons.Default.ContactMail,
        items = listOf(
            InfoItem("Name", user.fullName),
            InfoItem("Email", user.email),
            InfoItem("Phone", user.phone)
        )
    )
}

@Composable
private fun InfoSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    items: List<InfoItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkGray2
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = NeonGreen,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = White
                )
            }
            
            // Info Items
            items.forEach { item ->
                InfoRow(
                    label = item.label,
                    value = item.value
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = LightGray2,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = LightGray,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun LoadingProfile() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = NeonGreen,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Loading Profile...",
                fontSize = 16.sp,
                color = LightGray
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(date)
}

private fun formatLastLogin(lastLoginIso: String?): String {
    return if (lastLoginIso.isNullOrEmpty()) {
        "Never"
    } else {
        try {
            // Parse ISO string format: "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
            val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            isoFormatter.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val date = isoFormatter.parse(lastLoginIso)
            
            if (date != null) {
                val displayFormatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
                displayFormatter.format(date)
            } else {
                "Never"
            }
        } catch (e: Exception) {
            "Never"
        }
    }
}

data class InfoItem(
    val label: String,
    val value: String
) 