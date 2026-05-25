package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

// Retro-Modern Technical Styling palette mapped onto Miuix color tokens.
// MiuixTheme's color scheme is the source of truth across the app.
private val RetroMiuixColors = lightColorScheme(
    primary = RetroMagenta,
    onPrimary = Color.White,
    primaryVariant = RetroMagenta,
    onPrimaryVariant = RetroLightMagenta,
    primaryContainer = RetroLightMagenta,
    onPrimaryContainer = RetroMagenta,
    secondary = RetroBlue,
    onSecondary = Color.White,
    secondaryVariant = RetroLightBlue,
    onSecondaryVariant = RetroCharcoal,
    secondaryContainer = RetroLightBlue,
    onSecondaryContainer = RetroBlue,
    secondaryContainerVariant = RetroLightBlue,
    onSecondaryContainerVariant = RetroCharcoal,
    tertiaryContainer = RetroLightViolet,
    onTertiaryContainer = RetroViolet,
    tertiaryContainerVariant = RetroLightViolet,
    background = RetroCream,
    onBackground = RetroCharcoal,
    onBackgroundVariant = RetroCharcoal.copy(alpha = 0.6f),
    surface = RetroPaper,
    onSurface = RetroCharcoal,
    surfaceVariant = RetroPaper,
    onSurfaceSecondary = RetroCharcoal.copy(alpha = 0.8f),
    onSurfaceVariantSummary = RetroCharcoal.copy(alpha = 0.7f),
    onSurfaceVariantActions = RetroCharcoal.copy(alpha = 0.5f),
    surfaceContainer = RetroPaper,
    onSurfaceContainer = RetroCharcoal,
    onSurfaceContainerVariant = RetroCharcoal.copy(alpha = 0.6f),
    surfaceContainerHigh = RetroPaper,
    onSurfaceContainerHigh = RetroCharcoal,
    surfaceContainerHighest = RetroPaper,
    onSurfaceContainerHighest = RetroCharcoal,
    outline = RetroCharcoal,
    dividerLine = RetroCharcoal.copy(alpha = 0.15f),
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MiuixTheme(
        colors = RetroMiuixColors,
        content = content,
    )
}
