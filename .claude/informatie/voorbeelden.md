# Voorbeeldcode docent

Voorbeelden uit de demo-repo's van de docent (pjfmast). Werkwijze:
- Eerst `slides.md` en de voorbeelden hieronder bekijken.
- Staat het er niet in: zoek het op in een van de repo's (index hieronder), en zet het gebruikte voorbeeld onder **Voorbeelden** met bron (pad + commit).
- Alleen opnemen wat we echt gebruiken; niet de hele repo kopiëren.

## Repo's

### incident-list-demo-kmp
https://github.com/pjfmast/incident-list-demo-kmp — bekeken @ `19a957d` (2026-11-17)

KMP-app (Android, desktop/JVM, web/Wasm) met Kotlin Toolchain (`project.yaml`, `module.yaml`, `./kotlin`). Eén scherm met een `LazyColumn` van incidenten; hoort bij les 9.

| Bestand | Wat zie je er |
|---|---|
| `shared/module.yaml` | `kmp/lib` voor `[jvm, android, wasmJs]`; Compose-dependencies, lifecycle-viewmodel-compose, kotlinx-datetime, compose resources (`Res.*`) |
| `shared/src/App.kt` | `@Preview` + `@DevelopmentEntryPoint` (hot reload), theme + `Scaffold` met `innerPadding` |
| `shared/src/Incident.kt` | data class + enum met properties (`Priority(label, deadlineDays)`) |
| `shared/src/IncidentsViewModel.kt` | `ViewModel` met `StateFlow<List<…>>` via explicit backing field, `update { }` voor verwijderen/wijzigen (`filter`, `map` + `copy`) |
| `shared/src/IncidentListScreen.kt` | `viewModel { IncidentsViewModel() }`, `collectAsState()`, `LazyColumn` met `key`, `Card`, expand/collapse, `animateFloatAsState`/`animateColorAsState`, `DropdownMenu`, `AlertDialog`, gradient-header, `LocalDateTime` formatteren zonder `java.time` |
| `shared/src/IncidentScreen.kt` | detailscherm met state hoisting (state omlaag, events omhoog), `remember(priority) { }` voor afgeleide waarde, `SingleChoiceSegmentedButtonRow`, `TextField` zonder indicator, previews met lokale state |
| `shared/src/ui/theme/*` | Material 3 theme (Color, Theme, Type) |
| `shared/test/LocalDateTimeFormatTest.kt` | `kotlin.test` unit tests in common code |
| `androidApp/src/MainActivity.kt` | `ComponentActivity` + `enableEdgeToEdge()` + `setContent { App() }` |

### incident-details-demo-kmp
https://github.com/pjfmast/incident-details-demo-kmp — bekeken @ `25e6412` (2026-11-17)

Zelfde opzet; één detailscherm. Demonstreert `expect`/`actual` (les 9) en KMP-datumformattering.

| Bestand | Wat zie je er |
|---|---|
| `shared/src/App.kt` | `IncidentScreen` met `onShare`-callback, `LocalDateTime.Format { day(); char('-'); monthNumber() … }` (KMP-alternatief voor `String.format`), `buildString { appendLine(…) }` |
| `shared/src/IncidentSharer.kt` | `expect class IncidentSharer` + `@Composable expect fun rememberIncidentSharer()` |
| `shared/src@android/…` | `actual` met `Intent.ACTION_SEND` + `LocalContext.current` |
| `shared/src@jvm/…` | `actual` met AWT-clipboard |
| `shared/src@wasmJs/…` | `actual` met `@JsFun` naar `navigator.clipboard` |
| `shared/test/`, `test@android/`, `test@jvm/`, `test@wasmJs/` | common tests en platform-specifieke tests |

Platform-specifieke code staat in `src@<platform>`-mappen (Kotlin Toolchain-conventie).

## Voorbeelden

### Compose resources (meertaligheid)
Bron: `incident-list-demo-kmp/shared/module.yaml` @ 19a957d (de demo zet het aan, maar gebruikt nog geen `Res.*`)
```yaml
dependencies:
  - $compose.components.resources
settings:
  compose:
    enabled: true
    resources:
      packageName: nl.carsharing.app.resources
```
Teksten in `shared/composeResources/values/strings.xml`, vertalingen in `values-<taal>/strings.xml`. Gebruik: `stringResource(Res.string.x)` met imports `<packageName>.Res`, `<packageName>.x` en `org.jetbrains.compose.resources.stringResource`.

<!-- Format:
### <onderwerp>
Bron: `<repo>/<pad>` @ <commit>
```kotlin
...
```
-->
