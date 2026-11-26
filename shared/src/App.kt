package nl.carsharing.app

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import nl.carsharing.app.navigation.Route
import nl.carsharing.app.navigation.carSharingSavedStateConfig
import nl.carsharing.app.ui.components.NavigationTab
import nl.carsharing.app.ui.screens.*
import nl.carsharing.app.ui.theme.CarSharingTheme

@Composable
@Preview
fun App() {
    CarSharingTheme {
        val backStack = rememberNavBackStack(carSharingSavedStateConfig, Route.Search)

        val onTabClick: (NavigationTab) -> Unit = { tab ->
            backStack.clear()
            backStack.add(
                when (tab) {
                    NavigationTab.Rides -> Route.Rides
                    NavigationTab.Search -> Route.Search
                    NavigationTab.Account -> Route.Account
                }
            )
        }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding),
                entryProvider = entryProvider {
                    entry<Route.Search> {
                        HomeScreen(onTabClick = onTabClick)
                    }
                    entry<Route.Rides> {
                        PlaceholderScreen(tab = NavigationTab.Rides, onTabClick = onTabClick)
                    }
                    entry<Route.Account> {
                        PlaceholderScreen(
                            tab = NavigationTab.Account,
                            onTabClick = onTabClick,
                            onMyCarsClick = { backStack.add(Route.MyCars) }
                        )
                    }
                    entry<Route.MyCars> {
                        MyCarsScreen(onBack = { backStack.removeLastOrNull() }, onTabClick = onTabClick)
                    }
                },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
            )
        }
    }
}
