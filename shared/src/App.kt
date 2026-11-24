package nl.carsharing.app

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import nl.carsharing.app.ui.screens.HomeScreen
import nl.carsharing.app.ui.theme.CarSharingTheme

@Composable
@Preview
fun App() {
    CarSharingTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            HomeScreen(modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding))
        }
    }
}
