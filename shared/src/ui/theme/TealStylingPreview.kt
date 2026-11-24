package nl.carsharing.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

// Experiment only: styling variants of Teal A, one preview each with a light and a dark row of all screens.
// The labels are the palette names: preview-only, so hardcoded instead of string resources.

// 1. Reference: exactly the current Teal A.
private val TealCurrentPalette = TealPalette.copy(name = "Teal A – current")

// 2. Filled: no frames around blocks; cards and list rows get a fill a shade off the background
//    (like the navigation bar), fields, secondary buttons and tiles a slightly stronger shade so they
//    still stand out inside a filled card. Dark: the teal-black of Teal A, with lighter fills.
internal val TealFilledPalette = TealPalette.copy(
    name = "Teal A – filled, no borders",
    lightApp = TealPalette.lightApp.copy(
        blockBorders = false,
        blockFill = Color(0xFFE6F2EF),
        fieldFill = Color(0xFFD9EAE6),
        tile = Color(0xFFD9EAE6),
        card = Color(0xFFD3ECE6)
    ),
    darkApp = TealPalette.darkApp.copy(
        blockBorders = false,
        blockFill = Color(0xFF182625),
        fieldFill = Color(0xFF22322F),
        tile = Color(0xFF22322F),
        card = Color(0xFF1D3A36)
    )
)

// 3. Warm charcoal: neutral warm greys with a hint of teal only in the header and emphasized card,
//    elevation by lighter surfaces, muted teal/amber accents and warm off-white text.
internal val TealWarmCharcoalPalette = TealFilledPalette.copy(
    name = "Teal A – dark: warm charcoal",
    dark = darkColorScheme(
        primary = Color(0xFF8CC2B7), onPrimary = Color(0xFF10241F),
        primaryContainer = Color(0xFF2C3E3A), onPrimaryContainer = Color(0xFFD5E8E3),
        inversePrimary = Color(0xFF00796B),
        secondary = Color(0xFFA9B6BF), onSecondary = Color(0xFF1B2429),
        secondaryContainer = Color(0xFF353A3D), onSecondaryContainer = Color(0xFFDCE2E6),
        tertiary = Color(0xFFD9A85E), onTertiary = Color(0xFF2E1F05),
        tertiaryContainer = Color(0xFF4A3B24), onTertiaryContainer = Color(0xFFF0DDBE),
        error = Color(0xFFE59A90), onError = Color(0xFF3D0F0A),
        errorContainer = Color(0xFF5A2A24), onErrorContainer = Color(0xFFF5D6D1),
        background = Color(0xFF1C1B1A), onBackground = Color(0xFFE8E3DC),
        surface = Color(0xFF232120), onSurface = Color(0xFFE8E3DC),
        surfaceVariant = Color(0xFF302E2C), onSurfaceVariant = Color(0xFFA9A39B),
        surfaceTint = Color(0xFF232120),
        surfaceContainerLowest = Color(0xFF171615), surfaceContainerLow = Color(0xFF222120),
        surfaceContainer = Color(0xFF272524), surfaceContainerHigh = Color(0xFF2D2B29),
        surfaceContainerHighest = Color(0xFF343230),
        outline = Color(0xFF6F6A64), outlineVariant = Color(0xFF363331)
    ),
    darkApp = AppColors(
        header = Color(0xFF233230), onHeader = Color(0xFFE8E3DC),
        navigation = Color(0xFF222120), navigationSelected = Color(0xFF2E1F05),
        navigationUnselected = Color(0xFFA39D95), navigationIndicator = Color(0xFFD9A85E),
        success = Color(0xFF7FB58A), onSuccess = Color(0xFF13261A), successText = Color(0xFF93C59D),
        warning = Color(0xFF4A3D24), onWarning = Color(0xFFF0DBAE), errorText = Color(0xFFE59A90),
        chip = Color(0xFF34322F), onChip = Color(0xFFE0DAD2),
        tile = Color(0xFF302E2C), tileEmphasized = Color(0xFF4A3B24), onTile = Color(0xFFEDE7DF),
        card = Color(0xFF283331), photoPlaceholder = Color(0xFF2E2C2A),
        button = Color(0xFFD9A85E), onButton = Color(0xFF2E1F05),
        blockBorders = false,
        blockFill = Color(0xFF262524),
        fieldFill = Color(0xFF312F2D)
    )
)

// 4. Deep ink: cool blue-grey ink with almost no teal; teal only for the selected tab and the
//    emphasized blocks, amber only for the main button; cool off-white text.
internal val TealDeepInkPalette = TealFilledPalette.copy(
    name = "Teal A – dark: deep ink, muted accents",
    dark = darkColorScheme(
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
    ),
    darkApp = AppColors(
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
)

// Same size as the PalettePreview.kt previews: 7 phones in a light and a dark row plus the label.

@Preview(widthDp = 2700, heightDp = 1700)
@Composable
private fun TealCurrentPreview() = PaletteRows(TealCurrentPalette)

@Preview(widthDp = 2700, heightDp = 1700)
@Composable
private fun TealFilledPreview() = PaletteRows(TealFilledPalette)

@Preview(widthDp = 2700, heightDp = 1700)
@Composable
private fun TealWarmCharcoalPreview() = PaletteRows(TealWarmCharcoalPalette)

@Preview(widthDp = 2700, heightDp = 1700)
@Composable
private fun TealDeepInkPreview() = PaletteRows(TealDeepInkPalette)
