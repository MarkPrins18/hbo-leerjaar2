package nl.carsharing.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nl.carsharing.app.resources.Res
import nl.carsharing.app.resources.tab_account
import nl.carsharing.app.resources.tab_rides
import nl.carsharing.app.resources.tab_search
import nl.carsharing.app.ui.icons.AppIcons
import nl.carsharing.app.ui.icons.List
import nl.carsharing.app.ui.icons.Person
import nl.carsharing.app.ui.icons.Search
import nl.carsharing.app.ui.theme.CarSharingTheme
import nl.carsharing.app.ui.theme.LocalAppColors
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Tabs shown in the [BottomNavigationBar].
 */
enum class NavigationTab(val label: StringResource, val icon: ImageVector) {
    Rides(Res.string.tab_rides, AppIcons.List),
    Search(Res.string.tab_search, AppIcons.Search),
    Account(Res.string.tab_account, AppIcons.Person)
}

/**
 * Bottom navigation from the wireframes, with one item per [NavigationTab].
 */
@Composable
fun BottomNavigationBar(
    selectedTab: NavigationTab,
    onTabClick: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .background(LocalAppColors.current.navigation)
            .navigationBarsPadding()
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        NavigationTab.entries.forEach { tab ->
            BottomNavigationItem(
                tab = tab,
                selected = tab == selectedTab,
                onClick = { onTabClick(tab) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BottomNavigationItem(
    tab: NavigationTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val color = if (selected) colors.navigationSelected else colors.navigationUnselected

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier.clickable(role = Role.Tab, onClick = onClick)
    ) {
        Icon(tab.icon, contentDescription = null, tint = color)
        Text(
            text = stringResource(tab.label),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Preview
@Composable
private fun BottomNavigationBarPreview() {
    CarSharingTheme {
        BottomNavigationBar(selectedTab = NavigationTab.Search, onTabClick = { })
    }
}
