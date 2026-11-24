package nl.carsharing.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Experiment only: coloured palettes to compare in PalettePreview.kt; production uses CarSharingPalette (Theme.kt).
// Every role gets its own colour on purpose: light, subtly tinted backgrounds, a header that differs from
// the navigation bar and separate colours for chips, tiles, cards, photo placeholders and buttons.

// --- Teal A – calm: teal header, pale mint navigation, slate-blue chips and a warm amber call-to-action. ---

val TealPalette = Palette(
    name = "Teal A – calm",
    light = lightColorScheme(
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
    ),
    // Dark variant: teal-black surfaces, light teal accents and the same amber call-to-action.
    dark = darkColorScheme(
        primary = Color(0xFF5FD4C4), onPrimary = Color(0xFF00382F),
        primaryContainer = Color(0xFF005047), onPrimaryContainer = Color(0xFFB8F0E6),
        inversePrimary = Color(0xFF00796B),
        secondary = Color(0xFF9CC7E6), onSecondary = Color(0xFF0E2F45),
        secondaryContainer = Color(0xFF27455C), onSecondaryContainer = Color(0xFFD3E5F2),
        tertiary = Color(0xFFFFBE5C), onTertiary = Color(0xFF432A00),
        tertiaryContainer = Color(0xFF5E3F00), onTertiaryContainer = Color(0xFFFFE2B8),
        error = Color(0xFFFF8A80), onError = Color(0xFF5C0A0A),
        errorContainer = Color(0xFF8C1D18), onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF0F1817), onBackground = Color(0xFFDDE8E6),
        surface = Color(0xFF142120), onSurface = Color(0xFFDDE8E6),
        surfaceVariant = Color(0xFF243634), onSurfaceVariant = Color(0xFFA7BAB7),
        surfaceTint = Color(0xFF142120),
        surfaceContainerLowest = Color(0xFF0C1514), surfaceContainerLow = Color(0xFF182625),
        surfaceContainer = Color(0xFF1C2B29), surfaceContainerHigh = Color(0xFF22322F),
        surfaceContainerHighest = Color(0xFF2A3B39),
        outline = Color(0xFF5F7774), outlineVariant = Color(0xFF2B3B39)
    ),
    lightApp = AppColors(
        header = Color(0xFF00695C), onHeader = White,
        // Teal A only: selected tab gets the amber of the button with dark on-amber icon and label.
        navigation = Color(0xFFE3F1EE), navigationSelected = Color(0xFF3D2600),
        navigationUnselected = Color(0xFF566B68), navigationIndicator = Color(0xFFFFB74D),
        success = Color(0xFF2E7D32), onSuccess = White, successText = Color(0xFF2E7D32),
        warning = Color(0xFFFFF0C2), onWarning = Color(0xFF5A4300), errorText = Color(0xFFC62828),
        chip = Color(0xFFDCE8F2), onChip = Color(0xFF1F3B52),
        tile = Color(0xFFE6F3F1), tileEmphasized = Color(0xFFFFE6C2), onTile = Color(0xFF00453C),
        card = Color(0xFFEAF6F4), photoPlaceholder = Color(0xFFDDE7EE),
        button = Color(0xFFFFB74D), onButton = Color(0xFF3D2600)
    ),
    darkApp = AppColors(
        header = Color(0xFF0D3B35), onHeader = Color(0xFFE0F5F1),
        // Teal A only: same amber indicator in dark mode, dark on-amber icon and label.
        navigation = Color(0xFF101C1B), navigationSelected = Color(0xFF3D2600),
        navigationUnselected = Color(0xFF8AA19D), navigationIndicator = Color(0xFFFFB74D),
        success = Color(0xFF66BB6A), onSuccess = Color(0xFF0B2E0D), successText = Color(0xFF81C784),
        warning = Color(0xFF4F3B00), onWarning = Color(0xFFFFE08A), errorText = Color(0xFFFF8A80),
        chip = Color(0xFF23394A), onChip = Color(0xFFCFE3F3),
        tile = Color(0xFF1A2E2B), tileEmphasized = Color(0xFF4A3510), onTile = Color(0xFFB8F0E6),
        card = Color(0xFF173230), photoPlaceholder = Color(0xFF223038),
        button = Color(0xFFFFB74D), onButton = Color(0xFF3D2600)
    )
)

// --- Teal B – bold header: bright teal header on a deep teal navigation bar with amber selection. ---

