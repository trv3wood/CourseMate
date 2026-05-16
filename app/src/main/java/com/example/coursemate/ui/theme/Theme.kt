package com.example.coursemate.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = CourseMateSecondary,
    tertiary = CourseMateTertiary,
    background = DarkBackground,
    onBackground = Color(0xFFE1E3E6),
    surface = DarkSurface,
    onSurface = Color(0xFFE1E3E6),
    surfaceVariant = Color(0xFF414751),
    onSurfaceVariant = Color(0xFFC1C7D3)
)

private val LightColorScheme = lightColorScheme(
    primary = CourseMatePrimary,
    onPrimary = Color.White,
    primaryContainer = CourseMatePrimaryContainer,
    onPrimaryContainer = Color(0xFF001C39),
    secondary = CourseMateSecondary,
    onSecondary = Color.White,
    tertiary = CourseMateTertiary,
    onTertiary = Color.White,
    background = CourseMateBackground,
    onBackground = CourseMateOnSurface,
    surface = CourseMateSurface,
    onSurface = CourseMateOnSurface,
    surfaceContainer = CourseMateSurfaceContainer,
    surfaceVariant = CourseMateSurfaceVariant,
    onSurfaceVariant = CourseMateOnSurfaceVariant,
    outline = CourseMateOutline,
    outlineVariant = CourseMateOutlineVariant
)

@Composable
fun CourseMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
