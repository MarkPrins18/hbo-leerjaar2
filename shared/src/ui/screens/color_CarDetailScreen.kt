@file:Suppress("FunctionName")

package nl.carsharing.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import nl.carsharing.app.ui.components.NavigationTab
import nl.carsharing.app.ui.theme.CarSharingColorTheme
import nl.carsharing.app.ui.theme.LightDarkRows
import nl.carsharing.app.ui.theme.TealPalette

/** Coloured version of S02 [CarDetailScreen]: same screen in [CarSharingColorTheme]. */
@Composable
fun color_CarDetailScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { },
    onRentClick: (Int) -> Unit = { },
    onTabClick: (NavigationTab) -> Unit = { }
) {
    CarSharingColorTheme {
        CarDetailScreen(modifier = modifier, onBack = onBack, onRentClick = onRentClick, onTabClick = onTabClick)
    }
}

// Light on top, dark below, like TealPalettePreview; size from LightDarkRows.
@Preview(widthDp = 384, heightDp = 1648)
@Composable
private fun color_CarDetailScreenPreview() {
    LightDarkRows(TealPalette, listOf { CarDetailScreen(modifier = it) })
}
