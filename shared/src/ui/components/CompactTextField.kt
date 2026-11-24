package nl.carsharing.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import nl.carsharing.app.ui.theme.BorderField
import nl.carsharing.app.ui.theme.LocalAppColors

/**
 * Small single-line text field with a thin border, like the input fields in the wireframes.
 */
@Composable
fun CompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val textStyle = MaterialTheme.typography.bodyMedium
    val appColors = LocalAppColors.current

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        textStyle = textStyle.copy(color = colors.onSurface),
        cursorBrush = SolidColor(colors.onSurface),
        modifier = modifier,
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    // Experiment: fieldFill and optional frame (defaults: no fill, frame).
                    .background(appColors.fieldFill, MaterialTheme.shapes.extraSmall)
                    .then(
                        if (appColors.blockBorders) Modifier.border(BorderField, colors.outline, MaterialTheme.shapes.extraSmall)
                        else Modifier
                    )
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(placeholder, style = textStyle, color = colors.onSurfaceVariant)
                    }
                    innerTextField()
                }
                trailingIcon?.invoke()
            }
        }
    )
}
