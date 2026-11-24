package nl.carsharing.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalTime
import nl.carsharing.app.resources.Res
import nl.carsharing.app.resources.decimal_separator
import nl.carsharing.app.resources.detail_automatic
import nl.carsharing.app.resources.detail_car
import nl.carsharing.app.resources.detail_consumption
import nl.carsharing.app.resources.detail_opening_hours
import nl.carsharing.app.resources.detail_pickup
import nl.carsharing.app.resources.detail_seats
import nl.carsharing.app.resources.detail_trim
import nl.carsharing.app.resources.my_cars_add
import nl.carsharing.app.resources.my_cars_costs
import nl.carsharing.app.resources.my_cars_delete
import nl.carsharing.app.resources.my_cars_edit
import nl.carsharing.app.resources.my_cars_for_rent
import nl.carsharing.app.resources.my_cars_no_cars
import nl.carsharing.app.resources.my_cars_no_price
import nl.carsharing.app.resources.my_cars_no_terms
import nl.carsharing.app.resources.my_cars_price_per_day
import nl.carsharing.app.resources.my_cars_rental
import nl.carsharing.app.resources.my_cars_terms
import nl.carsharing.app.resources.my_cars_title
import nl.carsharing.app.resources.my_cars_your_cars
import nl.carsharing.app.resources.price_amount
import nl.carsharing.app.resources.price_per_day
import nl.carsharing.app.ui.components.BottomNavigationBar
import nl.carsharing.app.ui.components.BottomSheet
import nl.carsharing.app.ui.components.Chip
import nl.carsharing.app.ui.components.DetailRow
import nl.carsharing.app.ui.components.NavigationTab
import nl.carsharing.app.ui.components.PhotoCarousel
import nl.carsharing.app.ui.components.PhotoListRow
import nl.carsharing.app.ui.components.PrimaryButton
import nl.carsharing.app.ui.components.SecondaryButton
import nl.carsharing.app.ui.components.SectionHeader
import nl.carsharing.app.ui.components.TitleBar
import nl.carsharing.app.ui.formatDecimal
import nl.carsharing.app.ui.icons.AppIcons
import nl.carsharing.app.ui.icons.Delete
import nl.carsharing.app.ui.icons.Description
import nl.carsharing.app.ui.icons.Edit
import nl.carsharing.app.ui.icons.Euro
import nl.carsharing.app.ui.theme.BorderRegular
import nl.carsharing.app.ui.theme.BorderThin
import nl.carsharing.app.ui.theme.CarSharingTheme
import nl.carsharing.app.ui.theme.LocalAppColors
import org.jetbrains.compose.resources.stringResource

/**
 * A car of the logged-in owner; [terms] is null when the car has no rental terms (not for rent).
 */
data class MyCarItem(val car: CarDetail, val terms: RentalTermsDetail?)

private val SheetLabelWidth = 112.dp

private val sampleMyCars = listOf(
    MyCarItem(
        car = CarDetail(1, "Toyota Yaris", 2021, EnergyType.Petrol, true, 5, 5, "AB-123-C", "1.5 Hybrid", "Wit", 3.8, 36.0, 3),
        terms = RentalTermsDetail(
            pricePerDay = 45.0,
            pickupAddress = "Hoofdstraat 1, Breda",
            pickupOpen = LocalTime(8, 0),
            pickupClose = LocalTime(20, 0),
            returnAddress = "Hoofdstraat 1, Breda",
            returnOpen = LocalTime(8, 0),
            returnClose = LocalTime(22, 0)
        )
    ),
    MyCarItem(
        car = CarDetail(2, "Tesla Model 3", 2022, EnergyType.Electric, false, 5, 4, "XY-456-Z", null, "Zwart", 14.5, 60.0, 2),
        terms = RentalTermsDetail(
            pricePerDay = 79.0,
            pickupAddress = "Stationsplein 5, Breda",
            pickupOpen = null,
            pickupClose = null,
            returnAddress = "Stationsplein 5, Breda",
            returnOpen = null,
            returnClose = null
        )
    ),
    MyCarItem(
        car = CarDetail(3, "Hyundai Nexo", 2020, EnergyType.Hydrogen, false, 5, 5, "GH-789-K", null, "Blauw", 0.95, 6.3, 1),
        terms = null
    )
)

/**
 * S10: the cars of the logged-in owner. Tapping a car opens S10a, a bottom sheet with its details
 * and actions. Filled with sample data until the ViewModel and API are connected.
 */
