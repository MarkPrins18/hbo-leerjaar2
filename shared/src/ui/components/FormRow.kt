package nl.carsharing.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

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

@Composable
fun NumberField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    CompactTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
    )
}

@Composable
fun TextFormRow(
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
fun CheckboxFormRow(
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
