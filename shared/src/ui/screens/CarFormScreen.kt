package nl.carsharing.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import contracts.models.VehicleType
import nl.carsharing.app.resources.Res
import nl.carsharing.app.resources.detail_automatic
import nl.carsharing.app.resources.detail_car
import nl.carsharing.app.resources.detail_color
import nl.carsharing.app.resources.detail_license_plate
import nl.carsharing.app.resources.detail_trim
import nl.carsharing.app.resources.drive_fuel
import nl.carsharing.app.resources.form_add_photo
import nl.carsharing.app.resources.form_add_title
import nl.carsharing.app.resources.form_approved
import nl.carsharing.app.resources.form_battery_capacity_kwh
import nl.carsharing.app.resources.form_brand
import nl.carsharing.app.resources.form_check_rdw
import nl.carsharing.app.resources.form_co2
import nl.carsharing.app.resources.form_consumption_kg
import nl.carsharing.app.resources.form_consumption_kwh
import nl.carsharing.app.resources.form_consumption_l
import nl.carsharing.app.resources.form_coordinates
import nl.carsharing.app.resources.form_drive
import nl.carsharing.app.resources.form_location_here
import nl.carsharing.app.resources.form_model
import nl.carsharing.app.resources.form_other
import nl.carsharing.app.resources.form_production_year
import nl.carsharing.app.resources.form_rdw
import nl.carsharing.app.resources.form_rdw_details
import nl.carsharing.app.resources.form_road_tax
import nl.carsharing.app.resources.form_save
import nl.carsharing.app.resources.form_seats_doors
import nl.carsharing.app.resources.form_step_check
import nl.carsharing.app.resources.form_tank_capacity_kg
import nl.carsharing.app.resources.form_tank_capacity_l
import nl.carsharing.app.resources.form_vehicle_type
import nl.carsharing.app.resources.form_weight
import nl.carsharing.app.resources.form_year_color
import nl.carsharing.app.resources.fuel_electric
import nl.carsharing.app.resources.fuel_hydrogen
import nl.carsharing.app.resources.vehicle_light_commercial
import nl.carsharing.app.resources.vehicle_passenger_car
import nl.carsharing.app.ui.components.BottomNavigationBar
import nl.carsharing.app.ui.components.Chip
import nl.carsharing.app.ui.components.CompactTextField
import nl.carsharing.app.ui.components.DetailCard
import nl.carsharing.app.ui.components.DropdownField
import nl.carsharing.app.ui.components.FormRow
import nl.carsharing.app.ui.components.NavigationTab
import nl.carsharing.app.ui.components.PhotoCarousel
import nl.carsharing.app.ui.components.PrimaryButton
import nl.carsharing.app.ui.components.SecondaryButton
import nl.carsharing.app.ui.components.SectionHeader
import nl.carsharing.app.ui.components.TitleBar
import nl.carsharing.app.ui.formatDecimal
import nl.carsharing.app.ui.icons.AppIcons
import nl.carsharing.app.ui.icons.LocationOn
import nl.carsharing.app.ui.theme.BorderField
import nl.carsharing.app.ui.theme.BorderRegular
import nl.carsharing.app.ui.theme.CarSharingTheme
import nl.carsharing.app.ui.theme.LocalAppColors
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

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
    FormScaffold(stringResource(Res.string.form_add_title), onBack, onTabClick, modifier) {
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

    FormScaffold("${form.brand} ${form.model}", onBack, onTabClick, modifier) {
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

@Composable
private fun FormScaffold(
    title: String,
    onBack: () -> Unit,
    onTabClick: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TitleBar(title, onBack = onBack)
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            content()
        }
        BottomNavigationBar(selectedTab = NavigationTab.Account, onTabClick = onTabClick)
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
private fun NumberField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    CompactTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
    )
}

@Composable
private fun TextFormRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    number: Boolean = false,
    onValueChange: (String) -> Unit
) {
    FormRow(label, modifier) { fieldModifier ->
        if (number) {
            NumberField(value, onValueChange, fieldModifier)
        } else {
            CompactTextField(value = value, onValueChange = onValueChange, modifier = fieldModifier)
        }
    }
}

@Composable
private fun CheckboxFormRow(
    label: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit
) {
    FormRow(label, modifier) { fieldModifier ->
        Box(fieldModifier) {
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }
    }
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
            .border(BorderField, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.extraSmall)
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

/** Variant A and B side by side; shared by the grey and the coloured preview. */
@Composable
internal fun CarFormPreviewRow() {
    val phone = Modifier
        .width(360.dp)
        .fillMaxHeight()
        .border(BorderRegular, MaterialTheme.colorScheme.outline)
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(12.dp)
    ) {
        AddCarContent(
            form = sampleImportForm,
            onFormChange = { },
            onCheck = { },
            result = sampleApproved,
            onBack = { },
            onTabClick = { },
            modifier = phone
        )
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
    }
}

@Preview(widthDp = 768, heightDp = 824)
@Composable
private fun CarFormScreenPreview() {
    CarSharingTheme {
        CarFormPreviewRow()
    }
}
