package com.quadrants.memorix.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Dark Theme Colors (When Dark Mode is Enabled)
private val DarkColorScheme = darkColorScheme(
    primary = DarkViolet,
    secondary = MediumViolet,
    tertiary = RoyalBlue,
    background = DarkViolet,
    surface = DarkViolet,
    onPrimary = White,
    onSecondary = White,
    onTertiary = GoldenYellow, // Text color for accents
    onBackground = White,
    onSurface = White
)

// Light Theme Colors (For Light Mode)
private val LightColorScheme = lightColorScheme(
    primary = DarkViolet,
    secondary = MediumViolet,
    tertiary = RoyalBlue,
    background = White,
    surface = White,
    onPrimary = DarkViolet,
    onSecondary = DarkViolet,
    onTertiary = GoldenYellow,
    onBackground = DarkViolet,
    onSurface = DarkViolet
)

@Composable
fun MemorixTheme( 
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
