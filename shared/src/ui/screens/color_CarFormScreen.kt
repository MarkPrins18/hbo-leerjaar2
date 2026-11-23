@file:Suppress("FunctionName")

package nl.carsharing.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import nl.carsharing.app.ui.components.NavigationTab
import nl.carsharing.app.ui.theme.CarSharingColorTheme

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

@Preview(widthDp = 768, heightDp = 824)
@Composable
private fun color_CarFormScreenPreview() {
    CarSharingColorTheme {
        CarFormPreviewRow()
    }
}
