package nl.carsharing.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import contracts.responses.*
import nl.carsharing.app.resources.*
import nl.carsharing.app.ui.components.*
import nl.carsharing.app.ui.formatDecimal
import nl.carsharing.app.ui.formatMoney
import nl.carsharing.app.ui.icons.*
import nl.carsharing.app.ui.theme.*
import org.jetbrains.compose.resources.stringResource

private val sampleTco = TcoResponse(
    carId = 1,
    annualKm = 15000.0,
    annualFuelCost = 798.0,
    annualRoadTax = 392.0,
    annualTotal = 1190.0,
    monthlyTotal = 99.17
)

private val sampleFuelCost = FuelCostResponse(
    carId = 1,
    category = "PETROL",
    distanceKm = 100.0,
    consumptionPer100Km = 3.8,
    unit = "l",
    pricePerUnit = 2.10,
    estimatedCost = 7.98
)

/**
 * S13: total cost of ownership and usage costs of one of the owner's cars. Holds the screen
 * state; filled with sample data until the ViewModel and API are connected.
 */
@Composable
fun CarCostScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { },
    onTabClick: (NavigationTab) -> Unit = { }
) {
    var annualKm by rememberSaveable { mutableStateOf("15000") }
    var distanceKm by rememberSaveable { mutableStateOf("100") }

    CarCostContent(
        car = sampleCar,
        annualKm = annualKm,
        onAnnualKmChange = { annualKm = it },
        onCalculateTco = { },
        tco = sampleTco,
        distanceKm = distanceKm,
        onDistanceKmChange = { distanceKm = it },
        onCalculateFuelCost = { },
        fuelCost = sampleFuelCost,
        onBack = onBack,
        onTabClick = onTabClick,
        modifier = modifier
    )
}

@Composable
fun CarCostContent(
    car: CarDetail,
    annualKm: String,
    onAnnualKmChange: (String) -> Unit,
    onCalculateTco: () -> Unit,
    tco: TcoResponse?,
    distanceKm: String,
    onDistanceKmChange: (String) -> Unit,
    onCalculateFuelCost: () -> Unit,
    fuelCost: FuelCostResponse?,
    onBack: () -> Unit,
    onTabClick: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenScaffold(stringResource(Res.string.cost_title), NavigationTab.Account, onTabClick, modifier, onBack) { bodyModifier ->
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = bodyModifier
                .verticalScroll(rememberScrollState())
                .padding(ScreenContentPadding)
        ) {
            CarHeader(car)
            SectionHeader(stringResource(Res.string.cost_tco))
            CostBlock(
                icon = AppIcons.DateRange,
                label = stringResource(Res.string.cost_annual_km),
                value = annualKm,
                onValueChange = onAnnualKmChange,
                onCalculate = onCalculateTco
            ) {
                tco?.let { TcoResult(it) }
            }
            Spacer(Modifier.height(4.dp))
            SectionHeader(stringResource(Res.string.cost_usage))
            CostBlock(
                icon = AppIcons.Straighten,
                label = stringResource(Res.string.cost_distance),
                value = distanceKm,
                onValueChange = onDistanceKmChange,
                onCalculate = onCalculateFuelCost
            ) {
                fuelCost?.let { FuelCostResult(it) }
            }
        }
    }
}

@Composable
private fun CarHeader(car: CarDetail, modifier: Modifier = Modifier) {
    val separator = stringResource(Res.string.decimal_separator)

    PhotoListRow(onClick = null, modifier = modifier) {
        Text(text = car.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        Text(
            text = car.licensePlate,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Chip(stringResource(car.energyType.label))
            car.consumption?.let {
                Chip(stringResource(car.energyType.consumptionUnit, formatDecimal(it, 1, separator)))
            }
        }
    }
}

/** One cost calculation as a single block: input row on top, [result] below it. */
@Composable
private fun CostBlock(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onCalculate: () -> Unit,
    modifier: Modifier = Modifier,
    result: @Composable () -> Unit
) {
    DetailCard(title = null, modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            CompactTextField(
                value = value,
                onValueChange = onValueChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.width(80.dp)
            )
            Spacer(Modifier.width(8.dp))
            SecondaryButton(stringResource(Res.string.cost_calculate), onCalculate)
        }
        result()
    }
}

@Composable
private fun TcoResult(tco: TcoResponse, modifier: Modifier = Modifier) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        StatRow(
            firstLabel = stringResource(Res.string.cost_total_per_year),
            firstValue = formatMoney(tco.annualTotal, 0),
            secondLabel = stringResource(Res.string.cost_per_km),
            secondValue = formatMoney(if (tco.annualKm > 0) tco.annualTotal / tco.annualKm else 0.0, 2),
            emphasizeFirst = true
        )
        StatRow(
            firstLabel = stringResource(Res.string.cost_fuel_per_year),
            firstValue = formatMoney(tco.annualFuelCost, 0),
            secondLabel = stringResource(Res.string.cost_road_tax_per_year),
            secondValue = formatMoney(tco.annualRoadTax, 0)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.cost_total_per_month),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formatMoney(tco.monthlyTotal, 2),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FuelCostResult(fuelCost: FuelCostResponse, modifier: Modifier = Modifier) {
    val priceLabel = when (fuelCost.unit) {
        "kWh" -> Res.string.cost_price_per_kwh
        "kg" -> Res.string.cost_price_per_kg
        else -> Res.string.cost_price_per_litre
    }

    StatRow(
        firstLabel = stringResource(Res.string.cost_estimated),
        firstValue = formatMoney(fuelCost.estimatedCost, 2),
        secondLabel = stringResource(priceLabel),
        secondValue = formatMoney(fuelCost.pricePerUnit, 2),
        emphasizeFirst = true,
        modifier = modifier
    )
}

@Preview(widthDp = 384, heightDp = 824)
@Composable
private fun CarCostScreenPreview() {
    CarSharingTheme {
        CarCostScreen(
            modifier = Modifier
                .padding(12.dp)
                .width(360.dp)
                .border(BorderRegular, MaterialTheme.colorScheme.outline)
        )
    }
}
