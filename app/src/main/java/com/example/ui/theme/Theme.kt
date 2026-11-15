package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val RomanticLightColorScheme = lightColorScheme(
    primary = WarmRose,
    onPrimary = Color.White,
    primaryContainer = SoftPink,
    onPrimaryContainer = DeepRose,
    secondary = ChampagneGold,
    onSecondary = DarkBrownText,
    secondaryContainer = SoftGoldTint,
    onSecondaryContainer = DarkBrownText,
    tertiary = SunsetOrange,
    onTertiary = Color.White,
    tertiaryContainer = SunsetPeach,
    onTertiaryContainer = DarkBrownText,
    background = WarmCream,
    onBackground = DarkBrownText,
    surface = SoftParchment,
    onSurface = DarkBrownText,
    surfaceVariant = BlushSand,
    onSurfaceVariant = MutedCocoaText,
    outline = WarmOutline,
    outlineVariant = Color(0xFFF0DDD6),
    error = Color(0xFFB3261E),
    onError = Color.White
)

private val RomanticDarkColorScheme = darkColorScheme(
    primary = CandlelightRose,
    onPrimary = VelvetBurgundy,
    primaryContainer = DarkModeRoseContainer,
    onPrimaryContainer = IvoryText,
    secondary = MoonlightGold,
    onSecondary = VelvetBurgundy,
    secondaryContainer = DarkModeGoldContainer,
    onSecondaryContainer = IvoryText,
    tertiary = DuskPeach,
    onTertiary = VelvetBurgundy,
    tertiaryContainer = Color(0xFF4F2A22),
    onTertiaryContainer = IvoryText,
    background = VelvetBurgundy,
    onBackground = IvoryText,
    surface = DeepPlumSurface,
    onSurface = IvoryText,
    surfaceVariant = ElevatedPlum,
    onSurfaceVariant = SoftRoseMutedText,
    outline = PlumOutline,
    outlineVariant = Color(0xFF3E2931),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410)
)

val RomanticShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

val LocalReadingFontScale = staticCompositionLocalOf { 1.0f }

@Composable
fun JourneyOfTwoSoulsTheme(
    darkTheme: Boolean = false,
    readingFontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) RomanticDarkColorScheme else RomanticLightColorScheme

    CompositionLocalProvider(LocalReadingFontScale provides readingFontScale) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = RomanticShapes,
            content = content
        )
    }
}
