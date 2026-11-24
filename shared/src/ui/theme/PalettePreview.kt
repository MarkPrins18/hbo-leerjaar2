package nl.carsharing.app.ui.theme

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nl.carsharing.app.ui.screens.CarCostScreen
import nl.carsharing.app.ui.screens.CarDetailScreen
import nl.carsharing.app.ui.screens.CarFormPreviewSingle
import nl.carsharing.app.ui.screens.HomeScreen
import nl.carsharing.app.ui.screens.MyCarsPreviewSingle

// Experiment only: per palette one preview with a light and a dark row of all screens; not used by the app.

private const val PhoneWidth = 360
private const val PhoneHeight = 800

// All screens of the app, one phone each; every lambda gets the phone modifier (size + border).
private val AllPhones: List<@Composable (Modifier) -> Unit> = listOf(
    { HomeScreen(modifier = it) },
    { CarDetailScreen(modifier = it) },
    { MyCarsPreviewSingle(sheetOpen = false) },
    { MyCarsPreviewSingle(sheetOpen = true) },
    { CarFormPreviewSingle(details = false) },
    { CarFormPreviewSingle(details = true) },
    { CarCostScreen(modifier = it) }
)

// Size: 7 phones x 360 + 6 gaps x 24 + 2 x 12 padding = 2688 wide; label 12 + 28 + 12 + 2 rows x (800 + 2 x 12) = 1700 high.
// Also used by TealStylingPreview.kt.
@Composable
internal fun PaletteRows(palette: Palette) {
    Column {
        CarSharingTheme(palette = palette, darkTheme = false) {
            // Preview-only label, so hardcoded instead of a string resource.
            Text(
                text = palette.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(12.dp)
            )
        }
        LightDarkRows(palette, AllPhones)
    }
}

/**
 * The given phones in [palette]: a light row on top and a dark row below; also used by the color_ screen previews.
 * Size: n phones x 360 + (n - 1) gaps x 24 + 2 x 12 padding wide; 2 rows x (800 + 2 x 12) = 1648 high.
 */
@Composable
internal fun LightDarkRows(palette: Palette, phones: List<@Composable (Modifier) -> Unit>) {
    Column {
        PhoneRow(palette, darkTheme = false, phones = phones)
        PhoneRow(palette, darkTheme = true, phones = phones)
    }
}

@Composable
private fun PhoneRow(palette: Palette, darkTheme: Boolean, phones: List<@Composable (Modifier) -> Unit>) {
    CarSharingTheme(palette = palette, darkTheme = darkTheme) {
        // Surface gives the screens the same content colour as the app's Scaffold, so dark text stays readable.
        Surface(color = MaterialTheme.colorScheme.background) {
            val phone = Modifier
                .width(PhoneWidth.dp)
                .height(PhoneHeight.dp)
                .border(BorderRegular, MaterialTheme.colorScheme.outline)
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.padding(12.dp)) {
                phones.forEach { Box(Modifier.width(PhoneWidth.dp).height(PhoneHeight.dp)) { it(phone) } }
            }
        }
    }
}

@Preview(widthDp = 2700, heightDp = 1700)
@Composable
private fun TealPalettePreview() = PaletteRows(TealPalette)

@Preview(widthDp = 2700, heightDp = 1700)
@Composable
private fun TealBoldPalettePreview() = PaletteRows(TealBoldPalette)

@Preview(widthDp = 2700, heightDp = 1700)
@Composable
private fun TealPeachPalettePreview() = PaletteRows(TealPeachPalette)

@Preview(widthDp = 2700, heightDp = 1700)
@Composable
private fun TealPeachHeaderPalettePreview() = PaletteRows(TealPeachHeaderPalette)

@Preview(widthDp = 2700, heightDp = 1700)
@Composable
private fun IndigoCoralPalettePreview() = PaletteRows(IndigoCoralPalette)

@Preview(widthDp = 2700, heightDp = 1700)
@Composable
private fun IndigoCoralNavPalettePreview() = PaletteRows(IndigoCoralNavPalette)
