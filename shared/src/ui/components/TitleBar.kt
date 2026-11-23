package nl.carsharing.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.carsharing.app.resources.Res
import nl.carsharing.app.resources.detail_back
import nl.carsharing.app.ui.icons.AppIcons
import nl.carsharing.app.ui.icons.ArrowBack
import nl.carsharing.app.ui.theme.LocalAppColors
import org.jetbrains.compose.resources.stringResource

/**
 * Screen title from the wireframes; shows a back arrow when [onBack] is given.
 */
@Composable
fun TitleBar(title: String, modifier: Modifier = Modifier, onBack: (() -> Unit)? = null) {
    val colors = LocalAppColors.current
    val barModifier = modifier.fillMaxWidth().background(colors.header)

    if (onBack == null) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.onHeader,
            modifier = barModifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        return
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = barModifier.padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(40.dp).clickable(onClick = onBack)
        ) {
            Icon(
                imageVector = AppIcons.ArrowBack,
                contentDescription = stringResource(Res.string.detail_back),
                tint = colors.onHeader
            )
        }
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = colors.onHeader)
    }
}