@Composable
fun MyCarsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { },
    onAddCar: () -> Unit = { },
    onEditCar: (Int) -> Unit = { },
    onRentalTermsClick: (Int) -> Unit = { },
    onCostsClick: (Int) -> Unit = { },
    onDeleteCar: (Int) -> Unit = { },
    onTabClick: (NavigationTab) -> Unit = { }
) {
    var selectedCarId by remember { mutableStateOf<Int?>(null) }
    var currentPhoto by remember { mutableIntStateOf(0) }

    MyCarsContent(
        cars = sampleMyCars,
        selectedCar = sampleMyCars.find { it.car.id == selectedCarId },
        currentPhoto = currentPhoto,
        onPhotoChange = { currentPhoto = it },
        onCarClick = {
            selectedCarId = it
            currentPhoto = 0
        },
        onDismissSheet = { selectedCarId = null },
        onBack = onBack,
        onAddCar = onAddCar,
        onEditCar = onEditCar,
        onRentalTermsClick = onRentalTermsClick,
        onCostsClick = onCostsClick,
        onDeleteCar = {
            selectedCarId = null
            onDeleteCar(it)
        },
        onTabClick = onTabClick,
        modifier = modifier
    )
}

@Composable
fun MyCarsContent(
    cars: List<MyCarItem>,
    selectedCar: MyCarItem?,
    currentPhoto: Int,
    onPhotoChange: (Int) -> Unit,
    onCarClick: (Int) -> Unit,
    onDismissSheet: () -> Unit,
    onBack: () -> Unit,
    onAddCar: () -> Unit,
    onEditCar: (Int) -> Unit,
    onRentalTermsClick: (Int) -> Unit,
    onCostsClick: (Int) -> Unit,
    onDeleteCar: (Int) -> Unit,
    onTabClick: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TitleBar(stringResource(Res.string.my_cars_title), onBack = onBack)
        Box(Modifier.weight(1f)) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item { SectionHeader(stringResource(Res.string.my_cars_your_cars, cars.size)) }
                if (cars.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(Res.string.my_cars_no_cars),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(cars, key = { item -> item.car.id }) { item ->
                    MyCarRow(item, onClick = { onCarClick(item.car.id) })
                }
                item {
                    PrimaryButton(
                        text = stringResource(Res.string.my_cars_add),
                        onClick = onAddCar,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }
            }
            if (selectedCar != null) {
                val id = selectedCar.car.id
                MyCarSheet(
                    item = selectedCar,
                    currentPhoto = currentPhoto,
                    onPhotoChange = onPhotoChange,
                    onDismiss = onDismissSheet,
                    onEdit = { onEditCar(id) },
                    onRentalTerms = { onRentalTermsClick(id) },
                    onCosts = { onCostsClick(id) },
                    onDelete = { onDeleteCar(id) }
                )
            }
        }
        BottomNavigationBar(selectedTab = NavigationTab.Account, onTabClick = onTabClick)
    }
}

@Composable
private fun RentalStatusChip(forRent: Boolean, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current

    Chip(
        text = stringResource(if (forRent) Res.string.my_cars_for_rent else Res.string.my_cars_no_terms),
        containerColor = if (forRent) colors.success else colors.warning,
        contentColor = if (forRent) colors.onSuccess else colors.onWarning,
        modifier = modifier
    )
}

