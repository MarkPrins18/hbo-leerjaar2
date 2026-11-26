package nl.carsharing.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.carsharing.app.resources.Res
import nl.carsharing.app.resources.detail_automatic
import nl.carsharing.app.resources.detail_seats
import nl.carsharing.app.ui.screens.CarDetail
import org.jetbrains.compose.resources.stringResource

/** Scrollable row of chips with the key facts of [car]; [lastChip] (doors or colour) closes the row. */
@Composable
fun CarChips(car: CarDetail, lastChip: String?, modifier: Modifier = Modifier) {
    val chips = listOfNotNull(
        car.productionYear.toString(),
        stringResource(car.energyType.label),
        if (car.energyType.isFuel && car.automaticTransmission) stringResource(Res.string.detail_automatic) else null,
        car.seats?.let { stringResource(Res.string.detail_seats, it) },
        lastChip
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.horizontalScroll(rememberScrollState())
    ) {
        chips.forEach { Chip(it) }
    }
}
