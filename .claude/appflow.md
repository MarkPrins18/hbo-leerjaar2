# App-flow (Android + iOS)

Zoveel mogelijk zit in `shared`, zodat Android en iOS het delen. `androidApp` en `iosApp` bevatten alleen wat het platform verplicht.

## Waar staat wat?

| Ik zoek… | Bestand |
|---|---|
| een scherm | `shared/src/ui/screens/<Naam>Screen.kt` |
| welk scherm als eerste getoond wordt | `shared/src/App.kt` |
| kleuren (licht en donker) | `shared/src/ui/theme/Color.kt` |
| licht/donker kiezen | `shared/src/ui/theme/Theme.kt` |
| lettertypen | `shared/src/ui/theme/Type.kt` |
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
│   ├── App.kt                   root: thema + Scaffold + eerste scherm
│   ├── api/ApiClient.kt         Ktor-client naar de server
│   └── ui/
│       ├── screens/             één bestand per scherm (HomeScreen.kt, …)
│       └── theme/               Color.kt, Theme.kt, Type.kt
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
              CarSharingTheme              ui/theme/Theme.kt (+ Color.kt, Type.kt)
                       ↓
              Scaffold
                       ↓
              HomeScreen                   ui/screens/HomeScreen.kt
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
1. `shared/src/App.kt`: `CarSharingTheme { Scaffold { innerPadding -> HomeScreen(Modifier.padding(innerPadding)) } }`.
2. `shared/src/ui/theme/Theme.kt`: `isSystemInDarkTheme()` kiest `DarkColorScheme` of `LightColorScheme` (kleuren uit `Color.kt`, lettertypen uit `Type.kt`).
3. `shared/src/ui/screens/HomeScreen.kt`: `Text(stringResource(Res.string.home_welcome))`.

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
