@file:Suppress("FunctionName")

package nl.carsharing.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import nl.carsharing.app.ui.components.NavigationTab
import nl.carsharing.app.ui.theme.CarSharingColorTheme
import nl.carsharing.app.ui.theme.LightDarkRows
import nl.carsharing.app.ui.theme.TealPalette

/** Coloured version of S11 [CarFormScreen]: same screen in [CarSharingColorTheme]. */
@Composable
fun color_CarFormScreen(
    modifier: Modifier = Modifier,
    carId: Int? = null,
    onBack: () -> Unit = { },
    onCarCreated: (Int) -> Unit = { },
    onSaved: () -> Unit = { },
    onTabClick: (NavigationTab) -> Unit = { }
) {
    CarSharingColorTheme {
        CarFormScreen(
            modifier = modifier,
            carId = carId,
            onBack = onBack,
            onCarCreated = onCarCreated,
            onSaved = onSaved,
            onTabClick = onTabClick
        )
    }
}

// Light on top, dark below, like TealPalettePreview; size from LightDarkRows.
@Preview(widthDp = 768, heightDp = 1648)
@Composable
private fun color_CarFormScreenPreview() {
    LightDarkRows(
        TealPalette,
        listOf(
            { CarFormPreviewSingle(details = false) },
            { CarFormPreviewSingle(details = true) }
        )
    )
}
