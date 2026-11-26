package nl.carsharing.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import contracts.models.VehicleType
import nl.carsharing.app.resources.*
import nl.carsharing.app.ui.components.*
import nl.carsharing.app.ui.formatDecimal
import nl.carsharing.app.ui.icons.*
import nl.carsharing.app.ui.theme.*
import org.jetbrains.compose.resources.*

/**
 * Drive chosen before the RDW import; decides which capacity field is required.
 */
enum class DriveType(val label: StringResource, val capacityLabel: StringResource) {
    Fuel(Res.string.drive_fuel, Res.string.form_tank_capacity_l),
    Electric(Res.string.fuel_electric, Res.string.form_battery_capacity_kwh),
    Hydrogen(Res.string.fuel_hydrogen, Res.string.form_tank_capacity_kg)
}

data class CarImportForm(
    val licensePlate: String = "",
    val driveType: DriveType = DriveType.Fuel,
    val capacity: String = "",
    val automaticTransmission: Boolean = false
)

sealed interface RdwCheckResult {
    data class Approved(
        val carId: Int,
        val name: String,
        val productionYear: Int,
        val color: String,
        val energyType: EnergyType,
        val readyToDriveWeightKg: Int,
        val vehicleType: VehicleType
    ) : RdwCheckResult

    data class Rejected(val reason: StringResource) : RdwCheckResult
}

data class CarDetailsForm(
    val licensePlate: String,
    val brand: String,
    val model: String,
    val trim: String,
    val productionYear: String,
    val color: String,
    val energyType: EnergyType,
    val capacity: String,
    val consumption: String,
    val automaticTransmission: Boolean,
    val vehicleType: VehicleType,
    val seats: String,
    val doors: String,
    val readyToDriveWeightKg: String,
    val co2Emission: String,
    val roadTaxPerQuarter: String,
    val latitude: Double?,
    val longitude: Double?,
    val photoCount: Int
)

private val FuelTypes = listOf(EnergyType.Petrol, EnergyType.Diesel, EnergyType.Lpg)

private val sampleApproved = RdwCheckResult.Approved(
    carId = 1,
    name = "Toyota Yaris",
    productionYear = 2021,
    color = "Wit",
    energyType = EnergyType.Petrol,
    readyToDriveWeightKg = 1165,
    vehicleType = VehicleType.PASSENGER_CAR
)

private val sampleImportForm = CarImportForm(
    licensePlate = "AB-123-C",
    driveType = DriveType.Fuel,
    capacity = "36",
    automaticTransmission = true
)

private val sampleDetailsForm = CarDetailsForm(
    licensePlate = "AB-123-C",
    brand = "Toyota",
    model = "Yaris",
    trim = "1.5 Hybrid",
    productionYear = "2021",
    color = "Wit",
    energyType = EnergyType.Petrol,
    capacity = "36",
    consumption = "3,8",
    automaticTransmission = true,
    vehicleType = VehicleType.PASSENGER_CAR,
    seats = "5",
    doors = "5",
    readyToDriveWeightKg = "1165",
    co2Emission = "86",
    roadTaxPerQuarter = "98",
    latitude = 51.5890,
    longitude = 4.7760,
    photoCount = 3
)

/**
 * S11: add a car (variant A, [carId] null: check the licence plate at the RDW) or edit its
 * details (variant B). Holds the screen state; filled with sample data until the ViewModel and
 * API are connected.
 */
@Composable
fun CarFormScreen(
    modifier: Modifier = Modifier,
    carId: Int? = null,
    onBack: () -> Unit = { },
    onCarCreated: (Int) -> Unit = { },
    onSaved: () -> Unit = { },
    onTabClick: (NavigationTab) -> Unit = { }
) {
    if (carId == null) {
        var form by remember { mutableStateOf(CarImportForm()) }
        var result by remember { mutableStateOf<RdwCheckResult?>(null) }

        AddCarContent(
            form = form,
            onFormChange = { form = it },
            onCheck = {
                result = sampleApproved
                onCarCreated(sampleApproved.carId)
            },
            result = result,
            onBack = onBack,
            onTabClick = onTabClick,
            modifier = modifier
        )
    } else {
        var form by remember { mutableStateOf(sampleDetailsForm) }
        var currentPhoto by remember { mutableIntStateOf(0) }

        CarDetailsContent(
            form = form,
            onFormChange = { form = it },
            currentPhoto = currentPhoto,
            onPhotoChange = { currentPhoto = it },
            onAddPhoto = { },
            onSetLocation = { },
            onSave = onSaved,
            onBack = onBack,
            onTabClick = onTabClick,
            modifier = modifier
        )
    }
}

