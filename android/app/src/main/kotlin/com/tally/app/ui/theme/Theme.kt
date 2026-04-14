package com.tally.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// Gemini风格深色主题
private val TallyDarkColorScheme = darkColorScheme(
    primary = TallyGreen,
    onPrimary = Color(0xFF000000),
    background = TallyDarkBackground,
    onBackground = TallyTextPrimary,
    surface = TallyCardBackground,
    onSurface = TallyTextPrimary,
    surfaceVariant = TallySurfaceVariant,
    onSurfaceVariant = TallyTextSecondary,
    outline = TallyOutline
)

// 大圆角Shapes (Gemini核心特征)
private val TallyShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp)
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
        shapes = TallyShapes,
        content = content
    )
}