val TealBoldPalette = Palette(
    name = "Teal B – bold header",
    light = lightColorScheme(
        primary = Color(0xFF00695C), onPrimary = White,
        primaryContainer = Color(0xFFA7E3D8), onPrimaryContainer = Color(0xFF00332B),
        inversePrimary = Color(0xFF6FCFBF),
        secondary = Color(0xFF1F6F8B), onSecondary = White,
        secondaryContainer = Color(0xFFC4E5F2), onSecondaryContainer = Color(0xFF00303F),
        tertiary = Color(0xFFC77800), onTertiary = White,
        tertiaryContainer = Color(0xFFFFE3AD), onTertiaryContainer = Color(0xFF452A00),
        error = Color(0xFFBA1A1A), onError = White,
        errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF6E0A0A),
        background = Color(0xFFF2F8F8), onBackground = Color(0xFF121E1E),
        surface = Color(0xFFF9FCFC), onSurface = Color(0xFF121E1E),
        surfaceVariant = Color(0xFFDCEBEB), onSurfaceVariant = Color(0xFF475A5A),
        surfaceTint = Color(0xFFF9FCFC),
        surfaceContainerLowest = Color(0xFFFCFEFE), surfaceContainerLow = Color(0xFFECF4F4),
        surfaceContainer = Color(0xFFE6F0F0), surfaceContainerHigh = Color(0xFFE0ECEC),
        surfaceContainerHighest = Color(0xFFDAE8E8),
        outline = Color(0xFF7FA0A0), outlineVariant = Color(0xFFCFE1E1)
    ),
    // Dark variant: near-black teal navigation, deep teal header and amber highlights.
    dark = darkColorScheme(
        primary = Color(0xFF6FDCCB), onPrimary = Color(0xFF00332B),
        primaryContainer = Color(0xFF00574E), onPrimaryContainer = Color(0xFFA7F2E5),
        inversePrimary = Color(0xFF00695C),
        secondary = Color(0xFF8ACFE8), onSecondary = Color(0xFF00334A),
        secondaryContainer = Color(0xFF134C62), onSecondaryContainer = Color(0xFFC4E5F2),
        tertiary = Color(0xFFFFC94D), onTertiary = Color(0xFF3F2A00),
        tertiaryContainer = Color(0xFF634100), onTertiaryContainer = Color(0xFFFFE3AD),
        error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF0E1717), onBackground = Color(0xFFDCE6E6),
        surface = Color(0xFF131F1F), onSurface = Color(0xFFDCE6E6),
        surfaceVariant = Color(0xFF223535), onSurfaceVariant = Color(0xFFA3B8B8),
        surfaceTint = Color(0xFF131F1F),
        surfaceContainerLowest = Color(0xFF0A1313), surfaceContainerLow = Color(0xFF172424),
        surfaceContainer = Color(0xFF1B2A2A), surfaceContainerHigh = Color(0xFF213131),
        surfaceContainerHighest = Color(0xFF283A3A),
        outline = Color(0xFF5A7676), outlineVariant = Color(0xFF283838)
    ),
    lightApp = AppColors(
        header = Color(0xFF007E71), onHeader = White,
        navigation = Color(0xFF00332D), navigationSelected = Color(0xFFFFC94D),
        navigationUnselected = Color(0xFF9CC9C1), navigationIndicator = Color.Transparent,
        success = Color(0xFF2E7D32), onSuccess = White, successText = Color(0xFF2E7D32),
        warning = Color(0xFFFFF3C4), onWarning = Color(0xFF574000), errorText = Color(0xFFBA1A1A),
        chip = Color(0xFFCDEDE8), onChip = Color(0xFF00443C),
        tile = Color(0xFFE9F1F7), tileEmphasized = Color(0xFFB5E3DA), onTile = Color(0xFF003D36),
        card = Color(0xFFFFF4DC), photoPlaceholder = Color(0xFFE0E9EC),
        button = Color(0xFF00574E), onButton = White
    ),
    darkApp = AppColors(
        header = Color(0xFF00695C), onHeader = White,
        navigation = Color(0xFF061412), navigationSelected = Color(0xFFFFC94D),
        navigationUnselected = Color(0xFF7E9A96), navigationIndicator = Color.Transparent,
        success = Color(0xFF66BB6A), onSuccess = Color(0xFF0B2E0D), successText = Color(0xFF81C784),
        warning = Color(0xFF4D3A00), onWarning = Color(0xFFFFE597), errorText = Color(0xFFFFB4AB),
        chip = Color(0xFF16413B), onChip = Color(0xFFBDEDE4),
        tile = Color(0xFF1A2A33), tileEmphasized = Color(0xFF0F4A42), onTile = Color(0xFFC9F5EC),
        card = Color(0xFF332A14), photoPlaceholder = Color(0xFF1F2C30),
        button = Color(0xFF6FDCCB), onButton = Color(0xFF00332B)
    )
)

