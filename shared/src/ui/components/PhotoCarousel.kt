package nl.carsharing.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.carsharing.app.resources.Res
import nl.carsharing.app.resources.detail_photo_next
import nl.carsharing.app.resources.detail_photo_page
import nl.carsharing.app.resources.detail_photo_previous
import nl.carsharing.app.resources.home_car_photo
import nl.carsharing.app.ui.icons.AppIcons
import nl.carsharing.app.ui.icons.Image
import nl.carsharing.app.ui.theme.BorderRegular
import nl.carsharing.app.ui.theme.LocalAppColors
import org.jetbrains.compose.resources.stringResource

/**
 * Photo carousel from the wireframes: arrows at the sides, page dots and "1/3" below the photo.
 * [compact] gives the lower variant used next to other content (S11). Inside another block
 * (e.g. a bottom sheet) use [framed] false: a light fill instead of a frame of its own.
 */
@Composable
fun PhotoCarousel(
    photoCount: Int,
    currentPhoto: Int,
    onPhotoChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    framed: Boolean = true
) {
    val shape = MaterialTheme.shapes.extraSmall

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (framed) Modifier
                    .background(LocalAppColors.current.blockFill, shape)
                    .then(
                        if (LocalAppColors.current.blockBorders) Modifier.border(BorderRegular, MaterialTheme.colorScheme.outline, shape)
                        else Modifier
                    )
                else Modifier.background(LocalAppColors.current.photoPlaceholder, shape)
            )
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(if (compact) 56.dp else 100.dp)) {
            CarouselArrow("‹", stringResource(Res.string.detail_photo_previous), enabled = currentPhoto > 0) {
                onPhotoChange(currentPhoto - 1)
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = AppIcons.Image,
                    contentDescription = stringResource(Res.string.home_car_photo),
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(if (compact) 32.dp else 56.dp)
                )
            }
            CarouselArrow("›", stringResource(Res.string.detail_photo_next), enabled = currentPhoto < photoCount - 1) {
                onPhotoChange(currentPhoto + 1)
            }
        }
        if (photoCount > 0) {
            Box(Modifier.fillMaxWidth()) {
                Text(
                    text = List(photoCount) { if (it == currentPhoto) "●" else "○" }.joinToString(" "),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
                Text(
                    text = stringResource(Res.string.detail_photo_page, currentPhoto + 1, photoCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}

@Composable
private fun CarouselArrow(
    symbol: String,
    description: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Text(
        text = symbol,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
        modifier = modifier
            .clickable(enabled = enabled, onClickLabel = description, onClick = onClick)
            .padding(horizontal = 4.dp)
    )
}
