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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
        modifier = modifier.clickable(role = Role.Tab, onClick = onClick)
    ) {
        // Deviation: rounded indicator behind icon + label of the selected tab, drawn outside the layout so
        // the bar keeps its size; Transparent in the grey palette, so it looks like the wireframe there.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = if (selected) Modifier.navigationIndicator(colors.navigationIndicator) else Modifier
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
}

private fun Modifier.navigationIndicator(color: Color) = drawBehind {
    val horizontal = 16.dp.toPx()
    val vertical = 4.dp.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(-horizontal, -vertical),
        size = Size(size.width + 2 * horizontal, size.height + 2 * vertical),
        cornerRadius = CornerRadius(16.dp.toPx())
    )
}

@Preview
@Composable
private fun BottomNavigationBarPreview() {
    CarSharingTheme {
        BottomNavigationBar(selectedTab = NavigationTab.Search, onTabClick = { })
    }
}
