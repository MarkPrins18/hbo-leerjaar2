# Slides periode 1 – AI-powered Software Development (AIP)

Samenvatting van de lesslides van periode 1 (Ktor-API, module `server`), als naslag voor "hoe doe je X in Kotlin/Ktor" en voor het assessment.
Bron: `OneDrive - Safety Consulting & Technology\school portfolio` (pptx + FSA-portfolio xlsx).
Periode 2 (Compose-app): zie `slides-p2.md`. Codevoorbeelden en demo-repo's: zie `voorbeelden.md`.

**Let op:** dit bestand is nog niet tegen de pptx gecontroleerd en nog niet ingedeeld in *Uit de slides* / *Aanvulling* (leeswijzer: zie `slides-p2.md`). Dia-tekst, aanvullingen en projectkeuzes kunnen hier door elkaar staan.

**Periode 1** – leeruitkomst 1: backend Web API in Kotlin met Ktor (OO + FP concepten). Inleveren uiterlijk zo 25 okt 2026.

## Inhoud
- [Module & beoordeling](#module--beoordeling)
- [Les 1 – Kotlin basics](#les-1--kotlin-basics)
- [Les 2 – OO, interfaces, enum, sealed, extensions, REST](#les-2--oo-interfaces-enum-sealed-extensions-rest)
- [Les 3 – SOLID, UML→Kotlin, unit testen, JSON, DSL](#les-3--solid-umlkotlin-unit-testen-json-dsl)
- [Les 4 – Collections & JSON serialization](#les-4--collections--json-serialization)
- [Les 6 – JWT, Exposed, file upload, SSE](#les-6--jwt-exposed-file-upload-sse)
- [Les 7 – Collections (verdieping), coroutines](#les-7--collections-verdieping-coroutines)
- [Assessment-voorbeeldvragen](#assessment-voorbeeldvragen)
- [FSA individueel portfolio (checklist)](#fsa-individueel-portfolio-checklist)

---

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
