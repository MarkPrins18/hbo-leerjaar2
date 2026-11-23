@file:Suppress("FunctionName")

package nl.carsharing.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import nl.carsharing.app.ui.components.NavigationTab
import nl.carsharing.app.ui.theme.CarSharingColorTheme

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

@Preview(widthDp = 768, heightDp = 824)
@Composable
private fun color_MyCarsScreenPreview() {
    CarSharingColorTheme {
        MyCarsPreviewRow()
    }
}
