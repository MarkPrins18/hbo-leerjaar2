package nl.carsharing.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import nl.carsharing.app.resources.*
import nl.carsharing.app.ui.components.*
import nl.carsharing.app.ui.icons.*
import nl.carsharing.app.ui.theme.*
import org.jetbrains.compose.resources.*

private const val PASSWORD_MIN_LENGTH = 8
private const val PASSWORD_MAX_BYTES = 72
private val EMAIL_PATTERN = Regex("""^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""")

internal enum class PasswordRequirement(val label: StringResource) {
    MinLength(Res.string.password_rule_length),
    Uppercase(Res.string.password_rule_uppercase),
    Lowercase(Res.string.password_rule_lowercase),
    Digit(Res.string.password_rule_digit),
    SpecialCharacter(Res.string.password_rule_special);

    fun isMetBy(password: String): Boolean = when (this) {
        MinLength -> password.length >= PASSWORD_MIN_LENGTH
        Uppercase -> password.any(Char::isUpperCase)
        Lowercase -> password.any(Char::isLowerCase)
        Digit -> password.any(Char::isDigit)
        SpecialCharacter -> password.any { !it.isLetterOrDigit() }
    }
}

internal fun isValidPassword(password: String): Boolean =
    PasswordRequirement.entries.all { it.isMetBy(password) } &&
        password.encodeToByteArray().size <= PASSWORD_MAX_BYTES

internal fun isValidEmail(email: String): Boolean = EMAIL_PATTERN.matches(email.trim())

@Composable
internal fun MenuRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    value: String? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .block(LocalAppColors.current.blockFill)
            .clip(MaterialTheme.shapes.extraSmall)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        if (value != null) {
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(4.dp))
        }
        Icon(
            imageVector = AppIcons.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
internal fun ToggleRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .block(LocalAppColors.current.blockFill)
            .clip(MaterialTheme.shapes.extraSmall)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
internal fun FormDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.verticalScroll(rememberScrollState()),
                content = content
            )
        },
        confirmButton = confirmButton,
        dismissButton = dismissButton
    )
}

@Composable
internal fun MutedText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
internal fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    CompactTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            Icon(
                imageVector = if (visible) AppIcons.VisibilityOff else AppIcons.Visibility,
                contentDescription = stringResource(
                    if (visible) Res.string.common_hide_password else Res.string.common_show_password
                ),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp).clickable { visible = !visible }
            )
        },
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
internal fun PasswordChecklist(
    password: String,
    repeatedPassword: String,
    modifier: Modifier = Modifier
) {
    val metCount = PasswordRequirement.entries.count { it.isMetBy(password) }
    val strength = when {
        metCount == PasswordRequirement.entries.size -> Res.string.password_strength_strong
        metCount >= 3 -> Res.string.password_strength_medium
        else -> Res.string.password_strength_weak
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.password_strength), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.width(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                PasswordRequirement.entries.forEachIndexed { index, _ ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(6.dp)
                            .background(
                                if (index < metCount) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                MaterialTheme.shapes.extraSmall
                            )
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(stringResource(strength), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        PasswordRequirement.entries.forEach { requirement ->
            RequirementRow(stringResource(requirement.label), met = requirement.isMetBy(password))
        }
        RequirementRow(
            text = stringResource(Res.string.password_rule_match),
            met = password.isNotEmpty() && password == repeatedPassword
        )
    }
}

@Composable
private fun RequirementRow(text: String, met: Boolean, modifier: Modifier = Modifier) {
    val color = if (met) LocalAppColors.current.successText else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (met) AppIcons.Check else AppIcons.Close,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(text, style = MaterialTheme.typography.bodySmall, color = color)
    }
}
