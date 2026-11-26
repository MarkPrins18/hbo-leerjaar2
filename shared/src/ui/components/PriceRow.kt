package nl.carsharing.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.carsharing.app.resources.Res
import nl.carsharing.app.resources.price_per_day
import nl.carsharing.app.ui.formatMoney
import org.jetbrains.compose.resources.stringResource

/** "€ 45,00 per dag": the bold amount in [amountStyle], followed by "per dag". */
@Composable
fun PriceRow(
    pricePerDay: Double,
    modifier: Modifier = Modifier,
    amountStyle: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Text(text = formatMoney(pricePerDay), style = amountStyle, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(4.dp))
        Text(stringResource(Res.string.price_per_day), style = MaterialTheme.typography.bodyMedium)
    }
}
