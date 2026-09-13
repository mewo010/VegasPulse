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
    primary = CasinoGold,
    onPrimary = CasinoBackground,
    secondary = CasinoAmber,
    onSecondary = CasinoBackground,
    tertiary = CasinoGreen,
    onTertiary = CasinoBackground,
    background = CasinoBackground,
    onBackground = CasinoTextPrimary,
    surface = CasinoSurface,
    onSurface = CasinoTextPrimary,
    surfaceVariant = CasinoSurfaceVariant,
    onSurfaceVariant = CasinoTextSecondary,
    outline = CasinoBorder
  )

private val LightColorScheme = DarkColorScheme

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = DarkColorScheme, typography = Typography, content = content)
}