@Composable
fun AddCarContent(
    form: CarImportForm,
    onFormChange: (CarImportForm) -> Unit,
    onCheck: () -> Unit,
    result: RdwCheckResult?,
    onBack: () -> Unit,
    onTabClick: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenScaffold(stringResource(Res.string.form_add_title), NavigationTab.Account, onTabClick, modifier, onBack) { bodyModifier ->
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = bodyModifier
                .verticalScroll(rememberScrollState())
                .padding(ScreenContentPadding)
        ) {
            SectionHeader(stringResource(Res.string.form_step_check))
            Text(stringResource(Res.string.detail_license_plate), style = MaterialTheme.typography.bodyMedium)
            CompactTextField(
                value = form.licensePlate,
                onValueChange = { onFormChange(form.copy(licensePlate = it)) },
                modifier = Modifier.fillMaxWidth()
            )
            Text(stringResource(Res.string.form_drive), style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DriveType.entries.forEach { drive ->
                    Chip(
                        text = stringResource(drive.label),
                        emphasized = drive == form.driveType,
                        modifier = Modifier.clickable { onFormChange(form.copy(driveType = drive)) }
                    )
                }
            }
            TextFormRow(stringResource(form.driveType.capacityLabel), form.capacity, number = true) {
                onFormChange(form.copy(capacity = it))
            }
            if (form.driveType == DriveType.Fuel) {
                CheckboxFormRow(stringResource(Res.string.detail_automatic), form.automaticTransmission) {
                    onFormChange(form.copy(automaticTransmission = it))
                }
            }
            PrimaryButton(
                text = stringResource(Res.string.form_check_rdw),
                onClick = onCheck,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
            when (result) {
                is RdwCheckResult.Approved -> ApprovedCard(result)
                is RdwCheckResult.Rejected -> Text(
                    text = stringResource(result.reason),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalAppColors.current.errorText
                )
                null -> { }
            }
        }
    }
}

@Composable
fun CarDetailsContent(
    form: CarDetailsForm,
    onFormChange: (CarDetailsForm) -> Unit,
    currentPhoto: Int,
    onPhotoChange: (Int) -> Unit,
    onAddPhoto: () -> Unit,
    onSetLocation: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onTabClick: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val energy = form.energyType

    ScreenScaffold("${form.brand} ${form.model}", NavigationTab.Account, onTabClick, modifier, onBack) { bodyModifier ->
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = bodyModifier
                .verticalScroll(rememberScrollState())
                .padding(ScreenContentPadding)
        ) {
            Row(Modifier.height(IntrinsicSize.Min)) {
                PhotoCarousel(form.photoCount, currentPhoto, onPhotoChange, Modifier.weight(1f), compact = true)
                Spacer(Modifier.width(8.dp))
                AddPhotoTile(onAddPhoto, Modifier.width(82.dp).fillMaxHeight())
            }
            LocationRow(form.latitude, form.longitude, onSetLocation)

            SectionHeader(stringResource(Res.string.detail_car))
            TextFormRow(stringResource(Res.string.detail_license_plate), form.licensePlate) { onFormChange(form.copy(licensePlate = it)) }
            TextFormRow(stringResource(Res.string.form_brand), form.brand) { onFormChange(form.copy(brand = it)) }
            TextFormRow(stringResource(Res.string.form_model), form.model) { onFormChange(form.copy(model = it)) }
            TextFormRow(stringResource(Res.string.detail_trim), form.trim) { onFormChange(form.copy(trim = it)) }
            TextFormRow(stringResource(Res.string.form_production_year), form.productionYear, number = true) {
                onFormChange(form.copy(productionYear = it))
            }
            TextFormRow(stringResource(Res.string.detail_color), form.color) { onFormChange(form.copy(color = it)) }

            SectionHeader(stringResource(Res.string.form_drive))
            if (energy.isFuel) {
                FormRow(stringResource(Res.string.drive_fuel)) { fieldModifier ->
                    DropdownField(
                        value = stringResource(energy.label),
                        options = FuelTypes.map { stringResource(it.label) },
                        onSelect = { onFormChange(form.copy(energyType = FuelTypes[it])) },
                        modifier = fieldModifier
                    )
                }
            }
            TextFormRow(stringResource(capacityLabel(energy)), form.capacity, number = true) {
                onFormChange(form.copy(capacity = it))
            }
            TextFormRow(stringResource(consumptionLabel(energy)), form.consumption, number = true) {
                onFormChange(form.copy(consumption = it))
            }
            if (energy.isFuel) {
                CheckboxFormRow(stringResource(Res.string.detail_automatic), form.automaticTransmission) {
                    onFormChange(form.copy(automaticTransmission = it))
                }
            }

            SectionHeader(stringResource(Res.string.form_other))
            FormRow(stringResource(Res.string.form_vehicle_type)) { fieldModifier ->
                DropdownField(
                    value = stringResource(vehicleTypeLabel(form.vehicleType)),
                    options = VehicleType.entries.map { stringResource(vehicleTypeLabel(it)) },
                    onSelect = { onFormChange(form.copy(vehicleType = VehicleType.entries[it])) },
                    modifier = fieldModifier
                )
            }
            FormRow(stringResource(Res.string.form_seats_doors)) { fieldModifier ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = fieldModifier) {
                    NumberField(form.seats, { onFormChange(form.copy(seats = it)) }, Modifier.weight(1f))
                    NumberField(form.doors, { onFormChange(form.copy(doors = it)) }, Modifier.weight(1f))
                }
            }
            TextFormRow(stringResource(Res.string.form_weight), form.readyToDriveWeightKg, number = true) {
                onFormChange(form.copy(readyToDriveWeightKg = it))
            }
            TextFormRow(stringResource(Res.string.form_co2), form.co2Emission, number = true) {
                onFormChange(form.copy(co2Emission = it))
            }
            TextFormRow(stringResource(Res.string.form_road_tax), form.roadTaxPerQuarter, number = true) {
                onFormChange(form.copy(roadTaxPerQuarter = it))
            }
            PrimaryButton(
                text = stringResource(Res.string.form_save),
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }
    }
}

