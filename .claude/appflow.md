# App-flow (Android + iOS)

Zoveel mogelijk zit in `shared`, zodat Android en iOS het delen. `androidApp` en `iosApp` bevatten alleen wat het platform verplicht.

## Waar staat wat?

| Ik zoek… | Bestand |
|---|---|
| een scherm | `shared/src/ui/screens/<Naam>Screen.kt` |
| tijdelijk scherm voor tab Ritten/Account | `shared/src/ui/screens/PlaceholderScreen.kt` |
| onderdelen die schermen delen (knoppen, velden, titelbalk, bottom bar) | `shared/src/ui/components/` |
| de tabs van de bottom bar | `shared/src/ui/components/BottomNavigationBar.kt` (`NavigationTab`) |
| iconen | `shared/src/ui/icons/` (`AppIcons.<Naam>`) |
| routes (welke schermen er zijn + argumenten) | `shared/src/navigation/Routes.kt` |
| welk scherm bij welke route hoort, startscherm, tab wisselen | `shared/src/App.kt` (`NavDisplay`) |
| kleuren (licht en donker) | `shared/src/ui/theme/Theme.kt` (`CarSharingPalette`, `AppColors`) |
| licht/donker kiezen | `shared/src/ui/theme/Theme.kt` |
| lettertypen | `shared/src/ui/theme/Type.kt` |
| randdiktes en blok-stijl (`Modifier.block`) | `shared/src/ui/theme/Border.kt` |
| kleurenonderzoek (alleen previews) | `shared/src/ui/theme/Palettes.kt`, `PalettePreview.kt`, `TealStylingPreview.kt` |
| tekst in een scherm (Nederlands) | `shared/composeResources/values/strings.xml` |
| tekst in een scherm (Engels) | `shared/composeResources/values-en/strings.xml` |
| plaatje in een scherm | `shared/composeResources/drawable/` (map aanmaken bij het eerste plaatje) |
| verbinding met de server | `shared/src/api/ApiClient.kt` |
| libraries van de gedeelde code | `shared/module.yaml` |
| naam onder het app-icoon (Android) | `androidApp/res/values/strings.xml` |
| app-icoon (Android) | `androidApp/res/mipmap-anydpi-v26/` + `androidApp/res/drawable/` |
| app-icoon (iOS) | nog niet aanwezig |
| startpunt Android | `androidApp/src/MainActivity.kt` |
| startpunt iOS | `iosApp/src/iosApp.swift` → `ContentView.swift` → `main.kt` |
| http naar de lokale server toestaan (Android) | `androidApp/res/xml/network_security_config.xml` |

## Mappen in één oogopslag

```
shared/                          ← ALLES wat Android en iOS delen
├── module.yaml                  libraries + resources aanzetten
├── src/
│   ├── App.kt                   root: thema + Scaffold + NavDisplay (back stack)
│   ├── api/ApiClient.kt         Ktor-client naar de server
│   ├── navigation/Routes.kt     routes (NavKeys) + SavedStateConfiguration
│   └── ui/
│       ├── components/          gedeelde onderdelen (ScreenScaffold, BottomNavigationBar, knoppen, …)
│       ├── icons/               AppIcons
│       ├── screens/             één bestand per scherm (HomeScreen.kt, …, PlaceholderScreen.kt)
│       └── theme/               Theme.kt, Type.kt, Border.kt (+ Palettes/previews)
└── composeResources/
    ├── values/strings.xml       teksten NL (standaard)
    ├── values-en/strings.xml    teksten EN
    └── drawable/                plaatjes (nog aan te maken)

androidApp/                      ← alleen wat Android verplicht
├── module.yaml                  applicationId, minSdk
├── src/
│   ├── AndroidManifest.xml      start-activity, icoon, naam, thema, internet
│   └── MainActivity.kt          setContent { App() }
└── res/
    ├── values/strings.xml       app_name (naam onder het icoon)
    ├── values/themes.xml        venster-thema licht
    ├── values-night/themes.xml  venster-thema donker
    ├── drawable/ + mipmap-anydpi-v26/   app-icoon
    └── xml/network_security_config.xml  http naar 10.0.2.2 / localhost

iosApp/                          ← alleen wat iOS verplicht
├── module.yaml
└── src/
    ├── iosApp.swift             @main
    ├── ContentView.swift        Compose in SwiftUI
    └── main.kt                  ComposeUIViewController { App() }
```

## De flow

```
Android                                   iOS
AndroidManifest.xml (LAUNCHER)            iosApp.swift (@main)
        ↓                                         ↓
MainActivity.kt                           ContentView.swift
  setContent { App() }                      MainKt.MainViewController()
        ↓                                         ↓
        └──────────────┬──────────────────  main.kt
                       ↓                      ComposeUIViewController { App() }
              shared/src/App.kt  ←────────────────┘
                       ↓
              CarSharingTheme              ui/theme/Theme.kt (+ Type.kt)
                       ↓
              Scaffold
                       ↓
              NavDisplay                   back stack: rememberNavBackStack(…, Route.Search)
                       ↓                   routes: navigation/Routes.kt
              bovenste route → scherm      bv. Route.Search → HomeScreen (ui/screens/HomeScreen.kt)
               ├─ tekst:   stringResource(Res.string.x)    → composeResources/values*/strings.xml
               └─ plaatje: painterResource(Res.drawable.x) → composeResources/drawable/
```

## Stappen met code

