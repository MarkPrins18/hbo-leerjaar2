package nl.carsharing.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nl.carsharing.app.resources.*
import nl.carsharing.app.ui.components.*
import nl.carsharing.app.ui.icons.*
import nl.carsharing.app.ui.theme.*
import org.jetbrains.compose.resources.*

enum class AppLanguage(val label: StringResource) {
    Dutch(Res.string.language_dutch),
    English(Res.string.language_english)
}

enum class ProblemType(val label: StringResource) {
    Bug(Res.string.report_type_bug),
    Crash(Res.string.report_type_crash),
    Suggestion(Res.string.report_type_suggestion),
    Other(Res.string.report_type_other)
}

data class ProblemReport(
    val type: ProblemType,
    val summary: String,
    val steps: String
)

@Composable
fun AccountOptionsDialog(
    email: String,
    onChangeEmailClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    FormDialog(
        title = stringResource(Res.string.account_title),
        onDismiss = onDismiss,
        confirmButton = { SecondaryButton(stringResource(Res.string.common_close), onDismiss) },
        modifier = modifier
    ) {
        MutedText(email)
        HorizontalDivider(thickness = BorderThin, color = MaterialTheme.colorScheme.outlineVariant)
        DialogOptionRow(AppIcons.Mail, stringResource(Res.string.account_change_email), onChangeEmailClick)
        HorizontalDivider(thickness = BorderThin, color = MaterialTheme.colorScheme.outlineVariant)
        DialogOptionRow(AppIcons.Lock, stringResource(Res.string.account_change_password), onChangePasswordClick)
        HorizontalDivider(thickness = BorderThin, color = MaterialTheme.colorScheme.outlineVariant)
        DialogOptionRow(AppIcons.Delete, stringResource(Res.string.account_delete_account), onDeleteAccountClick)
    }
}