// --- Teal + Peach A – soft: teal header on warm cream, pale peach navigation, peach call-to-action. ---

val TealPeachPalette = Palette(
    name = "Teal + Peach A – soft",
    light = lightColorScheme(
        primary = Color(0xFF00796B), onPrimary = White,
        primaryContainer = Color(0xFFC2EAE3), onPrimaryContainer = Color(0xFF00382F),
        inversePrimary = Color(0xFF7FD3C6),
        secondary = Color(0xFFB2502A), onSecondary = White,
        secondaryContainer = Color(0xFFFFD2BC), onSecondaryContainer = Color(0xFF4A1C06),
        tertiary = Color(0xFF6B7F3A), onTertiary = White,
        tertiaryContainer = Color(0xFFE3ECC6), onTertiaryContainer = Color(0xFF27330A),
        error = Color(0xFFC62828), onError = White,
        errorContainer = Color(0xFFFDE3E3), onErrorContainer = Color(0xFF7A1010),
        background = Color(0xFFFFF8F4), onBackground = Color(0xFF221E1C),
        surface = Color(0xFFFFFCFA), onSurface = Color(0xFF221E1C),
        surfaceVariant = Color(0xFFF6E6DD), onSurfaceVariant = Color(0xFF5E5650),
        surfaceTint = Color(0xFFFFFCFA),
        surfaceContainerLowest = Color(0xFFFFFDFC), surfaceContainerLow = Color(0xFFFDF2EC),
        surfaceContainer = Color(0xFFFAEDE6), surfaceContainerHigh = Color(0xFFF6E7DF),
        surfaceContainerHighest = Color(0xFFF2E1D8),
        outline = Color(0xFFB8A195), outlineVariant = Color(0xFFEEDDD3)
    ),
    // Dark variant: deep teal-black with soft peach highlights.
    dark = darkColorScheme(
        primary = Color(0xFF5CC8B8), onPrimary = Color(0xFF00332D),
        primaryContainer = Color(0xFF00514A), onPrimaryContainer = Color(0xFFC2EAE3),
        inversePrimary = Color(0xFF00796B),
        secondary = Color(0xFFFFB38F), onSecondary = Color(0xFF4A1A00),
        secondaryContainer = Color(0xFF6B3A22), onSecondaryContainer = Color(0xFFFFDCCB),
        tertiary = Color(0xFFC3D48F), onTertiary = Color(0xFF27330A),
        tertiaryContainer = Color(0xFF3E4A1C), onTertiaryContainer = Color(0xFFE3ECC6),
        error = Color(0xFFFF8A80), onError = Color(0xFF5C0A0A),
        errorContainer = Color(0xFF8C1D18), onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF141C1B), onBackground = Color(0xFFE8EEEC),
        surface = Color(0xFF1A2423), onSurface = Color(0xFFE8EEEC),
        surfaceVariant = Color(0xFF33302C), onSurfaceVariant = Color(0xFFBFB3AC),
        surfaceTint = Color(0xFF1A2423),
        surfaceContainerLowest = Color(0xFF101817), surfaceContainerLow = Color(0xFF1D2827),
        surfaceContainer = Color(0xFF212C2B), surfaceContainerHigh = Color(0xFF283331),
        surfaceContainerHighest = Color(0xFF303B39),
        outline = Color(0xFF7F8F8B), outlineVariant = Color(0xFF2F3E3B)
    ),
    lightApp = AppColors(
        header = Color(0xFF00796B), onHeader = White,
        navigation = Color(0xFFFFEDE3), navigationSelected = Color(0xFFB2502A),
        navigationUnselected = Color(0xFF7A6A62), navigationIndicator = Color.Transparent,
        success = Color(0xFF2E7D32), onSuccess = White, successText = Color(0xFF2E7D32),
        warning = Color(0xFFFFF0C2), onWarning = Color(0xFF5A4300), errorText = Color(0xFFC62828),
        chip = Color(0xFFFFE3D3), onChip = Color(0xFF6B2F12),
        tile = Color(0xFFE6F4F1), tileEmphasized = Color(0xFFFFD6C0), onTile = Color(0xFF143B36),
        card = Color(0xFFFFF0E6), photoPlaceholder = Color(0xFFEDE6E1),
        button = Color(0xFFFF8A5B), onButton = Color(0xFF3A1200)
    ),
    darkApp = AppColors(
        header = Color(0xFF0D4A43), onHeader = Color(0xFFE0F5F1),
        navigation = Color(0xFF0F1716), navigationSelected = Color(0xFFFFA67E),
        navigationUnselected = Color(0xFF8FA9A5), navigationIndicator = Color.Transparent,
        success = Color(0xFF66BB6A), onSuccess = Color(0xFF0B2E0D), successText = Color(0xFF81C784),
        warning = Color(0xFF4F3B00), onWarning = Color(0xFFFFE08A), errorText = Color(0xFFFF8A80),
        chip = Color(0xFF5A3322), onChip = Color(0xFFFFDCCB),
        tile = Color(0xFF1D302D), tileEmphasized = Color(0xFF5E3520), onTile = Color(0xFFFFE6D8),
        card = Color(0xFF2A2420), photoPlaceholder = Color(0xFF2A2724),
        button = Color(0xFFFF9B73), onButton = Color(0xFF3A1200)
    )
)