### Android starten
1. `androidApp/src/AndroidManifest.xml`: `<activity android:name=".MainActivity">` met `action.MAIN` + `category.LAUNCHER`. Het venster krijgt `@style/Theme.CarSharing`, `@string/app_name` en `@mipmap/ic_launcher`.
2. `androidApp/module.yaml`: `namespace: nl.carsharing.app`, dus `.MainActivity` = `nl.carsharing.app.MainActivity`.
3. `androidApp/src/MainActivity.kt`: `enableEdgeToEdge()` en daarna `setContent { App() }`.

### iOS starten
1. `iosApp/src/iosApp.swift`: `@main struct iosApp: App` → `WindowGroup { ContentView() }`.
2. `iosApp/src/ContentView.swift`: `ComposeView().ignoresSafeArea()`, die `MainKt.MainViewController()` aanroept (`MainKt` komt van de bestandsnaam `main.kt`).
3. `iosApp/src/main.kt`: `fun MainViewController() = ComposeUIViewController { App() }`.

### Gedeelde UI
1. `shared/src/App.kt`: `CarSharingTheme { Scaffold { innerPadding -> NavDisplay(…, Modifier.padding(innerPadding)) } }`.
2. `shared/src/ui/theme/Theme.kt`: `isSystemInDarkTheme()` kiest de donkere of lichte kleuren van `CarSharingPalette` (lettertypen uit `Type.kt`).
3. `NavDisplay` toont het scherm van de bovenste route op de back stack; bij de start is dat `Route.Search` → `HomeScreen`.

### Navigatie (Navigation 3)
1. `shared/src/navigation/Routes.kt`: elke bestemming is een `@Serializable` route in `sealed interface Route : NavKey` (`data object`, of `data class` met argument, bv. `carId: Int`). `carSharingSavedStateConfig` registreert alle routes, zodat de back stack ook op iOS bewaard kan worden.
2. `shared/src/App.kt`: `rememberNavBackStack(carSharingSavedStateConfig, Route.Search)` is de back stack; `NavDisplay` koppelt in `entryProvider { entry<Route.X> { XScreen(…) } }` elke route aan een scherm.
3. Navigeren: `backStack.add(Route.X)`; terug (pijltje of systeem-terug): `backStack.removeLastOrNull()`.
4. Schermen kennen de back stack niet: ze krijgen callbacks (`onBack`, `onTabClick`, …) die in `App.kt` worden ingevuld.
5. Bottom bar: elk scherm tekent hem zelf via `ScreenScaffold`. Een tik op een tab doet `backStack.clear()` + `backStack.add(<route van de tab>)`: Zoeken → `Route.Search` (HomeScreen), Ritten → `Route.Rides`, Account → `Route.Account`.
6. Ritten en Account tonen voorlopig `PlaceholderScreen`; Account heeft daarop een knop naar Mijn auto's (`Route.MyCars`).

### Scherm toevoegen aan de navigatie
1. Route toevoegen in `Routes.kt`: `@Serializable data object X : Route` (of `data class X(val carId: Int) : Route`).
2. In `App.kt` een `entry<Route.X> { XScreen(onBack = { backStack.removeLastOrNull() }, onTabClick = onTabClick) }` toevoegen.
3. Vanaf een ander scherm: callback invullen met `backStack.add(Route.X)`.

### Tekst toevoegen
1. Zet de tekst in **beide** bestanden met dezelfde `name`:
   - `shared/composeResources/values/strings.xml`: `<string name="home_welcome">Welkom bij Car Sharing</string>`
   - `shared/composeResources/values-en/strings.xml`: `<string name="home_welcome">Welcome to Car Sharing</string>`
2. Bij het bouwen maakt de toolchain `Res.string.home_welcome` aan, in package `nl.carsharing.app.resources` (ingesteld in `shared/module.yaml`).
3. Gebruik in een scherm: `stringResource(Res.string.home_welcome)`. Imports: `nl.carsharing.app.resources.Res`, `nl.carsharing.app.resources.home_welcome`, `org.jetbrains.compose.resources.stringResource`.

De taal volgt de instelling van het toestel.

### Plaatje toevoegen
1. Zet het bestand in `shared/composeResources/drawable/<naam>.png` (of `.xml`/`.svg`).
2. Gebruik in een scherm: `Image(painterResource(Res.drawable.<naam>), contentDescription = stringResource(Res.string.…))`. Imports: `nl.carsharing.app.resources.Res`, `nl.carsharing.app.resources.<naam>`, `org.jetbrains.compose.resources.painterResource`.

Zet plaatjes voor schermen **niet** in `androidApp/res/drawable`; die map is alleen voor het Android-app-icoon.

## Goed om te weten
- **Waarom staan `app_name` en de thema's in `androidApp`?** Het Android-manifest kan geen Compose-resources lezen. De naam onder het icoon en het venster-thema (vóórdat Compose tekent) moeten daarom uit Android-resources komen. De echte kleuren van de schermen staan in `shared/src/ui/theme/`.
- **App-icoon:** kan niet gedeeld worden. Android gebruikt `res/mipmap` + `res/drawable`; iOS heeft later een `Assets.xcassets/AppIcon.appiconset` in `iosApp` nodig.
- **Server-adres:** `DEFAULT_BASE_URL = "http://10.0.2.2:8080"` in `shared/src/api/ApiClient.kt` werkt alleen op de Android-emulator. Op de iOS-simulator is het `localhost`; dat moet nog per platform geregeld worden.
- **Bouwen:** `./kotlin build` (Windows: `.\kotlin.bat build`). `iosApp` bouwt alleen op macOS.
