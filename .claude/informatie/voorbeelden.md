# Voorbeeldcode docent

Voorbeelden uit de demo-repo's van de docent (pjfmast) en de andere repo's uit de slides. Werkwijze:
- Eerst de slides (`slides-p1.md` voor de server, `slides-p2.md` voor de app) en de voorbeelden hieronder bekijken.
- Staat het er niet in: zoek het op in een van de repo's (index hieronder), en zet het gebruikte voorbeeld onder **Voorbeelden** met bron (pad + commit).
- Alleen opnemen wat we echt gebruiken; niet de hele repo kopiëren.

## Overzicht

| Repo | Onderwerp | Les | Wanneer gebruiken |
|---|---|---|---|
| [pjfmast/incident-list-demo-kmp](#incident-list-demo-kmp) | Lijst met `LazyColumn`, ViewModel + `StateFlow`, Kotlin Toolchain-opzet | 9 | Lijstscherm, ViewModel zonder DI, `module.yaml` van een KMP-app |
| [pjfmast/incident-details-demo-kmp](#incident-details-demo-kmp) | `expect`/`actual`, datum formatteren zonder `java.time` | 9 | Platformspecifieke code, `LocalDateTime.Format` |
| [pjfmast/Unscramble](#unscramble) | ViewModel + `UiState` + `StateFlow` (Android/Gradle) | 10 | Basispatroon ViewModel/UiState, ViewModel-unit-test |
| [pjfmast/unscramble-ktc](#unscramble-ktc) | Unscramble gemigreerd naar KMP + Kotlin Toolchain | 10 | Zelfde patroon in KMP: explicit backing field, `viewModel { }`, `%1$d`-strings |
| [pjfmast/HouseHelper-KTC](#househelper-ktc) | **Navigation 3** + **Koin** in KMP (KotlinConf-workshop) | 10 | Navigatie tussen schermen, argumenten, dialog-scene, ViewModel per scherm via Koin |
| [pjfmast/ktor-android-incident-demo](#ktor-android-incident-demo) | Full-stack: Ktor-server + Android-app met Ktor Client, JWT, Koin, Nav3 | – (niet in slides) | App ↔ eigen API koppelen (Ktor Client, token opslaan), Koin-DSL; server-opzet ter vergelijking |
| [android/nav3-recipes](#android-nav3-recipes) | Officiële Navigation 3-recepten (Android) | 10 | Eén specifiek navigatiepatroon opzoeken (dialog, bottom sheet, tabs, …) |
| [terrakok/nav3-recipes](#terrakok-nav3-recipes) | Dezelfde recepten in Compose Multiplatform | 10 | Als bij android/nav3-recipes, maar code die ook op iOS werkt |
| [google-developer-training/…-tip-calculator](#tip-calculator) | Codelab Tip Time: state hoisting, `TextField` | 8 | Referentie voor het codelab; let op `java.text` |
| [JetBrains/compose-multiplatform-core](#compose-multiplatform-core) | Broncode van de CMP-fork | 9 | Alleen achtergrond, niet voor voorbeeldcode |

## Repo's

### incident-list-demo-kmp
https://github.com/pjfmast/incident-list-demo-kmp — bekeken @ `19a957d` (2026-11-17), gecontroleerd 2026-11-24 (ongewijzigd)

**Waarvoor:** voorbeeld van een complete kleine KMP-app met de Kotlin Toolchain en één lijstscherm (les 9). Raadplegen als je een lijst toont met `LazyColumn`, een ViewModel zonder DI-library wilt (`viewModel { IncidentsViewModel() }`) of wilt zien hoe `module.yaml` voor Compose in elkaar zit. Niet in de slides van les 8–10 gelinkt, maar wel de les 9-demo.

KMP-app (Android, desktop/JVM, web/Wasm) met Kotlin Toolchain (`project.yaml`, `module.yaml`, `./kotlin`).

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
https://github.com/pjfmast/incident-details-demo-kmp — bekeken @ `25e6412` (2026-11-17), gecontroleerd 2026-11-24 (ongewijzigd)

**Waarvoor:** de `expect`/`actual`-demo uit les 9. `expect`/`actual` = in common code een functie/klasse *declareren* (`expect`) en per platform de *implementatie* leveren (`actual`); de compiler eist er één per target. Raadplegen als je iets platformspecifieks nodig hebt (delen, klembord) of een datum wilt formatteren zonder `java.time`/`String.format`.

Zelfde opzet als incident-list-demo-kmp; één detailscherm.

| Bestand | Wat zie je er |
|---|---|
| `shared/src/App.kt` | `IncidentScreen` met `onShare`-callback, `LocalDateTime.Format { day(); char('-'); monthNumber() … }` (KMP-alternatief voor `String.format`), `buildString { appendLine(…) }` |
| `shared/src/IncidentSharer.kt` | `expect class IncidentSharer` + `@Composable expect fun rememberIncidentSharer()` |
| `shared/src@android/…` | `actual` met `Intent.ACTION_SEND` + `LocalContext.current` |
| `shared/src@jvm/…` | `actual` met AWT-clipboard |
| `shared/src@wasmJs/…` | `actual` met `@JsFun` naar `navigator.clipboard` |
| `shared/test/`, `test@android/`, `test@jvm/`, `test@wasmJs/` | common tests en platform-specifieke tests |

Platform-specifieke code staat in `src@<platform>`-mappen (Kotlin Toolchain-conventie).

### Unscramble
https://github.com/pjfmast/Unscramble — bekeken @ `064ed50` (2025-11-27)

**Waarvoor:** oplossing van het codelab *Unscramble* (Unit 4 pathway 1, Architecture components), gebruikt in les 10 voor "ViewModel exposes StateFlow". Een **UiState** is één data class met alles wat het scherm toont; de ViewModel houdt die in een private `MutableStateFlow` en geeft alleen een read-only `StateFlow` naar buiten. Raadplegen voor het basispatroon ViewModel ↔ scherm en een unit-test van een ViewModel. Let op: Android-only met Gradle (`java`-mappen, `R.string`); voor onze KMP-app is [unscramble-ktc](#unscramble-ktc) beter.

| Bestand | Wat zie je er |
|---|---|
| `app/src/main/java/com/example/unscramble/ui/GameViewModel.kt` | `private val _uiState = MutableStateFlow(GameUiState())` + `val uiState = _uiState.asStateFlow()`, `_uiState.update { it.copy(...) }`, `var userGuess by mutableStateOf(""); private set` |
| `app/src/main/java/com/example/unscramble/ui/GameUiState.kt` | `data class GameUiState(...)` met defaults |
| `app/src/main/java/com/example/unscramble/ui/GameScreen.kt` | `viewModel()` als default-parameter, `collectAsStateWithLifecycle()` |
| `app/src/test/java/com/example/unscramble/ui/test/GameViewModelTest.kt` | unit tests op de ViewModel |

### unscramble-ktc
https://github.com/pjfmast/unscramble-ktc — bekeken @ `fc7b79a` (2026-11-24)

**Waarvoor:** dezelfde Unscramble-app, door de docent gemigreerd naar KMP met de Kotlin Toolchain (hoort bij slide "Unscramble: Android → KMP/CMP" van les 10; de repo zelf staat niet in de slides). Raadplegen als je het ViewModel/UiState-patroon in onze opzet (KMP, `module.yaml`, `Res.string`) wilt zien, en voor string-placeholders in CMP (`%1$d`).

| Bestand | Wat zie je er |
|---|---|
| `shared/module.yaml` | `kmp/lib` voor `[jvm, android, iosArm64, iosSimulatorArm64, wasmJs]`, `lifecycle-viewmodel-compose`, `lifecycle-runtime-compose`, compose resources met `exposedAccessors: true` |
| `shared/src/ui/GameViewModel.kt` | `val uiState: StateFlow<GameUiState>` + `field = MutableStateFlow(GameUiState())` (explicit backing field) |
| `shared/src/ui/GameScreen.kt` | `gameViewModel: GameViewModel = viewModel { GameViewModel() }`, `collectAsState()`, `stringResource(Res.string.score, score)` |
| `shared/composeResources/values/strings.xml` | `<string name="score">Score: %1$d</string>` (positionele placeholder) |
| `shared/test/ui/test/GameViewModelTest.kt` | ViewModel-test in common code met `kotlin.test` |
| `shared/src@<platform>/Platform.*.kt` | `expect`/`actual` per platform (android, ios, jvm, wasmJs) |

### HouseHelper-KTC
https://github.com/pjfmast/HouseHelper-KTC — bekeken @ `4003e3c` (2026-11-24), branch `solutions` (default). Branch `starter` @ `224101f` = beginpunt met `// TODO Task N`.

**Waarvoor:** de **Navigation 3**-demo van les 10: de KotlinConf 2026 Compose-workshop (origineel kropp/HouseHelper), door de docent gemigreerd naar de Kotlin Toolchain en Koin. **Navigation 3 (nav3)** = de nieuwe navigatiebibliotheek van Jetpack/Compose Multiplatform: je beheert zelf een **back stack** (een observable lijst van route-keys, `NavBackStack`) en `NavDisplay` toont het scherm dat bij de bovenste key hoort; navigeren = `backStack.add(key)`, terug = `backStack.removeLastOrNull()`. **Koin** = dependency-injection-library voor Kotlin (Multiplatform): je beschrijft één keer hoe services en ViewModels gemaakt worden, en Koin levert ze aan (`koinViewModel()`); wij gebruiken Koin in de app (`shared`), niet op de server. Raadplegen voor navigatie tussen onze schermen, argumenten meegeven, een scherm als dialog tonen, een ViewModel per scherm en de Koin-opzet. In de slides heet de app "House Helper" (tasks 9 en 10); de repo-link staat er niet in.

| Bestand | Wat zie je er |
|---|---|
| `shared/module.yaml` + `libs.versions.toml` | nav3-dependencies (`org.jetbrains.androidx.navigation3:navigation3-ui`, `lifecycle-viewmodel-navigation3`), op Android de `androidx.navigation3`-artifacts; `kotlin: serialization: enabled` |
| `shared/src/…/navigation/Routes.kt` | `@Serializable sealed interface Route : NavKey` met `data object`/`data class`-routes + `SavedStateConfiguration` met `subclassesOfSealed<Route>()` |
| `shared/src/…/App.kt` | `rememberNavBackStack(config, start)`, `NavDisplay` + `entryProvider { entry<…> }`, `add`/`removeLastOrNull`/`clear`, `DialogSceneStrategy`, `entryDecorators`, deep links (`UriDeepLinkMatcher` + `withBackStack`) |
| `shared/src/…/devices/LightDetailsScreen.kt` | scherm krijgt `deviceId` + `onNavigateUp`; ViewModel via `koinViewModel { parametersOf(deviceId) }`; `TopAppBar` met terug-icoon; `collectAsStateWithLifecycle()` |
| `shared/src/…/devices/LightDetailsViewModel.kt` | ViewModel met argument (`deviceId`), `Flow.stateIn(viewModelScope, WhileSubscribed(5000), null)`, acties via `viewModelScope.launch` |
| `shared/src/…/devices/RenameDeviceScreen.kt` | dialog-inhoud in `Surface(color = surface, shape = shapes.extraLarge)` |
| `shared/src/…/data/HouseService.kt` + `DemoHouseService.kt` | service-interface met `Flow`- en `suspend`-functies + demo-implementatie |
| `shared/src/…/di/AppModule.kt` | Koin-setup met annotations (`@Module`, `@Configuration`, `@ComponentScan`, `@KoinApplication`) + `initApp()` met `startKoin` |
| `androidApp/…/MyApplication.kt`, `desktopApp/src/…/main.kt`, `webApp/src/…/main.kt`, `iosApp/src/ViewController.kt` | per platform `initApp()` aanroepen vóór `App()` (Android met `androidContext`) |
| `shared/test@jvm/…/navigation/DeepLinkTest.kt` | tests dat een deep link de juiste back stack oplevert |
| `documentation/navigation3_architectuur.md` | Nederlandse uitleg van de nav3-kernbegrippen + diagram (zelfde tekst als de sprekersnotities van les 10) |
| `documentation/migratie_en_verschillen.md` | wat er veranderd is t.o.v. de originele workshop (Gradle → KTC, Metro → Koin, versies) |
| `TASKS.md` | alle workshop-taken (1–18 + bonus) met oplossing |

Niet gebruiken: de Lunch Tray-app uit het nav3-codelab (les 10) is als KMP-versie (nog) niet publiek; `pjfmast/fsa-lunch-tray-nav3` (`45a8ad7`, 2025-11-25) is een oudere Android/Gradle-versie met een oudere nav3 en een eigen workaround voor `rememberNavBackStack`.

### ktor-android-incident-demo
https://github.com/pjfmast/ktor-android-incident-demo — bekeken @ `4c7a26d` (2026-11-21), branch `main`

**Waarvoor:** full-stack demo van de docent (Kotlin Toolchain, modules `server`, `shared`, `android`, `dashboard`) rond het melden van incidenten in de openbare ruimte. Staat niet in de slides. **Ktor Client** = de HTTP-client van Ktor waarmee de app de eigen API aanroept (JSON via ContentNegotiation). Raadplegen als je de app aan onze API koppelt: API-klassen met Ktor Client, JWT-token opslaan en meesturen, foutafhandeling als `ApiResult`, Koin-modules in DSL-vorm, en Nav3 met login/uitloggen. Let op: de app is **Android-only** (`product: android/app`, niet KMP; `shared` is alleen `[jvm, android]`) en gebruikt Android/JVM-API's (`Context`, DataStore, `android.util.Log`, `java.net`/`java.io`-exceptions) – voor onze `shared`-module dus niet letterlijk overnemen. De README noemt de Android-app nog "coming soon", terwijl de code er wel staat.

| Bestand | Wat zie je er |
|---|---|
| `android/module.yaml` | Android-dependencies: nav3 `androidx.navigation3` 1.2.0, `lifecycle-viewmodel-navigation3`, Koin (`koin-core`, `koin-android`, `koin-compose`, `koin-compose-viewmodel` 4.2.2), Ktor Client (core, okhttp, content-negotiation, kotlinx-json), DataStore, Coil, MapLibre |
| `android/src/incident/android/di/AppModule.kt` | Koin-DSL: `module { single { HttpClient(OkHttp) { install(ContentNegotiation) { json(...) } } } }`, `single { IncidentApi(get(), get()) }`, `viewModel { LoginViewModel(get()) }` |
| `android/src/incident/android/MainApplication.kt` | `startKoin { androidContext(...); modules(networkModule, dataModule, viewModelModule) }` |
| `android/src/incident/android/di/NetworkConfig.kt` | `BASE_URL = "http://127.0.0.1:8080"` + uitleg `adb reverse tcp:8080 tcp:8080` vs `10.0.2.2`, en welke Ktor-engine per platform (OkHttp/Darwin) |
| `android/src/incident/android/data/api/IncidentApi.kt` | `client.get("$baseUrl/incidents/$id") { header("Authorization", "Bearer $token") }`, `post` met `contentType(Json)` + `setBody(request)` |
| `android/src/incident/android/util/ApiUtils.kt` + `data/model/ApiResult.kt` | `performRequest { }`: token ophalen, 401 → token wissen, statuscodes/timeouts → `ApiResult` |
| `android/src/incident/android/data/store/TokenPreferences.kt` | JWT-token opslaan in DataStore Preferences (Android-only) |
| `android/src/incident/android/navigation/NavKey.kt` + `AppNavigation.kt` | nav3 met `@Serializable object …Key : NavKey`, `rememberNavBackStack(LoginKey)` (zonder config), decorators, uitloggen = `removeAll { true }` + `add(LoginKey)`; argument `IncidentDetailKey(incidentId)` gaat naar het scherm, dat het met `LaunchedEffect(incidentId)` in de ViewModel laadt |
| `android/src/incident/android/viewmodel/*` | ViewModels met `_uiState`/`asStateFlow()`, `BaseViewModel` met `withLoading { }` |
| `server/src/Dependencies.kt` | server zonder DI-library: services via constructors in één `dependencies(...)`-functie (vergelijkbaar met onze server) |
| `server/test/http-requests/*.http` | HTTP-requestflows per rol (anonymous, admin, …) |
| `dashboard/src/LiveDashboard.kt` | SSE-client met Ktor (`io.ktor.client.plugins.sse`) |
| `docs/ontwikkeldocument.md`, `docs/theorie-ktor-introductie.md` | voorbeeld-ontwikkeldocument en Ktor-theorie (NL) |

### android nav3-recipes
https://github.com/android/nav3-recipes — bekeken @ `5ecbad6` (2026-11-10)

**Waarvoor:** officiële verzameling kleine Navigation 3-voorbeelden van Google; elk recept toont één concept (basis, opslaan van de back stack, dialog, bottom sheet, list-detail, tabs met meerdere back stacks, conditionele navigatie zoals login, ViewModel met argumenten, resultaat teruggeven, animaties). Les 10 gebruikt het **dialog**-recept (task 10). Raadplegen als je een specifiek navigatiepatroon zoekt. Let op: Android-only (Gradle, `Activity` per recept) en `main` gebruikt de nieuwste (soms snapshot) nav3-versie (`1.3.0-SNAPSHOT` op deze commit); stabiele versies staan onder *Releases*.

| Bestand | Wat zie je er |
|---|---|
| `README.md` | lijst van alle recepten met korte uitleg |
| `app/src/main/java/com/example/nav3recipes/basic/`, `basicdsl/`, `basicsaveable/` | basis: back stack, `NavDisplay`, `entryProvider`-DSL, opslaan |
| `…/dialog/DialogActivity.kt` | `DialogSceneStrategy` + `metadata = DialogSceneStrategy.dialog()` |
| `…/passingarguments/viewmodels/basic/BasicViewModelsActivity.kt` | argument uit de key naar een ViewModel, `rememberViewModelStoreNavEntryDecorator()` (gebruikt `ViewModelProvider.Factory` met `Class<T>` → JVM-variant) |
| `…/multiplestacks/`, `…/commonui/` | navigatiebalk met top-level bestemmingen, aparte back stack per tab |
| `…/conditional/` | andere flow bij een voorwaarde (bv. inloggen) |
| `…/results/` | resultaat teruggeven aan het vorige scherm |

### terrakok nav3-recipes
https://github.com/terrakok/nav3-recipes — bekeken @ `3348056` (2026-05-31)

**Waarvoor:** Compose Multiplatform-versie van android/nav3-recipes (Android, desktop, iOS, web), genoemd in les 10 voor KMP/CMP. Raadplegen als je een recept uit android/nav3-recipes wilt zien in common code (zonder Android-only API's). Let op: Gradle-project en nav3-versie `1.1.1` (ouder dan HouseHelper-KTC); `passingarguments/viewmodels/basic/` bevat op deze commit alleen een README, geen code.

| Bestand | Wat zie je er |
|---|---|
| `sharedUI/src/commonMain/kotlin/com/example/nav3recipes/basicsaveable/BasicSaveableActivity.kt` | `@Serializable … : NavKey`, `SavedStateConfiguration` met `polymorphic(NavKey::class) { subclass(...) }`, `rememberNavBackStack(config, RouteA)` |
| `…/dialog/`, `…/bottomsheet/`, `…/multiplestacks/`, `…/conditional/` | dezelfde recepten als bij Google, in common code |
| `…/sharedviewmodel/` | ViewModel delen tussen entries met een eigen `NavEntryDecorator` |
| `gradle/libs.versions.toml` | `org.jetbrains.androidx.navigation3` / `lifecycle-viewmodel-navigation3`-artifacts |

### tip-calculator
https://github.com/google-developer-training/basic-android-kotlin-compose-training-tip-calculator — bekeken via GitHub API @ `b029b3d` (2025-01-10), branch `main`

**Waarvoor:** oplossing van het codelab *Calculate a custom tip* (huiswerk na les 8): state hoisting met `remember { mutableStateOf("") }` en een herbruikbare `EditNumberField`. Alleen als referentie voor dat codelab; gebruikt `java.text.NumberFormat` (niet KMP, niet voor ons).

| Bestand | Wat zie je er |
|---|---|
| `app/src/main/java/com/example/tiptime/MainActivity.kt` | `TipTimeLayout` met gehoiste state, `EditNumberField`, `RoundTheTipRow` (`Switch`), `calculateTip(...)` |

### compose-multiplatform-core
https://github.com/JetBrains/compose-multiplatform-core — bekeken via GitHub API @ `3595213` (2026-11-24), branch `jb-main`

**Waarvoor:** de JetBrains-fork van AndroidX waarmee Compose Multiplatform gebouwd wordt (les 9, "Compose Multiplatform fork"). Alleen achtergrond: laat zien dat de meeste code al common is en Android-artifacts naar de originele Jetpack Compose verwijzen. Niet gebruiken als voorbeeldcode.

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

Met een argument (bron: `unscramble-ktc` @ fc7b79a, `shared/composeResources/values/strings.xml` en `shared/src/ui/GameScreen.kt`) – CMP vereist positionele placeholders (`%1$d`, `%1$s`):
```xml
<string name="score">Score: %1$d</string>
```
```kotlin
text = stringResource(Res.string.score, score),
```

### Datum formatteren zonder java.time
Bron: `incident-details-demo-kmp/shared/src/App.kt` @ 25e6412. Gebruikt in `shared/src/ui/components/DateTimeField.kt`.
```kotlin
val DeadlineDateFormat = LocalDateTime.Format {
    day()
    char('-')
    monthNumber()
    char('-')
    year()
}
DeadlineDateFormat.format(dateTime)
```
DatePicker/TimePicker en NavigationBar komen niet voor in de demo's; daarvoor is de standaard Material 3-API gebruikt.

### Navigation 3 – basis
Bron: `HouseHelper-KTC` @ 4003e3c (branch `solutions`), tenzij anders vermeld. Begrippen: zie `slides-p2.md` → Les 10.

**1. Dependencies** (`shared/module.yaml` + `libs.versions.toml`, alleen de nav3-relevante regels):
```yaml
dependencies:
  - $libs.androidx.lifecycle.viewmodel.compose
  - $libs.androidx.navigation3.ui
  - $libs.androidx.lifecycle.viewmodel.navigation3
  - $libs.kotlinx.serialization.core

dependencies@android:
  # Use the latest stable Navigation 3 artifacts on Android; multiplatform targets use JetBrains 1.2.0.
  - $libs.androidx.navigation3.android.runtime
  - $libs.androidx.navigation3.android.ui

settings:
  kotlin:
    serialization: enabled
```
```toml
androidx-lifecycle = "2.11.0"
androidx-navigation3 = "1.2.0"
androidx-navigation3-android = "1.2.2"
androidx-lifecycle-viewmodel-navigation3 = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3", version.ref = "androidx-lifecycle" }
androidx-navigation3-ui = { module = "org.jetbrains.androidx.navigation3:navigation3-ui", version.ref = "androidx-navigation3" }
androidx-navigation3-android-runtime = { module = "androidx.navigation3:navigation3-runtime", version.ref = "androidx-navigation3-android" }
androidx-navigation3-android-ui = { module = "androidx.navigation3:navigation3-ui", version.ref = "androidx-navigation3-android" }
```

**2. Route-keys** (`shared/src/com/kotlinconf/workshop/househelper/navigation/Routes.kt`). Elke route is een `@Serializable` `NavKey`; argumenten zijn properties van een `data class`. De `SavedStateConfiguration` registreert alle routes voor (de)serialisatie, nodig om de back stack op alle platforms (ook iOS) te kunnen bewaren/herstellen.
```kotlin
@Serializable
sealed interface Route : NavKey {
    // Onboarding routes
    @Serializable data object OnboardingWelcome : Route
    @Serializable data object OnboardingAbout : Route
    @Serializable data object OnboardingDone : Route

    // Main routes
    @Serializable data object Dashboard : Route
    @Serializable data class LightDetails(val deviceId: DeviceId) : Route
    @Serializable data class CameraDetails(val deviceId: DeviceId) : Route
    @Serializable data class RenameDevice(val deviceId: DeviceId) : Route
}

// Central configuration for safe state restoration across process restarts
// See: https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html#recommended-serialization-approaches
@OptIn(ExperimentalSerializationApi::class)
val houseHelperSavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclassesOfSealed<Route>()
        }
    }
}
```
(`DeviceId` is een `@JvmInline @Serializable value class DeviceId(val value: String)`; bij ons zou dat een `Int`-id zijn.)

**3. Back stack + `NavDisplay`** (`shared/src/com/kotlinconf/workshop/househelper/App.kt`, ingekort met `// …`). Navigeren = key toevoegen, terug = laatste key weghalen; `onBack` vangt de systeem-terugknop/-gebaar af.
```kotlin
val backStack = rememberNavBackStack(
    houseHelperSavedStateConfig,
    OnboardingWelcome
)
val dialogStrategy = remember { DialogSceneStrategy<NavKey>() }

NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    sceneStrategies = listOf(dialogStrategy),
    entryProvider = entryProvider {
        // …
        entry<OnboardingDone> {
            OnboardingScreen(
                // …
                onNext = {
                    backStack.clear()
                    backStack.add(Dashboard)
                }
            )
        }
        entry<Dashboard> {
            DashboardScreen(
                onNavigateToLightDetails = { deviceId ->
                    backStack.add(LightDetails(deviceId))
                },
                onNavigateToCameraDetails = { deviceId ->
                    backStack.add(CameraDetails(deviceId))
                }
            )
        }
        entry<LightDetails> {
            LightDetailsScreen(
                deviceId = it.deviceId,
                onNavigateUp = { backStack.removeLastOrNull() },
            )
        }
        // …
        entry<RenameDevice>(
            metadata = DialogSceneStrategy.dialog()
        ) {
            RenameDeviceScreen(
                deviceId = it.deviceId,
                onDismiss = {
                    backStack.removeLastOrNull()
                },
            )
        }
    },
    entryDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
    ),
)
```
- **Argumenten:** zitten in de key (`LightDetails(deviceId)`); in `entry<LightDetails> { … }` is `it` de key, dus `it.deviceId`.
- **Stack vervangen** (bv. na inloggen/onboarding): `backStack.clear()` + `backStack.add(Dashboard)`.
- **Schermen kennen de back stack niet:** ze krijgen callbacks (`onNavigateUp`, `onNavigateToLightDetails`), zoals bij state hoisting.
- **Dialog:** `DialogSceneStrategy` in `sceneStrategies` + `metadata = DialogSceneStrategy.dialog()` op de entry; de inhoud zelf in een `Surface` (`RenameDeviceScreen.kt`):
  ```kotlin
  Surface(
      color = MaterialTheme.colorScheme.surface,
      shape = MaterialTheme.shapes.extraLarge
  ) {
  ```

**4. ViewModel per entry met argument (Koin, `parametersOf`).** Het HouseHelper-voorbeeld is compleet; alle stukken:
- *Dependencies:* `lifecycle-viewmodel-navigation3` (stap 1) + Koin (`koin-core`, `koin-compose-viewmodel`, `koin-annotations` + compiler plugin; zie *Koin in de app – basis*, stijl A).
- *Decorators* in `NavDisplay`: `rememberSaveableStateHolderNavEntryDecorator()` + `rememberViewModelStoreNavEntryDecorator()` (stap 3). De laatste geeft elke entry een eigen ViewModelStore: elke key-instantie krijgt een eigen ViewModel, die wordt opgeruimd zodra de entry van de back stack gaat.
- *Aanmelden bij Koin:* `@KoinViewModel` op de klasse, gevonden via `@ComponentScan` in `AppModule` (zie *Koin in de app – basis*, stijl A).
- *Aanroep vanuit `entry<…>`:* de entry geeft het argument uit de key door (`entry<LightDetails> { LightDetailsScreen(deviceId = it.deviceId, onNavigateUp = …) }`, stap 3); `koinViewModel { parametersOf(deviceId) }` staat in de demo als default-parameter van het scherm, niet letterlijk in de `entry`-lambda (`devices/LightDetailsScreen.kt`):
```kotlin
@Composable
fun LightDetailsScreen(
    deviceId: DeviceId,
    onNavigateUp: () -> Unit,
    viewModel: LightDetailsViewModel = koinViewModel {
        parametersOf(deviceId)
    }
) {
    val device = viewModel.light.collectAsStateWithLifecycle().value
```
- *ViewModel* ontvangt het argument met `@InjectedParam` (`devices/LightDetailsViewModel.kt`):
```kotlin
@KoinViewModel
class LightDetailsViewModel(
    private val houseService: HouseService,
    @InjectedParam private val deviceId: DeviceId,
) : ViewModel() {
```

Ook valide (andere aanpak): ViewModel zonder argument aanmaken en de data laden met een `LaunchedEffect` op het argument. Bron: `ktor-android-incident-demo` @ 4c7a26d, `android/src/incident/android/ui/screens/management/IncidentDetailScreen.kt` (entry: `IncidentDetailScreen(incidentId = it.incidentId, …)` in `navigation/AppNavigation.kt`, met dezelfde twee decorators):
```kotlin
@Composable
fun IncidentDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMyIncidentList: () -> Unit,
    incidentId: Long?,
    viewModel: IncidentDetailViewModel = koinViewModel()
) {
    // …
    // Load incident when screen opens or incidentId changes
    LaunchedEffect(incidentId) {
        if (incidentId != null) {
            viewModel.getIncidentById(incidentId)
        }
    }
```

**Versies.** Deze sectie volgt HouseHelper-KTC: JetBrains-multiplatform-artifacts nav3 `1.2.0` (`org.jetbrains.androidx.navigation3`), op Android `androidx.navigation3` `1.2.2`, lifecycle `2.11.0`, en `rememberNavBackStack(config, start)`. Andere bronnen wijken af: terrakok/nav3-recipes gebruikt nav3 `1.1.1`, android/nav3-recipes `main` een `1.3.0-SNAPSHOT`, ktor-android-incident-demo (Android-only) `rememberNavBackStack(LoginKey)` zonder config, en slide 22 van les 10 een getypte `NavBackStack<LunchTrayRoute>`.

### Koin in de app – basis
> **Keuze nog open: de AI vraagt de gebruiker expliciet welke stijl gebruikt wordt voordat Koin geïmplementeerd wordt.** Hieronder staan beide stijlen uit de demo's, zonder voorkeur.

Alleen voor de app (`shared`); de server blijft zonder DI-library. In beide stijlen haalt een scherm zijn ViewModel op met `koinViewModel()` (zonder argument) of `koinViewModel { parametersOf(x) }` (met argument, zie *Navigation 3 – basis* stap 4).

#### Stijl A – annotations + Koin compiler plugin
Bron: `HouseHelper-KTC` @ 4003e3c (branch `solutions`), KMP.

**A1. Dependencies + compiler plugin** (`shared/module.yaml`, alleen de Koin-regels):
```yaml
dependencies:
  - $libs.koin.core
  - $libs.koin.compose.viewmodel
  - $libs.koin.annotations

settings:
  kotlin:
    serialization: enabled
    compilerPlugins:
      - id: io.insert-koin.compiler.plugin
        dependency: $libs.koin.compiler.plugin
```
`androidApp/module.yaml` heeft daarnaast `$libs.koin.android` en `$libs.koin.core`. Uit `libs.versions.toml`:
```toml
koin = "4.2.2"
koin-compiler = "1.2.3"
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel", version.ref = "koin" }
koin-annotations = { module = "io.insert-koin:koin-annotations", version.ref = "koin" }
koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }
koin-compiler-plugin = { module = "io.insert-koin:koin-compiler-plugin", version.ref = "koin-compiler" }
```

**A2. Module + start** (`shared/src/com/kotlinconf/workshop/househelper/di/AppModule.kt`). `@ComponentScan` vindt alle geannoteerde klassen in het package:
```kotlin
@Module
@Configuration
@ComponentScan("com.kotlinconf.workshop.househelper")
class AppModule

@KoinApplication
object KoinApp

fun initApp(appDeclaration: KoinAppDeclaration? = null) {
    startKoin<KoinApp> {
        appDeclaration?.invoke(this)
    }
}
```

**A3. Klassen aanmelden met annotations:** een service als singleton (`data/DemoHouseService.kt`; in de demo wordt hij geïnjecteerd waar een `HouseService` gevraagd wordt, zonder extra binding-code), een ViewModel met `@KoinViewModel` (+ `@InjectedParam` voor een argument, zie *Navigation 3 – basis* stap 4):
```kotlin
@Singleton
class DemoHouseService : HouseService {
```

**A4. `initApp()` per platform aanroepen vóór `App()`:**
```kotlin
// androidApp/src/…/MyApplication.kt (in AndroidManifest: android:name=".MyApplication")
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initApp {
            androidContext(this@MyApplication)
        }
    }
}

// iosApp/src/ViewController.kt
fun ViewController() = ComposeUIViewController(
    configure = {
        initApp()
    }
) { App() }
```
(desktop en web: `initApp()` als eerste regel in `main()`.)

#### Stijl B – Koin-DSL (zonder annotations/compiler plugin)
Bron: `ktor-android-incident-demo` @ 4c7a26d, Android-only (`android/module.yaml`, `android/src/incident/android/di/AppModule.kt`, `MainApplication.kt`).

**B1. Dependencies** (`android/module.yaml`, alleen de Koin-regels):
```yaml
  # Koin
  - io.insert-koin:koin-core:4.2.2
  - io.insert-koin:koin-android:4.2.2
  - io.insert-koin:koin-compose:4.2.2
  - io.insert-koin:koin-compose-viewmodel:4.2.2
```

**B2. Modules in code** (elke klasse met `single { }` of `viewModel { }`, constructor-argumenten met `get()`):
```kotlin
val dataModule = module {
    single { TokenPreferences(androidContext()) }
    single { IncidentDataStore(androidContext()) }
    single { AuthApi(get()) }
    single { UserApi(get(), get()) }
    single { IncidentApi(get(), get()) }
    single { AuthRepository(get(), get()) }
    single { UserRepository(get()) }
    single { IncidentRepository(get()) }
}

val viewModelModule = module {
    viewModel { LoginViewModel(get()) }
    // …
    viewModel { IncidentDetailViewModel(get(), get(), get()) }
}
```

**B3. Start** (in `Application.onCreate()`, `MainApplication.kt`):
```kotlin
startKoin {
    androidContext(this@MainApplication)
    modules(networkModule, dataModule, viewModelModule)
}
```
De demo is Android-only; hoe deze stijl in KMP (iOS) gestart wordt, staat er niet in.

<!-- Format:
### <onderwerp>
Bron: `<repo>/<pad>` @ <commit>
```kotlin
...
```
-->
