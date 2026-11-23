package nl.carsharing.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import nl.carsharing.app.resources.detail_battery_capacity
import nl.carsharing.app.resources.detail_car
import nl.carsharing.app.resources.detail_color
import nl.carsharing.app.resources.detail_consumption
import nl.carsharing.app.resources.detail_doors
import nl.carsharing.app.resources.detail_license_plate
import nl.carsharing.app.resources.detail_opening_hours
import nl.carsharing.app.resources.detail_pickup
import nl.carsharing.app.resources.detail_rent
import nl.carsharing.app.resources.detail_rental_terms
import nl.carsharing.app.resources.detail_return
import nl.carsharing.app.resources.detail_seats
import nl.carsharing.app.resources.detail_tank_capacity
import nl.carsharing.app.resources.detail_trim
import nl.carsharing.app.resources.fuel_diesel
import nl.carsharing.app.resources.fuel_electric
import nl.carsharing.app.resources.fuel_hydrogen
import nl.carsharing.app.resources.fuel_lpg
import nl.carsharing.app.resources.fuel_petrol
import nl.carsharing.app.resources.price_amount
import nl.carsharing.app.resources.price_per_day
import nl.carsharing.app.resources.unit_kg_h2
import nl.carsharing.app.resources.unit_kg_per_100km
import nl.carsharing.app.resources.unit_kwh
import nl.carsharing.app.resources.unit_kwh_per_100km
import nl.carsharing.app.resources.unit_litres
import nl.carsharing.app.resources.unit_litres_per_100km
import nl.carsharing.app.ui.components.BottomNavigationBar
import nl.carsharing.app.ui.components.Chip
import nl.carsharing.app.ui.components.DetailCard
import nl.carsharing.app.ui.components.DetailRow
import nl.carsharing.app.ui.components.NavigationTab
import nl.carsharing.app.ui.components.PhotoCarousel
import nl.carsharing.app.ui.components.PrimaryButton
import nl.carsharing.app.ui.components.TitleBar
import nl.carsharing.app.ui.formatDecimal
import nl.carsharing.app.ui.theme.CarSharingTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Energy source of a car. Decides the label and units of the consumption and capacity rows;
 * only fuel cars ([isFuel]) can have a manual or automatic transmission.
 */
enum class EnergyType(
    val label: StringResource,
    val consumptionUnit: StringResource,
    val capacityLabel: StringResource,
    val capacityUnit: StringResource,
    val isFuel: Boolean
) {
    Petrol(Res.string.fuel_petrol, Res.string.unit_litres_per_100km, Res.string.detail_tank_capacity, Res.string.unit_litres, true),
    Diesel(Res.string.fuel_diesel, Res.string.unit_litres_per_100km, Res.string.detail_tank_capacity, Res.string.unit_litres, true),
    Lpg(Res.string.fuel_lpg, Res.string.unit_litres_per_100km, Res.string.detail_tank_capacity, Res.string.unit_litres, true),
    Electric(Res.string.fuel_electric, Res.string.unit_kwh_per_100km, Res.string.detail_battery_capacity, Res.string.unit_kwh, false),
    Hydrogen(Res.string.fuel_hydrogen, Res.string.unit_kg_per_100km, Res.string.detail_tank_capacity, Res.string.unit_kg_h2, false)
}

data class CarDetail(
    val id: Int,
    val name: String,
    val productionYear: Int,
    val energyType: EnergyType,
    val automaticTransmission: Boolean,
    val seats: Int?,
    val doors: Int?,
    val licensePlate: String,
    val trim: String?,
    val color: String,
    val consumption: Double?,
    val capacity: Double,
    val photoCount: Int
)

data class RentalTermsDetail(
    val pricePerDay: Double,
    val pickupAddress: String,
    val pickupOpen: LocalTime?,
    val pickupClose: LocalTime?,
    val returnAddress: String,
    val returnOpen: LocalTime?,
    val returnClose: LocalTime?
)

private val sampleCar = CarDetail(
    id = 1,
    name = "Toyota Yaris",
    productionYear = 2021,
    energyType = EnergyType.Petrol,
    automaticTransmission = true,
    seats = 5,
    doors = 5,
    licensePlate = "AB-123-C",
    trim = "1.5 Hybrid",
    color = "Wit",
    consumption = 3.8,
    capacity = 36.0,
    photoCount = 3
)

private val sampleTerms = RentalTermsDetail(
    pricePerDay = 45.0,
    pickupAddress = "Hoofdstraat 1, Breda",
    pickupOpen = LocalTime(8, 0),
    pickupClose = LocalTime(20, 0),
    returnAddress = "Hoofdstraat 1, Breda",
    returnOpen = LocalTime(8, 0),
    returnClose = LocalTime(22, 0)
)

/**
 * S02: details of one car with its rental terms. Holds the screen state; filled with sample data
 * until the ViewModel and API are connected.
 */