// --- Teal + Peach B – peach header: peach header with dark text, teal navigation and teal buttons. ---

val TealPeachHeaderPalette = Palette(
    name = "Teal + Peach B – peach header",
    light = lightColorScheme(
        primary = Color(0xFF00796B), onPrimary = White,
        primaryContainer = Color(0xFFB4E4DB), onPrimaryContainer = Color(0xFF00352E),
        inversePrimary = Color(0xFF7FD3C6),
        secondary = Color(0xFFC0582E), onSecondary = White,
        secondaryContainer = Color(0xFFFFDBC9), onSecondaryContainer = Color(0xFF4D1D05),
        tertiary = Color(0xFF7A5C9E), onTertiary = White,
        tertiaryContainer = Color(0xFFEBDDFA), onTertiaryContainer = Color(0xFF2E1647),
        error = Color(0xFFC62828), onError = White,
        errorContainer = Color(0xFFFDE3E3), onErrorContainer = Color(0xFF7A1010),
        background = Color(0xFFFDF8F3), onBackground = Color(0xFF231E1A),
        surface = Color(0xFFFFFCF9), onSurface = Color(0xFF231E1A),
        surfaceVariant = Color(0xFFF3E7DC), onSurfaceVariant = Color(0xFF5F564E),
        surfaceTint = Color(0xFFFFFCF9),
        surfaceContainerLowest = Color(0xFFFFFDFB), surfaceContainerLow = Color(0xFFFAF2EA),
        surfaceContainer = Color(0xFFF7EDE4), surfaceContainerHigh = Color(0xFFF3E8DE),
        surfaceContainerHighest = Color(0xFFEFE2D7),
        outline = Color(0xFFB3A293), outlineVariant = Color(0xFFEBDDD0)
    ),
    // Dark variant: burnt peach header, deep teal navigation, light teal buttons.
    dark = darkColorScheme(
        primary = Color(0xFF66D2C2), onPrimary = Color(0xFF00352E),
        primaryContainer = Color(0xFF00564D), onPrimaryContainer = Color(0xFFB4F0E5),
        inversePrimary = Color(0xFF00796B),
        secondary = Color(0xFFFFB592), onSecondary = Color(0xFF4D1D05),
        secondaryContainer = Color(0xFF703A20), onSecondaryContainer = Color(0xFFFFDBC9),
        tertiary = Color(0xFFD3BCF2), onTertiary = Color(0xFF2E1647),
        tertiaryContainer = Color(0xFF4A3466), onTertiaryContainer = Color(0xFFEBDDFA),
        error = Color(0xFFFF8A80), onError = Color(0xFF5C0A0A),
        errorContainer = Color(0xFF8C1D18), onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF1A1613), onBackground = Color(0xFFEFE6DF),
        surface = Color(0xFF211C18), onSurface = Color(0xFFEFE6DF),
        surfaceVariant = Color(0xFF3A3029), onSurfaceVariant = Color(0xFFC9B9AC),
        surfaceTint = Color(0xFF211C18),
        surfaceContainerLowest = Color(0xFF15110F), surfaceContainerLow = Color(0xFF241F1B),
        surfaceContainer = Color(0xFF29231F), surfaceContainerHigh = Color(0xFF302925),
        surfaceContainerHighest = Color(0xFF38302B),
        outline = Color(0xFF8A7B70), outlineVariant = Color(0xFF3A322C)
    ),
    lightApp = AppColors(
        header = Color(0xFFFFB08A), onHeader = Color(0xFF3A1200),
        navigation = Color(0xFF00594F), navigationSelected = Color(0xFFFFC2A3),
        navigationUnselected = Color(0xFF9FCFC7), navigationIndicator = Color.Transparent,
        success = Color(0xFF2E7D32), onSuccess = White, successText = Color(0xFF2E7D32),
        warning = Color(0xFFFFF1BF), onWarning = Color(0xFF5A4300), errorText = Color(0xFFC62828),
        chip = Color(0xFFD2EEE9), onChip = Color(0xFF00443C),
        tile = Color(0xFFFFF0E8), tileEmphasized = Color(0xFFBDE5DE), onTile = Color(0xFF2A1A10),
        card = Color(0xFFE9F6F3), photoPlaceholder = Color(0xFFEAE4DF),
        button = Color(0xFF00796B), onButton = White
    ),
    darkApp = AppColors(
        header = Color(0xFF7A3E22), onHeader = Color(0xFFFFE3D3),
        navigation = Color(0xFF0D2623), navigationSelected = Color(0xFFFFB38F),
        navigationUnselected = Color(0xFF86A9A3), navigationIndicator = Color.Transparent,
        success = Color(0xFF66BB6A), onSuccess = Color(0xFF0B2E0D), successText = Color(0xFF81C784),
        warning = Color(0xFF4F3B00), onWarning = Color(0xFFFFE08A), errorText = Color(0xFFFF8A80),
        chip = Color(0xFF15423C), onChip = Color(0xFFC2F0E7),
        tile = Color(0xFF2D231D), tileEmphasized = Color(0xFF16453F), onTile = Color(0xFFFFE6D8),
        card = Color(0xFF1B2C29), photoPlaceholder = Color(0xFF2D2824),
        button = Color(0xFF66D2C2), onButton = Color(0xFF00352E)
    )
)

