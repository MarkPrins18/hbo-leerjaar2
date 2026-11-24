package nl.carsharing.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

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

val LocalAppColors = staticCompositionLocalOf { CarSharingAppColors }
