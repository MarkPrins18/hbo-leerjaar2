package nl.carsharing.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

// Light grey scheme that reproduces the wireframes; DarkColorScheme below is its dark variant.
private val ColorScheme = lightColorScheme(
    primary = DarkGrey,
    onPrimary = White,
    primaryContainer = LightGrey,
    onPrimaryContainer = Black,
    inversePrimary = LightGrey,
    secondary = DarkGrey,
    onSecondary = White,
    secondaryContainer = LightGrey,
    onSecondaryContainer = Black,
    tertiary = DarkGrey,
    onTertiary = White,
    tertiaryContainer = LightGrey,
    onTertiaryContainer = Black,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = LightGrey,
    onSurfaceVariant = MutedText,
    surfaceTint = White,
    surfaceContainerLowest = White,
    surfaceContainerLow = SubtleGrey,
    surfaceContainer = White,
    surfaceContainerHigh = White,
    surfaceContainerHighest = LightGrey,
    outline = IconGrey,
    outlineVariant = LineGrey
)

// Experiment: dark grey variant; the wireframes only define the light version.
private val DarkColorScheme = darkColorScheme(
    primary = LightGrey,
    onPrimary = Black,
    primaryContainer = Color(0xFF3A3A3A),
    onPrimaryContainer = White,
    inversePrimary = DarkGrey,
    secondary = LightGrey,
    onSecondary = Black,
    secondaryContainer = Color(0xFF3A3A3A),
    onSecondaryContainer = White,
    tertiary = LightGrey,
    onTertiary = Black,
    tertiaryContainer = Color(0xFF3A3A3A),
    onTertiaryContainer = White,
    background = Color(0xFF121212),
    onBackground = Color(0xFFEEEEEE),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFEEEEEE),
    surfaceVariant = Color(0xFF3A3A3A),
    onSurfaceVariant = Color(0xFFAAAAAA),
    surfaceTint = Color(0xFF121212),
    surfaceContainerLowest = Color(0xFF121212),
    surfaceContainerLow = Color(0xFF1C1C1C),
    surfaceContainer = Color(0xFF121212),
    surfaceContainerHigh = Color(0xFF121212),
    surfaceContainerHighest = Color(0xFF3A3A3A),
    outline = Color(0xFF6E6E6E),
    outlineVariant = Color(0xFF333333)
)

/** A light and a dark colour set; [CarSharingTheme] picks one of the two. */
data class Palette(
    val name: String,
    val light: ColorScheme,
    val dark: ColorScheme,
    val lightApp: AppColors,
    val darkApp: AppColors
)

/** Production palette: light reproduces the wireframes. */
val GreyPalette = Palette("Grey", ColorScheme, DarkColorScheme, GreyAppColors, GreyDarkAppColors)

// Deviation: palette is selectable and the app now follows the system dark mode (was always light grey).
@Composable
fun CarSharingTheme(
    palette: Palette = GreyPalette,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalAppColors provides if (darkTheme) palette.darkApp else palette.lightApp) {
        MaterialTheme(
            colorScheme = if (darkTheme) palette.dark else palette.light,
            typography = Typography,
            content = content
        )
    }
}

/** Coloured theme for the color_ screens; same layout, [TealPalette] from Palettes.kt. */
@Composable
fun CarSharingColorTheme(content: @Composable () -> Unit) = CarSharingTheme(palette = TealPalette, content = content)