// --- Indigo + Coral A – indigo header: deep indigo header, pale lavender navigation, coral call-to-action. ---

val IndigoCoralPalette = Palette(
    name = "Indigo + Coral A – indigo header",
    light = lightColorScheme(
        primary = Color(0xFF3F3D9E), onPrimary = White,
        primaryContainer = Color(0xFFDCDBFF), onPrimaryContainer = Color(0xFF1A1868),
        inversePrimary = Color(0xFFB4B2FF),
        secondary = Color(0xFFC2412D), onSecondary = White,
        secondaryContainer = Color(0xFFFFDAD2), onSecondaryContainer = Color(0xFF5C1A0E),
        tertiary = Color(0xFF8A6D00), onTertiary = White,
        tertiaryContainer = Color(0xFFFFE8A3), onTertiaryContainer = Color(0xFF3A2D00),
        error = Color(0xFFB3261E), onError = White,
        errorContainer = Color(0xFFF9DEDC), onErrorContainer = Color(0xFF601410),
        background = Color(0xFFF7F6FD), onBackground = Color(0xFF1B1A33),
        surface = Color(0xFFFCFBFF), onSurface = Color(0xFF1B1A33),
        surfaceVariant = Color(0xFFE6E4F6), onSurfaceVariant = Color(0xFF55546E),
        surfaceTint = Color(0xFFFCFBFF),
        surfaceContainerLowest = Color(0xFFFEFDFF), surfaceContainerLow = Color(0xFFF3F2FC),
        surfaceContainer = Color(0xFFEEEDFA), surfaceContainerHigh = Color(0xFFE9E8F7),
        surfaceContainerHighest = Color(0xFFE3E1F4),
        outline = Color(0xFF9A98C2), outlineVariant = Color(0xFFDDDBF0)
    ),
    // Dark variant: midnight indigo with coral highlights.
    dark = darkColorScheme(
        primary = Color(0xFFB4B2FF), onPrimary = Color(0xFF1E1C66),
        primaryContainer = Color(0xFF34328A), onPrimaryContainer = Color(0xFFE0DFFF),
        inversePrimary = Color(0xFF3F3D9E),
        secondary = Color(0xFFFF8A75), onSecondary = Color(0xFF4A0F04),
        secondaryContainer = Color(0xFF7A2E20), onSecondaryContainer = Color(0xFFFFDAD2),
        tertiary = Color(0xFFE8C65A), onTertiary = Color(0xFF3A2D00),
        tertiaryContainer = Color(0xFF574500), onTertiaryContainer = Color(0xFFFFE8A3),
        error = Color(0xFFF2B8B5), onError = Color(0xFF601410),
        errorContainer = Color(0xFF8C1D18), onErrorContainer = Color(0xFFF9DEDC),
        background = Color(0xFF13122A), onBackground = Color(0xFFE6E4FF),
        surface = Color(0xFF1A1936), onSurface = Color(0xFFE6E4FF),
        surfaceVariant = Color(0xFF2E2C5C), onSurfaceVariant = Color(0xFFB0AED6),
        surfaceTint = Color(0xFF1A1936),
        surfaceContainerLowest = Color(0xFF0F0E24), surfaceContainerLow = Color(0xFF1E1D3D),
        surfaceContainer = Color(0xFF232244), surfaceContainerHigh = Color(0xFF29284C),
        surfaceContainerHighest = Color(0xFF302F55),
        outline = Color(0xFF6E6CA0), outlineVariant = Color(0xFF2E2D52)
    ),
    lightApp = AppColors(
        header = Color(0xFF2B2A7A), onHeader = White,
        navigation = Color(0xFFEEEDFA), navigationSelected = Color(0xFFB83A26),
        navigationUnselected = Color(0xFF66648A), navigationIndicator = Color.Transparent,
        success = Color(0xFF2A7F4F), onSuccess = White, successText = Color(0xFF1E7A48),
        warning = Color(0xFFFFF0C2), onWarning = Color(0xFF5A4300), errorText = Color(0xFFB3261E),
        chip = Color(0xFFE4E3FB), onChip = Color(0xFF2B2A7A),
        tile = Color(0xFFF0EFFB), tileEmphasized = Color(0xFFFFD6CC), onTile = Color(0xFF2B2A7A),
        card = Color(0xFFFFF1ED), photoPlaceholder = Color(0xFFE3E2EE),
        button = Color(0xFFFF6F59), onButton = Color(0xFF3B0A00)
    ),
    darkApp = AppColors(
        header = Color(0xFF24235E), onHeader = White,
        navigation = Color(0xFF0E0D22), navigationSelected = Color(0xFFFF8A75),
        navigationUnselected = Color(0xFF8F8DC0), navigationIndicator = Color.Transparent,
        success = Color(0xFF5CCB8A), onSuccess = Color(0xFF0B2E1A), successText = Color(0xFF5CCB8A),
        warning = Color(0xFF4F3F00), onWarning = Color(0xFFFFE597), errorText = Color(0xFFF2B8B5),
        chip = Color(0xFF2E2D5E), onChip = Color(0xFFDCDBFF),
        tile = Color(0xFF201F42), tileEmphasized = Color(0xFF5A2A22), onTile = Color(0xFFFFDAD2),
        card = Color(0xFF2A1F38), photoPlaceholder = Color(0xFF26253F),
        button = Color(0xFFFF8A75), onButton = Color(0xFF3B0A00)
    )
)

