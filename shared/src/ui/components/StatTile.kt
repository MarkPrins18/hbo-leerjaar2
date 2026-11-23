package nl.carsharing.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Result tile with a small grey [label] and a big bold [value] (`$stat_row` in the wireframes).
 * Tiles sit inside a block, so they get a light fill instead of a frame; an [emphasized] tile
 * (the main result) gets a darker fill.
 */
@Composable
fun StatTile(label: String, value: String, modifier: Modifier = Modifier, emphasized: Boolean = false) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .background(
                color = if (emphasized) colors.surfaceContainerHighest else colors.surfaceContainerLow,
                shape = MaterialTheme.shapes.extraSmall
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

/**
 * Two [StatTile]s side by side, together the full width; [emphasizeFirst] marks the first as main result.
 */
@Composable
fun StatRow(
    firstLabel: String,
    firstValue: String,
    secondLabel: String,
    secondValue: String,
    modifier: Modifier = Modifier,
    emphasizeFirst: Boolean = false
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.fillMaxWidth()) {
        StatTile(firstLabel, firstValue, Modifier.weight(1f), emphasized = emphasizeFirst)
        StatTile(secondLabel, secondValue, Modifier.weight(1f))
    }
}