private fun capacityLabel(energy: EnergyType): StringResource = when {
    energy.isFuel -> Res.string.form_tank_capacity_l
    energy == EnergyType.Electric -> Res.string.form_battery_capacity_kwh
    else -> Res.string.form_tank_capacity_kg
}

private fun consumptionLabel(energy: EnergyType): StringResource = when {
    energy.isFuel -> Res.string.form_consumption_l
    energy == EnergyType.Electric -> Res.string.form_consumption_kwh
    else -> Res.string.form_consumption_kg
}

private fun vehicleTypeLabel(type: VehicleType): StringResource = when (type) {
    VehicleType.PASSENGER_CAR -> Res.string.vehicle_passenger_car
    VehicleType.LIGHT_COMMERCIAL_VEHICLE -> Res.string.vehicle_light_commercial
}

@Composable
private fun ApprovedCard(result: RdwCheckResult.Approved, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current

    DetailCard(title = null, emphasized = true, modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.form_approved),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = colors.successText,
                modifier = Modifier.weight(1f)
            )
            Chip(
                text = stringResource(Res.string.form_rdw),
                containerColor = colors.success,
                contentColor = colors.onSuccess
            )
        }
        Row {
            Text(result.name, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(Res.string.form_year_color, result.productionYear, result.color),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = stringResource(
                Res.string.form_rdw_details,
                stringResource(result.energyType.label),
                result.readyToDriveWeightKg,
                stringResource(vehicleTypeLabel(result.vehicleType))
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AddPhotoTile(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .block(LocalAppColors.current.fieldFill, BorderField)
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Text("+", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            text = stringResource(Res.string.form_add_photo),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LocationRow(
    latitude: Double?,
    longitude: Double?,
    onSetLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        SecondaryButton(
            text = stringResource(Res.string.form_location_here),
            onClick = onSetLocation,
            icon = AppIcons.LocationOn
        )
        if (latitude != null && longitude != null) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(
                    Res.string.form_coordinates,
                    formatDecimal(latitude, 4, "."),
                    formatDecimal(longitude, 4, ".")
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** One phone of [CarFormPreviewRow]: variant A (import) or B ([details]); also used by PalettePreview.kt. */
@Composable
internal fun CarFormPreviewSingle(details: Boolean) {
    val phone = Modifier
        .width(360.dp)
        .fillMaxHeight()
        .border(BorderRegular, MaterialTheme.colorScheme.outline)
    if (details) {
        CarDetailsContent(
            form = sampleDetailsForm,
            onFormChange = { },
            currentPhoto = 0,
            onPhotoChange = { },
            onAddPhoto = { },
            onSetLocation = { },
            onSave = { },
            onBack = { },
            onTabClick = { },
            modifier = phone
        )
    } else {
        AddCarContent(
            form = sampleImportForm,
            onFormChange = { },
            onCheck = { },
            result = sampleApproved,
            onBack = { },
            onTabClick = { },
            modifier = phone
        )
    }
}

@Composable
internal fun CarFormPreviewRow() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(12.dp)
    ) {
        CarFormPreviewSingle(details = false)
        CarFormPreviewSingle(details = true)
    }
}

@Preview(widthDp = 768, heightDp = 824)
@Composable
private fun CarFormScreenPreview() {
    CarSharingTheme {
        CarFormPreviewRow()
    }
}
