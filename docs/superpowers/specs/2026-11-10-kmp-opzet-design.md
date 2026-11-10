# Ontwerp: KMP-opzet naast de server

Datum: 2026-11-10

## Doel en waarom

Het team gaat een Android-app bouwen die alle functies van de bestaande Ktor-API aanbiedt, zodat verhuurders en huurders auto's kunnen delen via de app. Deze stap legt alleen de **opzet** neer: de modulestructuur, een gedeeld API-contract tussen server en app, en een lege app die start. Schermen en netwerkcode komen in latere stappen.

Omdat de app de hele API moet dekken, moeten server en app het altijd eens zijn over de vorm van auto's, reserveringen, ritten enzovoort. Daarom verhuist het API-contract naar een gedeelde module die beide kanten gebruiken: verandert het contract, dan breekt de build in plaats van de app tijdens runtime.

## Doelen

- Een `shared`-module (Kotlin Multiplatform, platforms `jvm` en `android`) met het API-contract.
- De server gebruikt het contract uit `shared` in plaats van eigen kopieën.
- Een Compose Multiplatform-app met voorlopig alleen Android als platform, die bouwt en een leeg startscherm toont.
- `.\kotlin.bat build` en `.\kotlin.bat test` slagen, alle bestaande servertests groen.

## Niet-doelen

- Schermen, navigatie, een API-client, login of token-opslag in de app.
- iOS, desktop of web als platform (de opzet moet die later wel toelaten).
- Inhoudelijke wijzigingen aan modellen, DTO's of API-gedrag.
- Validatie naar `shared` verplaatsen.
- Overstappen van Amper naar Gradle.

## Anti-doelen

Het is mislukt, ook als het technisch werkt, als:

- de server zich anders gedraagt: andere JSON, andere statuscodes, andere foutmeldingen;
- server-only gegevens in `shared` terechtkomen (zoals `passwordHash`), waardoor ze in de app belanden;
- de UI-code in een module komt die alleen Android kan, zodat later een ander platform toevoegen een herbouw vereist;
- de verhuizing zo lang openstaat of zo breed is dat de teamgenoten er niet omheen kunnen werken.

## Randvoorwaarden

- Build-tool is Amper (`kotlin`/`kotlin.bat`, Kotlin Toolchain 0.12.0), geen Gradle. Modules staan in `project.yaml`, elk met een eigen `module.yaml`. Versies in `libs.versions.toml`.
- Alle regels uit `.claude/CLAUDE.md` blijven gelden voor de server (Route → Service → Repository, exceptions uit `services/Exception.kt`, geen `java.*`, enzovoort).
- `.claude/decisions.md`: modellen veranderen alleen van plek en package, niet van inhoud.
- Team van drie. Het werk gebeurt in een aparte branch en komt als één PR binnen.
- Een Android-build heeft de Android SDK nodig, lokaal en in CI. **Besloten:** iedereen in het team installeert de SDK; er komt geen aparte server-only build. De build-regel in `CLAUDE.md` (`./kotlin build`) blijft gelijk.
- CI (`.github/workflows/test.yml`) draait nu alleen `./kotlin test` voor de server.
- Fouten van de server zijn platte tekst (`"<status>: <bericht>"`, zie `StatusPages.kt`). Daar verandert niets aan.

## Overwogen aanpakken

### Structuur

| Aanpak | Afweging |
|---|---|
| **A. Gedeelde module `shared` + server + app** (gekozen) | Eén bron voor het contract; contractbreuk zie je bij het bouwen. Kost een verhuizing in de server. |
| B. Server ongemoeid, app met eigen kopieën van de DTO's | Geen risico voor de server, maar het contract staat dubbel en loopt uit elkaar zodra de app de hele API dekt. |

**Gekozen: A.** De server mag aangepast worden, in een aparte branch.

### Verhuizing van het contract

| Aanpak | Afweging |
|---|---|
| 1. Verhuizen met dezelfde packagenamen | Kleinste diff, imports blijven werken. Packagenaam zegt niet uit welke module iets komt. |
| **2. Verhuizen naar nieuwe packages onder `shared.`** (gekozen) | Duidelijke naamgeving: je ziet aan de import dat iets uit `shared` komt. Alle imports in server en tests veranderen, dus meer kans op mergeconflicten. |
| 3. Eerst leeg skelet, dan per domein verhuizen | Kleine stappen, maar lang een half-verhuisde toestand, en de modellen hangen aan elkaar (`CarDto` gebruikt `FuelType`). |

**Gekozen: 2.**

### App

**Gekozen:** Compose Multiplatform, voorlopig alleen Android.

## Wat er gebouwd wordt

### Modules

```
project.yaml
├── shared/       kmp/lib, platforms [jvm, android]   API-contract, geen logica
├── server/       jvm/app                             hangt af van ../shared
├── composeApp/   kmp/lib, platforms [android]        Compose UI (schermen, later)
└── androidApp/   android/app                         dun startpunt: MainActivity
```

*Mijn keuze, met reden:* in Amper is een app-module altijd voor één platform (`android/app`, `jvm/app`, ...). Om de UI multiplatform te houden, staat de UI in een `kmp/lib` met Compose aan (`composeApp`, nu alleen `android`) en is `androidApp` alleen het Android-startpunt dat `composeApp` laat zien. Komt er later een platform bij, dan voeg je dat toe aan `composeApp` en maak je een tweede dun app-module, zonder de UI te verhuizen. Dit zijn vier modules in plaats van de drie uit het gesprek; `shared` blijft vrij van Compose zodat de server geen UI-dependencies krijgt.