@Composable
fun CarDetailScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { },
    onRentClick: (Int) -> Unit = { },
    onTabClick: (NavigationTab) -> Unit = { }
) {
    var currentPhoto by remember { mutableIntStateOf(0) }

    CarDetailContent(
        car = sampleCar,
        terms = sampleTerms,
        currentPhoto = currentPhoto,
        onPhotoChange = { currentPhoto = it },
        onBack = onBack,
        onRentClick = { onRentClick(sampleCar.id) },
        onTabClick = onTabClick,
        modifier = modifier
    )
}

@Composable
fun CarDetailContent(
    car: CarDetail,
    terms: RentalTermsDetail,
    currentPhoto: Int,
    onPhotoChange: (Int) -> Unit,
    onBack: () -> Unit,
    onRentClick: () -> Unit,
    onTabClick: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TitleBar(car.name, onBack = onBack)
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            PhotoCarousel(car.photoCount, currentPhoto, onPhotoChange)
            ChipsRow(car)
            CarCard(car)
            RentalTermsCard(terms)
            PriceRow(terms.pricePerDay, onRentClick)
        }
        BottomNavigationBar(selectedTab = NavigationTab.Search, onTabClick = onTabClick)
    }
}

@Composable
private fun ChipsRow(car: CarDetail, modifier: Modifier = Modifier) {
    val chips = listOfNotNull(
        car.productionYear.toString(),
        stringResource(car.energyType.label),
        if (car.energyType.isFuel && car.automaticTransmission) stringResource(Res.string.detail_automatic) else null,
        car.seats?.let { stringResource(Res.string.detail_seats, it) },
        car.doors?.let { stringResource(Res.string.detail_doors, it) }
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.horizontalScroll(rememberScrollState())
    ) {
        chips.forEach { Chip(it) }
    }
}

@Composable
private fun CarCard(car: CarDetail, modifier: Modifier = Modifier) {
    val separator = stringResource(Res.string.decimal_separator)
    val unit = car.energyType

    DetailCard(stringResource(Res.string.detail_car), modifier) {
        DetailRow(stringResource(Res.string.detail_license_plate), car.licensePlate)
        car.trim?.let { DetailRow(stringResource(Res.string.detail_trim), it) }
        DetailRow(stringResource(Res.string.detail_color), car.color)
        car.consumption?.let {
            DetailRow(
                stringResource(Res.string.detail_consumption),
                stringResource(unit.consumptionUnit, formatDecimal(it, 1, separator))
            )
        }
        DetailRow(
            stringResource(unit.capacityLabel),
            stringResource(unit.capacityUnit, formatDecimal(car.capacity, if (car.capacity % 1.0 == 0.0) 0 else 1, separator))
        )
    }
}

@Composable
private fun RentalTermsCard(terms: RentalTermsDetail, modifier: Modifier = Modifier) {
    DetailCard(stringResource(Res.string.detail_rental_terms), modifier) {
        DetailRow(stringResource(Res.string.detail_pickup), terms.pickupAddress)
        OpeningHoursRow(terms.pickupOpen, terms.pickupClose)
        DetailRow(stringResource(Res.string.detail_return), terms.returnAddress)
        OpeningHoursRow(terms.returnOpen, terms.returnClose)
    }
}

@Composable
private fun OpeningHoursRow(open: LocalTime?, close: LocalTime?, modifier: Modifier = Modifier) {
    if (open == null || close == null) return
    DetailRow(
        label = "",
        value = stringResource(Res.string.detail_opening_hours, open.toString(), close.toString()),
        muted = true,
        modifier = modifier
    )
}

@Composable
private fun PriceRow(pricePerDay: Double, onRentClick: () -> Unit, modifier: Modifier = Modifier) {
    val separator = stringResource(Res.string.decimal_separator)

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.price_amount, formatDecimal(pricePerDay, 2, separator)),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(4.dp))
        Text(stringResource(Res.string.price_per_day), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(16.dp))
        PrimaryButton(stringResource(Res.string.detail_rent), onRentClick, Modifier.weight(1f))
    }
}

@Preview
@Composable
private fun CarDetailScreenPreview() {
    CarSharingTheme {
        CarDetailScreen()
    }
}

@Preview
@Composable
private fun CarDetailElectricPreview() {
    CarSharingTheme {
        CarDetailContent(
            car = sampleCar.copy(
                name = "Tesla Model 3",
                energyType = EnergyType.Electric,
                trim = null,
                consumption = 14.5,
                capacity = 60.0,
                photoCount = 1
            ),
            terms = sampleTerms.copy(pricePerDay = 79.0, returnOpen = null, returnClose = null),
            currentPhoto = 0,
            onPhotoChange = { },
            onBack = { },
            onRentClick = { },
            onTabClick = { }
        )
    }
}
