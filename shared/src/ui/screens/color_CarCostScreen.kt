@file:Suppress("FunctionName")

package nl.carsharing.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import nl.carsharing.app.ui.components.NavigationTab
import nl.carsharing.app.ui.theme.CarSharingColorTheme
import nl.carsharing.app.ui.theme.LightDarkRows
import nl.carsharing.app.ui.theme.TealPalette

/** Coloured version of S13 [CarCostScreen]: same screen in [CarSharingColorTheme]. */
@Composable
fun color_CarCostScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { },
    onTabClick: (NavigationTab) -> Unit = { }
) {
    CarSharingColorTheme {
        CarCostScreen(modifier = modifier, onBack = onBack, onTabClick = onTabClick)
    }
}

// Light on top, dark below, like TealPalettePreview; size from LightDarkRows.
@Preview(widthDp = 384, heightDp = 1648)
@Composable
private fun color_CarCostScreenPreview() {
    LightDarkRows(TealPalette, listOf { CarCostScreen(modifier = it) })
}
