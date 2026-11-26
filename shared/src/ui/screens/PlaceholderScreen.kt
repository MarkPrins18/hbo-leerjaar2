package nl.carsharing.app.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nl.carsharing.app.resources.*
import nl.carsharing.app.ui.components.*
import nl.carsharing.app.ui.theme.*
import org.jetbrains.compose.resources.stringResource

/**
 * Temporary tab root for screens that are not built yet (S06 Rides, S08 Account). Only shows the
 * links to existing screens that the wireframe of that tab has.
 */
@Composable
fun PlaceholderScreen(
    tab: NavigationTab,
    onTabClick: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier,
    onMyCarsClick: (() -> Unit)? = null
) {
    ScreenScaffold(stringResource(tab.label), tab, onTabClick, modifier) { bodyModifier ->
        Column(bodyModifier.fillMaxWidth().padding(ScreenContentPadding)) {
            if (onMyCarsClick != null) {
                SecondaryButton(
                    text = stringResource(Res.string.my_cars_title),
                    onClick = onMyCarsClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(widthDp = 384, heightDp = 824)
@Composable
private fun PlaceholderScreenPreview() {
    CarSharingTheme {
        PlaceholderScreen(
            tab = NavigationTab.Account,
            onTabClick = { },
            onMyCarsClick = { },
            modifier = Modifier
                .padding(12.dp)
                .width(360.dp)
                .border(BorderRegular, MaterialTheme.colorScheme.outline)
        )
    }
}
