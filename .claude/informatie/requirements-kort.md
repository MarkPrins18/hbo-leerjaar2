# Requirements – kort overzicht

Samenvatting van `requirements.md` voor AI-agents. Bij twijfel of tegenspraak geldt `requirements.md`. Daar staan ook de letterlijke Brightspace-teksten.

## Stand van zaken
- **Periode 1 (backend, Ktor):** afgerond. Assessment geweest in oktober 2026.
- **Periode 2 (app, Compose):** loopt nu. Inleveren uiterlijk **zo 17 januari 2027, 23:59**. Assessment: 19, 21, 26 of 28 januari 2027.
- Team van 3. Casus: "Rent my car" (autodeelplatform).
- De app gebruikt de backend uit periode 1. Die backend moet dus blijven werken en getest blijven.

## Altijd geldig
- Code en documentatie in het **Engels**. ❓ Nederlandse foutmeldingen naar de client: zie *Open vragen*.
- Kotlin-naamconventies. Packagenamen sluiten aan op de architectuur.
- Gebruik van OO, FP, design patterns en architectuur moet je kunnen **onderbouwen**.
- KDoc en commentaar "precies voldoende": alleen waar het iets toevoegt.
- Realistische testdata, zodat de docent zelf kan testen.
- Draait lokaal op eigen computer.
- **AI-tooling verantwoorden:** waarvoor, welk model en waarom, hoe geverifieerd, wat is overgenomen of aangepast. Iedereen moet zijn eigen code kunnen uitleggen.

## Periode 1 – backend (afgerond, ter referentie)
Opgeleverd en aanwezig: Ktor REST API (Route → Service → Repository), Exposed + H2, OpenAPI-spec, JWT-auth, `sealed class Car` (ICE/BEV/FCEV), filteren, reserveren, ritten met bonuspunten, TCO en verbruikskosten, foto-upload, AI-component (`GeminiClient` achter `CostEstimator`).

Eisen waar wijzigingen aan de backend aan moeten blijven voldoen:
- ≥ 3 unit tests **per student** (domeinlogica, happy én edge/error cases) + integratietests.
- Tests voor alle 4 CRUD-soorten (POST/GET/PATCH/DELETE).
- Consistente foutafhandeling via eigen exceptions en `StatusPages`.

## Periode 2 – Android-app (huidig)

### De app moet
- In Kotlin met **Jetpack Compose** (Android-only of KMP/CMP mag) en de Ktor API uit periode 1 gebruiken.
- **Minimaal 2 sensoren** gebruiken. Gepland: camera (foto's van auto's), GPS (locatie, afstand, route). Optioneel: accelerometer (rijgedrag → bonuspunten).
- Een **tweetalige UI** hebben (NL + EN): alle teksten in string resources, niets hardcoded.
- **Minimaal 3 schermen** met navigatie (Navigation-3).
- **MVVM** volgen: ViewModel met `StateFlow`, UDF (state omlaag, events omhoog), state hoisting.
- Per scherm dat data laadt: **loading-, error-, empty- en success-state** (sealed `UiState`).
- Een **complete end-to-end gebruikersflow** aantoonbaar laten werken.
- Gebouwd zijn volgens de lessen (`slides-p2.md`, `voorbeelden.md`): Ktor Client, Koin voor DI.

### Functionaliteit volgens de casus
- Kiezen tussen auto aanbieden en auto huren.
- Aanbieden: autogegevens (via kenteken/RDW), verhuurvoorwaarden, foto's met de camera.
- Eigenaar: TCO en verbruikskosten per km bekijken.
- Huren: filteren op afstand en tarief, resultaten in een lijst of op een kaart, auto + tijdsblok kiezen en reserveren.
- Route van de huidige locatie naar de auto.
- Rit: afstand en rijgedrag bijhouden, bonuspunten tonen.

### Testen
- ≥ 3 **unit tests** voor verschillende functies/use cases (bv. ViewModels met een fake repository).
- ≥ 3 **UI-tests** voor verschillende UI-interacties (kritieke paden, bv. inloggen → zoeken → reserveren).
- In de demo worden unit-, UI- én integratietests (backend) getoond.

### Design patterns (individueel)
- Iedere student **2 design patterns**, elk met klassendiagram + sequence diagram.
- **Minstens 1 per student zelf uitgewerkt in eigen code.** Het andere mag herkend worden in een library.
- Zelf uitwerken, kandidaten: Repository, Adapter/Mapper (DTO ↔ UI-model), State (sealed `UiState`), Strategy, Factory, Observer (eigen `StateFlow`).
- Herkend in libraries, kandidaten: DI (Koin), Observer (Flow), Builder (`HttpClient { }`), Decorator (`Modifier`), Composite (Compose UI-tree).
- Werk zo verdelen dat ieders pattern in de eigen code zit.

### Opleveren (veilige combinatie van alle Brightspace-bronnen)
- **Ontwikkeldocument** (uitbreiding van dat van P1, Word/pdf):
  - use case diagram, wireframes/mockups, klassendiagram, packagediagram;
  - toelichting architectuur en realisatie (Ktor + Compose);
  - per student een eigen sectie met 2 design patterns (klassen- + sequence diagram);
  - toelichting op het testen;
  - AI-gebruik bij **ontwerp**, **realisatie** en **tests/testdata**, met concrete voorbeelden;
  - overzicht werkzaamheden per student, installatie- en gebruiksinstructies, highlights en lowlights;
  - aantoonbaar ingerichte werk- en samenwerkomgeving (git).
- **Filmpje:** demo van de app door alle beschreven use cases + taakverdeling.
- **Code + tests** (archive / GitHub-repo).

### Assessment (45 min per groep)
- ~10 min demo: app + API, gebruikte sensoren, tests.
- ~5 min architectuur aan de hand van het ontwikkeldocument.
- ~30 min individuele vragen: Compose, keuze architectuur en libraries, interessante code, testen, AI-gebruik (selectief en verantwoord?), design patterns.
- Praktisch: backend draait lokaal (emulator → `10.0.2.2`, telefoon → IP van de laptop). Sensoren live tonen, dus liefst een echte telefoon.

## Bewuste afwijkingen (verantwoorden in het document)
- Reserveren met vrije `startTime`/`endTime` in plaats van vaste dagdelen, zonder endpoint voor beschikbare tijdsblokken.

## Open vragen voor de docent
- Is de `GeminiClient` (één LLM-call) voldoende als "AI-agent", of wordt een echte agent (Koog) verwacht?
- Mogen foutmeldingen van de API Nederlands zijn, of moet alles in de code Engels zijn (met vertaling in de app)? Testnamen en logberichten zijn nu ook Nederlands.
- Is logging zonder request-logging (`CallLogging`) voldoende?
- Telt een ontwerpprincipe (bv. SOLID) mee als een van de 2 design patterns?
