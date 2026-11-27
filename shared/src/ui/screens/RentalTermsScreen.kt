package nl.carsharing.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nl.carsharing.app.resources.*
import nl.carsharing.app.ui.components.*
import nl.carsharing.app.ui.theme.*
import org.jetbrains.compose.resources.*

private const val TIME_DIGITS = 4

data class RentalTermsForm(
    val pricePerDay: String = "",
    val pickupLocation: String = "",
    val pickupTimeStart: String = "",
    val pickupTimeEnd: String = "",
    val returnLocation: String = "",
    val returnTimeStart: String = "",
    val returnTimeEnd: String = ""
)

enum class TimeWindowError(val message: StringResource) {
    Format(Res.string.rental_terms_error_time_format),
    Incomplete(Res.string.rental_terms_error_time_pair),
    Order(Res.string.rental_terms_error_time_order)
}

fun RentalTermsForm.priceIsValid(): Boolean =
    (pricePerDay.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0.0

fun RentalTermsForm.pickupTimeError(): TimeWindowError? = timeWindowError(pickupTimeStart, pickupTimeEnd)

fun RentalTermsForm.returnTimeError(): TimeWindowError? = timeWindowError(returnTimeStart, returnTimeEnd)

fun RentalTermsForm.isValid(): Boolean =
    priceIsValid() &&
        pickupLocation.isNotBlank() &&
        pickupTimeError() == null &&
        returnTimeError() == null

private fun timeWindowError(start: String, end: String): TimeWindowError? = when {
    start.isEmpty() && end.isEmpty() -> null
    (start.isNotEmpty() && !isValidTime(start)) || (end.isNotEmpty() && !isValidTime(end)) -> TimeWindowError.Format
    start.isEmpty() || end.isEmpty() -> TimeWindowError.Incomplete
    start >= end -> TimeWindowError.Order
    else -> null
}

private fun isValidTime(digits: String): Boolean {
    if (digits.length != TIME_DIGITS || !digits.all(Char::isDigit)) return false
    return digits.take(2).toInt() in 0..23 && digits.takeLast(2).toInt() in 0..59
}

private val sampleRentalTermsForm = RentalTermsForm(
    pricePerDay = "45,00",
    pickupLocation = "Hoofdstraat 1, Breda",
    pickupTimeStart = "0800",
    pickupTimeEnd = "2000",
    returnLocation = "Hoofdstraat 1, Breda",
    returnTimeStart = "0800",
    returnTimeEnd = "2200"
)

@Composable
fun RentalTermsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { },
    onSaved: () -> Unit = { },
    onTabClick: (NavigationTab) -> Unit = { }
) {
    var form by remember { mutableStateOf(sampleRentalTermsForm) }

    RentalTermsContent(
        carName = "Toyota Yaris",
        licensePlate = "AB-123-C",
        forRent = true,
        form = form,
        onFormChange = { form = it },
        onSave = { if (form.isValid()) onSaved() },
        onBack = onBack,
        onTabClick = onTabClick,
        modifier = modifier
    )
}

@Composable
fun RentalTermsContent(
    carName: String,
    licensePlate: String,
    forRent: Boolean,
    form: RentalTermsForm,
    onFormChange: (RentalTermsForm) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onTabClick: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenScaffold(stringResource(Res.string.rental_terms_title), NavigationTab.Account, onTabClick, modifier, onBack) { bodyModifier ->
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = bodyModifier
                .verticalScroll(rememberScrollState())
                .padding(ScreenContentPadding)
        ) {
            PhotoListRow(onClick = null) {
                Text(carName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(licensePlate, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Chip(
                    text = stringResource(if (forRent) Res.string.rental_terms_for_rent else Res.string.rental_terms_not_for_rent),
                    emphasized = forRent
                )
            }

            SectionHeader(stringResource(Res.string.rental_terms_price), Modifier.padding(top = 8.dp))
            DetailCard(title = null) {
                TextFormRow(stringResource(Res.string.rental_terms_price_per_day), form.pricePerDay, number = true) { value ->
                    onFormChange(form.copy(pricePerDay = value.filter { it.isDigit() || it == ',' || it == '.' }))
                }
            }
            if (form.pricePerDay.isNotEmpty() && !form.priceIsValid()) {
                ErrorText(stringResource(Res.string.rental_terms_error_price))
            }

            SectionHeader(stringResource(Res.string.rental_terms_pickup), Modifier.padding(top = 8.dp))
            LocationCard(
                location = form.pickupLocation,
                start = form.pickupTimeStart,
                end = form.pickupTimeEnd,
                onLocationChange = { onFormChange(form.copy(pickupLocation = it)) },
                onStartChange = { onFormChange(form.copy(pickupTimeStart = it)) },
                onEndChange = { onFormChange(form.copy(pickupTimeEnd = it)) }
            )
            form.pickupTimeError()?.let { ErrorText(stringResource(it.message)) }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                SectionHeader(stringResource(Res.string.rental_terms_return))
                Chip(stringResource(Res.string.rental_terms_optional))
            }
            LocationCard(
                location = form.returnLocation,
                start = form.returnTimeStart,
                end = form.returnTimeEnd,
                onLocationChange = { onFormChange(form.copy(returnLocation = it)) },
                onStartChange = { onFormChange(form.copy(returnTimeStart = it)) },
                onEndChange = { onFormChange(form.copy(returnTimeEnd = it)) }
            )
            form.returnTimeError()?.let { ErrorText(stringResource(it.message)) }

            PrimaryButton(
                text = stringResource(Res.string.common_save),
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun LocationCard(
    location: String,
    start: String,
    end: String,
    onLocationChange: (String) -> Unit,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    DetailCard(title = null, modifier = modifier) {
        TextFormRow(stringResource(Res.string.rental_terms_location), location, onValueChange = onLocationChange)
        FormRow(stringResource(Res.string.rental_terms_from)) { fieldModifier ->
            TimeField(start, onStartChange, fieldModifier)
        }
        FormRow(stringResource(Res.string.rental_terms_until)) { fieldModifier ->
            TimeField(end, onEndChange, fieldModifier)
        }
    }
}

@Composable
private fun TimeField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    CompactTextField(
        value = value,
        onValueChange = { input -> onValueChange(input.filter(Char::isDigit).take(TIME_DIGITS)) },
        placeholder = stringResource(Res.string.rental_terms_time_placeholder),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = TimeVisualTransformation,
        modifier = modifier
    )
}

@Composable
private fun ErrorText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = LocalAppColors.current.errorText,
        modifier = modifier
    )
}

private object TimeVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = if (digits.length > 2) "${digits.take(2)}:${digits.drop(2)}" else digits
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = if (offset <= 2) offset else offset + 1

            override fun transformedToOriginal(offset: Int): Int =
                (if (offset <= 2) offset else offset - 1).coerceAtMost(digits.length)
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun RentalTermsScreenPreview() {
    CarSharingTheme {
        RentalTermsScreen()
    }
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun RentalTermsScreenErrorPreview() {
    CarSharingTheme {
        RentalTermsContent(
            carName = "Hyundai Nexo",
            licensePlate = "GH-789-K",
            forRent = false,
            form = RentalTermsForm(pricePerDay = "0", pickupTimeStart = "2000", pickupTimeEnd = "0800"),
            onFormChange = { },
            onSave = { },
            onBack = { },
            onTabClick = { }
        )
    }
}
