package nl.carsharing.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Border widths. One logical element (a car, a result, a popup) is one block; parts inside a
// block get no frame of their own, at most a separator.
// thick: popup and emphasized results, regular: a block, field: input fields and secondary
// buttons, thin: separators (in the lighter outlineVariant colour).
val BorderThick = 1.5.dp
val BorderRegular = 1.dp
val BorderField = 0.75.dp
val BorderThin = 0.5.dp

/**
 * Block background: [fill] in [shape], plus a border when [bordered] (by default when the palette uses block borders).
 */
@Composable
fun Modifier.block(
    fill: Color,
    borderWidth: Dp = BorderRegular,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    shape: Shape = MaterialTheme.shapes.extraSmall,
    bordered: Boolean = LocalAppColors.current.blockBorders
): Modifier = background(fill, shape).then(if (bordered) Modifier.border(borderWidth, borderColor, shape) else Modifier)
