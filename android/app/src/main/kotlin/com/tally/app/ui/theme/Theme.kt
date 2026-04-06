package com.tally.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Tally 使用纯深色主题
private val TallyDarkColorScheme = darkColorScheme(
    primary = TallyGreen,
    background = TallyDarkBackground,
    surface = TallyCardBackground,
    onPrimary = TallyTextPrimary,
    onBackground = TallyTextPrimary,
    onSurface = TallyTextPrimary,
)

@Composable
fun TallyTheme(content: @Composable () -> Unit) {
    val colorScheme = TallyDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = TallyDarkBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TallyTypography,
        content = content
    )
}
