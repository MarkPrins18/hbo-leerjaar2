package nl.carsharing.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nl.carsharing.app.resources.*
import nl.carsharing.app.ui.components.*
import nl.carsharing.app.ui.icons.*
import nl.carsharing.app.ui.theme.*
import org.jetbrains.compose.resources.stringResource

data class AccountSummary(
    val email: String,
    val bonusPoints: Int,
    val completedRides: Int,
    val carCount: Int
)

private enum class AccountDialog { Options, ChangeEmail, ChangePassword, DeleteAccount, ReportProblem, Language }

private val sampleAccount = AccountSummary(
    email = "jan@voorbeeld.nl",
    bonusPoints = 128,
    completedRides = 12,
    carCount = 2
)

@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    darkMode: Boolean = false,
    onDarkModeChange: (Boolean) -> Unit = { },
    onMyCarsClick: () -> Unit = { },
    onRideHistoryClick: () -> Unit = { },
    onLogout: () -> Unit = { },
    onTabClick: (NavigationTab) -> Unit = { }
) {
    var dialog by remember { mutableStateOf<AccountDialog?>(null) }
    var language by remember { mutableStateOf(AppLanguage.Dutch) }
    val account = sampleAccount

    AccountContent(
        account = account,
        darkMode = darkMode,
        language = language,
        onEditAccountClick = { dialog = AccountDialog.Options },
        onDarkModeChange = onDarkModeChange,
        onLanguageClick = { dialog = AccountDialog.Language },
        onMyCarsClick = onMyCarsClick,
        onRideHistoryClick = onRideHistoryClick,
        onLogout = onLogout,
        onReportProblemClick = { dialog = AccountDialog.ReportProblem },
        onTabClick = onTabClick,
        modifier = modifier
    )

    when (dialog) {
        AccountDialog.Options -> AccountOptionsDialog(
            email = account.email,
            onChangeEmailClick = { dialog = AccountDialog.ChangeEmail },
            onChangePasswordClick = { dialog = AccountDialog.ChangePassword },
            onDeleteAccountClick = { dialog = AccountDialog.DeleteAccount },
            onDismiss = { dialog = null }
        )
        AccountDialog.ChangeEmail -> ChangeEmailDialog(
            currentEmail = account.email,
            onConfirm = { _, _ -> dialog = AccountDialog.Options },
            onDismiss = { dialog = AccountDialog.Options }
        )
        AccountDialog.ChangePassword -> ChangePasswordDialog(
            onConfirm = { _, _ -> dialog = AccountDialog.Options },
            onDismiss = { dialog = AccountDialog.Options }
        )
        AccountDialog.DeleteAccount -> DeleteAccountDialog(
            onConfirm = {
                dialog = null
                onLogout()
            },
            onDismiss = { dialog = AccountDialog.Options }
        )
        AccountDialog.ReportProblem -> ReportProblemDialog(
            onConfirm = { dialog = null },
            onDismiss = { dialog = null }
        )
        AccountDialog.Language -> LanguageDialog(
            selectedLanguage = language,
            onConfirm = {
                language = it
                dialog = null
            },
            onDismiss = { dialog = null }
        )
        null -> { }
    }
}

@Composable
fun AccountContent(
    account: AccountSummary,
    darkMode: Boolean,
    language: AppLanguage,
    onEditAccountClick: () -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onLanguageClick: () -> Unit,
    onMyCarsClick: () -> Unit,
    onRideHistoryClick: () -> Unit,
    onLogout: () -> Unit,
    onReportProblemClick: () -> Unit,
    onTabClick: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenScaffold(stringResource(Res.string.account_title), NavigationTab.Account, onTabClick, modifier) { bodyModifier ->
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = bodyModifier
                .verticalScroll(rememberScrollState())
                .padding(ScreenContentPadding)
        ) {
            ProfileRow(account.email, onEditAccountClick)

            SectionHeader(stringResource(Res.string.account_section_appearance), Modifier.padding(top = 8.dp))
            ToggleRow(AppIcons.DarkMode, stringResource(Res.string.account_dark_mode), darkMode, onDarkModeChange)

            SectionHeader(stringResource(Res.string.account_language), Modifier.padding(top = 8.dp))
            MenuRow(
                icon = AppIcons.Language,
                label = stringResource(Res.string.account_language),
                onClick = onLanguageClick,
                value = stringResource(language.label)
            )

            SectionHeader(stringResource(Res.string.account_section_your_cars), Modifier.padding(top = 8.dp))
            MenuRow(AppIcons.Key, stringResource(Res.string.account_my_cars, account.carCount), onMyCarsClick)

            SectionHeader(stringResource(Res.string.ride_history_rides), Modifier.padding(top = 8.dp))
            MenuRow(AppIcons.Schedule, stringResource(Res.string.account_ride_history_count, account.completedRides), onRideHistoryClick)

            SectionHeader(stringResource(Res.string.account_bonus_points), Modifier.padding(top = 8.dp))
            StatRow(
                firstLabel = stringResource(Res.string.account_balance),
                firstValue = account.bonusPoints.toString(),
                secondLabel = stringResource(Res.string.account_completed_rides),
                secondValue = account.completedRides.toString(),
                emphasizeFirst = true
            )

            SecondaryButton(
                text = stringResource(Res.string.account_logout),
                onClick = onLogout,
                icon = AppIcons.Logout,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )
            MenuRow(
                icon = AppIcons.BugReport,
                label = stringResource(Res.string.account_report_problem),
                onClick = onReportProblemClick,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun ProfileRow(email: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .block(LocalAppColors.current.blockFill)
            .clip(MaterialTheme.shapes.extraSmall)
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .background(LocalAppColors.current.photoPlaceholder, MaterialTheme.shapes.extraSmall)
        ) {
            Icon(AppIcons.Person, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = email,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            MutedText(stringResource(Res.string.account_edit_details))
        }
        Icon(AppIcons.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun AccountScreenPreview() {
    CarSharingTheme {
        AccountScreen()
    }
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun AccountScreenDarkPreview() {
    CarSharingTheme(darkTheme = true) {
        AccountScreen(darkMode = true)
    }
}