@Composable
fun ChangeEmailDialog(
    currentEmail: String,
    onConfirm: (newEmail: String, password: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newEmail by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val emailInvalid = newEmail.isNotBlank() && !isValidEmail(newEmail)

    FormDialog(
        title = stringResource(Res.string.account_change_email),
        onDismiss = onDismiss,
        confirmButton = {
            PrimaryButton(stringResource(Res.string.common_save), onClick = {
                if (isValidEmail(newEmail) && password.isNotEmpty()) onConfirm(newEmail.trim(), password)
            })
        },
        dismissButton = { SecondaryButton(stringResource(Res.string.common_cancel), onDismiss) },
        modifier = modifier
    ) {
        MutedText(stringResource(Res.string.account_current_email, currentEmail))
        HorizontalDivider(thickness = BorderThin, color = MaterialTheme.colorScheme.outlineVariant)
        Text(stringResource(Res.string.account_new_email), style = MaterialTheme.typography.bodyMedium)
        CompactTextField(
            value = newEmail,
            onValueChange = { newEmail = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        if (emailInvalid) {
            Text(
                text = stringResource(Res.string.account_email_invalid),
                style = MaterialTheme.typography.bodySmall,
                color = LocalAppColors.current.errorText
            )
        }
        Text(stringResource(Res.string.account_confirm_password), style = MaterialTheme.typography.bodyMedium)
        PasswordField(password, { password = it })
        Text(
            text = stringResource(Res.string.account_email_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ChangePasswordDialog(
    onConfirm: (currentPassword: String, newPassword: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var repeatedPassword by rememberSaveable { mutableStateOf("") }

    FormDialog(
        title = stringResource(Res.string.account_change_password),
        onDismiss = onDismiss,
        confirmButton = {
            PrimaryButton(stringResource(Res.string.common_save), onClick = {
                if (currentPassword.isNotEmpty() && isValidPassword(newPassword) && newPassword == repeatedPassword) {
                    onConfirm(currentPassword, newPassword)
                }
            })
        },
        dismissButton = { SecondaryButton(stringResource(Res.string.common_cancel), onDismiss) },
        modifier = modifier
    ) {
        Text(stringResource(Res.string.account_current_password), style = MaterialTheme.typography.bodyMedium)
        PasswordField(currentPassword, { currentPassword = it })
        Text(stringResource(Res.string.account_new_password), style = MaterialTheme.typography.bodyMedium)
        PasswordField(newPassword, { newPassword = it })
        Text(stringResource(Res.string.account_repeat_password), style = MaterialTheme.typography.bodyMedium)
        PasswordField(repeatedPassword, { repeatedPassword = it })
        Spacer(Modifier.height(4.dp))
        PasswordChecklist(password = newPassword, repeatedPassword = repeatedPassword)
    }
}

@Composable
fun DeleteAccountDialog(
    onConfirm: (password: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var password by rememberSaveable { mutableStateOf("") }

    FormDialog(
        title = stringResource(Res.string.account_delete_account),
        onDismiss = onDismiss,
        confirmButton = {
            PrimaryButton(stringResource(Res.string.account_delete), onClick = {
                if (password.isNotEmpty()) onConfirm(password)
            })
        },
        dismissButton = { SecondaryButton(stringResource(Res.string.common_cancel), onDismiss) },
        modifier = modifier
    ) {
        Text(stringResource(Res.string.account_delete_warning), style = MaterialTheme.typography.bodyMedium)
        MutedText(stringResource(Res.string.account_delete_restriction))
        Spacer(Modifier.height(4.dp))
        Text(stringResource(Res.string.account_confirm_password), style = MaterialTheme.typography.bodyMedium)
        PasswordField(password, { password = it })
    }
}

@Composable
fun ReportProblemDialog(
    onConfirm: (ProblemReport) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var type by rememberSaveable { mutableStateOf(ProblemType.Bug) }
    var summary by rememberSaveable { mutableStateOf("") }
    var steps by rememberSaveable { mutableStateOf("") }

    FormDialog(
        title = stringResource(Res.string.account_report_problem),
        onDismiss = onDismiss,
        confirmButton = {
            PrimaryButton(stringResource(Res.string.report_send), onClick = {
                if (summary.isNotBlank()) onConfirm(ProblemReport(type, summary.trim(), steps.trim()))
            })
        },
        dismissButton = { SecondaryButton(stringResource(Res.string.common_cancel), onDismiss) },
        modifier = modifier
    ) {
        MutedText(stringResource(Res.string.report_intro))
        HorizontalDivider(thickness = BorderThin, color = MaterialTheme.colorScheme.outlineVariant)
        Text(stringResource(Res.string.report_type), style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ProblemType.entries.forEach { option ->
                Chip(
                    text = stringResource(option.label),
                    emphasized = option == type,
                    modifier = Modifier.clickable { type = option }
                )
            }
        }
        Text(stringResource(Res.string.report_summary), style = MaterialTheme.typography.bodyMedium)
        CompactTextField(value = summary, onValueChange = { summary = it }, modifier = Modifier.fillMaxWidth())
        Text(stringResource(Res.string.report_steps), style = MaterialTheme.typography.bodyMedium)
        CompactTextField(value = steps, onValueChange = { steps = it }, minLines = 3, modifier = Modifier.fillMaxWidth())
        Text(
            text = stringResource(Res.string.report_device_info),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LanguageDialog(
    selectedLanguage: AppLanguage,
    onConfirm: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var language by rememberSaveable { mutableStateOf(selectedLanguage) }

    FormDialog(
        title = stringResource(Res.string.account_language),
        onDismiss = onDismiss,
        confirmButton = { PrimaryButton(stringResource(Res.string.common_save), onClick = { onConfirm(language) }) },
        dismissButton = { SecondaryButton(stringResource(Res.string.common_cancel), onDismiss) },
        modifier = modifier
    ) {
        MutedText(stringResource(Res.string.language_hint))
        Column(Modifier.selectableGroup()) {
            AppLanguage.entries.forEachIndexed { index, option ->
                if (index > 0) HorizontalDivider(thickness = BorderThin, color = MaterialTheme.colorScheme.outlineVariant)
                RadioOptionRow(
                    label = stringResource(option.label),
                    selected = option == language,
                    onClick = { language = option }
                )
            }
        }
    }
}

@Composable
private fun DialogOptionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Icon(
            imageVector = AppIcons.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun RadioOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview
@Composable
private fun AccountOptionsDialogPreview() {
    CarSharingTheme {
        AccountOptionsDialog("jan@voorbeeld.nl", { }, { }, { }, { })
    }
}

@Preview
@Composable
private fun ChangeEmailDialogPreview() {
    CarSharingTheme {
        ChangeEmailDialog("jan@voorbeeld.nl", { _, _ -> }, { })
    }
}

@Preview
@Composable
private fun ChangePasswordDialogPreview() {
    CarSharingTheme {
        ChangePasswordDialog({ _, _ -> }, { })
    }
}

@Preview
@Composable
private fun DeleteAccountDialogPreview() {
    CarSharingTheme {
        DeleteAccountDialog({ }, { })
    }
}

@Preview
@Composable
private fun ReportProblemDialogPreview() {
    CarSharingTheme {
        ReportProblemDialog({ }, { })
    }
}

@Preview
@Composable
private fun LanguageDialogPreview() {
    CarSharingTheme {
        LanguageDialog(AppLanguage.Dutch, { }, { })
    }
}
