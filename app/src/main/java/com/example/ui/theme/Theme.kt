package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// We target the Retro-Modern Technical Styling from the Tensor G5 Inherent TPU Bench image
private val RetroTechnicalColorScheme = lightColorScheme(
    primary = RetroMagenta,            // Vibrant Magenta Accent Focus
    secondary = RetroBlue,             // Robust Retro Electric Blue Accent
    tertiary = RetroViolet,            // Conformer Violet Accent
    background = RetroCream,           // Warm Antique Paper Cream Canvas
    surface = RetroPaper,              // Block Frame Solid Fill Paper Tone
    primaryContainer = RetroLightMagenta,
    secondaryContainer = RetroLightBlue,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = RetroCharcoal,       // Industrial Black Text & Graphics
    onSurface = RetroCharcoal,
    onSurfaceVariant = RetroCharcoal.copy(alpha = 0.7f),
    outline = RetroCharcoal              // Clean Bold Outer Boundaries
)

// To maintain consistency with the uploaded document design asset, we enforce this beautiful,
// high-contrast technical layout style.
private val DarkColorScheme = RetroTechnicalColorScheme
private val LightColorScheme = RetroTechnicalColorScheme

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Enforce strict brand retro themes
    content: @Composable () -> Unit,
) {
    val colorScheme = RetroTechnicalColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
