package nl.carsharing.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import nl.carsharing.app.resources.Res
import nl.carsharing.app.resources.home_car_photo
import nl.carsharing.app.ui.icons.AppIcons
import nl.carsharing.app.ui.icons.Image
import nl.carsharing.app.ui.theme.BorderRegular
import nl.carsharing.app.ui.theme.LocalAppColors
import org.jetbrains.compose.resources.stringResource

/**
 * One car (or other item) as a framed block, like `$card_photo_begin` in the wireframes: a photo
 * placeholder on the left and [content] as the right-hand column. Clickable when [onClick] is given.
 */
@Composable
fun PhotoListRow(
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(LocalAppColors.current.blockFill, MaterialTheme.shapes.extraSmall)
            .then(
                if (LocalAppColors.current.blockBorders) {
                    Modifier.border(BorderRegular, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.extraSmall)
                } else Modifier
            )
            .clip(MaterialTheme.shapes.extraSmall)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(66.dp)
                .background(LocalAppColors.current.photoPlaceholder, MaterialTheme.shapes.extraSmall)
        ) {
            Icon(
                imageVector = AppIcons.Image,
                contentDescription = stringResource(Res.string.home_car_photo),
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), content = content)
    }
}
