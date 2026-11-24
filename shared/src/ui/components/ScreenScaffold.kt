package nl.carsharing.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val ScreenContentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)

/**
 * Screen frame: [TitleBar] on top, [BottomNavigationBar] at the bottom. [content] gets the modifier for the
 * area in between and decides its own scrolling, spacing and padding (usually [ScreenContentPadding]).
 */
@Composable
fun ScreenScaffold(
    title: String,
    selectedTab: NavigationTab,
    onTabClick: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    content: @Composable (Modifier) -> Unit
) {
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TitleBar(title, onBack = onBack)
        content(Modifier.weight(1f))
        BottomNavigationBar(selectedTab = selectedTab, onTabClick = onTabClick)
    }
}
