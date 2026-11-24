@file:Suppress("FunctionName")

package nl.carsharing.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import nl.carsharing.app.ui.components.NavigationTab
import nl.carsharing.app.ui.theme.CarSharingColorTheme
import nl.carsharing.app.ui.theme.LightDarkRows
import nl.carsharing.app.ui.theme.TealPalette

/** Coloured version of S10 [MyCarsScreen]: same screen in [CarSharingColorTheme]. */
@Composable
fun color_MyCarsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { },
    onAddCar: () -> Unit = { },
    onEditCar: (Int) -> Unit = { },
    onRentalTermsClick: (Int) -> Unit = { },
    onCostsClick: (Int) -> Unit = { },
    onDeleteCar: (Int) -> Unit = { },
    onTabClick: (NavigationTab) -> Unit = { }
) {
    CarSharingColorTheme {
        MyCarsScreen(
            modifier = modifier,
            onBack = onBack,
            onAddCar = onAddCar,
            onEditCar = onEditCar,
            onRentalTermsClick = onRentalTermsClick,
            onCostsClick = onCostsClick,
            onDeleteCar = onDeleteCar,
            onTabClick = onTabClick
        )
    }
}

// Light on top, dark below, like TealPalettePreview; size from LightDarkRows.
@Preview(widthDp = 768, heightDp = 1648)
@Composable
private fun color_MyCarsScreenPreview() {
    LightDarkRows(
        TealPalette,
        listOf(
            { MyCarsPreviewSingle(sheetOpen = false) },
            { MyCarsPreviewSingle(sheetOpen = true) }
        )
    )
}
