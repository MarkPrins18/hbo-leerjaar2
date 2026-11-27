package nl.carsharing.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nl.carsharing.app.resources.*
import nl.carsharing.app.ui.components.*
import nl.carsharing.app.ui.icons.*
import nl.carsharing.app.ui.theme.*
import org.jetbrains.compose.resources.stringResource

data class RideHistoryItem(
    val id: Int,
    val carName: String,
    val licensePlate: String,
    val date: String,
    val timeRange: String,
    val duration: String,
    val distance: String,
    val averageSpeed: String,
    val averageAcceleration: String,
    val averageDeceleration: String,
    val bonusPoints: Int
)

private val sampleTotalRides = 12
private val sampleTotalDistance = "412 km"

private val sampleRides = listOf(
    RideHistoryItem(1, "Toyota Yaris", "AB-123-C", "wo 18 nov 2026", "09:10 – 10:02", "52 min", "31,4 km", "36 km/u", "1,2 m/s²", "1,6 m/s²", 9),
    RideHistoryItem(2, "Tesla Model 3", "XY-456-Z", "do 12 nov 2026", "13:30 – 16:45", "3 u 15 min", "88,0 km", "27 km/u", "1,5 m/s²", "1,8 m/s²", 20),
    RideHistoryItem(3, "Hyundai Nexo", "GH-789-K", "do 5 nov 2026", "08:15 – 08:50", "35 min", "12,7 km", "22 km/u", "2,1 m/s²", "2,4 m/s²", 4),
    RideHistoryItem(4, "Toyota Yaris", "AB-123-C", "ma 2 nov 2026", "17:05 – 17:48", "43 min", "24,9 km", "35 km/u", "1,3 m/s²", "1,7 m/s²", 7)
)

@Composable
fun RideHistoryScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { },
    onTabClick: (NavigationTab) -> Unit = { }
) {
    var selectedRideId by remember { mutableStateOf<Int?>(null) }

    RideHistoryContent(
        totalRides = sampleTotalRides,
        totalDistance = sampleTotalDistance,
        rides = sampleRides,
        onRideClick = { selectedRideId = it },
        onBack = onBack,
        onTabClick = onTabClick,
        modifier = modifier
    )

    sampleRides.find { it.id == selectedRideId }?.let { ride ->
        RideDetailsDialog(ride, onDismiss = { selectedRideId = null })
    }
}

@Composable
fun RideHistoryContent(
    totalRides: Int,
    totalDistance: String,
    rides: List<RideHistoryItem>,
    onRideClick: (Int) -> Unit,
    onBack: () -> Unit,
    onTabClick: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenScaffold(stringResource(Res.string.account_ride_history), NavigationTab.Account, onTabClick, modifier, onBack) { bodyModifier ->
        LazyColumn(
            contentPadding = ScreenContentPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = bodyModifier.fillMaxSize()
        ) {
            item {
                StatRow(
                    firstLabel = stringResource(Res.string.ride_history_rides),
                    firstValue = totalRides.toString(),
                    secondLabel = stringResource(Res.string.ride_history_total_distance),
                    secondValue = totalDistance
                )
            }
            item { SectionHeader(stringResource(Res.string.account_completed_rides), Modifier.padding(top = 8.dp)) }
            if (rides.isEmpty()) {
                item { MutedText(stringResource(Res.string.ride_history_empty_title)) }
            }
            items(rides, key = { ride -> ride.id }) { ride ->
                RideRow(ride, onClick = { onRideClick(ride.id) })
            }
        }
    }
}

@Composable
private fun RideRow(ride: RideHistoryItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    PhotoListRow(onClick = onClick, modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = ride.carName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Chip(stringResource(Res.string.ride_history_points, ride.bonusPoints), emphasized = true)
        }
        IconText(AppIcons.DateRange, ride.date)
        IconText(AppIcons.Schedule, ride.timeRange)
        IconText(AppIcons.LocationOn, "${ride.distance}  ·  ${ride.duration}", muted = true)
    }
}

@Composable
private fun IconText(icon: ImageVector, text: String, modifier: Modifier = Modifier, muted: Boolean = false) {
    val color = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

@Composable
private fun RideDetailsDialog(ride: RideHistoryItem, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    FormDialog(
        title = stringResource(Res.string.ride_details_title),
        onDismiss = onDismiss,
        confirmButton = { SecondaryButton(stringResource(Res.string.common_close), onDismiss) },
        modifier = modifier
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(ride.carName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            MutedText(ride.licensePlate, Modifier.weight(1f))
            Chip(stringResource(Res.string.ride_status_completed), emphasized = true)
        }
        HorizontalDivider(thickness = BorderThin, color = MaterialTheme.colorScheme.outlineVariant)
        DetailRow(stringResource(Res.string.ride_details_date), ride.date)
        DetailRow(stringResource(Res.string.ride_details_time), ride.timeRange)
        DetailRow(stringResource(Res.string.ride_details_duration), ride.duration)
        DetailRow(stringResource(Res.string.ride_details_distance), ride.distance)
        DetailRow(stringResource(Res.string.ride_details_avg_speed), ride.averageSpeed)
        DetailRow(stringResource(Res.string.ride_details_avg_acceleration), ride.averageAcceleration)
        DetailRow(stringResource(Res.string.ride_details_avg_deceleration), ride.averageDeceleration)
        DetailRow(stringResource(Res.string.account_bonus_points), "+${ride.bonusPoints}", bold = true)
    }
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun RideHistoryScreenPreview() {
    CarSharingTheme {
        RideHistoryScreen()
    }
}

@Preview
@Composable
private fun RideDetailsDialogPreview() {
    CarSharingTheme {
        RideDetailsDialog(sampleRides.first(), onDismiss = { })
    }
}
