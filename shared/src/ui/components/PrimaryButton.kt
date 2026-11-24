package nl.carsharing.app.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.carsharing.app.ui.theme.LocalAppColors

/**
 * Filled main action of a screen, like the `[ .. ]` buttons in the wireframes.
 */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraSmall,
        colors = ButtonDefaults.buttonColors(
            // Deviation: own button colour, so it differs from the date picker selection (grey: same LightGrey).
            containerColor = LocalAppColors.current.button,
            contentColor = LocalAppColors.current.onButton
        ),
        elevation = null,
        contentPadding = PaddingValues(vertical = 8.dp),
        modifier = modifier.height(40.dp)
    ) {
        Text(text)
    }
}
