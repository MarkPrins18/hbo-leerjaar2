package nl.carsharing.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.char
import nl.carsharing.app.resources.Res
import nl.carsharing.app.resources.date_time_placeholder
import nl.carsharing.app.resources.dialog_cancel
import nl.carsharing.app.resources.dialog_ok
import nl.carsharing.app.resources.select_date_time
import nl.carsharing.app.ui.icons.AppIcons
import nl.carsharing.app.ui.jsonSaver
import nl.carsharing.app.ui.icons.DateRange
import nl.carsharing.app.ui.theme.CarSharingTheme
import org.jetbrains.compose.resources.stringResource

private const val MILLIS_PER_DAY = 86_400_000L

val DateTimeFormat = LocalDateTime.Format {
    day()
    char('-')
    monthNumber()
    char('-')
    year()
    char(' ')
    hour()
    char(':')
    minute()
}

/**
 * Read-only text field that opens a date picker followed by a time picker when tapped.
 */
@Composable
fun DateTimeField(
    value: LocalDateTime?,
    onValueChange: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var pickedDate by rememberSaveable(stateSaver = jsonSaver<LocalDate?>()) { mutableStateOf(null) }

    Box(modifier) {
        CompactTextField(
            value = value?.let { DateTimeFormat.format(it) } ?: "",
            onValueChange = { },
            readOnly = true,
            placeholder = stringResource(Res.string.date_time_placeholder),
            trailingIcon = {
                Icon(
                    imageVector = AppIcons.DateRange,
                    contentDescription = stringResource(Res.string.select_date_time),
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        Box(Modifier.matchParentSize().clickable { showDatePicker = true })
    }

    if (showDatePicker) {
        DatePickerPopup(
            initial = value?.date,
            onConfirm = { date ->
                showDatePicker = false
                pickedDate = date
            },
            onDismiss = { showDatePicker = false }
        )
    }

    pickedDate?.let { date ->
        TimePickerPopup(
            initial = value?.time,
            onConfirm = { time ->
                pickedDate = null
                onValueChange(LocalDateTime(date, time))
            },
            onDismiss = { pickedDate = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerPopup(
    initial: LocalDate?,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial?.let { it.toEpochDays() * MILLIS_PER_DAY }
    )
    val colors = MaterialTheme.colorScheme
    val pickerColors = DatePickerDefaults.colors(
        containerColor = colors.surface,
        selectedDayContainerColor = colors.primaryContainer,
        selectedDayContentColor = colors.onPrimaryContainer,
        selectedYearContainerColor = colors.primaryContainer,
        selectedYearContentColor = colors.onPrimaryContainer,
        todayDateBorderColor = colors.outline,
        todayContentColor = colors.onSurface
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = pickerColors,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let {
                        onConfirm(LocalDate.fromEpochDays((it / MILLIS_PER_DAY).toInt()))
                    }
                },
                enabled = state.selectedDateMillis != null
            ) { Text(stringResource(Res.string.dialog_ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.dialog_cancel)) }
        }
    ) {
        DatePicker(state = state, colors = pickerColors)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerPopup(
    initial: LocalTime?,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initial?.hour ?: 10,
        initialMinute = initial?.minute ?: 0,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime(state.hour, state.minute)) }) {
                Text(stringResource(Res.string.dialog_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.dialog_cancel)) }
        },
        text = {
            TimePicker(
                state = state,
                colors = TimePickerDefaults.colors(
                    clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
                    timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun DateTimeFieldPreview() {
    CarSharingTheme {
        var value by remember { mutableStateOf<LocalDateTime?>(LocalDateTime(2026, 11, 20, 10, 0)) }
        DateTimeField(value = value, onValueChange = { value = it })
    }
}
