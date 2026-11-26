package nl.carsharing.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char
import kotlinx.serialization.Serializable
import nl.carsharing.app.resources.*
import nl.carsharing.app.ui.components.*
import nl.carsharing.app.ui.formatDecimal
import nl.carsharing.app.ui.jsonSaver
import nl.carsharing.app.ui.icons.*
import nl.carsharing.app.ui.theme.*
import org.jetbrains.compose.resources.stringResource

@Serializable
data class SearchFilters(
    val maxPrice: String = "",
    val maxDistanceKm: String = "",
    val startTime: LocalDateTime? = null,
    val endTime: LocalDateTime? = null
) {
    val hasValues: Boolean
        get() = maxPrice.isNotBlank() || maxDistanceKm.isNotBlank() || startTime != null || endTime != null
}

data class CarListItem(
    val id: Int,
    val name: String,
    val pricePerDay: Double,
    val distanceKm: Double
)

private val ShortDateTimeFormat = LocalDateTime.Format {
    day()
    char('-')
    monthNumber()
    char(' ')
    hour()
    char(':')
    minute()
}

/** Case-insensitive match on the name, or on price and distance as shown (`,` and `.` both work as decimal separator). */
//check: prices are compared without thousands separator, while formatMoney shows "€ 1.250,00" (search "1.250" fails)
fun matchesQuery(car: CarListItem, query: String, decimalSeparator: String): Boolean {
    val needle = query.trim().lowercase()
    if (needle.isEmpty()) return true
    val numberNeedle = needle.replace(",", decimalSeparator).replace(".", decimalSeparator)
    return car.name.lowercase().contains(needle) ||
        formatDecimal(car.pricePerDay, 2, decimalSeparator).contains(numberNeedle) ||
        formatDecimal(car.distanceKm, 1, decimalSeparator).contains(numberNeedle)
}

private val sampleCars = listOf(
    CarListItem(1, "Toyota Yaris", 45.0, 2.4),
    CarListItem(2, "Tesla Model 3", 79.0, 5.1),
    CarListItem(3, "Hyundai Nexo", 65.0, 8.7)
)

private val sampleLastSearch = SearchFilters(
    maxPrice = "50",
    maxDistanceKm = "10",
    startTime = LocalDateTime(2026, 11, 20, 10, 0)
)

