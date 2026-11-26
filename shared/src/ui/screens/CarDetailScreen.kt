package nl.carsharing.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalTime
import nl.carsharing.app.resources.*
import nl.carsharing.app.ui.components.*
import nl.carsharing.app.ui.formatDecimal
import nl.carsharing.app.ui.formatTrimmed
import nl.carsharing.app.ui.theme.CarSharingTheme
import org.jetbrains.compose.resources.*

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

internal val sampleCar = CarDetail(
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
    var currentPhoto by rememberSaveable { mutableIntStateOf(0) }

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
    ScreenScaffold(car.name, NavigationTab.Search, onTabClick, modifier, onBack) { bodyModifier ->
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = bodyModifier
                .verticalScroll(rememberScrollState())
                .padding(ScreenContentPadding)
        ) {
            PhotoCarousel(car.photoCount, currentPhoto, onPhotoChange)
            CarChips(car, lastChip = car.doors?.let { stringResource(Res.string.detail_doors, it) })
            CarCard(car)
            RentalTermsCard(terms)
            RentRow(terms.pricePerDay, onRentClick)
        }
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
            stringResource(unit.capacityUnit, formatTrimmed(car.capacity, 2, separator))
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
private fun RentRow(pricePerDay: Double, onRentClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        PriceRow(pricePerDay, amountStyle = MaterialTheme.typography.titleLarge)
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
