package com.example.drivealert.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Custom Dark Color Scheme for Driver App
private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = DarkBlack,
    primaryContainer = DarkGray2,
    onPrimaryContainer = NeonGreen,
    secondary = NeonBlue,
    onSecondary = DarkBlack,
    secondaryContainer = DarkGray2,
    onSecondaryContainer = NeonBlue,
    tertiary = NeonPurple,
    onTertiary = DarkBlack,
    tertiaryContainer = DarkGray2,
    onTertiaryContainer = NeonPurple,
    background = DarkBlack,
    onBackground = LightGray,
    surface = DarkGray,
    onSurface = LightGray,
    surfaceVariant = DarkGray2,
    onSurfaceVariant = LightGray2,
    outline = NeonGreen,
    outlineVariant = DarkGray3,
    error = ErrorRed,
    onError = White,
    errorContainer = DarkGray2,
    onErrorContainer = ErrorRed,
    inverseSurface = LightGray,
    inverseOnSurface = DarkBlack,
    inversePrimary = NeonGreen,
    surfaceTint = NeonGreen,
    scrim = DarkBlack.copy(alpha = 0.32f)
)

// Custom Light Color Scheme (for accessibility)
private val LightColorScheme = lightColorScheme(
    primary = NeonGreen,
    onPrimary = White,
    primaryContainer = LightGray2,
    onPrimaryContainer = DarkBlack,
    secondary = NeonBlue,
    onSecondary = White,
    secondaryContainer = LightGray2,
    onSecondaryContainer = DarkBlack,
    tertiary = NeonPurple,
    onTertiary = White,
    tertiaryContainer = LightGray2,
    onTertiaryContainer = DarkBlack,
    background = White,
    onBackground = DarkBlack,
    surface = LightGray2,
    onSurface = DarkBlack,
    surfaceVariant = LightGray,
    onSurfaceVariant = DarkGray,
    outline = NeonGreen,
    outlineVariant = DarkGray3,
    error = ErrorRed,
    onError = White,
    errorContainer = LightGray2,
    onErrorContainer = ErrorRed,
    inverseSurface = DarkBlack,
    inverseOnSurface = LightGray,
    inversePrimary = NeonGreen,
    surfaceTint = NeonGreen,
    scrim = DarkBlack.copy(alpha = 0.32f)
)

@Composable
fun DriveAlertTheme(
    darkTheme: Boolean = true, // Default to dark theme for driver app
    dynamicColor: Boolean = false, // Disable dynamic colors for consistent neon theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar to match theme
            window.statusBarColor = if (darkTheme) DarkBlack.toArgb() else White.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
} 