@Composable
private fun MyCarRow(item: MyCarItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val separator = stringResource(Res.string.decimal_separator)

    PhotoListRow(onClick = onClick, modifier = modifier) {
        Text(
            text = item.car.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = item.car.licensePlate,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (item.terms == null) {
            Text(
                text = stringResource(Res.string.my_cars_no_price),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Row {
                Text(
                    text = stringResource(Res.string.price_amount, formatDecimal(item.terms.pricePerDay, 2, separator)),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(4.dp))
                Text(stringResource(Res.string.price_per_day), style = MaterialTheme.typography.bodyMedium)
            }
        }
        RentalStatusChip(forRent = item.terms != null)
    }
}

@Composable
private fun MyCarSheet(
    item: MyCarItem,
    currentPhoto: Int,
    onPhotoChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onRentalTerms: () -> Unit,
    onCosts: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    BottomSheet(title = item.car.name, onDismiss = onDismiss, modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = item.car.licensePlate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            RentalStatusChip(forRent = item.terms != null)
        }
        PhotoCarousel(item.car.photoCount, currentPhoto, onPhotoChange, framed = false)
        SheetChipsRow(item.car)
        CarSummaryCard(item)
        SheetActions(onEdit, onRentalTerms, onCosts, onDelete)
    }
}

@Composable
private fun SheetChipsRow(car: CarDetail, modifier: Modifier = Modifier) {
    val chips = listOfNotNull(
        car.productionYear.toString(),
        stringResource(car.energyType.label),
        if (car.energyType.isFuel && car.automaticTransmission) stringResource(Res.string.detail_automatic) else null,
        car.seats?.let { stringResource(Res.string.detail_seats, it) },
        car.color
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.horizontalScroll(rememberScrollState())
    ) {
        chips.forEach { Chip(it) }
    }
}

@Composable
private fun CarSummaryCard(item: MyCarItem, modifier: Modifier = Modifier) {
    val separator = stringResource(Res.string.decimal_separator)
    val car = item.car
    val terms = item.terms

    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = modifier) {
        HorizontalDivider(thickness = BorderThin, color = MaterialTheme.colorScheme.outlineVariant)
        SectionHeader(stringResource(Res.string.detail_car))
        car.trim?.let { DetailRow(stringResource(Res.string.detail_trim), it, labelWidth = SheetLabelWidth) }
        car.consumption?.let {
            DetailRow(
                label = stringResource(Res.string.detail_consumption),
                value = stringResource(car.energyType.consumptionUnit, formatDecimal(it, 1, separator)),
                labelWidth = SheetLabelWidth
            )
        }
        DetailRow(
            label = stringResource(car.energyType.capacityLabel),
            value = stringResource(
                car.energyType.capacityUnit,
                formatDecimal(car.capacity, if (car.capacity % 1.0 == 0.0) 0 else 1, separator)
            ),
            labelWidth = SheetLabelWidth
        )
        HorizontalDivider(thickness = BorderThin, color = MaterialTheme.colorScheme.outlineVariant)
        SectionHeader(stringResource(Res.string.my_cars_rental))
        if (terms == null) {
            Text(
                text = stringResource(Res.string.my_cars_no_terms),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            DetailRow(
                label = stringResource(Res.string.my_cars_price_per_day),
                value = stringResource(Res.string.price_amount, formatDecimal(terms.pricePerDay, 2, separator)),
                labelWidth = SheetLabelWidth,
                bold = true
            )
            DetailRow(stringResource(Res.string.detail_pickup), terms.pickupAddress, labelWidth = SheetLabelWidth)
            if (terms.pickupOpen != null && terms.pickupClose != null) {
                DetailRow(
                    label = "",
                    value = stringResource(
                        Res.string.detail_opening_hours,
                        terms.pickupOpen.toString(),
                        terms.pickupClose.toString()
                    ),
                    labelWidth = SheetLabelWidth,
                    muted = true
                )
            }
        }
    }
}

@Composable
private fun SheetActions(
    onEdit: () -> Unit,
    onRentalTerms: () -> Unit,
    onCosts: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.padding(bottom = 4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryButton(stringResource(Res.string.my_cars_edit), onEdit, Modifier.weight(1f), icon = AppIcons.Edit)
            SecondaryButton(stringResource(Res.string.my_cars_terms), onRentalTerms, Modifier.weight(1f), icon = AppIcons.Description)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryButton(stringResource(Res.string.my_cars_costs), onCosts, Modifier.weight(1f), icon = AppIcons.Euro)
            SecondaryButton(stringResource(Res.string.my_cars_delete), onDelete, Modifier.weight(1f), icon = AppIcons.Delete)
        }
    }
}

@Composable
private fun MyCarsPreviewPhone(selectedCar: MyCarItem?, modifier: Modifier = Modifier) {
    MyCarsContent(
        cars = sampleMyCars,
        selectedCar = selectedCar,
        currentPhoto = 0,
        onPhotoChange = { },
        onCarClick = { },
        onDismissSheet = { },
        onBack = { },
        onAddCar = { },
        onEditCar = { },
        onRentalTermsClick = { },
        onCostsClick = { },
        onDeleteCar = { },
        onTabClick = { },
        modifier = modifier
            .width(360.dp)
            .fillMaxHeight()
            .border(BorderRegular, MaterialTheme.colorScheme.outline)
    )
}

/** One phone of [MyCarsPreviewRow]: S10, or S10a with the car sheet open; used by PalettePreview.kt. */
@Composable
internal fun MyCarsPreviewSingle(sheetOpen: Boolean) {
    MyCarsPreviewPhone(selectedCar = if (sheetOpen) sampleMyCars.first() else null)
}

@Composable
internal fun MyCarsPreviewRow() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(12.dp)
    ) {
        MyCarsPreviewPhone(selectedCar = null)
        MyCarsPreviewPhone(selectedCar = sampleMyCars.first())
    }
}

@Preview(widthDp = 768, heightDp = 824)
@Composable
private fun MyCarsScreenPreview() {
    CarSharingTheme {
        MyCarsPreviewRow()
    }
}
