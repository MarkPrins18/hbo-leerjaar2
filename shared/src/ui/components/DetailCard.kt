package nl.carsharing.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalTime
import nl.carsharing.app.resources.Res
import nl.carsharing.app.resources.detail_opening_hours
import nl.carsharing.app.ui.theme.BorderRegular
import nl.carsharing.app.ui.theme.BorderThick
import nl.carsharing.app.ui.theme.BorderThin
import nl.carsharing.app.ui.theme.LocalAppColors
import nl.carsharing.app.ui.theme.block
import org.jetbrains.compose.resources.stringResource

val LabelColumnWidth = 144.dp

/**
 * Framed card like `$card_begin(..)` in the wireframes; a [title] gets a thin line below it.
 * An [emphasized] card (an important result) gets its own fill and, in bordered palettes, a thicker border.
 */
@Composable
fun DetailCard(
    title: String?,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val appColors = LocalAppColors.current

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
            .block(
                fill = if (emphasized) appColors.card else appColors.blockFill,
                borderWidth = if (emphasized) BorderThick else BorderRegular,
                borderColor = if (emphasized) colors.primary else colors.outline
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        if (title != null) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            HorizontalDivider(thickness = BorderThin, color = MaterialTheme.colorScheme.outlineVariant)
        }
        content()
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    labelWidth: Dp = LabelColumnWidth,
    muted: Boolean = false,
    bold: Boolean = false
) {
    Row(modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(labelWidth)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else null,
            color = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )
    }
}

/** Muted "open–close" row below an address; shows nothing when the hours are unknown. */
@Composable
fun OpeningHoursRow(
    open: LocalTime?,
    close: LocalTime?,
    modifier: Modifier = Modifier,
    labelWidth: Dp = LabelColumnWidth
) {
    if (open == null || close == null) return
    DetailRow(
        label = "",
        value = stringResource(Res.string.detail_opening_hours, open.toString(), close.toString()),
        modifier = modifier,
        labelWidth = labelWidth,
        muted = true
    )
}
