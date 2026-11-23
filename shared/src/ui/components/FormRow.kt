package nl.carsharing.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Form line from the wireframes: label in the fixed label column, [field] fills the rest.
 */
@Composable
fun FormRow(
    label: String,
    modifier: Modifier = Modifier,
    field: @Composable (Modifier) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(LabelColumnWidth)
        )
        field(Modifier.weight(1f))
    }
}
