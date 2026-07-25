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
import java.util.Calendar

private val DarkColorScheme = darkColorScheme(
    primary = BentoDarkPrimary,
    onPrimary = BentoOnPrimary,
    primaryContainer = BentoDarkPrimaryContainer,
    onPrimaryContainer = BentoDarkOnPrimaryContainer,
    secondary = BentoSecondary,
    secondaryContainer = BentoSecondaryContainer,
    onSecondaryContainer = BentoOnSecondaryContainer,
    background = BentoDarkBackground,
    surface = BentoDarkSurface,
    surfaceVariant = BentoDarkSurfaceVariant,
    onBackground = BentoDarkOnSurface,
    onSurface = BentoDarkOnSurface,
    onSurfaceVariant = BentoOnSurfaceVariant,
    outline = BentoOutline,
    outlineVariant = BentoOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = BentoPrimary,
    onPrimary = BentoOnPrimary,
    primaryContainer = BentoPrimaryContainer,
    onPrimaryContainer = BentoOnPrimaryContainer,
    secondary = BentoSecondary,
    onSecondary = BentoOnSecondary,
    secondaryContainer = BentoSecondaryContainer,
    onSecondaryContainer = BentoOnSecondaryContainer,
    tertiary = BentoTertiary,
    tertiaryContainer = BentoTertiaryContainer,
    onTertiaryContainer = BentoOnTertiaryContainer,
    background = BentoBackground,
    surface = BentoSurface,
    surfaceVariant = BentoSurfaceVariant,
    onBackground = BentoOnSurface,
    onSurface = BentoOnSurface,
    onSurfaceVariant = BentoOnSurfaceVariant,
    outline = BentoOutline,
    outlineVariant = BentoOutlineVariant
)

@Composable
fun NaneiTheme(
    forceNightMode: Boolean? = null,
    dynamicColor: Boolean = false, // Use intentional theme colors by default
    content: @Composable () -> Unit
) {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    // Auto night mode between 20h and 7h if not explicitly overridden
    val isAutoNightTime = currentHour >= 20 || currentHour < 7
    val darkTheme = forceNightMode ?: (isSystemInDarkTheme() || isAutoNightTime)

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
