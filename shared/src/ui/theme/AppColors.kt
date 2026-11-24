package nl.carsharing.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Colours that have no Material colour-scheme role: header, navigation, status colours and a few
 * elements that each get their own colour in the coloured palettes.
 * The grey values reproduce the wireframes exactly.
 */
@Immutable
data class AppColors(
    val header: Color,
    val onHeader: Color,
    val navigation: Color,
    val navigationSelected: Color,
    val navigationUnselected: Color,
    // Deviation: highlight behind the selected tab; Transparent keeps the wireframe look.
    val navigationIndicator: Color,
    val success: Color,
    val onSuccess: Color,
    val successText: Color,
    val warning: Color,
    val onWarning: Color,
    val errorText: Color,
    // Deviation: own roles so chips, tiles, cards, photo placeholders and buttons can differ per palette
    // (grey maps them to the Material roles they used before).
    val chip: Color,
    val onChip: Color,
    val tile: Color,
    val tileEmphasized: Color,
    val onTile: Color,
    val card: Color,
    val photoPlaceholder: Color,
    val button: Color,
    val onButton: Color,
    // Experiment: block style. The defaults keep the wireframe look (frames, no fill); a "filled"
    // palette sets blockBorders false and gives blocks (cards, list rows) and fields/secondary
    // buttons their own fill instead of a frame.
    val blockBorders: Boolean = true,
    val blockFill: Color = Color.Transparent,
    val fieldFill: Color = Color.Transparent
)

val GreyAppColors = AppColors(
    header = White,
    onHeader = Black,
    navigation = White,
    navigationSelected = Black,
    navigationUnselected = Black,
    navigationIndicator = Color.Transparent,
    success = DarkGrey,
    onSuccess = White,
    successText = Black,
    warning = LightGrey,
    onWarning = Black,
    errorText = MutedText,
    chip = LightGrey,
    onChip = Black,
    tile = SubtleGrey,
    tileEmphasized = LightGrey,
    onTile = Black,
    card = SubtleGrey,
    photoPlaceholder = SubtleGrey,
    button = LightGrey,
    onButton = Black
)

// Experiment: dark variant of GreyAppColors.
val GreyDarkAppColors = AppColors(
    header = Color(0xFF121212),
    onHeader = Color(0xFFEEEEEE),
    navigation = Color(0xFF121212),
    navigationSelected = Color(0xFFEEEEEE),
    navigationUnselected = Color(0xFFEEEEEE),
    navigationIndicator = Color.Transparent,
    success = LightGrey,
    onSuccess = Black,
    successText = Color(0xFFEEEEEE),
    warning = Color(0xFF3A3A3A),
    onWarning = Color(0xFFEEEEEE),
    errorText = Color(0xFFAAAAAA),
    chip = Color(0xFF3A3A3A),
    onChip = White,
    tile = Color(0xFF1C1C1C),
    tileEmphasized = Color(0xFF3A3A3A),
    onTile = Color(0xFFEEEEEE),
    card = Color(0xFF1C1C1C),
    photoPlaceholder = Color(0xFF1C1C1C),
    button = Color(0xFF3A3A3A),
    onButton = White
)

val LocalAppColors = staticCompositionLocalOf { GreyAppColors }