/**
 * S01: search for available cars. Holds the screen state; filled with sample data until the
 * ViewModel and API are connected.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onCarClick: (Int) -> Unit = { },
    onTabClick: (NavigationTab) -> Unit = { }
) {
    var query by rememberSaveable { mutableStateOf("") }
    var filtersExpanded by rememberSaveable { mutableStateOf(false) }
    var filters by rememberSaveable(stateSaver = jsonSaver()) { mutableStateOf(SearchFilters()) }

    HomeContent(
        query = query,
        onQueryChange = { query = it },
        filtersExpanded = filtersExpanded,
        onToggleFilters = { filtersExpanded = !filtersExpanded },
        filters = filters,
        onFiltersChange = { filters = it },
        onSearch = { },
        lastSearch = sampleLastSearch,
        onRestoreLastSearch = { filters = sampleLastSearch },
        cars = sampleCars,
        onCarClick = onCarClick,
        onTabClick = onTabClick,
        modifier = modifier
    )
}

@Composable
fun HomeContent(
    query: String,
    onQueryChange: (String) -> Unit,
    filtersExpanded: Boolean,
    onToggleFilters: () -> Unit,
    filters: SearchFilters,
    onFiltersChange: (SearchFilters) -> Unit,
    onSearch: () -> Unit,
    lastSearch: SearchFilters?,
    onRestoreLastSearch: () -> Unit,
    cars: List<CarListItem>,
    onCarClick: (Int) -> Unit,
    onTabClick: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val separator = stringResource(Res.string.decimal_separator)
    val shownCars = cars.filter { matchesQuery(it, query, separator) }

    ScreenScaffold(stringResource(Res.string.app_title), NavigationTab.Search, onTabClick, modifier) { bodyModifier ->
        LazyColumn(
            contentPadding = ScreenContentPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = bodyModifier
        ) {
            item {
                SearchRow(
                    query = query,
                    onQueryChange = onQueryChange,
                    filtersExpanded = filtersExpanded,
                    hasActiveFilters = filters.hasValues,
                    onToggleFilters = onToggleFilters
                )
            }
            item {
                AnimatedVisibility(visible = filtersExpanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterFields(filters, onFiltersChange)
                        PrimaryButton(stringResource(Res.string.home_search), onSearch, Modifier.fillMaxWidth())
                    }
                }
            }
            if (lastSearch != null) {
                item { LastSearchRow(lastSearch, onRestoreLastSearch) }
            }
            item { HorizontalDivider(thickness = BorderThin, color = MaterialTheme.colorScheme.outlineVariant) }
            item { SectionHeader(stringResource(Res.string.home_available_cars, shownCars.size)) }
            if (shownCars.isEmpty()) {
                item {
                    Text(
                        text = stringResource(Res.string.home_no_cars),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(shownCars, key = { car -> car.id }) { car ->
                CarRow(car, onClick = { onCarClick(car.id) })
            }
        }
    }
}

@Composable
private fun SearchRow(
    query: String,
    onQueryChange: (String) -> Unit,
    filtersExpanded: Boolean,
    hasActiveFilters: Boolean,
    onToggleFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val appColors = LocalAppColors.current
    val shape = MaterialTheme.shapes.extraSmall

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        CompactTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = stringResource(Res.string.home_search_placeholder),
            leadingIcon = {
                Icon(
                    imageVector = AppIcons.Search,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.padding(end = 6.dp).size(18.dp)
                )
            },
            modifier = Modifier.weight(1f)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .block(
                    fill = if (filtersExpanded) colors.primary else appColors.fieldFill,
                    borderWidth = BorderField,
                    shape = shape,
                    bordered = appColors.blockBorders && !filtersExpanded
                )
                .clickable(role = Role.Button, onClick = onToggleFilters)
        ) {
            Icon(
                imageVector = AppIcons.Tune,
                contentDescription = stringResource(Res.string.home_filters_toggle),
                tint = if (filtersExpanded) colors.onPrimary else colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            if (hasActiveFilters && !filtersExpanded) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .size(8.dp)
                        .background(colors.primary, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun FilterFields(
    filters: SearchFilters,
    onFiltersChange: (SearchFilters) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = modifier) {
        TextFormRow(stringResource(Res.string.home_max_price), filters.maxPrice, number = true) {
            onFiltersChange(filters.copy(maxPrice = it))
        }
        TextFormRow(stringResource(Res.string.home_max_distance), filters.maxDistanceKm, number = true) {
            onFiltersChange(filters.copy(maxDistanceKm = it))
        }
        FormRow(stringResource(Res.string.home_period_start)) { fieldModifier ->
            DateTimeField(
                value = filters.startTime,
                onValueChange = { onFiltersChange(filters.copy(startTime = it)) },
                modifier = fieldModifier
            )
        }
        FormRow(stringResource(Res.string.home_period_end)) { fieldModifier ->
            DateTimeField(
                value = filters.endTime,
                onValueChange = { onFiltersChange(filters.copy(endTime = it)) },
                modifier = fieldModifier
            )
        }
    }
}

@Composable
private fun LastSearchRow(
    lastSearch: SearchFilters,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary = listOfNotNull(
        lastSearch.maxPrice.ifBlank { null }
            ?.let { stringResource(Res.string.home_last_search_max_price, it) },
        lastSearch.maxDistanceKm.ifBlank { null }
            ?.let { stringResource(Res.string.distance_km, it) },
        lastSearch.startTime?.let { ShortDateTimeFormat.format(it) }
    ).joinToString(", ")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 4.dp)
    ) {
        Icon(AppIcons.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(Res.string.home_last_search),
            style = MaterialTheme.typography.bodyMedium,
            textDecoration = TextDecoration.Underline
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = summary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CarRow(car: CarListItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val separator = stringResource(Res.string.decimal_separator)

    PhotoListRow(onClick = onClick, modifier = modifier) {
        Text(
            text = car.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        PriceRow(car.pricePerDay)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = AppIcons.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(Res.string.distance_km, formatDecimal(car.distanceKm, 1, separator)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    CarSharingTheme {
        HomeScreen()
    }
}

@Preview
@Composable
private fun HomeScreenEmptyPreview() {
    CarSharingTheme {
        HomeContent(
            query = "",
            onQueryChange = { },
            filtersExpanded = false,
            onToggleFilters = { },
            filters = SearchFilters(),
            onFiltersChange = { },
            onSearch = { },
            lastSearch = null,
            onRestoreLastSearch = { },
            cars = emptyList(),
            onCarClick = { },
            onTabClick = { }
        )
    }
}

@Preview
@Composable
private fun HomeScreenFiltersExpandedPreview() {
    CarSharingTheme {
        HomeContent(
            query = "",
            onQueryChange = { },
            filtersExpanded = true,
            onToggleFilters = { },
            filters = sampleLastSearch,
            onFiltersChange = { },
            onSearch = { },
            lastSearch = sampleLastSearch,
            onRestoreLastSearch = { },
            cars = sampleCars,
            onCarClick = { },
            onTabClick = { }
        )
    }
}