### Wat naar `shared` verhuist

Alleen klassen die over de lijn gaan (request-body of response). Inhoud blijft identiek, alleen het package verandert.

| Nu | Wordt | Klassen |
|---|---|---|
| `models` (`Car.kt`) | `shared.models` | `FuelType`, `VehicleType`, `Car`, `ICECar`, `BEVCar`, `FCEVCar` |
| `models` (`Photo.kt`) | `shared.models` | `Photo` |
| `models` (`RentalTerms.kt`) | `shared.models` | `RentalTerms` |
| `models` (`Reservation.kt`) | `shared.models` | `Reservation`, `ReservationStatus` |
| `models` (`Ride.kt`) | `shared.models` | `Ride`, `RideStatus` |
| `dto` | `shared.dto` | `CarDto`, `CarChangeDto`, `RentalTermsRequest`, `RideFinishRequest` |
| `requests` | `shared.requests` | `LoginRequest`, `RegisterRequest`, `ReservationRequest` |
| `responses` | `shared.responses` | `FuelCostResponse`, `LoginResponse`, `TcoResponse`, `UserResponse` |

*Mijn keuze:* de bestaande indeling `dto` / `requests` blijft, alleen met het `shared.`-voorvoegsel. Ze samenvoegen is een opruimactie die niets met deze opzet te maken heeft.

### Wat op de server blijft

| Wat | Waarom |
|---|---|
| `models.User` | Bevat `passwordHash`; mag nooit naar de app. |
| `fun User.toResponse()` (nu in `UserResponse.kt`) | Verwijst naar `User`. Blijft in de server, bijv. in `mappers/`. |
| `ACTIVE_RESERVATION_STATUSES` (nu in `Reservation.kt`) | Bedrijfsregel voor queries, geen contract. Blijft in de server naast de code die hem gebruikt. *Mijn keuze.* |
| `models.Location` | Niet `@Serializable`, wordt niet verstuurd. |
| `dto.CarFilter` | Niet `@Serializable`; de route bouwt hem uit queryparameters. *Mijn keuze:* blijft voorlopig op de server. |
| `models.rdw.*`, `models.gemini.*` | Externe API's van de server. |
| Tabellen, mappers, repositories, services, validatie | Serverlogica. |

Alle bestanden in `server/src` en `server/test` die verhuisde klassen gebruiken, krijgen de nieuwe imports. `diagrams/classes/Architecture.puml` krijgt de nieuwe packages en de module-indeling.

### Dependencies en versies

- `shared`: kotlinx-serialization (via `settings: kotlin: serialization: enabled`) en kotlinx-datetime (voor `LocalTime`). kotlinx-datetime komt nu indirect binnen via `exposed-kotlin-datetime`; voeg het expliciet toe aan `libs.versions.toml` met de versie die Exposed al gebruikt, zodat server en `shared` niet verschillen.
- `composeApp`: Compose aan, alleen wat een leeg startscherm nodig heeft.
- `androidApp`: `applicationId` en namespace. *Mijn keuze:* `nl.carsharing.app`, `minSdk` 26. Kotlin-packages in de app volgen die naam.

### Lege app

`androidApp` start een `MainActivity` die een `App()`-composable uit `composeApp` toont: één scherm met de app-naam. Geen netwerk, geen navigatie.

### CI

- De workflow bouwt alle modules (`./kotlin build`) vóór `./kotlin test`. De `ubuntu-latest`-runner heeft een Android SDK aan boord.
- De cache-key neemt alle `module.yaml`-bestanden mee, niet alleen `server/module.yaml`.
- Het testrapportpad van de server blijft gelijk; controleer na de build of het nog klopt.

### Projectinstructies

`.claude/CLAUDE.md` krijgt een korte sectie over de modules:
- `shared` bevat alleen `@Serializable` contractklassen, geen logica en geen server-only velden.
- De bestaande regels gelden voor `server`.
- Regels voor `composeApp`/`androidApp` volgen zodra er schermen komen.

## Wat aan de bouwer is

- Exacte Amper-syntax van de `module.yaml`-bestanden en de mappenindeling (`src/`, `src@android/`).
- Waar `User.toResponse()` en `ACTIVE_RESERVATION_STATUSES` precies terechtkomen in de server.
- De versie van Compose en kotlinx-datetime, mits consistent met wat Kotlin 2.4.0 en Exposed 1.3.1 verwachten.
- Hoe het werk in de plan-fase wordt verdeeld over (sub)taken.

## Risico's

- **Android SDK lokaal.** Na de merge kan niemand zonder SDK nog bouwen, ook de server niet. Controleer in de eerste stap of Amper de SDK zelf downloadt. Zo niet: zet de installatie-instructie in de README en spreek vóór de merge met het team af dat iedereen de SDK heeft geïnstalleerd.
- **Mergeconflicten.** Veel imports veranderen. Spreek met het team een moment af om de PR te mergen en houd hem kort open.
- **Serialisatie.** De `@SerialName`-waarden van `Car` bepalen de JSON. Ze verhuizen ongewijzigd mee; de bestaande tests moeten dat bevestigen.
