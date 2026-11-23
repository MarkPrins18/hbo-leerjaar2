@file:Suppress("FunctionName")

package nl.carsharing.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import nl.carsharing.app.ui.components.NavigationTab
import nl.carsharing.app.ui.theme.CarSharingColorTheme

/** Coloured version of S01 [HomeScreen]: same screen in [CarSharingColorTheme]. */
@Composable
fun color_HomeScreen(
    modifier: Modifier = Modifier,
    onCarClick: (Int) -> Unit = { },
    onTabClick: (NavigationTab) -> Unit = { }
) {
    CarSharingColorTheme {
        HomeScreen(modifier = modifier, onCarClick = onCarClick, onTabClick = onTabClick)
    }
}

@Preview
@Composable
private fun color_HomeScreenPreview() {
    color_HomeScreen()
}
