package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = GoldenEmber,
    secondary = NeonTeal,
    tertiary = RoyalPurple,
    background = CosmicBg,
    surface = CosmicSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for cinematic immersion
  dynamicColor: Boolean = false, // Disable default system dynamic light schemes
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = DarkColorScheme, typography = Typography, content = content)
}
