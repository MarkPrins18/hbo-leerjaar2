package nl.carsharing.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Status chip from the wireframes; [emphasized] gives the dark variant (`$chip_dark`).
 * [containerColor] and [contentColor] override the theme colours, e.g. for a status.
 */
@Composable
fun Chip(
    text: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    containerColor: Color? = null,
    contentColor: Color? = null
) {
    val colors = MaterialTheme.colorScheme

    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = contentColor ?: if (emphasized) colors.onPrimary else colors.onSecondaryContainer,
        modifier = modifier
            .background(containerColor ?: if (emphasized) colors.primary else colors.secondaryContainer)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}
