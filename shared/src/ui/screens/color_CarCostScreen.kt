@file:Suppress("FunctionName")

package nl.carsharing.app.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nl.carsharing.app.ui.components.NavigationTab
import nl.carsharing.app.ui.theme.BorderRegular
import nl.carsharing.app.ui.theme.CarSharingColorTheme

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

@Preview(widthDp = 384, heightDp = 824)
@Composable
private fun color_CarCostScreenPreview() {
    CarSharingColorTheme {
        CarCostScreen(
            modifier = Modifier
                .padding(12.dp)
                .width(360.dp)
                .border(BorderRegular, MaterialTheme.colorScheme.outline)
        )
    }
}
