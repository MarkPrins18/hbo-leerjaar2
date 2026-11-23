package nl.carsharing.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Colours that have no Material colour-scheme role: header, navigation and status colours.
 * The grey values reproduce the wireframes exactly.
 */
@Immutable
data class AppColors(
    val header: Color,
    val onHeader: Color,
    val navigation: Color,
    val navigationSelected: Color,
    val navigationUnselected: Color,
    val success: Color,
    val onSuccess: Color,
    val successText: Color,
    val warning: Color,
    val onWarning: Color,
    val errorText: Color
)

val GreyAppColors = AppColors(
    header = White,
    onHeader = Black,
    navigation = White,
    navigationSelected = Black,
    navigationUnselected = Black,
    success = DarkGrey,
    onSuccess = White,
    successText = Black,
    warning = LightGrey,
    onWarning = Black,
    errorText = MutedText
)

val ColorAppColors = AppColors(
    header = Teal,
    onHeader = White,
    navigation = White,
    navigationSelected = Teal,
    navigationUnselected = InkMuted,
    success = Green,
    onSuccess = White,
    successText = Green,
    warning = AmberLight,
    onWarning = OnAmber,
    errorText = Red
)

val LocalAppColors = staticCompositionLocalOf { GreyAppColors }
