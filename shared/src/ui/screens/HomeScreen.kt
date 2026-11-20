package nl.carsharing.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import nl.carsharing.app.resources.Res
import nl.carsharing.app.resources.home_welcome
import nl.carsharing.app.ui.theme.CarSharingTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Start screen of the app.
 *
 * All screens live in this package (`shared/src/ui/screens/`), one file per screen named
 * `<Name>Screen.kt`, each with a `@Preview` wrapped in [CarSharingTheme].
 */
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(stringResource(Res.string.home_welcome))
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    CarSharingTheme {
        HomeScreen()
    }
}
