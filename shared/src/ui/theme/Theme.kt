package nl.carsharing.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

// Light only for now; a dark scheme follows when the final colours are chosen.
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

// Coloured version of the scheme above, used by the color_ screens.
private val BrandColorScheme = lightColorScheme(
    primary = Teal,
    onPrimary = White,
    primaryContainer = Teal,
    onPrimaryContainer = White,
    inversePrimary = TealLight,
    secondary = TealDark,
    onSecondary = White,
    secondaryContainer = TealLight,
    onSecondaryContainer = TealDark,
    tertiary = Amber,
    onTertiary = OnAmber,
    tertiaryContainer = AmberLight,
    onTertiaryContainer = OnAmber,
    error = Red,
    onError = White,
    errorContainer = RedLight,
    onErrorContainer = Red,
    background = Mint,
    onBackground = Ink,
    surface = White,
    onSurface = Ink,
    surfaceVariant = TealLight,
    onSurfaceVariant = InkMuted,
    surfaceTint = White,
    surfaceContainerLowest = White,
    surfaceContainerLow = TealLight,
    surfaceContainer = White,
    surfaceContainerHigh = White,
    surfaceContainerHighest = TealMid,
    outline = Mist,
    outlineVariant = MistLight
)

@Composable
fun CarSharingTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppColors provides GreyAppColors) {
        MaterialTheme(
            colorScheme = ColorScheme,
            typography = Typography,
            content = content
        )
    }
}

/** Coloured theme for the color_ screens; same layout, brand palette from Color.kt. */
@Composable
fun CarSharingColorTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppColors provides ColorAppColors) {
        MaterialTheme(
            colorScheme = BrandColorScheme,
            typography = Typography,
            content = content
        )
    }
}
