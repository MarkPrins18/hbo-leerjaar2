# Slides – AI-powered Software Development (AIP)

Samenvatting van de lesslides, als naslag voor "hoe doe je X in Kotlin/Ktor" en voor het assessment.
Bron: Periode 1 – `OneDrive - Safety Consulting & Technology\school portfolio` (pptx + FSA-portfolio xlsx); Periode 2 – `OneDrive - Avans Hogeschool\Code & Conquer leerjaar 2\Periode 2\Slides` (`AIP Les 8/9/10.pptx`, incl. sprekersnotities).
Codevoorbeelden en demo-repo's: zie `voorbeelden.md`.

- **Periode 1** – leeruitkomst 1: backend Web API in Kotlin met Ktor (OO + FP concepten). Inleveren uiterlijk zo 25 okt 2026.
- **Periode 2** – leeruitkomst 2: Android app in Kotlin met Jetpack Compose die de API gebruikt. Inleveren uiterlijk zo 17 jan 2027.

## Inhoud
- [Periode 1](#periode-1)
  - [Module & beoordeling](#module--beoordeling)
  - [Les 1 – Kotlin basics](#les-1--kotlin-basics)
  - [Les 2 – OO, interfaces, enum, sealed, extensions, REST](#les-2--oo-interfaces-enum-sealed-extensions-rest)
  - [Les 3 – SOLID, UML→Kotlin, unit testen, JSON, DSL](#les-3--solid-umlkotlin-unit-testen-json-dsl)
  - [Les 4 – Collections & JSON serialization](#les-4--collections--json-serialization)
  - [Les 6 – JWT, Exposed, file upload, SSE](#les-6--jwt-exposed-file-upload-sse)
  - [Les 7 – Collections (verdieping), coroutines](#les-7--collections-verdieping-coroutines)
  - [Assessment-voorbeeldvragen](#assessment-voorbeeldvragen)
  - [FSA individueel portfolio (checklist)](#fsa-individueel-portfolio-checklist)
- [Periode 2](#periode-2)
  - [Planning & werkwijze P2](#planning--werkwijze-p2)
  - [Les 8 – Introductie Compose](#les-8--introductie-compose)
  - [Les 9 – Lijsten, state, ViewModel, coroutines, KMP](#les-9--lijsten-state-viewmodel-coroutines-kmp)
  - [Les 10 – ViewModel + StateFlow, Navigation 3, KMP-migratie](#les-10--viewmodel--stateflow-navigation-3-kmp-migratie)

---

# Periode 1

## Module & beoordeling

**Leerdoelen P1:** goede kennis van OO/FP in Kotlin en toepassing in een Ktor API.
- Basics: datatypes, nullable types, control flow
- OO: class/object, interface, abstract class, inheritance, polymorfisme, delegation
- FP (basis): lambda's, hogere-orde functies, scope functions
- Stdlib/collections: List/Set/Map (+ Mutable), collection-functies, Comparable/Comparator
- Unit testen, gerealiseerde Ktor API, AI-tooling als ondersteuning

**Doel:** elk klassendiagram (overerving, interfaces, delegation) kunnen omzetten naar idiomatische Kotlin. Goede decompositie, geen onnodig complexe code.

**OO toolset / doelen:** encapsulation, inheritance, polymorphism, delegation, abstract classes, interfaces → cohesion, loose coupling, reuse, geen duplicatie, duidelijke verantwoordelijkheden. Principes: SOLID, KISS, high cohesion / low coupling, *favor delegation over inheritance*.

**Portfolio P1 (beoordeling lesweek 9/10):**
- **[Individueel]** Word/pdf met uitwerkingen: Hyperskill (topics, projecten, gems, Knowledge map) of alternatief Atomic Kotlin + eigen demo; eigen uitwerking workshop-opdrachten.
- **[Profgroep]** Word/pdf ontwikkeldocument + testen + realisatie Web API in Kotlin (unit- en integratietesten).
- **[Profgroep]** Filmpje 5–10 min: per student Hyperskill Knowledge map/gems/projecten; git-omgeving tonen en API openen in IntelliJ; API demonstreren door alle use cases.

**AI-tooling:** in het ontwikkeldocument verantwoorden hoe AI is gebruikt. Iedereen moet "in control" zijn over eigen code: begrijpen (ook als AI het genereerde) en kunnen uitleggen waar aanpassingen nodig zijn.

**Tooling:** Kotlin Toolchain (alternatief voor Gradle, `module.yaml`) aanbevolen. HTTP requests-files i.p.v. Postman.

**Bronnen:** kotlinlang.org/docs, Kotlin tour, typealias.com (Dave Leeds, betrouwbaarst), Hyperskill.

## Les 1 – Kotlin basics

- **Entry point:** `fun main()` top-level; `print` / `println`. Geen `;`.
- **Variabelen:** `val` = read-only, `var` = mutable; type meestal inferred; deferred assignment kan (`val c: Int; c = 3`). `const val NAME` = compile-time constante (UPPERCASE).
- **Functies:** single-expression `fun max(a: Int, b: Int) = if (a > b) a else b`; `Unit` = geen zinvolle return; default arguments `b: Int = 42`.
- **`if` is een expressie** (kan returnen).
- **`when`:**
  - *expression* (resultaat wordt gebruikt) → alle gevallen moeten gedekt zijn (`else` verplicht, tenzij exhaustive bij enum/sealed).
  - *statement* → `else` optioneel.
  - `when (x) { 1 -> … }` (met subject) vs `when { x < 0 -> … }` (condities in branches).
  - Complex: `is SpecialScore ->`, `is Number -> when { score.toDouble() in 0.0..<5.5 -> … }`.
- **Boolean:** `&&`/`||` short-circuit; `and`/`or` niet.
- **Loops:** `for (item in items)`, `items.indices`, `withIndex()`, `while`, `do…while`, labels `break@label`/`continue@label`, `repeat(n) {}`.
- **Ranges:** `1..10`, `9 downTo 0 step 3`, `0..<5` (`downTo`/`step` zijn extension functions).
- **Null safety:** `String?`; check `if (x != null)`; **Elvis** `x ?: default` (ook `?: return null` / `?: throw`); **safe call** `a?.b?.c`; `?.let { }`; vermijd `!!`.
- **`TODO()`** retourneert `Nothing` → compileert, gooit `NotImplementedError` at runtime.
- **String templates:** `"$i"`, `"${s.length}"`; `StringBuilder` / `buildString`.
- **Data class:** gratis `equals`, `hashCode`, `toString`, `copy`, `componentN`. Gebruik **kotlinx.datetime** (niet Java DateTime).
- **Lambda's:** `val isPositive: (Int) -> Boolean = { it > 0 }`; `list.filter(isPositive)`; function reference `::isAdult`, `Student::isAdult`.
- **Trailing lambda:** laatste parameter is functie → lambda buiten de haakjes: `numbers.scan(0) { acc, n -> acc + n }`; enige argument → haakjes weg.

## Les 2 – OO, interfaces, enum, sealed, extensions, REST

- **Class:** properties + member functions; primary constructor `class Person(val name: String, var isMarried: Boolean)`.
- **Constructors:** secondary constructor zelden nodig (gebruik default arguments). Volgorde: primary → `init` → secondary. Secondary moet uiteindelijk primary aanroepen (`: this(...)`). `init { require(...) }` voor checks.
- **4 OO-concepten:** Abstraction (wat anderen zien), Encapsulation (implementatie verbergen), Inheritance (hergebruik), Polymorphism (één interface, meerdere types).
- **Visibility:** `public` (default), `private` (class/file), `protected` (class + subclasses), `internal` (module).
- **Encapsulation-patronen:**
  - `var turns = 0; private set`
  - backing property: `private val _clues = mutableListOf<String>(); val clues: List<String> get() = _clues`
- **Inheritance:** "is-a"; class moet `open` zijn (abstract altijd open); 1 class + meerdere interfaces; parent-constructor aanroepen `class Car(s: Int) : Vehicle(s)`. Vaak beter: compositie.
- **Polymorfisme & LSP:** subclass mag contract van superclass niet breken; client weet niet welk subtype.
- **Abstract class vs interface:** interface heeft geen state (properties abstract), kan default-implementaties hebben; abstract class kan state hebben, geen instantie. ISP-voorbeeld: `Locatable`, `Drawable`, `MapShowable : Locatable, Drawable`.
- **Extension functions:** `fun String.isValidWord() = ...`; class verandert niet, geen toegang tot private; **static dispatch** (type van expressie, niet runtime-type). Member met zelfde signatuur wint.
- **Data class:** `equals/hashCode/toString/componentN/copy`; destructuring `val (name, age) = user`; liever named data class dan `Pair`/`Triple`.
- **Enum class:** constanten zijn objecten; kunnen properties, methods en interfaces hebben: `enum class Drink(val water: Int, …) { ESPRESSO(250, …) }`. `entries`, `ordinal`.
- **Sealed class/interface:** alle subtypes bekend at compile time (zelfde module) → exhaustive `when` zonder `else`. Sealed class is abstract.
  ```kotlin
  sealed interface UiState {
      data object Loading : UiState
      data class Success(val data: String) : UiState
      data class Error(val message: String) : UiState
  }
  ```
- **SAM / `fun interface`:** `fun interface IntPredicate { fun accept(i: Int): Boolean }` → `IntPredicate { it % 2 == 0 }`.
- **Type-hierarchie:** `Any` bovenaan, `Nothing` onderaan; elke type heeft nullable variant (`Any?` top).
- **REST:** resources, endpoints, routes. HTTP-verbs:

  | Verb | CRUD | Collection | Item |
  |---|---|---|---|
  | POST | Create | 201 + `Location` header | 404 / 409 conflict |
  | GET | Read | 200 (paginering/filter) | 200 / 404 |
  | PUT | Replace | 405 | 200/204 / 404 |
  | PATCH | Modify | 405 | 200/204 / 404 |
  | DELETE | Delete | 405 | 200 / 404 |

  Safe: GET, HEAD. Idempotent: GET, HEAD, PUT, DELETE. Niet idempotent: POST, PATCH.
- **Proftaak:** eigen casus of "Rent my car" schoolcasus; Android-app moet 2 sensoren/actuatoren gebruiken (gps, camera, trillen, …).

## Les 3 – SOLID, UML→Kotlin, unit testen, JSON, DSL

- **SOLID:**
  - **S**RP – één reden om te veranderen. Scheid data (`PureEmployee`) van persistence (`EmployeeRepository` interface + `InMemoryRepository`). Geen God objects.
  - **O**CP – open voor uitbreiding, gesloten voor wijziging. Program to an interface (`interface Shape { fun area() }`) i.p.v. `when (shape.type)`. Higher-order functions zijn OCP (`map { … }`).
  - **L**SP – subtypes vervangbaar (Vehicle/Car).
  - **I**SP – kleine interfaces (Locatable/Drawable).
  - **D**IP – afhankelijk van abstracties (repository-interface in constructor van service).
- **Lambda with receiver in service:** `change: PureEmployee.() -> PureEmployee` → `repo.findById(id)?.change()?.let { repo.save(it) }`.
- **Workshop HousingMarket:** UML → Kotlin (abstract/sealed class, interface, inheritance, polymorfisme, enum, List/MutableList). Gebruik `require`, `check`, `assert`.
- **Associaties:** aggregation (kunnen los bestaan, Student–Course) vs composition ("bestaat uit", child kan niet zonder parent).
- **MutableList als List exposen:**
  - Optie 1: `private val _bids = mutableListOf<Bid>(); val allBids: List<Bid> get() = _bids.toList()`
  - Optie 2 (Kotlin 2.4+, explicit backing field): `val allBids: List<Bid> field: MutableList<Bid> = mutableListOf()`
- **Static → `companion object`**; of **private top-level** functies/variabelen (vervuilen namespace niet). Top-level extension functions op eigen classes, bv. `fun Property.energyFactor() = when (this) { is Apartment -> … }` (exhaustive bij sealed).
- **Rekenen met nullable:** `val price = priceAsked ?: return null` en daarna gewoon met `Int` rekenen; of `price?.let { }`; of `?.times()`, `?.plus()`.
- **Tijd:** `kotlin.time.Instant`/`Clock` voor tijdstippen; `kotlinx.datetime.LocalDate` voor kalenderdatums.
- **Testen:** testpiramide; unit test = één use case per test, snel. Kotlin Toolchain gebruikt standaard `kotlin.test`: `@Test`, `@BeforeTest`, `@AfterTest`, `@BeforeClass`, `@AfterClass`. JUnit5: `@ParameterizedTest` + `@MethodSource`. IntelliJ: Ctrl-Shift-T → test aanmaken.
- **JSON / kotlinx.serialization:** in Toolchain `settings: kotlin: serialization: json`.
  ```kotlin
  @Serializable data class Student(val name: String, val yearBorn: Int)
  val json = Json { prettyPrint = true; encodeDefaults = true; ignoreUnknownKeys = true }
  json.encodeToString(students)
  json.decodeFromString<List<Student>>(text)
  ```
- **Lambda with receiver / type-safe DSL:** `T.() -> R` = anonieme extension function; basis van Ktor `routing { route("/api") { get("/users") { call.respondText(...) } } }` en Compose.
- Euro-teken niet zichtbaar → VM options `-Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8`.

## Les 4 – Collections & JSON serialization

(slides "FSA Les 4"; collections-deel is grotendeels gelijk aan les 7)

- **List** (geordend, duplicaten, index), **Set** (uniek, via `equals`), **Map** (unieke keys → values).
- Hiërarchie: `Iterable` → `Collection` → `List`/`Set`; `Mutable*` varianten; `Map`/`MutableMap` apart.
- Read-only `listOf` heeft geen `add`. **Mutable collection ≠ mutable variable** (`val` + `mutableListOf` → wel `add`, niet herassignen).
- Gebruik `isEmpty()`/`isNotEmpty()` i.p.v. `size == 0`; `indices`, `lastIndex`.
- `subList` geeft een view (referentie) terug.
- **Builders:** `emptyList()`, `listOf()`, `mutableListOf()`, `buildList { add(…) }` (zelfde voor Set → LinkedHashSet, Map → LinkedHashMap; `mapOf(1 to "one")`, `buildMap`).
- **hashCode-contract:** consistent; `a == b` ⇒ `a.hashCode() == b.hashCode()`. Override beide samen (bv. auto gelijk bij zelfde kenteken, case-insensitive).
- Map loop: `for ((key, value) in map)`; `getOrDefault`.
- JSON-intro + kotlinx.serialization (zie les 3). Gradle-variant: plugin `kotlin("plugin.serialization")` + dependency `kotlinx-serialization-json`.

## Les 6 – JWT, Exposed, file upload, SSE

- **JWT (rollen in proftaak):** car owner beheert eigen advertenties; user zoekt en reserveert. JWT = header (type + algoritme) . payload (JSON met claims: registered + custom) . signature (server verifieert dat payload niet vervalst is).
- **Exposed:** werkt met MySQL, Postgres, H2, …; tip: **H2** (in-memory of file). Slides Simon Vergauwen: nomisrev.github.io/exposed-fundamentals.
- **Static content:**
  ```kotlin
  staticFiles(remotePath = "/api/incidents/images", dir = uploadsDir, index = "incident.png") {
      default("incident.png")
  }
  ```
- **File upload (multipart):**
  ```kotlin
  post("/images") {
      call.receiveMultipart().forEachPart { part ->
          when (part) {
              is PartData.FileItem -> part.provider().copyAndClose(file.writeChannel())
              else -> {}
          }
      }
  }
  ```
  Ook `FormItem`, `BinaryItem`. Docs: ktor.io server-requests (form data) en server-testing (multipart).
- **Async server→app communicatie:**

  | | SSE | WebSockets | Push / FCM |
  |---|---|---|---|
  | Richting | server → client | bi-directioneel | via cloud → OS → app |
  | Protocol | HTTP `text/event-stream` | ws:// wss:// | Google/APNs |
  | App-status | voorgrond | voorgrond | gesloten/achtergrond |
  | Latency | ms | ms | sec–uren |
  | Complexiteit | laag | gemiddeld | gemiddeld/hoog |
  | Voorbeeld | live dashboard, AI agents | live chat | statuswijziging na uren |

- **Ktor SSE stappen:** 1) dependency in `module.yaml`; 2) SSE-plugin installeren; 3) event data class (`IncidentStreamEvent`); 4) in-memory event bus `MutableSharedFlow<Event>`; 5) SSE-endpoint; 6) mutaties broadcasten naar collectors; 7) heartbeat; 8/9) live dashboard.
- **Exposed enum-kolom:** `enumerationByName("user_type", 80, UserType::class)`.
- **JSON in een kolom:** 1) DB-specifiek JSON-type (bruikbaar in queries); 2) zelf (de)serializen naar `text` als je er niet op hoeft te queryen; 3) `ArrayColumnType` in nieuwe Exposed. Voorbeeld: `UserDetails` met `companion object { fun encodeToString(...); fun decodeFromString(...) }` (static factory methods), `.default(...)` op de kolom.
- **Planning:** gastles Urs Peter over coroutines & Flow; week van 19 okt geen les.

## Les 7 – Collections (verdieping), coroutines

- Collections-interfaces; op JVM implementaties uit `java.util`.
- **ArrayList vs LinkedList:** ArrayList in >95% de juiste keuze (get O(1); invoegen begin/midden O(n); eind amortized O(1)). LinkedList: get O(n), invoegen O(1).
- **Set/HashSet demo:** `Car` met custom `equals`/`hashCode` op kenteken.
- **`associateBy`** → `Map<K, T>` (unieke key): `cars.associateBy { it.licensePlate }`.
- **`associate`** → `Map<K, V>`: `args.chunked(2).associate { it.first() to it.last() }`.
- **`groupBy`** → `Map<K, List<V>>`: `cars.groupBy { it.brand }`; optioneel `valueTransform`.
- **Ranges/progressions:** `'a'..'c'`, `1..5`, `1L..5L`, `10 downTo 0 step 3`.
- **Sequence:** lazy, mogelijk oneindig. `asSequence()`, `sequenceOf`, `sequence { yield(1) }`, `generateSequence(1) { it + 2 }`. List verwerkt per stap de hele lijst (eager); sequence per element alle stappen en stopt zodra `take(n)` vol is; pas uitgevoerd bij terminal operation (`toList()`).
- **Handige functies:** `binarySearch`, `sort`, `groupBy`, `partition`, `chunked`, `windowed`, `drop`, `intersect`. Eerst zoeken of de stdlib het al heeft.
- **Coroutines/Flow intro:** kotlinlang.org/docs/coroutines-guide.html, coroutines-basics.

## Assessment-voorbeeldvragen

(niet uitputtend – uit les 6)
- Class vs object, en het keyword `object`?
- `val` vs `var`?
- `when`-statement vs `when`-expression?
- Hoe test je op null en geef je een default terug? (`?:`, `?.let`)
- Wat is een sealed class?
- Wanneer List, Set, Map?
- Class vs data class?
- Abstract class vs interface?
- Wat is polymorfisme, en het belang van abstract methods daarbij?
- Wanneer top-level function vs companion object?
- **Waar in jouw code** gebruik je: visibility modifiers, null safety, collections, collection-functies (filter, map, group, sort), interfaces, overerving, overloading, overriding, named parameters, generics?

## FSA individueel portfolio (checklist)

Uit `FSA individueel portfolio - David.xlsx`. Per topic score: 0 = niet toegepast, 1 = toegepast, 2 = uitgebreid toegepast. Kolommen: Parking Lot, ZooKeeper, eigen projecten.

**Basic Kotlin constructions**
1. Entry point `main()`, packages, imports
2. `val` en `var`
3. Basic types
4. Special types: `Any`, `Unit`, `Nothing`
5. Null safety: nullable vs non-nullable, safe calls `?.`, Elvis `?:`
6. Control flow
7. Type checks en (safe) casts (`is`, `as?`)
8. Collecties (List, MutableList, Set, Map) – **belangrijk om aan te tonen**

**OO concepts in Kotlin**
1. Abstractie en encapsulatie
2. Overerving
3. Abstracte klassen
4. Interfaces
5. Overloading
6. Overriding
7. Polymorfisme
8. Class, constructor(s), init-blocks
9. Equality: structural (`==`) vs referential (`===`)
10. Data class vs class
11. `this` / `it`
12. Visibility (private, public, protected)
13. Property
14. Member functions vs top-level functions
15. Default en named arguments
16. Extension functions
17. Top-level function
18. Companion object
19. Object declaration (singleton)
20. Delegated property (`by lazy`, …)
21. Generics
22. Exception handling

**FP concepts in Kotlin**
1. Function types
2. Higher-order function
3. Lambda expression syntax
4. Trailing lambda
5. Implicit parameter `it`
6. Returning value from lambda
7. Scope functions: `let`, `apply`, `also`, `run`, `with`
8. Lambda with receiver (veel gebruikt in DSL's zoals Compose)

**Other:** coroutines, regular expressions

---

# Periode 2

Android (of KMP) app met Jetpack Compose / Compose Multiplatform die de API uit periode 1 gebruikt.
Bron: `OneDrive - Avans Hogeschool\Code & Conquer leerjaar 2\Periode 2\Slides`.

## Planning & werkwijze P2

| Week | Les |
|---|---|
| 10–12 nov | Les 8 – Introductie Compose |
| 17–19 nov | Les 9 – Lijsten, material design, state & state hoisting |
| 24–26 nov | Les 10 – Navigation-3, app-architectuur, KMP-demo |
| 1–3 dec | Les 11 – Gastles Dave Leeds: state management |
| 8–10 dec | Les 12 – Ktor Client, data persistence |
| 15–17 dec | Les 13 – GPS en Maps (MapLibre) |
| 5–7 jan | Werken aan project |
| 12–14 jan | Afronden; portfolio inleveren uiterlijk **zo 17 jan 2027** |
| 19–28 jan | Assessments deel 2 |

- Les 1–2: Compose UI + state hoisting → losse screen-composables + previews maken.
- Les 3–4: navigatie, ViewModel, **Koin** (DI, i.p.v. Hilt), **Coil** (images) → navigatie + Ktor API koppelen.
- Les 5–6: (State)Flow, persistence, GPS/Maps, camera.
- Aanbevolen: Navigation-3 (type-safe), Koin (bij ons: Koin in de app `shared`, server zonder DI-library; zie `voorbeelden.md` → *Koin in de app – basis*). Tooling (Android Studio/IntelliJ, Gradle/Kotlin Toolchain, KMP of Android-only) kiest de profgroep zelf.
- Leerbronnen: *Android Basics with Compose* (course + codelabs), codelab jetpack-compose-basics, Hyperskill track 66 / knowledge-map 2560, MAD skills playlist. Officiële docs regelmatig lezen (meest actueel). Alles voor Android/Compose is 100% toepasbaar op het Android-deel van een KMP-app; Hyperskill bouwt een KMP-track.
- Tooling: Android Studio is gebaseerd op IntelliJ Community en loopt 1–2 versies achter; goed alternatief is IntelliJ met Android- en KMP-plugin.
- Leertip docent: neem de tijd voor Composable/State/Recomposition/State hoisting; werk één concreet voorbeeld goed door en switch niet te snel tussen bronnen.

Vooruitblik uit P1: coroutines, Flow, sealed `UiState`, design patterns + SOLID (individueel), unit- en UI-testen, ontwikkeldocument van P1 uitbreiden, filmpje met alle use cases.

## Les 8 – Introductie Compose

**Composable functions**
- `@Composable` = gewone Kotlin-functie die UI beschrijft op basis van input, roept andere composables aan, retourneert niets (emit UI naar de Compose runtime → UI-tree → render).
- Naamgeving: **PascalCase** (`DoneButton()`); retourneert een composable een waarde → camelCase.
- Kan stateful of stateless zijn. Niet-aangeroepen composable (bv. in false `if`) zit niet in de Composition.

```kotlin
@Composable
fun Greeting(name: String, friendly: Boolean) {
    if (friendly) Text("Hello $name, nice to see you!") else Text("Hello $name.")
}

@Composable
fun Greeting(names: List<String>) {
    Column { for (name in names) Text("Hi $name!") }   // Row = horizontaal
}
```

**Jetpack Compose vs Compose Multiplatform:** Jetpack Compose (Google) = Android; Compose Multiplatform (JetBrains) = iOS, desktop, web + Android (via Jetpack Compose). "Compose" = beide; ze groeien steeds meer naar elkaar toe.

**Layouts:** `Column` (verticaal), `Row` (horizontaal), `Box` (stapelen), `LazyColumn`/`LazyRow`/grids; verder Pager, Flow layouts, ConstraintLayout.

**Slots** – composable lambda als parameter:
```kotlin
@Composable fun Button(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit)

Button(onClick = {}) { Text("Click me!") }
Button(onClick = {}) { Icon(Icons.Default.Home, contentDescription = "Home") }
```

**Modifiers** – size, layout, gedrag, uiterlijk, accessibility, input, clickable/scrollable:
- `size(40.dp)`, `height`, `width`, `fillMaxSize()`, `fillMaxWidth()`, `padding(32.dp)`, `background(Color.Magenta)`, `border(1.dp, Color.Red)`, `clip(CircleShape)`, `alpha(0.5f)`, `rotate(90f)`, `weight(1f)` (in Row/Column), `wrapContentSize(Alignment.Center)`.
- **Volgorde telt:** `.background().padding()` ≠ `.padding().background()`; `.size(32.dp).padding(8.dp)` ≠ `.padding(8.dp).size(32.dp)`. Modifiers passen eerst de constraints aan van links naar rechts en geven daarna de grootte terug van rechts naar links.
- **Best practice:** elke composable accepteert `modifier: Modifier = Modifier` (eerste optionele parameter); de parent bepaalt hoe het kind gemeten wordt.
- Herbruikbare stijl via custom modifier:
  ```kotlin
  fun Modifier.cardStyle() = this
      .fillMaxWidth()
      .padding(16.dp)
      .background(Color.White, shape = RoundedCornerShape(8.dp))
      .shadow(4.dp)
  ```

**Eenheden:** `dp` (density-independent) voor afmetingen; `sp` (schaalt met tekstgrootte-instelling) voor tekst.

**Material components:**
```kotlin
Button(onClick = { println("Clicked!") }) { Text("Click me!") }
Icon(Icons.Default.Build, contentDescription = "Build")
var checked by remember { mutableStateOf(true) }
Switch(checked, onCheckedChange = { checked = it })
var value by remember { mutableFloatStateOf(0.5f) }
Slider(value, onValueChange = { value = it })
```

**State (Dice roller / BMI app)**
- `MutableState<T>` = observable value holder; lezen in een composable abonneert die scope; schrijven (andere waarde) → **recomposition**.
- `var result by remember { mutableStateOf(1) }` → `when (result) { 1 -> R.drawable.dice_1 … }`.
- Nadenken over bewaren bij configuration change (rotatie, taal) en achtergrond → `rememberSaveable` of ViewModel.

**State hoisting / Unidirectional Data Flow (UDF)** – *state goes down, events go up*. Vervang state door `value: T` + `onValueChange: (T) -> Unit`; hoist naar de laagste gemeenschappelijke ouder.
```kotlin
@Composable
fun EditNumberField(
    value: String,
    onValueChanged: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
    leadingIcon: (@Composable () -> Unit)? = null,   // slot
) {
    TextField(
        value = value,
        onValueChange = onValueChanged,
        singleLine = true,
        label = { Text(label) },
        leadingIcon = leadingIcon,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
        modifier = modifier,
    )
}

@Composable
fun BmiScreen(modifier: Modifier = Modifier) {
    var lengthInput by remember { mutableStateOf("170") }
    var weightInput by remember { mutableStateOf("65") }
    Column(modifier) {
        EditNumberField(value = lengthInput, onValueChanged = { lengthInput = it }, label = stringResource(R.string.height),
            leadingIcon = { Icon(AppIcons.Height, contentDescription = stringResource(R.string.length_icon_content_description)) })
        EditNumberField(value = weightInput, onValueChanged = { weightInput = it }, label = "Weight", imeAction = ImeAction.Done)
    }
}
```
- Meertaligheid via `stringResource(R.string.x)` (CMP: `Res.string.x`).

**Conventies**
- Namen: `BmiScreen` (top-level scherm, vaak met state), `BmiHeader` (beschrijvend deel), `BmiInputSection`/`BmiResultSection` (logisch deel), `MyGridLayout` (herbruikbare stateless layout).
- Parametervolgorde: verplichte params → `modifier` (één) → optionele params → composable trailing lambda.
- **Icons:** `material-icons-extended` wordt afgeraden; gebruik **Valkyrie plugin** (SVG → ImageVector, bv. `AppIcons.Height`) of Compose Multiplatform resources.

**Huiswerk voor les 9:** Android Basics with Compose t/m **Unit 2**, o.a. codelab *Calculate a custom tip* (Tip Time; code: zie `voorbeelden.md`).

## Les 9 – Lijsten, state, ViewModel, coroutines, KMP

Workshop: codelab *State in Compose* (WellnessTasks / WaterCounter).

**State-driven UI**
```kotlin
@Composable
fun WaterCounter(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        var count by remember { mutableStateOf(0) }
        if (count > 0) Text("You've had $count glasses.")   // in/uit de Composition
        Button(onClick = { count++ }, modifier = Modifier.padding(top = 8.dp)) { Text("Add one") }
    }
}
```
- `val s = remember { mutableStateOf(0) }` (gebruik `s.value`) vs `var s by remember { … }` (property delegate, direct `s`).
- `remember` → overleeft recomposition, **niet** rotatie/process recreation. `rememberSaveable` → overleeft ook configuration changes (saved instance state).

**Stateless variant (hoisted):**
```kotlin
@Composable
fun StatelessCounter(count: Int, onIncrement: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        if (count > 0) Text("You've had $count glasses.")
        Button(onClick = onIncrement, enabled = count < 10) { Text("Add one") }
    }
}
```

**Expand/collapse-voorbeeld:** `var expanded by remember { mutableStateOf(false) }`, `Surface(color = MaterialTheme.colorScheme.primary)`, `Row { Column(Modifier.weight(1f)) { … }; ElevatedButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Show less" else "Show more") } }`, `Spacer(Modifier.height(20.dp))`.

**Lijsten – LazyColumn met stabiele key** (vervangt RecyclerView):
```kotlin
LazyColumn(state = state, modifier = Modifier.fillMaxSize()) {
    items(items = list, key = { task -> task.id }) { task ->
        WellnessTaskItem(
            taskName = task.label,
            checked = task.checked,
            onCheckedChange = { checked -> onCheckedTask(task, checked) },
            onClose = { onCloseTask(task) },
        )
    }
}
```
- Observable lijst: `mutableStateListOf()` / `toMutableStateList()` (gewone `MutableList` triggert geen recomposition).

**ViewModel**
- UI-logica (hoe tonen: navigatie, snackbars) vs business-logica (wat doen: betalen, opslaan) → business-logica nooit in de UI-laag.
- ViewModel levert UI-state + toegang tot business-logica en **overleeft configuration changes** (langere levensduur dan de Composition).
- Elke input is een **event** (tap, tekstwijziging, timer); de ViewModel handelt events af en werkt de UI-state bij.
- Explicit backing fields (stabiel vanaf Kotlin 2.4) voor private mutable / public read-only state:
  ```kotlin
  val tasks: List<WellnessTask>
      field = mutableStateListOf<WellnessTask>()
  ```

**Coroutines + animaties – FAB "scroll to top"**
```kotlin
val listState = rememberLazyListState()
val coroutineScope = rememberCoroutineScope()

AnimatedVisibility(
    visible = showButton,
    enter = fadeIn(),
    exit = fadeOut(),
    modifier = Modifier.align(Alignment.BottomCenter),
) {
    FloatingActionButton(onClick = {
        coroutineScope.launch { listState.animateScrollToItem(index = 0) }
    }) { /* icon */ }
}
```
- Coroutines houden lang werk (netwerk, opslag, animaties) van de main thread → app bevriest niet.
- Start nooit een job als side effect van de compositie zelf; daarvoor `LaunchedEffect`. In event-handlers: `rememberCoroutineScope()`.

**Scoping (lambda with receiver):** `Button`'s content is `@Composable RowScope.() -> Unit` → Icon + Text komen naast elkaar en `Modifier.weight` werkt. `FloatingActionButton` is niet RowScope-gescoped. Icon + tekst in een Button: `Icon(...)`, `Spacer(Modifier.width(ButtonDefaults.IconSpacing))`, `Text(...)`.

**KMP 101**
- Targets: Kotlin/JVM (.class), Native (.so/.dll/iOS), JS, Wasm.
- Source sets: `commonMain`, `androidMain`, `iosMain`, `jvmMain`, `webMain`.
- `expect`/`actual`:
  ```kotlin
  // commonMain
  expect fun getPlatform(): String
  // androidMain
  actual fun getPlatform(): String = "Android ${Build.VERSION.SDK_INT}"
  // jvmMain
  actual fun getPlatform(): String = "Java ${System.getProperty("java.version")}"
  ```
- **expect/actual vs interface:** expect/actual voor kleine, stateless platformverschillen (platformnaam, UUID, datum formatteren); compiler dwingt een `actual` per target af. **Interface** voor alles wat op een service lijkt (netwerk, database, analytics, meerdere methods, state, test double nodig) → fake injecteren vanuit `commonTest`.
- Valkuil bij alleen Android + JVM targetten: JVM-only libraries sluipen erin (`java.io/nio`, `String.format`, Retrofit). Zolang er geen niet-JVM target (iOS, Wasm/JS) is, controleert de compiler `commonMain` alleen tegen de JVM; voeg je later iOS toe, dan faalt de build op al die plekken. Gebruik KMP-libraries (klibs.io):
  - datum formatteren → kotlinx-datetime `LocalDateTime.format(...)` i.p.v. `String.format`
  - UUID → `kotlin.uuid.Uuid`
  - bestands-I/O → kotlinx-io of Okio i.p.v. `java.io.File`/`java.nio`
  - netwerk → Ktor i.p.v. Retrofit
- Integratie: gedeelde logica (KMP) met native UI, of ook gedeelde UI (Compose Multiplatform). CMP is een JetBrains-fork (github.com/JetBrains/compose-multiplatform-core); Android-artifacts verwijzen naar de originele Jetpack Compose (`androidx.compose.*`), andere platforms naar `org.jetbrains.compose.*`.
- Demo: pjfmast/incident-details-demo-kmp (zie `voorbeelden.md`).

**Huiswerk voor les 10:** Android Basics with Compose **Unit 3** – pathway 1 (Kotlin fundamentals, snelle herhaling), pathway 2 *Build a scrollable list* (kern, belangrijk voor proftaak), pathway 3 *Build beautiful apps* (material design, animaties; verfraaien kan later).

## Les 10 – ViewModel + StateFlow, Navigation 3, KMP-migratie

Demo's: pjfmast/Unscramble (+ KMP-versie unscramble-ktc), Navigation 3-workshop HouseHelper (KotlinConf, door docent gemigreerd naar Kotlin Toolchain: pjfmast/HouseHelper-KTC), android/nav3-recipes en terrakok/nav3-recipes. Code: zie `voorbeelden.md` → *Navigation 3 – basis*.

**ViewModel in Compose**
- Deelt UI-state tussen composables; tussenlaag tussen UI en andere lagen (bv. repository); overleeft configuration changes.
- Stelt bloot: **UI-state als `StateFlow`** (observable state holder) + **event handlers** die de state wijzigen. Oud alternatief `LiveData<T>` kom je nog tegen; TL;DR: gebruik StateFlow.
- **StateFlow:** read-only, geeft altijd de laatste waarde (`value`), is de publieke kant van een private `MutableStateFlow`; één single source of truth; updates zijn conflated (trage collector ziet alleen de laatste waarde).
- Best practice: UI-logica in de ViewModel, niet in composables ("dumb UI").
- **Brug naar Compose:** `val x by viewModel.flow.collectAsStateWithLifecycle()` (aanbevolen, niet meer experimental). Houd de ViewModel vrij van Compose-code (`uiState: StateFlow`), zodat het patroon ook buiten Compose/KMP werkt.
- Unscramble: `private val _uiState = MutableStateFlow(GameUiState())` + `val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()`, `data class GameUiState(...)`, `var userGuess by mutableStateOf(""); private set`. (De KMP-versie gebruikt een explicit backing field, zie les 9.)

**Android → CMP: string resources**
- CMP vereist **positionele placeholders**: `<string name="score">Score: %1$d</string>`, `%1$s` (Android's `getString` accepteert ook `%d`/`%s`). Gebruik `stringResource(Res.string.score, score)`.

**Navigation 3**
- Compose-first navigatiebibliotheek met meer controle over de back stack; Navigation 2 wordt uitgefaseerd. Veel bestaande voorbeelden (ook de codelabs) gebruiken nog Nav-2: `NavController`/`rememberNavController`, `NavGraph`, `NavHost` mag je vergeten.
- Kernbegrippen:
  - **`NavKey`** – route/sleutel van een scherm: puur data (`@Serializable` object/class), alleen de parameters die het scherm nodig heeft.
  - **`NavBackStack`** – observable Compose-lijst met NavKeys = navigatiegeschiedenis; navigeren met gewone lijstoperaties `add`, `removeLastOrNull`, `clear`. Aanmaken met `rememberNavBackStack(...)`.
  - **`NavEntry` / `entry<T> { }`** – koppelt een key aan de UI (+ id en optionele metadata).
  - **`entryProvider { }`** – lookup key → NavEntry; alle bestemmingen op één centrale plek.
  - **`SceneStrategy`** – bepaalt hoe entries getoond worden: `SinglePaneSceneStrategy` (standaard), `TwoPaneSceneStrategy`, `DialogSceneStrategy` (entry als dialog via metadata).
  - **`NavDisplay`** – composable die de back stack observeert, entries ophaalt, de scene kiest en terug afhandelt (`onBack`).
  - **`NavEntryDecorator`** – extra gedrag per entry: `rememberSaveableStateHolderNavEntryDecorator()` (rememberSaveable-state behouden), `rememberViewModelStoreNavEntryDecorator()` (ViewModel per entry, opgeruimd als het scherm van de stack gaat).
- Basisstappen: 1) type-safe routes (`@Serializable data object ListRoute : NavKey`, `data class DetailRoute(val id: String) : NavKey`); 2) back stack `rememberNavBackStack(ListRoute)`; 3) `NavDisplay(backStack, onBack = { backStack.removeLastOrNull() }, entryProvider = …)`; 4) `entry<ListRoute> { … }`, `entry<DetailRoute> { key -> … key.id … }`.
- KMP: routes als `@Serializable sealed interface Route : NavKey` en `rememberNavBackStack(savedStateConfig, ListRoute)` met een `SavedStateConfiguration` (polymorfe serialisatie, werkt platformonafhankelijk). Bron: kotlinlang.org/docs/multiplatform/compose-navigation-3.html. Wij houden deze vorm aan (zoals HouseHelper-KTC); versieverschillen tussen de bronnen: zie `voorbeelden.md` → *Navigation 3 – basis*.
- `NavDisplay` kan eenvoudig navigatie-animaties toevoegen.
- **Dialog-scene (workshop task 10):** `DialogSceneStrategy` in `sceneStrategies` + `entry<RenameDevice>(metadata = DialogSceneStrategy.dialog())`. Wrap de inhoud in een `Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.extraLarge)`: de kale `Dialog` is transparant (achtergrond schijnt door), M3-dialogen hebben `extraLarge`-hoeken (28.dp) en `Surface` zet de juiste contentkleur (light/dark).
- Talk: *Peeling back the Layers: A Navigation3 Deep Dive* (Marcello Galhardo & Ian Lake, YouTube). Docs scenes: developer.android.com/guide/navigation/navigation-3/scenes.

**Codelab Lunch Tray (Unit 4 pathway 2, *Add navigation*) – migratie-challenges**
- Challenge 1: Navigation 2 → 3. Stappen: dependencies naar Nav-3; enum met schermen → `@Serializable sealed interface LunchTrayRoute : NavKey { @Serializable data object Start : LunchTrayRoute … }`; NavController → `val backStack: NavBackStack<LunchTrayRoute> = rememberNavBackStack(Start)` (optioneel met `SavedStateConfiguration`). `CenterAlignedTopAppBar` niet meer nodig.
- Challenge 2: Android/Gradle → KMP met Kotlin Toolchain. Leerpunten (agentic migratie):
  - Begin met een nieuw KMP/Kotlin Toolchain-project en laat de AI-agent de code overzetten (voorkomt achtergebleven Gradle-artifacts).
  - Gebruik de door JetBrains onderhouden multiplatform-varianten `org.jetbrains.androidx.*` (bv. `org.jetbrains.androidx.navigation3:navigation3-ui`, `lifecycle-viewmodel-navigation3`).
  - Material 3: `@OptIn(ExperimentalMaterial3Api::class)` vaak niet meer nodig.
  - `R.string.x` → `Res.string.x`; placeholders `%1$s`; `java.text.NumberFormat` → eigen Kotlin-extensie (`Double.formatPrice()`); geen Android-resource-ids in routes.
  - Platformmodules (`androidApp`, `desktopApp`, `iosApp`, `webApp`) bevatten alleen opstart-wrappers naar de gedeelde `App()`; tests in `shared/test` met `kotlin.test` + `kotlinx-coroutines-test`.

**Huiswerk voor les 11:** Android Basics with Compose t/m **Unit 4** – pathway 1 *Architecture components* (ViewModel, Unscramble; zie ook developer.android.com/topic/architecture en MAD Skills), pathway 2 *Navigation* (belangrijk voor proftaak: pas zelf Navigation 3 toe in de codelabs), pathway 3 *Adaptive* (optioneel). Bedenk per profgroep een vraag voor de gastles state management van Dave Leeds.
