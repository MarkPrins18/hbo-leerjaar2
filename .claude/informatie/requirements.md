# Requirements – module AI-powered Software Development

Eisen en module-informatie uit Brightspace. Per onderdeel:
- **Brightspace** – samenvatting van wat er letterlijk staat.
- **Wat betekent dit voor ons** – onze interpretatie voor dit project. Twijfel → markeren met ❓ en navragen bij de docent.

## Inhoud
- [Over de module](#over-de-module)
- [LU 1 – AI-powered backend development (P1)](#lu-1--ai-powered-backend-development-p1)
- [LU 2 – AI-powered frontend development (P2)](#lu-2--ai-powered-frontend-development-p2)
- [Proftaak – portfolio-onderdelen](#proftaak--portfolio-onderdelen)
- [Eisen aan de applicatie](#eisen-aan-de-applicatie)
- [Checklist oplevering portfolio](#checklist-oplevering-portfolio)
- [Voorbeeldcasus – Rent my car](#voorbeeldcasus--rent-my-car)
- [Assessment periode 1 (afgerond)](#assessment-periode-1-afgerond)
- [Assessment periode 2 – full stack applicatie](#assessment-periode-2--full-stack-applicatie)
- [Achtergrond: module-aanpassing 2026-2027](#achtergrond-module-aanpassing-2026-2027)

---

## Over de module

### Brightspace
**Doel:** moderne full stack applicaties ontwikkelen met native development: backend REST services + Android-app (iOS optioneel).

**AI op twee vlakken:**
1. AI-tooling tijdens het hele ontwikkelproces (ontwerp t/m testen) – de developer blijft in de lead.
2. Een AI-agent ín de app die complexe functionaliteit oplost.

**Verder:**
- Device-sensoren gebruiken in de app, zoals camera en GPS.
- Kotlin als multi-paradigma taal: OO, functioneel en concurrent programming.
- Kotlin voor server side (Spring Boot, Ktor), Android (sinds Google I/O 2026 "Compose First" → Kotlin only) en Kotlin Multiplatform.
- Solide, volwaardig functionele Android-apps met backend services, met design principes en design patterns.

**Periode 1 – OO en FP in Kotlin (backend met Ktor)**
- OO, asynchrone en enkele FP-concepten bestuderen.
- Daarmee volgens een nette architectuur een REST API ontwikkelen met **Ktor**.
- Optioneel: sockets of gRPC in de Ktor-backend.
- Optioneel: AI-agent met **Koog** of **Android AppFunctions** voor een deel van de functionaliteit.

**Periode 2 – App development met Compose UI**
- Android-app in Kotlin met **Jetpack Compose**.
- De app gebruikt de API uit periode 1.

**Vervolg – module Multiplatform Software Development**
- Scrum-project met .NET MAUI; als alternatieve stack mag ook **KMP + Compose Multiplatform**.
- Deze module is de opvolger van "Full stack met Android" (t/m 2025-26).

### Wat betekent dit voor ons
- **Backend:** Ktor REST API met nette gelaagdheid (bij ons: Route → Service → Repository). Spring Boot wordt alleen genoemd, niet gebruikt.
- **Asynchroon:** wordt expliciet genoemd naast OO en FP → coroutines/`suspend` in de backend zijn relevant om te kunnen aanwijzen (bij ons: `suspend` repository-functies, `suspendTransaction`).
- **Sensoren:** camera en GPS als voorbeelden; volgens les 2 (P1) moet de app **2 sensoren/actuatoren** gebruiken. Camera + GPS ligt voor de hand voor een autodeelplatform (foto's van auto's, locatie van auto's).
- **AI-agent in de app:** de inleiding zegt dat AI-agent "centraal staat", maar bij periode 1 staat het als **optioneel**. ❓ Navragen of een AI-agent verplicht is of een extra.
- **AI-tooling:** verantwoorden in het ontwikkeldocument hoe AI is gebruikt (zie `slides-p1.md`, les 1); iedereen moet zijn eigen code kunnen uitleggen.
- **iOS / KMP:** optioneel. KMP is wel interessant als we de stack willen hergebruiken in de vervolgmodule (Scrum-project).
- **Sockets/gRPC:** optioneel; SSE is in les 6 behandeld als alternatief voor live updates.

---

## LU 1 – AI-powered backend development (P1)

### Brightspace
Zelfstandig een backend (Web API) ontwerpen, realiseren, testen en opleveren voor een native app; voldoet aan functionele en niet-functionele eisen, veilig, betrouwbaar en onderhoudbaar.

1. **Domeinmodel & logica** – onderbouwd OO toepassen (encapsulatie, compositie, polymorfisme, SOLID waar passend) én doelgericht FP-concepten.
2. **API & architectuur** – endpoints met duidelijke contracts (request/response, statuscodes, validatie); verantwoordelijkheden scheiden (routing/controller, service/use-case, domein, persistence); consistente foutafhandeling, logging/observability, configuratiebeheer.
3. **AI-agent** – een afgebakend deel van de backend-functionaliteit met een AI-agent; helder data- en API-contract; AI-component past in de architectuur, **gescheiden van HTTP-laag en domeinlogica**.
4. **Testen** – teststrategie en dekking onderbouwen; unit tests (domein/use-cases), integratietests (persistence en HTTP-laag), contract-/API-tests voor kritieke flows; reproduceerbaar (testdata, mocks/fakes, testcontainers); testresultaten → verbeteringen.
5. **AI-tooling** – doelgericht inzetten bij analyse, ontwerp, implementatie, testen; AI-output kritisch beoordelen (correctheid, veiligheid, performance, onderhoudbaarheid) en onderbouwen wat je wel/niet overneemt.
6. **Professionaliteit** – codekwaliteit, traceerbaarheid, bronvermelding, vertrouwelijkheid projectinformatie; eigen leerdoelen; keuzes, trade-offs, beperkingen en vervolgstappen documenteren.
7. **Beroepsproducten** (voorbeelden): API-specificatie, architectuurschets, testplan + testrapport, deploy-/run-instructies.

Gedrag: analyseren • ontwerpen • realiseren • testen • valideren • verantwoorden • verdiepen.

### Wat betekent dit voor ons
Status op basis van een snelle blik in de code (19-11-2026); ✅ = aanwezig, ⚠️ = aandachtspunt, ❓ = navragen.

| # | Eis | Bij ons |
|---|---|---|
| 1 | OO + FP onderbouwd | ✅ in de code (interfaces voor repositories/clients, data classes, collection-functies). ⚠️ "Onderbouwd" = je moet in het ontwikkeldocument kunnen aanwijzen *waar* en *waarom* (zie assessment-vragen in `slides-p1.md`). |
| 2 | Contracts, statuscodes, validatie | ✅ `openapi/documentation.yaml`, `CarValidation`/`UserValidation`, DTO's/responses. |
| 2 | Lagen scheiden | ✅ Route → Service → Repository; `dto`, `models`, `mappers`, `clients`, `config`. |
| 2 | Consistente foutafhandeling | ✅ eigen exceptions in `services/Exception.kt` + `StatusPages.kt`. |
| 2 | Logging / observability | ⚠️ `logback.xml` + logging bij DB-config en in `StatusPages`. Geen request-logging (`CallLogging`) gevonden. ❓ Is dat voldoende of verwacht de docent meer? |
| 2 | Configuratiebeheer | ✅ `application.yaml` + `config/*Config.kt` (DB, JWT, RDW, Gemini, foto's). |
| 3 | AI-agent | ✅ `GeminiClient` achter interface `CostEstimator` in `clients/` → los van routes en domein. ❓ Telt één LLM-call als "AI-agent", of wordt een echt agent-framework (Koog: tools, meerdere stappen) verwacht? Bij "Over de module" stond AI-agent nog als *optioneel*; hier staat het als eis. Volgens de module-aanpassing is Koog expliciet optioneel ("zonder dat dit de basisdoelen vervangt"), dus een eigen LLM-koppeling lijkt toegestaan. |
| 4 | Unit / integratie / API-tests | ✅ `server/test/` met e2e-ondersteuning (`E2eSupport.kt`) en CI (`.github/workflows/test.yml`). ⚠️ Teststrategie en dekking moeten **onderbouwd** worden in een testplan/testrapport; unit tests voor services los van de DB (fakes) controleren. Zie `.claude/testing.md`. |
| 5 | AI-tooling verantwoorden | ⚠️ Documenteren in het ontwikkeldocument: waarvoor AI is gebruikt, wat is overgenomen/afgewezen en waarom. Iedereen moet eigen code kunnen uitleggen. |
| 6 | Vertrouwelijkheid | ⚠️ API-keys (Gemini) niet in git; bronvermelding van AI-gegenereerde code. |
| 7 | Beroepsproducten | ✅ API-spec (OpenAPI). ⚠️ Architectuurschets, testplan, testrapport, deploy-/run-instructies (README?) controleren. |

**Onduidelijk in de tekst:**
- "Zelfstandig" terwijl het een groepsproject is → waarschijnlijk: iedereen moet zijn eigen deel kunnen verantwoorden (sluit aan bij "in control" uit les 1). ❓
- "Contract-/API-tests" naast integratietests voor de HTTP-laag: onderscheid is vaag. Pragmatisch: tests die request/response en statuscodes van kritieke flows (inloggen, auto toevoegen, reserveren) vastleggen tegen de OpenAPI-spec.
- "Testcontainers waar passend" → bij ons niet nodig: H2 wordt per start gereset en geseed, dat is al reproduceerbaar.

---

## LU 2 – AI-powered frontend development (P2)

### Brightspace
Voor een realistische casus een gebruikersgerichte front-end voor een mobiele app realiseren en testen: interactieve UI op basis van heldere requirements en een onderbouwd UX-proces, binnen een gekozen cross- of native UI-technologie.

1. **UX/UI-ontwerp** – doelgroepen en gebruiksscenario's analyseren; user flows en wireframes/prototypes; iteratief verbeteren op basis van feedback; toegankelijkheid en usability waar passend.
2. **Backend-integratie** – veilig en betrouwbaar data ophalen/wegschrijven via de Web API; responses verwerken in domein- en state-model; foutafhandeling en gebruikersfeedback (**loading-, error- en empty-states**); een complete **end-to-end gebruikersflow** werkt aantoonbaar.
3. **Architectuur (MVVM of MVI)** – state, events en side-effects expliciet modelleren; UI en domeinlogica scheiden; onderhoudbare, testbare codebase met consistente structuur, herbruikbare componenten en afspraken (state hoisting, single source of truth, navigatiepatronen).
4. **Testen** – geautomatiseerde unit- en UI-tests; kritieke onderdelen en gebruikerspaden testbaar maken; UI- en (waar relevant) integratietests; resultaten → verbeteringen.

Gedrag: analyseren • ontwerpen • realiseren • testen • valideren • verantwoorden • verdiepen.

### Wat betekent dit voor ons
- **UX-proces:** wireframes staan al in `diagrams/wireframes/` (PlantUML). ⚠️ Onderbouwen met doelgroep/scenario's, user flows, en vastleggen welke feedback tot welke iteratie leidde.
- **Toegankelijkheid:** in Compose o.a. `contentDescription` bij iconen/afbeeldingen, `sp` voor tekst, voldoende contrast (zie `slides-p2.md`, les 8).
- **Backend-integratie:** Ktor Client (les 12). Elk scherm dat data laadt heeft een `UiState` met loading / success / error / empty, bv. een sealed interface (zie `slides-p1.md`, les 2, en `slides-p2.md`, les 9).
- **Architectuur:** MVVM ligt het meest voor de hand (ViewModel met `StateFlow`, zie `voorbeelden.md` → `IncidentsViewModel`). Events omhoog via callbacks, state omlaag (UDF). Navigatie met Navigation-3 (les 10). DI met Koin.
- **Testen:** unit tests voor ViewModels (met fake repository/API), UI-tests voor kritieke paden (bv. inloggen → auto zoeken → reserveren).
- **Cross- of native:** keuze tussen Android-only (Jetpack Compose) en KMP/CMP mag; KMP sluit aan op de vervolgmodule.
- **AI in LU 2:** de titel zegt "AI-powered", maar de tekst noemt geen AI-eis voor de app. ❓ Waarschijnlijk gaat het om AI-tooling tijdens ontwikkeling (verantwoorden zoals bij LU 1).

---

## Proftaak – portfolio-onderdelen

### Brightspace
Profgroep van richtlijn 4 personen. Het portfolio heeft twee delen.

**Deel 1 (P1) – OO/FP in Kotlin + Ktor backend**

| Onderdeel | Wie | Inhoud |
|---|---|---|
| Kotlin/Ktor-document | individueel | Hyperskill: afgeronde projecten + knowledge map; optioneel andere zelfstudie-opdrachten |
| Ontwikkeldocument Ktor backend | profgroep | ontwerp met **relevante UML-diagrammen + toelichting**; **motivatie van beslissingen**; **hoe AI-tooling is ingezet en welke best practices** zijn geleerd |
| Filmpje | profgroep | 1) beoogde full stack app + benodigde backend-functionaliteit kort beschrijven; 2) backend demonstreren door de gedocumenteerde use cases; 3) toelichten hoe AI-tooling is ingezet (leerproces én ontwikkelproces) |
| Link naar GitHub-repo | profgroep | – |

**Deel 2 (P2) – Android-app die de Web API uit deel 1 gebruikt**

| Onderdeel | Wie | Inhoud |
|---|---|---|
| Ontwikkeldocument Android-app | profgroep | Word/pdf met **alle ontwerpen + toelichting**; **overzicht van werkzaamheden per student** |
| Eigen-aandeel-document | **individueel** | Word: je eigen aandeel in de app + de **toegepaste design patterns** toelichten. Iedereen moet voldoende bijdragen. |
| Filmpje | profgroep | app demonstreren aan de hand van **alle** beschreven use cases |
| Archive | profgroep | realisatie: code + tests |

Minimum-eisen: meer opleveren mag, minder niet. Bij twijfel: vragen. Bij dubbelzinnigheid: kies wat voor jullie applicatie het meest logisch is.

### Wat betekent dit voor ons
- **Groepsgrootte:** hier staat richtlijn 4, maar bij "Eisen aan de applicatie" staat **drietallen** → met 3 personen zitten we goed.
- **Verschillen met de slides (les 2, P1):** in de slides moet het filmpje ook per student de Hyperskill knowledge map/gems laten zien en de git-omgeving tonen + API in IntelliJ openen; volgens de slides is het Hyperskill-document een "Word of pdf met uitwerkingen". Deze tekst noemt dat niet in het filmpje, maar wel **AI-tooling toelichten**. Veilig: beide doen.
- **Use cases zijn de rode draad:** ze moeten gedocumenteerd zijn in het ontwikkeldocument en precies zo in het filmpje doorlopen worden (P1 voor de backend, P2 voor de app).
- **UML in deel 1:** "relevante" diagrammen, bv. klassendiagram (domein + lagen), use-case-diagram, sequentiediagram voor een kritieke flow (reserveren), ERD/datamodel. De PlantUML-bestanden in `diagrams/` zijn hiervoor bruikbaar.
- **Beslissingen motiveren:** `.claude/decisions.md` bevat al ontwerpkeuzes die als basis kunnen dienen.
- **Deel 2 individueel:** design patterns per persoon toelichten → bij het verdelen van het werk erop letten dat iedereen een herkenbaar deel heeft met eigen patterns (bv. MVVM/ViewModel, Repository, Observer via `StateFlow`, Factory/DI met Koin, Adapter/Mapper voor DTO's).
- **Werkzaamheden per student:** git-historie helpt, maar houd ook zelf bij wie wat doet.

---

## Eisen aan de applicatie

### Brightspace
Generiek geformuleerd; uitwerken in **drietallen**, met de voorbeeldcasus "Rent my car" of een eigen casus (laten beoordelen door de docent).

**Niet-functionele eisen**
1. Deel 1: Web API in Kotlin met **Ktor** (Spring Boot bespreekbaar).
2. Deel 2: Android-app in Kotlin met **Jetpack Compose** die deel 1 gebruikt.
3. Internationaal: **documentatie en code volledig in het Engels**; GUI minimaal **tweetalig (NL + EN)**.
4. Minimaal **2 sensoren** (microfoon, camera, accelerometer, GPS, thermometer, …) naast touchscreen en toetsenbord. Onderwerp eerst laten beoordelen.
5. Consistente codestijl: Kotlin/Android-conventies; **zinvolle packagenamen** die aansluiten op design patterns/frameworks/architectuur; zinnig gebruik van OO.
6. Onderbouwd gebruik van ontwerpprincipes, architectuur, frameworks en/of design patterns.
7. De applicatie draait op je eigen computer.
8. Deel 1: minstens **3 unit tests + 3 integratietests**.
9. Deel 2: minstens **3 unit tests + 3 UI-tests**.

**Functionele eisen** ("foobar" = het centrale domeinobject)
1. Minstens **3 soortgelijke klassen** (foobars) beheren, met **gedeelde functionaliteit**.
2. Aanmaken.
3. Meerdere tegelijk bekijken (collectie).
4. Zoeken of filteren in collecties.
5. Verwijderen.
6. Lezen/schrijven van/naar een database (in-memory mag); bij het assessment **realistische data** aanwezig.
7. App-UI: navigatiestructuur met minimaal **3 schermen**.

### Wat betekent dit voor ons
Status op basis van een snelle blik in de code (19-11-2026).

| Eis | Bij ons |
|---|---|
| Ktor | ✅ |
| 3 soortgelijke klassen + gedeelde functionaliteit | ✅ `sealed class Car` met `ICECar`, `BEVCar`, `FCEVCar` (gedeelde abstracte properties; verbruik/kosten per subtype). |
| Aanmaken / collectie / verwijderen | ✅ auto's toevoegen (o.a. via RDW-import), lijst, `delete("/{id}")` in `CarRouting.kt`. |
| Zoeken/filteren | ✅ `CarFilter` (`services/CarFilterMatching.kt`): afstand en prijs. |
| Database + realistische data | ✅ H2 (file, `MODE=MySQL`), bij elke start gereset en geseed uit `resources/data/seed/`. ⚠️ Voor het assessment controleren dat de seed realistisch en compleet is. |
| 3 unit + 3 integratietests (deel 1) | ✅ ruim, o.a. `BonusPointsTest`, `FuelCostTest`, `TcoTest` (unit) en `CarTest`, `ReservationTest`, `PhotoUploadTest` (HTTP/integratie). |
| Code en documentatie in het Engels | ⚠️ Testnamen zijn Nederlands (bv. `` `rustige rit krijgt 1 punt per volle kilometer` ``), logberichten ook (`DatabaseConfig`, `DatabaseSeeder`). Foutmeldingen naar de client zijn bewust Nederlands (projectafspraak) → ❓ botst dit met "volledig Engels"? De app moet die meldingen in elk geval tweetalig kunnen tonen. |
| Packagenamen | ✅ `routes`, `services`, `repositories`, `models`, `dto`, `mappers`, `clients`, `config`, `responses` → sluiten aan op de gelaagde architectuur. |
| 2 sensoren | ⏳ deel 2. Voor de hand liggend: **camera** (foto's van auto's; upload-endpoint bestaat al) en **GPS** (locatie/afstand; `Location` + afstandsfilter bestaan al). De **accelerometer** past ook: `BonusPoints` rekent met acceleratie/deceleratie van een rit. |
| Tweetalige GUI | ⏳ deel 2: alle teksten in string resources (`values/` + `values-nl/`, of CMP `Res.string`), niets hardcoded. |
| ≥ 3 schermen + navigatie | ⏳ deel 2: wireframes in `diagrams/wireframes/` hebben er ruim meer; Navigation-3 (les 10). |
| 3 unit + 3 UI-tests (deel 2) | ⏳ deel 2. |
| Draait lokaal | ✅ `./kotlin run` (backend); app op emulator/eigen device. |

**Onduidelijk:**
- "Documentatie en code volledig in het Engels" vs. onze afspraak dat foutmeldingen naar de client Nederlands zijn → bij twijfel Engels in de code, en vertaling in de app. ❓ Kort afstemmen met het team en eventueel de docent.

---

## Checklist oplevering portfolio

Deel 2 is een **uitbreiding** van deel 1 (zelfde ontwikkeldocument, aangevuld).

### Brightspace – deel 1

**Individueel: kennis en kunde OO en FP in Kotlin**
- Aantonen met een Excel-matrix: horizontaal je projecten, verticaal per concept 0/1/2 (niet / wel / uitgebreid toegepast). Mag binnen één uitgewerkte casus (klassendiagram → Kotlin).
- Concepten: zie de volledige lijst in `slides-p1.md` → *FSA individueel portfolio (checklist)*; die is identiek (basis, OO, FP incl. lambda with receiver en scope functions).

**Ontwikkeldocument Web API (profgroep)**
1. **User stories** die in de app (deel 2) worden uitgewerkt.
2. **Documentatie van de Ktor API.**
3. **Use case diagram** met de architecturally relevant use cases.
4. **Klassendiagram** van eigen klassen, met zichtbare abstractie (overerving, interfaces, abstracte klassen).
5. **Packagediagram**: logische groepering van (abstracte) klassen en interfaces.
6. Minimaal **3 sequence diagrams** van objectinteractie.
7. **Testrapportage** van alle relevante API-requests.
8. **Bijlage productverantwoording:**
   - installatie- en gebruiksinstructies;
   - taakverdeling;
   - highlights en lowlights;
   - **AI-verantwoording**: wanneer generatieve/analytische AI gebruikt en hoe; hoe geverifieerd (voorbeelden van correcte uitkomsten én van uitkomsten die flink getweakt moesten worden); **welke modellen en waarom**; lessons learned; advies over AI in dit soort projecten.
- Advies van de docent: ook al beschrijven hoe de app eruitziet (navigatiestructuur, schermontwerpen bv. in Figma) en welke API-requests de app per use case doet (request-flow).

**Filmpje (profgroep)**
- Hoe Git is ingezet om samen te werken.
- Individuele bijdrage per student.
- Demo van de Web API door de use cases uit het ontwikkeldocument.

**Realisatie: functioneel geteste, werkende Ktor Web API**
- Broncode Engelstalig; Kotlin/Ktor-naamconventies; zinvolle packagenamen.
- **"Precies voldoende" KDoc/commentaar**: alleen met toegevoegde waarde (tip: Dokka).
- Ktor + persistence (Exposed, SqlDelight, …).
- Zinvolle testdata zodat de docent zelf kan testen.
- Unit tests voor domeinlogica, divers; **iedere student minimaal 3** zinvolle, diverse unit tests; happy flow én edge/error cases.
- Tests voor **alle 4 soorten CRUD-requests**.

### Brightspace – deel 2

**Ontwikkeldocument (aanvulling)**
1. Use case diagram (architecturally relevant use cases).
2. Wireframes en/of mockups.
3. Klassendiagram + toelichting op de gebruikte design patterns.
4. **Per student minimaal 2 design patterns** in de app, gemotiveerd en geïllustreerd met **klassendiagram én sequence diagram**.
5. Packagediagram.
6. Unit tests voor minimaal 3 verschillende functies/use cases.
7. UI-tests (Espresso of ander framework) voor minimaal 3 verschillende UI-interacties.
8. Relevante testdata zodat de docent zelf kan testen.
9. Aantoonbaar ingerichte werkomgeving + samenwerkomgeving.
10. Installatie- en gebruiksinstructies.
11. Highlights en lowlights.

**Realisatie Android-app met Compose**
- Functioneel getest en werkend, gebruikt de Web API van deel 1 en minimaal 2 sensoren.
- Broncode Engels; UI minstens tweetalig (waarvan één Engels).
- Kotlin-naamconventies; zinvolle packagenamen; "precies voldoende" KDoc/commentaar.
- Gebouwd volgens de technieken uit de lessen; **MAD** met Jetpack Compose.
- Testbaar door docenten met aangeleverde testdata.

### Wat betekent dit voor ons
Status deel 1 op basis van een snelle blik in de repo (19-11-2026).

| Onderdeel | Bij ons |
|---|---|
| API-documentatie | ✅ `server/resources/openapi/documentation.yaml` |
| Klassendiagram | ✅ `diagrams/classes/Architecture.puml`, datamodel in `diagrams/models/CarDataModel.puml`. ⚠️ Controleren dat abstractie (sealed `Car`, repository-interfaces, `CostEstimator`) zichtbaar is. |
| ≥ 3 sequence diagrams | ✅ 6 stuks in `diagrams/sequence/` (RegisterUser, SearchCars, ReserveCar, SetRentalTerms, StartRide, FinishRide). |
| Use case diagram | ⚠️ Niet gevonden in `diagrams/`. |
| Packagediagram | ⚠️ Niet gevonden in `diagrams/`. |
| User stories | ❓ Niet in de repo; mogelijk in het ontwikkeldocument. |
| Testrapportage alle API-requests | ⚠️ Tests bestaan; een **rapport** (welke requests, verwacht/werkelijk resultaat) moet nog gemaakt worden. |
| CRUD-tests | ✅ Routes gebruiken POST/GET/PATCH/DELETE en alle vier worden getest in `server/test/`. |
| ≥ 3 unit tests **per student** | ⚠️ Totaal ruim voldoende; per student nagaan wie welke tests heeft geschreven (git blame) en zorgen dat iedereen er 3 heeft die **unit** tests zijn (domeinlogica, niet HTTP). |
| Happy + edge/error cases | ✅ bv. `BonusPointsTest` test drempelwaarden. Bij nieuwe tests blijven doen. |
| KDoc "precies voldoende" | ✅ Sluit aan op onze afspraak "weinig commentaar"; wel KDoc waar het waarde heeft (zie `models/Car.kt`). |
| Engelse broncode | ⚠️ Testnamen en logberichten zijn Nederlands (zie *Eisen aan de applicatie*). |
| Testdata | ✅ seed-SQL in `server/resources/data/seed/`. |
| Productverantwoording + AI-verantwoording | ⚠️ Document, niet in de repo. Voor de AI-verantwoording: bijhouden welke modellen (bv. Claude, Gemini) waarvoor zijn gebruikt, plus concrete voorbeelden van goede en flink aangepaste output. |
| Installatie-instructies | ❓ `README.md` controleren (`./kotlin run`, API-keys zoals Gemini instellen, testaccounts uit de seed). |

**Deel 2 – aandachtspunten om nu al rekening mee te houden**
- **2 design patterns per student** (dus ≥ 6 in totaal, met klassendiagram + sequence diagram per pattern). Werk zo verdelen dat iedereen een eigen, herkenbaar deel heeft. Kandidaten: MVVM, Repository, Observer (`StateFlow`), Dependency Injection (Koin), Adapter/Mapper (DTO ↔ UI-model), State (sealed `UiState`), Factory, Strategy.
- **Wireframes** bestaan al in `diagrams/wireframes/`; de docent noemt ook Figma-mockups.
- **Request-flow per use case** (welke API-calls doet de app) kan nu al in het ontwikkeldocument van deel 1.

**Verschil met eerdere teksten:** "iedere student minimaal 3 unit tests" en "tests voor alle 4 CRUD-requests" zijn strenger/specifieker dan de algemene eis "3 unit + 3 integratietests" bij *Eisen aan de applicatie*. Deze checklist aanhouden.

---

## Voorbeeldcasus – Rent my car

Dit is de casus die wij uitwerken (Car Sharing API).

### Brightspace
Platform om autodelen te bevorderen (duurzamer vervoer). Gebruikers kunnen:
- een eigen auto aanbieden voor verhuur;
- een auto reserveren en huren voor een **dagdeel**.

Twee onderdelen: Web API (auto's en beschikbaarheid beheren) en Android-app (auto's toevoegen, zoeken en reserveren).

**Functionaliteit**
1. Gebruiker kiest: auto aanbieden of auto huren.
2. **Aanbieden:** autogegevens (merk, type, …) + verhuurvoorwaarden (prijs, ophalen/inleveren); **foto's maken met de camera** en toevoegen.
3. **Categorieën:** ICE (benzine, diesel, gas), BEV (accu), FCEV (waterstof-brandstofcel).
4. **Voor de eigenaar:** TCO berekenen op basis van een standaard aantal km per jaar; **verbruikskosten per km** (berekening verschilt per categorie en brandstoftype).
5. **Huren:** filteren op criteria (bv. afstand, tarief); resultaten in een **lijst of op de kaart**; auto selecteren + **beschikbaar tijdsblok** kiezen.
6. **(Loop)route** van de huidige locatie naar de huurauto.
7. **Tijdens het rijden:** gereden afstand en rijgedrag (acceleratie bij optrekken en afremmen) bijhouden; rustig rijden → **bonuspunten** voor de huurder.

### Wat betekent dit voor ons
Status backend op basis van een snelle blik in de code (19-11-2026).

| # | Casus | Backend | App (deel 2) |
|---|---|---|---|
| 1 | Aanbieden of huren | ✅ één gebruikersaccount (`register`/`login`, JWT); eigenaarschap via `userId` | keuzescherm/navigatie |
| 2 | Autogegevens + verhuurvoorwaarden | ✅ `CarRouting` (o.a. `POST /cars/import` via RDW op kenteken), `RentalTermsRouting` | formulieren |
| 2 | Foto's | ✅ `POST /cars/{id}/photos`, `GET /cars/{id}/photos`, `GET /photos/{id}` | **sensor: camera** |
| 3 | ICE / BEV / FCEV | ✅ `sealed class Car` → `ICECar`, `BEVCar`, `FCEVCar`; `FuelType` PETROL/DIESEL/LPG | – |
| 4 | TCO | ✅ `GET /cars/{id}/tco` (`TotalCostOfOwnership`, `RoadTaxRules`, `CostEstimator`/Gemini) | scherm eigenaar |
| 4 | Verbruikskosten per km | ✅ `GET /cars/{id}/fuel-cost` (`FuelCost`) | scherm eigenaar |
| 5 | Filteren op afstand/tarief | ✅ `CarFilter` (afstand + prijs) | lijst + **kaart** |
| 5 | Tijdsblok reserveren | ✅ `ReservationRouting` met vrije `startTime`/`endTime` (`Instant`) + annuleren. ❓ Casus zegt "dagdeel" en "beschikbaar tijdsblok": wij gebruiken vrije tijden i.p.v. vaste dagdelen, en een endpoint dat beschikbare blokken teruggeeft heb ik niet gezien. Bewuste keuze? → motiveren in het ontwikkeldocument. | tijdsblok kiezen |
| 6 | (Loop)route naar de auto | ✅ `GET /cars/{carId}/maps` geeft een maps-URL terug (`LocationService`) | **sensor: GPS** (huidige locatie) |
| 7 | Rit + rijgedrag + bonuspunten | ✅ `POST /reservations/{id}/rides`, `PATCH /rides/{id}/finish` met `distanceM`, `avgAcceleration`, `avgDeceleration`; `BonusPoints` | **sensor: accelerometer** (+ GPS voor afstand) |

**Conclusie:** de backend dekt de casus vrijwel volledig. De app heeft met camera, GPS en accelerometer ruim de vereiste 2 sensoren. Enige afwijking om te verantwoorden: reserveren met vrije tijden in plaats van dagdelen, en zonder overzicht van beschikbare tijdsblokken.

---

## Assessment periode 1 (afgerond)

### Brightspace
- **Wanneer:** di 27 en do 29 oktober 2026, op locatie; **45 minuten per profgroep**.
- **Inleveren:** uiterlijk zo 25 oktober 2026, 23:59:
  - [individueel] Word/pdf met uitwerkingen Kotlin-opdrachten;
  - [profgroep] Word/pdf ontwikkeldocument: ontwerp, testen en realisatie Ktor Web API, **incl. reflectie op AI-tooling**;
  - [profgroep] filmpje (demo + toelichting taakverdeling).
- **Beoordeling op:** individueel portfolio (OO/FP in Kotlin), bijdrage aan de proftaak, toelichting en antwoorden tijdens het assessment.
- **Voorbereiding:** alles ingeleverd (anders geen assessment); afspreken wie de demo geeft en welke laptop op het smartboard gaat; opgeladen laptop, 10 min vooraf aanwezig; geen Windows/IntelliJ-updates tijdens het assessment.

**Verloop**
1. **~10 min demo:** functionaliteit kort doorlopen; REST API demonstreren via **user flows in IntelliJ HTTP-requests** (of externe tool); **unit- en integratietests** laten zien.
2. **~5 min ontwerp:** ontwerpdocument (UML) laten zien en toelichten.
3. **~30 min individuele vragen** over o.a.:
   - ontwerpkeuzes;
   - implementatie van klassen uit het ontwerp;
   - relaties tussen klassen: **overerving, associatie, aggregatie, compositie**;
   - uitleg van "interessante" methoden in de code;
   - Ktor: **routes**, **exception/error handling**;
   - persistentie: **Exposed** en de **vertaling tussen OO- en relationeel model**;
   - individueel portfolio: OO en FP in Kotlin;
   - hoe AI-tooling is toegepast en welke best practices je hebt geleerd.
4. Voorlopige feedback direct; definitief resultaat later in de week.

### Wat betekent dit voor ons
- Het assessment van P1 is geweest. Het assessment van P2 heeft dezelfde opzet, zie *Assessment periode 2*.
- **Iedereen moet elk deel kunnen uitleggen:** de vragen zijn individueel en gaan over de code. Vooral "interessante" methoden zijn handig om voor te bereiden, bijv.:
  - de vertaling OO ↔ relationeel: `sealed class Car` met subtypes ↔ tabel `car` via de `mappers/*RowMapper` (zie `.claude/decisions.md`);
  - de foutafhandeling: eigen exceptions → `StatusPages`;
  - authenticatie: `requireAuthentication { }` + `authenticatedUserId()`, eigendomscontrole in de service;
  - de berekeningen: `TotalCostOfOwnership`, `FuelCost`, `BonusPoints`.
- **Voorbeeldvragen** over Kotlin-concepten staan in `slides-p1.md` → *Assessment-voorbeeldvragen*.
- **HTTP-requests-files** voor de demo-user-flows: handig om die per use case klaar te hebben (registreren → inloggen → auto importeren → voorwaarden → zoeken → reserveren → rit).

---

## Assessment periode 2 – full stack applicatie

### Brightspace
- **Wanneer:** di 19 en 26 januari 2027, do 21 en 28 januari 2027, op locatie; **45 minuten per profgroep**.
- **Inleveren:** uiterlijk **zo 17 januari 2027, 23:59**, per profgroep:
  - **Design patterns:** iedere student **2 toegepaste design patterns**. **Eén moet zelf uitgewerkt zijn in de code**; de andere mag herkend en beschreven worden in het gebruik van methoden uit een library.
  - **[Profgroep] Ontwikkeldocument** (Word/pdf):
    1. architectuur en relevante UML-diagrammen met toelichting, **+ hoe AI-tooling is gebruikt bij architectuur/ontwerp**;
    2. toelichting op de realisatie van de full stack app (Ktor + Android/KMP met Compose), **+ hoe AI-tooling is gebruikt bij de realisatie**;
    3. **[individueel, in hetzelfde document]** per student een beschrijving van 2 toegepaste ontwerpprincipes/design patterns;
    4. toelichting op het testen, **+ hoe AI-tooling is gebruikt voor tests en testdata**.
  - **[Profgroep] Filmpje:** demo van de Android/KMP-app + toelichting taakverdeling.
- **Beoordeling op:** individueel portfolio (kennis Android/KMP, Compose UI, eigen design patterns), bijdrage aan de proftaak, antwoorden tijdens het assessment, en de opgeleverde producten (ontwikkeldocument, tests, realisatie).
- **Voorbereiding:** zoals bij P1 (alles ingeleverd, demo-laptop afspreken, 10 min vooraf, geen updates; nu ook Android Studio).

**Verloop**
1. **~10 min demo:**
   - functionaliteit in de app en het gebruik van de Ktor REST API;
   - **welke sensoren/actuatoren** zijn toegepast;
   - de tests: **unit, UI en integratie**.
2. **~5 min:** architectuur van de app bespreken (met het ontwikkeldocument).
3. **~30 min individuele vragen** over o.a.:
   - een Android-app ontwikkelen met Compose UI;
   - de keuze voor architectuur en libraries;
   - uitleg van "interessante" uitwerkingen in de app-code;
   - hoe er getest is;
   - hoe AI-tooling is gebruikt, en of dat **selectief en verantwoord** was;
   - individueel: kennis van design patterns en de toepassing daarvan in het Android-framework en in je eigen app.
4. Voorlopige feedback direct; definitief resultaat later in de week.

### Wat betekent dit voor ons
- **Design patterns per student (2):** **minstens één zelf in onze code uitgewerkt** (dus niet alleen "Koin doet DI"). Met 3 personen: minstens 3 zelf uitgewerkte patterns, plus 3 die herkend mogen worden in libraries.
  - *Zelf uitgewerkt*, kandidaten: Repository (interface + implementatie, ook in de app), Adapter/Mapper (DTO ↔ UI-model), State (sealed `UiState`), Strategy (bv. kostenberekening per autotype), Factory, Observer via eigen `StateFlow` in de ViewModel, MVVM.
  - *Herkend in libraries*, kandidaten: Dependency Injection (Koin), Observer (Flow/`collectAsState`), Builder (`Json { }`, Ktor `HttpClient { }`), Composite (Compose UI-tree / slots), Decorator (`Modifier`-chain), Singleton (`object`).
  - Werk zo verdelen dat ieders eigen pattern in **zijn eigen code** zit.
- **Verschil met de checklist** (*Checklist oplevering portfolio*, deel 2): daar staat een *apart individueel Word-document* en "per pattern klassendiagram + sequence diagram". Hier: de individuele beschrijving zit **in het ontwikkeldocument**. Veilig: in het ontwikkeldocument per student een eigen sectie, mét klassendiagram + sequence diagram per pattern.
- **"Ontwerpprincipes/design patterns":** hier lijkt ook een ontwerpprincipe (bv. SOLID) mee te tellen, maar elders staat expliciet "design patterns". ❓ Bij twijfel: echte design patterns kiezen.
- **AI-verantwoording op drie plekken:** bij ontwerp, bij realisatie, en bij tests/testdata. Verzamel tijdens het werk al voorbeelden per fase (prompt, wat AI opleverde, wat je hebt aangepast en waarom).
- **Tests:** in de demo unit-, UI- én integratietests laten zien → die moeten dus voor de app (unit + UI) en de backend (integratie) klaar en draaibaar zijn.
- **Sensoren in de demo:** camera en GPS (en eventueel accelerometer) moeten live te tonen zijn; met een emulator is dat lastig → **een echte telefoon** klaarzetten, of zorgen dat het in de emulator te simuleren is (locatie, virtuele camera).
- **Backend moet draaien tijdens de demo:** de app praat met de Ktor API → backend lokaal starten en de app laten verbinden (emulator: `10.0.2.2`; echte telefoon: IP van de laptop in hetzelfde netwerk).

---

## Achtergrond: module-aanpassing 2026-2027

### Brightspace
- "Full stack met Android" is hernoemd naar **AI-powered software development**. De oude derde leeruitkomst (werkplek, ICT en duurzaamheid) is vervallen of verplaatst naar de tweede module van het jaar.
- Reden: AI-tooling (Claude Code, Gemini in Android Studio, JetBrains AI Chat/Junie) is onderdeel van het ontwikkelproces geworden, en AI-agents realiseren delen van functionaliteit. Kotlin Multiplatform wordt steeds belangrijker, dus "Android" in de naam is niet meer actueel.
- Doel blijft: full stack Android- of KMP-app met backend API en Compose UI.
- Meer nadruk op **AI als ontwikkelpartner**. Te leren vaardigheden:
  - goede prompts formuleren;
  - AI-output beoordelen;
  - gegenereerde code begrijpen;
  - fouten herkennen en verbeteren;
  - AI gebruiken voor refactoring, documentatie en testen.
- AI ondersteunt, maar de student leert zelf de kennis om een betrouwbare en onderhoudbare applicatie te bouwen.
- **Koog is optioneel**: ruimte om te experimenteren met AI-agents of AI-ondersteunde backend-componenten, *zonder dat dit de basisdoelen vervangt*.
- Eerder in de module starten met API-begrippen en een korte toepassing in de app.

### Wat betekent dit voor ons
- **AI-verantwoording in het ontwikkeldocument:** de vijf vaardigheden hierboven zijn een bruikbare indeling. Per vaardigheid een concreet voorbeeld uit ons project geven, bv. een prompt die werkte, AI-code die we hebben afgewezen of aangepast (en waarom), en refactoring, documentatie of tests die met AI zijn gemaakt.
- **AI-agent (LU 1):** Koog is optioneel. Onze `GeminiClient` achter `CostEstimator` lijkt daarom voldoende als "AI-component", mits goed afgebakend en gedocumenteerd. ❓ Toch even bevestigen bij de docent.
- **KMP:** wordt hier als volwaardig alternatief voor Android-only genoemd.
- De oude leeruitkomst (werkplek, ICT, duurzaamheid) hoeft niet in ons portfolio.
