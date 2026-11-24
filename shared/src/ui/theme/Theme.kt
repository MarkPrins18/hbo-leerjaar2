package nl.carsharing.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val White = Color(0xFFFFFFFF)

/**
 * Colours that have no Material colour-scheme role: header, navigation, status colours and own
 * colours for chips, tiles, cards, photo placeholders and buttons.
 */
@Immutable
data class AppColors(
    val header: Color,
    val onHeader: Color,
    val navigation: Color,
    val navigationSelected: Color,
    val navigationUnselected: Color,
    val navigationIndicator: Color,
    val success: Color,
    val onSuccess: Color,
    val successText: Color,
    val warning: Color,
    val onWarning: Color,
    val errorText: Color,
    val chip: Color,
    val onChip: Color,
    val tile: Color,
    val tileEmphasized: Color,
    val onTile: Color,
    val card: Color,
    val photoPlaceholder: Color,
    val button: Color,
    val onButton: Color,
    // Bordered palettes frame blocks and fields; filled palettes (blockBorders false) use blockFill/fieldFill instead.
    val blockBorders: Boolean = true,
    val blockFill: Color = Color.Transparent,
    val fieldFill: Color = Color.Transparent
)

val LocalAppColors = staticCompositionLocalOf { CarSharingAppColors }

// Light: Teal A filled; amber call-to-action and selected tab.
val CarSharingAppColors = AppColors(
    header = Color(0xFF00695C), onHeader = White,
    navigation = Color(0xFFE3F1EE), navigationSelected = Color(0xFF3D2600),
    navigationUnselected = Color(0xFF566B68), navigationIndicator = Color(0xFFFFB74D),
    success = Color(0xFF2E7D32), onSuccess = White, successText = Color(0xFF2E7D32),
    warning = Color(0xFFFFF0C2), onWarning = Color(0xFF5A4300), errorText = Color(0xFFC62828),
    chip = Color(0xFFDCE8F2), onChip = Color(0xFF1F3B52),
    tile = Color(0xFFD9EAE6), tileEmphasized = Color(0xFFFFE6C2), onTile = Color(0xFF00453C),
    card = Color(0xFFD3ECE6), photoPlaceholder = Color(0xFFDDE7EE),
    button = Color(0xFFFFB74D), onButton = Color(0xFF3D2600),
    blockBorders = false,
    blockFill = Color(0xFFE6F2EF),
    fieldFill = Color(0xFFD9EAE6)
)

// Dark (deep ink): teal only for the selected tab and emphasized blocks, amber only for the main button.
val CarSharingDarkAppColors = AppColors(
    header = Color(0xFF1A2729), onHeader = Color(0xFFDCE1E4),
    navigation = Color(0xFF1B1F22), navigationSelected = Color(0xFFCFE8E3),
    navigationUnselected = Color(0xFF8E979E), navigationIndicator = Color(0xFF3C5A56),
    success = Color(0xFF82BC8F), onSuccess = Color(0xFF10261A), successText = Color(0xFF94C9A0),
    warning = Color(0xFF3F3524), onWarning = Color(0xFFF2DCAE), errorText = Color(0xFFE79C94),
    chip = Color(0xFF283239), onChip = Color(0xFFCAD6DE),
    tile = Color(0xFF2A2F33), tileEmphasized = Color(0xFF243B38), onTile = Color(0xFFDCE1E4),
    card = Color(0xFF203230), photoPlaceholder = Color(0xFF282D31),
    button = Color(0xFFE0B26A), onButton = Color(0xFF2B1D05),
    blockBorders = false,
    blockFill = Color(0xFF1F2327),
    fieldFill = Color(0xFF2A2F33)
)

// Light: teal accents on a pale mint background (Teal A from Palettes.kt).
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00796B), onPrimary = White,
    primaryContainer = Color(0xFFB8E6DF), onPrimaryContainer = Color(0xFF00382F),
    inversePrimary = Color(0xFF7FD3C6),
    secondary = Color(0xFF3E6A85), onSecondary = White,
    secondaryContainer = Color(0xFFC9DDEC), onSecondaryContainer = Color(0xFF0F3047),
    tertiary = Color(0xFFB26A00), onTertiary = White,
    tertiaryContainer = Color(0xFFFFE2B8), onTertiaryContainer = Color(0xFF4A2C00),
    error = Color(0xFFC62828), onError = White,
    errorContainer = Color(0xFFFDE3E3), onErrorContainer = Color(0xFF7A1010),
    background = Color(0xFFF4FAF9), onBackground = Color(0xFF16201F),
    surface = Color(0xFFFAFDFC), onSurface = Color(0xFF16201F),
    surfaceVariant = Color(0xFFE1EEEC), onSurfaceVariant = Color(0xFF4D5E5B),
    surfaceTint = Color(0xFFFAFDFC),
    surfaceContainerLowest = Color(0xFFFCFEFE), surfaceContainerLow = Color(0xFFEEF6F5),
    surfaceContainer = Color(0xFFE8F2F0), surfaceContainerHigh = Color(0xFFE2EEEC),
    surfaceContainerHighest = Color(0xFFDCE9E7),
    outline = Color(0xFF8FA8A4), outlineVariant = Color(0xFFD3E3E0)
)

// Dark "deep ink": cool blue-grey ink with muted teal and amber accents.
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8DC4BA), onPrimary = Color(0xFF0E2420),
    primaryContainer = Color(0xFF26403C), onPrimaryContainer = Color(0xFFCFE8E3),
    inversePrimary = Color(0xFF00796B),
    secondary = Color(0xFFA3B4C4), onSecondary = Color(0xFF17222C),
    secondaryContainer = Color(0xFF2A3540), onSecondaryContainer = Color(0xFFD6E0EA),
    tertiary = Color(0xFFE0B26A), onTertiary = Color(0xFF2B1D05),
    tertiaryContainer = Color(0xFF3F3524), onTertiaryContainer = Color(0xFFF2DFBF),
    error = Color(0xFFE79C94), onError = Color(0xFF3B0E0A),
    errorContainer = Color(0xFF552826), onErrorContainer = Color(0xFFF6D7D3),
    background = Color(0xFF15181B), onBackground = Color(0xFFDCE1E4),
    surface = Color(0xFF1B1F22), onSurface = Color(0xFFDCE1E4),
    surfaceVariant = Color(0xFF2A2F33), onSurfaceVariant = Color(0xFF9AA3AA),
    surfaceTint = Color(0xFF1B1F22),
    surfaceContainerLowest = Color(0xFF111316), surfaceContainerLow = Color(0xFF1B1F22),
    surfaceContainer = Color(0xFF202428), surfaceContainerHigh = Color(0xFF262A2E),
    surfaceContainerHighest = Color(0xFF2C3135),
    outline = Color(0xFF5F676E), outlineVariant = Color(0xFF2C3136)
)

/** A light and a dark colour set; [CarSharingTheme] picks one of the two. */
data class Palette(
    val name: String,
    val light: ColorScheme,
    val dark: ColorScheme,
    val lightApp: AppColors,
    val darkApp: AppColors
)

/** Production palette "Teal Deep Ink": filled blocks without borders, deep ink dark mode. */
val CarSharingPalette = Palette("Teal Deep Ink", LightColorScheme, DarkColorScheme, CarSharingAppColors, CarSharingDarkAppColors)

@Composable
fun CarSharingTheme(
    palette: Palette = CarSharingPalette,
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