// --- Indigo + Coral B – coral navigation: indigo header on a blush background, coral navigation bar, indigo buttons. ---

val IndigoCoralNavPalette = Palette(
    name = "Indigo + Coral B – coral navigation",
    light = lightColorScheme(
        primary = Color(0xFF3F3D9E), onPrimary = White,
        primaryContainer = Color(0xFFE0DFFF), onPrimaryContainer = Color(0xFF1A1868),
        inversePrimary = Color(0xFFB4B2FF),
        secondary = Color(0xFFB83A26), onSecondary = White,
        secondaryContainer = Color(0xFFFFD3C9), onSecondaryContainer = Color(0xFF5C1A0E),
        tertiary = Color(0xFF00727A), onTertiary = White,
        tertiaryContainer = Color(0xFFC6EEF0), onTertiaryContainer = Color(0xFF002F33),
        error = Color(0xFFB3261E), onError = White,
        errorContainer = Color(0xFFF9DEDC), onErrorContainer = Color(0xFF601410),
        background = Color(0xFFFFF8F6), onBackground = Color(0xFF221A2A),
        surface = Color(0xFFFFFCFB), onSurface = Color(0xFF221A2A),
        surfaceVariant = Color(0xFFF4E6E6), onSurfaceVariant = Color(0xFF5E5262),
        surfaceTint = Color(0xFFFFFCFB),
        surfaceContainerLowest = Color(0xFFFFFDFD), surfaceContainerLow = Color(0xFFFCF1EF),
        surfaceContainer = Color(0xFFF8ECEA), surfaceContainerHigh = Color(0xFFF4E6E4),
        surfaceContainerHighest = Color(0xFFEFE0DE),
        outline = Color(0xFFAE9AA6), outlineVariant = Color(0xFFEBDBDB)
    ),
    // Dark variant: indigo header, burnt coral navigation, light indigo buttons.
    dark = darkColorScheme(
        primary = Color(0xFFB4B2FF), onPrimary = Color(0xFF1E1C66),
        primaryContainer = Color(0xFF34328A), onPrimaryContainer = Color(0xFFE0DFFF),
        inversePrimary = Color(0xFF3F3D9E),
        secondary = Color(0xFFFF9580), onSecondary = Color(0xFF4A0F04),
        secondaryContainer = Color(0xFF7A2E20), onSecondaryContainer = Color(0xFFFFDAD2),
        tertiary = Color(0xFF7DD6DD), onTertiary = Color(0xFF002F33),
        tertiaryContainer = Color(0xFF00494F), onTertiaryContainer = Color(0xFFC6EEF0),
        error = Color(0xFFF2B8B5), onError = Color(0xFF601410),
        errorContainer = Color(0xFF8C1D18), onErrorContainer = Color(0xFFF9DEDC),
        background = Color(0xFF181522), onBackground = Color(0xFFECE4EE),
        surface = Color(0xFF1F1B2B), onSurface = Color(0xFFECE4EE),
        surfaceVariant = Color(0xFF392F40), onSurfaceVariant = Color(0xFFC0B2C2),
        surfaceTint = Color(0xFF1F1B2B),
        surfaceContainerLowest = Color(0xFF13111C), surfaceContainerLow = Color(0xFF221E2F),
        surfaceContainer = Color(0xFF272334), surfaceContainerHigh = Color(0xFF2E293B),
        surfaceContainerHighest = Color(0xFF353043),
        outline = Color(0xFF7C6E80), outlineVariant = Color(0xFF352D3A)
    ),
    lightApp = AppColors(
        header = Color(0xFF3F3D9E), onHeader = White,
        navigation = Color(0xFFFF7A63), navigationSelected = Color(0xFF1E1C66),
        navigationUnselected = Color(0xFF5E2116), navigationIndicator = Color.Transparent,
        success = Color(0xFF2A7F4F), onSuccess = White, successText = Color(0xFF1E7A48),
        warning = Color(0xFFFFF0C2), onWarning = Color(0xFF5A4300), errorText = Color(0xFFB3261E),
        chip = Color(0xFFFFE0D8), onChip = Color(0xFF6B2416),
        tile = Color(0xFFEFEEFB), tileEmphasized = Color(0xFFD9D8FA), onTile = Color(0xFF1E1C66),
        card = Color(0xFFF3F2FC), photoPlaceholder = Color(0xFFEDE5E8),
        button = Color(0xFF3F3D9E), onButton = White
    ),
    darkApp = AppColors(
        header = Color(0xFF2E2C80), onHeader = White,
        navigation = Color(0xFF8C3426), navigationSelected = Color(0xFFFFE3DB),
        navigationUnselected = Color(0xFFEBB3A6), navigationIndicator = Color.Transparent,
        success = Color(0xFF5CCB8A), onSuccess = Color(0xFF0B2E1A), successText = Color(0xFF5CCB8A),
        warning = Color(0xFF4F3F00), onWarning = Color(0xFFFFE597), errorText = Color(0xFFF2B8B5),
        chip = Color(0xFF5E2A20), onChip = Color(0xFFFFDAD2),
        tile = Color(0xFF221F3A), tileEmphasized = Color(0xFF34328A), onTile = Color(0xFFE6E4FF),
        card = Color(0xFF26223F), photoPlaceholder = Color(0xFF2A2433),
        button = Color(0xFFB4B2FF), onButton = Color(0xFF1E1C66)
    )
)
