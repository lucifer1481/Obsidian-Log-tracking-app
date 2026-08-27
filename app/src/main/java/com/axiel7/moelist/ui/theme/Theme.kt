package com.axiel7.moelist.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)

// --- LUCIFER DARK COLOR SCHEME ---
private val LuciferDarkColors = darkColorScheme(
    primary = lucifer_primary,
    onPrimary = lucifer_onPrimary,
    primaryContainer = lucifer_primaryContainer,
    onPrimaryContainer = lucifer_onPrimaryContainer,
    secondary = lucifer_secondary,
    onSecondary = lucifer_onSecondary,
    secondaryContainer = lucifer_secondaryContainer,
    onSecondaryContainer = lucifer_onSecondaryContainer,
    tertiary = lucifer_primary,
    onTertiary = lucifer_onPrimary,
    tertiaryContainer = lucifer_primaryContainer,
    onTertiaryContainer = lucifer_onPrimaryContainer,
    error = stat_dropped_dark,
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    background = lucifer_dark_background,
    onBackground = lucifer_dark_onBackground,
    surface = lucifer_dark_surface,
    onSurface = lucifer_dark_onSurface,
    surfaceVariant = lucifer_dark_surfaceVariant,
    onSurfaceVariant = lucifer_dark_onSurfaceVariant,
    outline = lucifer_dark_outline,
    inverseOnSurface = lucifer_dark_background,
    inverseSurface = lucifer_dark_onBackground,
    inversePrimary = lucifer_primary,
    surfaceTint = lucifer_primary,
    outlineVariant = lucifer_dark_surfaceVariant,
    scrim = Color(0xFF000000),
)

private fun ColorScheme.toBlack() = this.copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceVariant = surfaceVariant.copy(alpha = 0.4f).compositeOver(Color.Black),
    surfaceContainer = Color.Black,
    surfaceContainerHigh = surfaceContainerHigh.copy(alpha = 0.5f).compositeOver(Color.Black),
    surfaceContainerHighest = surfaceContainerHighest.copy(alpha = 0.6f).compositeOver(Color.Black)
)

@Composable
fun MoeListTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // 🚀 Disabled dynamic colors by default so your Red/Black theme takes control!
    useBlackColors: Boolean = true, // 🚀 Forced true by default for pure black backgrounds
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = remember(dynamicColor, darkTheme, useBlackColors) {
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (darkTheme) dynamicDarkColorScheme(context).let {
                    return@let if (useBlackColors) it.toBlack() else it
                }
                else dynamicLightColorScheme(context)
            }

            darkTheme -> if (useBlackColors) LuciferDarkColors.toBlack() else LuciferDarkColors
            else -> LightColors
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}