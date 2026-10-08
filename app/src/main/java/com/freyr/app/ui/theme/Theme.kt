package com.freyr.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = FreyrPrimary,
    onPrimary = FreyrSurface,
    primaryContainer = FreyrSecondary,
    onPrimaryContainer = FreyrPrimary,
    secondary = FreyrSecondary,
    onSecondary = FreyrPrimary,
    background = FreyrBackground,
    onBackground = FreyrTextPrimary,
    surface = FreyrSurface,
    onSurface = FreyrTextPrimary,
    surfaceVariant = FreyrSecondaryLight,
    onSurfaceVariant = FreyrTextSecondary,
    outline = FreyrBorder,
    error = FreyrError,
    onError = FreyrSurface
)

@Composable
fun FreyrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme // Minimalist elegant light theme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = FreyrBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
