# Slides periode 2 – AI-powered Software Development (AIP)

Samenvatting van de lesslides van periode 2 (Compose-app, module `shared`), als naslag voor "hoe doe je X in Kotlin/Compose" en voor het assessment.
Bron: `OneDrive - Avans Hogeschool\Code & Conquer leerjaar 2\Periode 2\Slides` (`AIP Les 8/9/10.pptx`, incl. sprekersnotities).
Periode 1 (Ktor-API): zie `slides-p1.md`. Codevoorbeelden en demo-repo's: zie `voorbeelden.md`.

**Leeswijzer** (geldt voor elke les die al is ingedeeld met *Uit de slides* / *Aanvulling*; nu: *Planning & werkwijze P2*, les 8, 9 en 10):
- `### Uit de slides` = alleen wat in de pptx staat. Elk punt eindigt met `(dia N)` (tekst op de dia), `(dia N, afbeelding)` (overgenomen uit een screenshot op de dia) of `(notities dia N)` (sprekersnotities). Dit is de lesstof en heeft **voorrang**.
- `### Aanvulling (niet uit de slides)` = geen docentmateriaal. Elk punt begint met een bronlabel:
  - `[Repo: naam @ commit]` – demo-repo; code staat in `voorbeelden.md`.
  - `[Extern: url]` – officiële docs of codelab buiten de slides.
  - `[Projectkeuze]` – besluit van ons team.
  - `[Projectregel]` – regel uit `CLAUDE.md`.
  - `[Let op: bronnen verschillen]` – slides en/of repo's spreken elkaar tegen; de gekozen lijn staat erbij.
  - `[Niet gecontroleerd: bron]` – overgenomen uit een bron die nog niet tegen het origineel is gecontroleerd.
- Gebruik een aanvulling alleen als de slides het onderwerp niet behandelen, of als de aanvulling aantoonbaar een beter resultaat geeft; noem dan de bron.
- Lessen zonder deze indeling zijn nog niet tegen de pptx gecontroleerd: daar kunnen dia-tekst en aanvullingen door elkaar staan.

**Periode 2** – leeruitkomst 2: Android (of KMP) app in Kotlin met Jetpack Compose / Compose Multiplatform die de API uit periode 1 gebruikt. Inleveren uiterlijk zo 17 jan 2027.

## Inhoud
- [Planning & werkwijze P2](#planning--werkwijze-p2)
- [Les 8 – Introductie Compose](#les-8--introductie-compose)
- [Les 9 – Lijsten, state, ViewModel, coroutines, KMP](#les-9--lijsten-state-viewmodel-coroutines-kmp)
- [Les 10 – ViewModel + StateFlow, Navigation 3, KMP-migratie](#les-10--viewmodel--stateflow-navigation-3-kmp-migratie)

---

## Planning & werkwijze P2

Bron: `AIP Les 8.pptx` (dia 4–6, 45–47) en `AIP Les 10.pptx` (dia 2), gecontroleerd 2026-11-26.

### Uit de slides

| Week | Les |
|---|---|
| 10–12 nov | Les 8 – Introductie Compose |
| 17–19 nov | Les 9 – Lijsten, material design, state & state hoisting |
| 24–26 nov | Les 10 – Navigation-3, app-architectuur, KMP-demo |
| 1–3 dec | Les 11 – Gastles Dave Leeds: state management |
| 8–10 dec | Les 12 – Ktor Client, data persistence |
| 15–17 dec | Les 13 – GPS en Maps (MapLibre) |
| 5–7 jan | Werken aan project (herkansingen deel 1) |
| 12–14 jan | Afronden; portfolio inleveren uiterlijk **zo 17 jan 2027** |
| 19–28 jan | Assessments deel 2 |

(Les 10 dia 2, meest recente versie; Les 8 dia 5 heeft een oudere versie.)

- Werkwijze: met de basiskennis Kotlin/Ktor uit P1 kun je snel aan onderdelen van de app beginnen. Lesweek 1–5 = basisvaardigheden voor iedereen in de groep: (Les 8 dia 4)
  - Les 1–2 (= les 8–9): Compose UI + state hoisting → losse screen-composables + previews uitwerken.
  - Les 3–4: navigatie, state management met ViewModel, Koin, Coil → navigatie tussen screens + Ktor API koppelen.
  - Les 5–6: (State)Flow en libraries voor data persistence, GPS en Maps, camera → alles om de app af te ronden.
- Alles wat je leert voor Android met Jetpack Compose is 100% toepasbaar op het Android-deel van een KMP-app; Hyperskill bouwt een KMP-track (hyperskill.org/learn/step/48198). (notities Les 8 dia 5)
- Tooling: Android Studio is gebaseerd op IntelliJ Community en loopt 1–2 versies achter, meestal met een nieuwe AGP per versie; goed alternatief is IntelliJ met Android- en KMP-plugin. (Les 8 dia 6)
- Keuzes die de profgroep zelf maakt na eerst kleinschalig oefenen: Android Studio of IntelliJ, Gradle of Kotlin Toolchain (Amper), KMP-structuur of Android-only. (Les 8 dia 45)
- Leerroute van de docent: (Les 8 dia 46–47)
  - Neem de tijd om Composable, State, Recomposition en State hoisting eigen te maken; gebruik variatie in actuele leermiddelen maar switch niet te snel; werk één concreet voorbeeld goed door.
  - Nuttig: codelab jetpack-compose-basics, hyperskill.org/knowledge-map/2560, hyperskill.org/tracks/66, *Android Basics with Compose* (course + codelabs), MAD Skills-playlist; regelmatig de officiële documentatie lezen (meest actueel).
  - Aanbevolen afwijkingen van de course: type-safe navigatie met **Navigation-3**, **Koin** i.p.v. Hilt voor dependency injection.

### Aanvulling (niet uit de slides)
- [Projectkeuze] Koin alleen in de app (`shared`); de server heeft geen DI-library (dependencies via constructors vanuit `Routing.kt`). Zie `voorbeelden.md` → *Koin in de app – basis*.
- [Let op: bronnen verschillen] De slides nummeren de P2-lessen soms als les 1–6 (Les 8 dia 4, Les 8 dia 36 "huiswerk voor les 2", Les 10 dia 39 "les 4"); in dit bestand heten ze les 8–13.
- [Niet gecontroleerd: P1-slides] Vooruitblik uit P1: coroutines, Flow, sealed `UiState`, design patterns + SOLID (individueel), unit- en UI-testen, ontwikkeldocument van P1 uitbreiden, filmpje met alle use cases.

## Les 8 – Introductie Compose

Bron: `AIP Les 8.pptx` (50 dia's + sprekersnotities), gecontroleerd 2026-11-26. Niet opgenomen: dia 1–2 (inschrijven modules P3); dia 4–6 en 45–47 staan onder *Planning & werkwijze P2*.

### Uit de slides

**Thinking in Compose**
- Hoe de Compose runtime werkt: theakram.com/understanding-jetpack-compose (dia 7); developer.android.com/develop/ui/compose/first (dia 8).
- Levenscyclus van een composable: komt de Composition binnen, recomposeert N keer, verlaat de Composition. (dia 9, afbeelding)

**Composable functions**
- `@Composable` = gewone Kotlin-functie die andere composables kan aanroepen, een deel van de UI beschrijft op basis van input, niets retourneert en stateful of stateless kan zijn. (dia 10)
- Naamgeving **PascalCase** (`DoneButton()`, `RoundIcon()`); retourneert een composable een waarde → camelCase. (dia 10) UI-emitterende composables gedragen zich als constructor voor UI-elementen, vandaar PascalCase. (dia 11)
- Wordt aangeroepen in de compositiefase en emit UI-componenten naar de Compose runtime; die bouwt een UI-tree van UI-nodes, en die tree wordt gerenderd. (dia 11)
- Live templates voor Compose: developer.android.com/develop/ui/compose/tooling/editor-actions. (dia 10)
- Compose 101: (dia 13–17)
  ```kotlin
  @Composable
  fun Greeting(name: String, friendly: Boolean) {
      if (friendly) Text("Hello $name, nice to see you!") else Text("Hello $name.")
  }

  @Composable
  fun Greeting(names: List<String>) {
      Column { for (name in names) Text("Hi $name!") }   // Row = horizontaal (dia 16)
  }
  ```

**Slots** – composable lambda als parameter: (dia 18)
```kotlin
@Composable fun Button(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) { … }

Button(onClick = {}) { Text("Click me!") }
Button(onClick = {}) { Icon(Icons.Default.Home, contentDescription = "Home") }
```

**Modifiers**
- Volgorde telt: `.background(Color.Magenta).padding(32.dp)` ≠ `.padding(32.dp).background(Color.Magenta)`. (dia 19)
- `size(40.dp)`, `height(6.dp)`, `width(6.dp)`, `fillMaxSize()`, `fillMaxWidth()`, `fillMaxHeight()`, `clip(CircleShape)`, `alpha(0.5f)`, `rotate(90f)`. (dia 20)
- Volgorde van `size` en `padding`: `.border(1.dp, Color.Red).size(32.dp).padding(8.dp).border(1.dp, Color.Blue)` ≠ `.border(…).padding(8.dp).size(32.dp).border(…)`. (dia 25–26) Modifiers passen eerst de constraints aan van links naar rechts en geven daarna de grootte terug van rechts naar links. (notities dia 25–26)
- Modifiers wijzigen grootte, layout, gedrag en uiterlijk, voegen informatie toe (accessibility-labels), verwerken user input en voegen interacties toe (clickable, scrollable, draggable, zoomable). (dia 30)
- Best practice: elke composable accepteert een `modifier`-parameter, de eerste parameter met een default (`modifier: Modifier = Modifier`). (dia 30) De parent bepaalt hoe het kind gemeten wordt, zodat je de composable op verschillende plekken met ander gedrag kunt gebruiken (chrisbanes.me/posts/always-provide-a-modifier). (dia 31) De modifier wordt toegepast op het eerste kind van de functie. (notities dia 31)
- Links: developer.android.com/jetpack/compose/modifiers, blog.protein.tech/jetpack-compose-respect-the-contract-of-modifiers-ecbbe8ce03db. (dia 30)
- Zelfstudie: codelab Unit 1 pathway 3 (image resource, layouts en modifiers). (dia 31)

**Material components** (dia 21)
```kotlin
Button(onClick = { println("Clicked!") }) { Text("Click me!") }
Icon(Icons.Default.Build, contentDescription = "Build")
var checked by remember { mutableStateOf(true) }
Switch(checked, onCheckedChange = { checked = it })
var value by remember { mutableFloatStateOf(0.5f) }
Slider(value, onValueChange = { value = it })
```

**Jetpack Compose vs Compose Multiplatform:** Jetpack Compose (Google) = Android; Compose Multiplatform (JetBrains) = iOS, desktop, web + Android (via Jetpack). "Compose" = beide. (dia 22–23) De scheidslijn is niet statisch: beide groeien naar een grotere gemeenschappelijke kern. (notities dia 23)

**Layouts**
- Basis: `Column` (verticaal), `Row` (horizontaal), `Box` (stapelen), `Lazy(Column/Row/…)`. Geavanceerd: Pager, Flow layouts, Alignment lines, `ConstraintLayout` (voorheen in Android XML Views "de" layout manager). Verfijnde sturing via modifiers. (dia 24) Docs: developer.android.com/jetpack/compose/layouts. (notities dia 24)
- "Must see"-instructie: youtu.be/xc8nAcVvpxY. (dia 27) Zie ook developer.android.com/jetpack/compose/layouts/basics en de `Arrangement`-reference. (notities dia 27)

**Eenheden** (dia 29): **dp** (density-independent pixels) voor alle afmetingen die niet van de tekstgrootte-instelling afhangen; **sp** (scalable pixels) voor tekstgrootte of ruimte die afhangt van schermdichtheid én de tekstgrootte-instelling van de gebruiker. Inleiding Scaffold, Surface, Material theme: hyperskill.org/knowledge-map/1167. (notities dia 28–29)

**State**
- Dice roller: `MutableState` is een value holder; lezen tijdens een composable abonneert de huidige RecomposeScope; schrijven van een andere waarde plant een recomposition, dezelfde waarde niet. (dia 32)
  ```kotlin
  var result by remember { mutableStateOf(1) }
  val imageResource = when (result) { 1 -> R.drawable.dice_1 … else -> R.drawable.dice_6 }
  ```
  Een stateless composable slaat geen eigen state op en toont wat het als argumenten krijgt. (notities dia 32)
- State is een superbelangrijk onderwerp: composables hangen af van `State<T>`-waarden; een wijziging triggert een recomposition. Denk na wanneer je state wilt bewaren: bij een configuration change (rotatie, taalinstelling) of als de app naar de achtergrond gaat → state holder of ViewModel (latere les). (dia 35)
- State hoisting toepassen (codelab *Intro to state in Compose*, stap 10): (dia 33)
  ```kotlin
  @Composable
  fun EditNumberField(value: String, onValueChanged: (String) -> Unit, modifier: Modifier = Modifier) {
      TextField(
          label = { Text(text = stringResource(id = R.string.bill_amount)) },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          value = value,
          onValueChange = onValueChanged,
          modifier = modifier
      )
  }
  ```
- BMI app: weinig design, één scherm zonder navigatie, eenvoudige `Column`, hergebruik van een composable voor lengte en gewicht, icons (Valkyrie plugin), state hoisting, slot API, meertaligheid. (dia 34)
- Herbruikbaar invoerveld met `imeAction = if (isDone) ImeAction.Done else ImeAction.Next` via `keyboardOptions.copy(…)`. (dia 36)
- Hoist UI-state naar de laagste gemeenschappelijke ouder van alle composables die hem lezen en schrijven (developer.android.com/develop/ui/compose/state-hoisting#best-practice). (dia 37)
- **Unidirectional Data Flow (UDF)**: *state goes down, events go up*. Vervang de state-variabele door `value: T` (huidige waarde) + `onValueChange: (T) -> Unit` (event dat een nieuwe waarde vraagt). (dia 38)
  ```kotlin
  @Composable
  fun BmiScreen(modifier: Modifier = Modifier) {
      var lengthInput by remember { mutableStateOf("170") }
      var weightInput by remember { mutableStateOf("60") }
      // …
      EditNumberField(value = lengthInput, onValueChanged = { lengthInput = it }, label = stringResource(R.string.height))
      EditNumberField(value = weightInput, onValueChanged = { weightInput = it }, isDone = true, label = "Weight")
  }
  ```
- Slot API in de BMI app (dia 39):
  ```kotlin
  @Composable
  fun EditNumberField(
      value: String, onValueChanged: (String) -> Unit, label: String, placeholder: String,
      minValue: Double, maxValue: Double, unit: String,
      modifier: Modifier = Modifier,
      imeAction: ImeAction = ImeAction.Next,
      leadingIcon: (@Composable () -> Unit)? = null
  ) { … }

  EditNumberField(…, leadingIcon = {
      Icon(imageVector = AppIcons.Height, contentDescription = stringResource(Res.string.length_icon_content_description))
  })
  ```
- Meertaligheid: strings in `res/values/strings.xml` (Android). (dia 40, afbeelding) In Android Studio als 2e taal voor string resources Dutch kiezen; in de emulator Settings → System & languages → Nederlands. (notities dia 40–41)

**Conventies** (dia 42; proandroiddev.com *Jetpack Compose tips and conventions*)
- Namen: `BmiScreen` (top-level scherm, vaak met state), `BmiHeader` (beschrijvende naam voor een deel), `BmiInputSection`/`BmiResultSection` (logische delen), `MyGridLayout` (herbruikbare stateless layout).
- Parametervolgorde: verplichte params → `modifier` (één) → optionele params → composable trailing lambda.

**Icons** (dia 43)
- Wordt nu afgeraden: `androidx.compose.material.icons.*` (bv. `Icons.filled.Height`, `Icons.filled.MonitorWeight`).
- Beter: **Valkyrie plugin** (SVG → ImageVector; goed voor het converteren van bestaande Android apps en voor KMP/CMP de snelste oplossing bij een kleine set iconen), of Compose Multiplatform resources (default in KMP/CMP-apps).

**Styling** (dia 44): weinig aandacht in de les, komt aan bod in Unit 3 van *Android Basics with Compose*. Herbruikbare stijl via een custom modifier:
```kotlin
fun Modifier.cardStyle() = this
    .fillMaxWidth()
    .padding(16.dp)
    .background(Color.White, shape = RoundedCornerShape(8.dp))
    .shadow(4.dp)
```

**Huiswerk voor les 9:** Android Basics with Compose t/m **Unit 2** (dia 48), o.a. codelab *Calculate a custom tip* (Tip Time). (dia 36)

### Aanvulling (niet uit de slides)
- [Repo: google-developer-training/basic-android-kotlin-compose-training-tip-calculator @ b029b3d] Code van Tip Time (link in notities dia 36); zie `voorbeelden.md` → *tip-calculator* (let op `java.text`).
- [Projectregel] In onze app teksten via `stringResource(Res.string.x)` met `composeResources/values/` (NL) én `values-en/` (EN), nooit `R.string` of hard (`CLAUDE.md`).
- [Projectkeuze] Icons staan als `AppIcons.X` in `shared/src/ui/icons/` (ImageVector, zoals Valkyrie oplevert; dia 43).

## Les 9 – Lijsten, state, ViewModel, coroutines, KMP

Bron: `AIP Les 9.pptx` (34 dia's + sprekersnotities), gecontroleerd 2026-11-26.

### Uit de slides

**Terugblik state**
- Expand/collapse: (dia 3)
  ```kotlin
  var expanded by remember { mutableStateOf(false) }
  Surface(color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)) {
      Row(modifier = Modifier.padding(24.dp)) {
          Column(modifier = Modifier.weight(1f)) {
              Text(text = "Hello, "); Text(text = name)
              Spacer(Modifier.height(20.dp))
              if (expanded) { Text(text = "Today is a great day") }
          }
          ElevatedButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Show less" else "Show more") }
      }
  }
  ```
  Een composable die niet wordt aangeroepen (bv. in een `if` waarvan de voorwaarde false is) zit niet in de Composition. (dia 3)
- Events: `WaterCounter` met `var count by remember { mutableStateOf(0) }` en `Button(onClick = { count++ })`. Vraag: `= remember` vs `by remember`, en `rememberSaveable`? (dia 4) Oefening: hyperskill.org/learn/step/40628. (notities dia 4) Herhaling: hyperskill.org/learn/step/41511 en /41507. (dia 5)

**Workshop codelab *State in Compose*** (developer.android.com/codelabs/jetpack-compose-state) (dia 6)
- Todo-achtige app: CRUD op items, herhaling state + state hoisting, problemen bij configuration changes en de oplossing, lijsten met `Lazy(Column|Row)` en `Lazy(Vertical|Horizontal)Grid` (eenvoudiger dan XML RecyclerView), eerste blik op ViewModel. (dia 6)
- State-driven UI (stap 6): `if (count > 0) { Text("You've had $count glasses.") }` – de Text wordt opgenomen in of weggelaten uit de Composition. (dia 7)
- `remember` vs `rememberSaveable` (stap 7–8): `remember` bewaart de waarde over recompositions; `rememberSaveable` doet hetzelfde maar overleeft ook activity- of process-recreation via saved instance state (bv. bij rotatie). (dia 8)
- State hoisting en UDF (stap 9): composables zonder state heten stateless; state hoisting verplaatst state naar de aanroeper; vervang de state-variabele door `value: T` + `onValueChange: (T) -> Unit`; *state goes down, events go up* = UDF. (dia 9, afbeelding)
- Work with lists (stap 10): `items(list) { task -> … }` geeft een type mismatch (`LazyItemScope.(Int) -> Unit` verwacht) totdat je de extension `LazyListScope.items` importeert. (dia 10, afbeelding)
- LazyColumn met stabiele key: (dia 11)
  ```kotlin
  LazyColumn(state = state, modifier = Modifier.fillMaxSize()) {
      items(items = list, key = { task -> task.id }) { task ->
          WellnessTaskItem(
              taskName = task.label,
              checked = task.checked,
              onCheckedChange = { checked -> onCheckedTask(task, checked) },
              onClose = { onCloseTask(task) }
          )
      }
  }
  ```
- Observable MutableList (stap 11): state gaat omlaag (App → WellnessTasksList → WellnessTaskItem), events gaan omhoog (`onClose` → `onCloseTask`). (dia 12, afbeelding)

**ViewModel** (codelab stap 12)
- Twee soorten logica: UI-logica (hoe tonen: navigatie, snackbars) en business-logica (wat doen: betalen, voorkeuren opslaan); business-logica hoort in de business- of datalaag, nooit in de UI-laag. ViewModels leveren UI-state + toegang tot business-logica en overleven configuration changes (langere levensduur dan de Composition). (dia 13)
- Elke input is een event (taps, tekstwijzigingen, timers); de ViewModel handelt events af en werkt de UI-state bij. (notities dia 13)
- Explicit backing fields zijn stabiel vanaf Kotlin 2.4+; IntelliJ-quickfix *Convert to explicit backing field*: (dia 14, afbeelding)
  ```kotlin
  // voor
  private val _tasks = getWellnessTasks().toMutableStateList()
  val tasks: List<WellnessTask>
      get() = _tasks
  // na
  val tasks: List<WellnessTask>
      field = getWellnessTasks().toMutableStateList()
  ```

**Animaties en coroutines – FAB "scroll to top"** (dia 15–16)
```kotlin
val listState = rememberLazyListState()
val coroutineScope = rememberCoroutineScope()

AnimatedVisibility(
    visible = showButton,
    enter = fadeIn(),
    exit = fadeOut(),
    modifier = Modifier.align(Alignment.BottomCenter)
) {
    FloatingActionButton(onClick = {
        coroutineScope.launch { listState.animateScrollToItem(index = 0) }
    }) { … }
}
```
- Lange taken (internet, lokale opslag, animaties) op de main thread laten de app bevriezen; coroutines houden ze van de main thread met eenvoudige, leesbare code. (dia 15)
- Start nooit een job als side effect van de compositie zelf; voor jobs die door de compositie gestart worden: `LaunchedEffect`. (dia 16)
- Een coroutine start je met een builder (`launch`, `async`); dat zijn extensions op `CoroutineScope`, dus altijd in een scope; de scope beheert de levensduur van de coroutines erin. (notities dia 16)

**Scoping in Compose (lambda with receiver)** (dia 17)
- De content van de meeste Button-types is `@Composable (RowScope.() -> Unit)`; uitzondering is bv. `FloatingActionButton`.
- Icon + tekst in een Button: (notities dia 17)
  ```kotlin
  Button(onClick = onIncrement, modifier = Modifier.padding(top = 8.dp), enabled = count < 10) {
      Icon(Icons.Default.Add, contentDescription = "Add one")
      Spacer(Modifier.width(ButtonDefaults.IconSpacing))
      Text("Add one")
  }
  ```

**KMP 101**
- Targets: Kotlin/JVM (`.class`), Kotlin/Native (`.so`, `.dll`, …), Kotlin/JS (`.js`), Kotlin/Wasm (`.wasm`). (dia 19) Kotlin/JVM op Android: `.kt` → `.class` → D8/R8 → `.dex` → `.apk`/`.aab` → ART. Kotlin/Native compileert via LLVM naar native machinecode zonder VM. (notities dia 19)
- Source sets: `commonMain`, `androidMain`, `iosMain`, `jvmMain`, `webMain`. (dia 20)
- `expect`/`actual`: (dia 21–22)
  ```kotlin
  // commonMain
  expect fun getPlatform(): String
  // jvmMain
  actual fun getPlatform(): String = "Java ${System.getProperty("java.version")}"
  // androidMain
  actual fun getPlatform(): String = "Android ${Build.VERSION.SDK_INT}"
  // iosMain
  actual fun getPlatform(): String = "iOS ${UIDevice.currentDevice.systemVersion}"
  // webMain
  actual fun getPlatform(): String = "Wasm with ${window.navigator.userAgent}"
  ```
- Valkuil: met alleen Android + desktop (JVM) heb je een basis-KMP-structuur (met hot reload), maar sluipen JVM-only libraries ongemerkt in je code: `java.io`/`nio`, `String.format(…)` voor DateTime, Retrofit. Wel KMP (klibs.io): kotlinx-io, kotlinx-datetime, Ktor. (dia 21) Target je alle platformen, dan controleert de compiler het gebruik van KMP-libraries. (dia 22)
- Uitwerking valkuil: zolang er geen niet-JVM target is, valideert de compiler `commonMain` alleen tegen de JVM; voeg je later iOS toe, dan faalt de build op al die plekken. Voorbeelden: (notities dia 21)
  - datum formatteren → kotlinx-datetime `LocalDateTime.format(format: DateTimeFormat<LocalDateTime>)` i.p.v. `String.format()`
  - UUID → `kotlin.uuid.Uuid`
  - bestands-I/O (`java.io.File`, `Paths.get`, `Files.readAllBytes`, …) → kotlinx-io of Okio
- Integratie in apps: gedeelde logica (KMP) met native UI per platform, of ook gedeelde UI (Compose Multiplatform) met eventueel nog platform-specifieke UI/logica. (dia 23–26)
- CMP is een fork (github.com/JetBrains/compose-multiplatform-core): de meeste code staat al in de common source set; JetBrains voegt vooral ondersteuning voor andere platforms toe; Android-artifacts verwijzen altijd naar de originele Jetpack Compose-artifacts. (dia 27) Gepubliceerd door Google (`androidx.compose.*`) en JetBrains (`org.jetbrains.compose.*`, bv. `org.jetbrains.compose.ui:ui` → `androidx.compose.ui:ui` op Android, `ui-uikitarm64`/`ui-desktop`/`ui-wasm-js` op andere platforms). (dia 28–30)
- **expect/actual vs interface** (Hyperskill-topic, hyperskill.org/learn/step/53895): (dia 31)
  - expect/actual: compiler faalt als een `actual` ontbreekt; compile-time, geen indirectie; één `actual` per target; testen vraagt een eigen test-source-set of test per platform. Best voor kleine, stateless platformverschillen (platformnaam, UUID, datum formatteren met een native API).
  - Interface: niets dwingt een implementatie per platform af; runtime; meerdere implementaties (echt, fake, test double); fake direct via de constructor vanuit `commonTest`. Best voor alles wat op een service/dependency lijkt (netwerk, database, analytics, meerdere methods, state, test double nodig).
- Demo: github.com/pjfmast/incident-details-demo-kmp (code: `voorbeelden.md`). (dia 32)

**Huiswerk voor les 10:** Android Basics with Compose **Unit 3** – pathway 1 (Kotlin fundamentals, snel door te lezen na P1), pathway 2 *Build a scrollable list* (kern van Unit 3, belangrijk voor de proftaak), pathway 3 *Build beautiful apps* (material design en animaties; verfraaien kan later). (dia 33)

### Aanvulling (niet uit de slides)
- [Extern: developer.android.com/develop/ui/compose/state] Antwoord op de vraag van dia 4: `val count = remember { mutableStateOf(0) }` → lezen/schrijven via `count.value`; `var count by remember { mutableStateOf(0) }` → property delegate, direct `count` (imports `getValue`/`setValue`).
- [Extern: codelab jetpack-compose-state, stap 11] Een gewone `MutableList` triggert geen recomposition; gebruik `mutableStateListOf()` of `toMutableStateList()`.
- [Projectregel] Alleen Kotlin stdlib, geen `java.*` (`CLAUDE.md`); past bij de valkuil van dia 21. Onze app target Android + iOS, dus de compiler controleert dit.

## Les 10 – ViewModel + StateFlow, Navigation 3, KMP-migratie

Bron: `AIP Les 10.pptx` (40 dia's + sprekersnotities), gecontroleerd 2026-11-26.

### Uit de slides

**ViewModel in Compose**
- Twee soorten logica: UI-logica (hoe tonen: navigatie, snackbars) en business-logica (wat doen: betalen, voorkeuren opslaan); business-logica hoort in de business- of datalaag, nooit in de UI-laag. ViewModels leveren UI-state + toegang tot business-logica en overleven configuration changes (langere levensduur dan de Composition). (dia 3)
- Bij Compose: een ViewModel deelt UI-state tussen composables, is een tussenlaag tussen UI en andere lagen (bv. repository) en overleeft configuration changes. Stelt bloot: **UI-state als `StateFlow`** (observable state holder) + **event handlers** die de state wijzigen. Oud alternatief `LiveData<T>` kom je nog tegen; TL;DR: gebruik StateFlow. (dia 4)
- **StateFlow:** read-only, geeft altijd de laatste waarde (`value`), is de publieke kant van een private `MutableStateFlow`; één single source of truth die alle collectors bijwerkt. (dia 5) Updates zijn conflated: een trage collector ziet alleen de laatste waarde. (notities dia 5)
- Best practice: UI-logica in de ViewModel, niet in composables. (dia 5)
- **Brug naar Compose:** `collectAsStateWithLifecycle()` is aanbevolen (niet meer experimental): (dia 6)
  ```kotlin
  val viewModel: ColorViewModel = viewModel()
  val flowColor by viewModel.color.collectAsStateWithLifecycle()
  ```
- Het ViewModel-patroon wordt ook buiten Compose en in KMP gebruikt: houd de ViewModel vrij van Compose-code (`uiState: StateFlow`) en maak er in de UI Compose-state van met `collectAsStateWithLifecycle()`. (dia 7)
- Unscramble (github.com/pjfmast/Unscramble): (dia 9)
  ```kotlin
  private val _uiState = MutableStateFlow(GameUiState())
  val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
  var userGuess by mutableStateOf("")
      private set

  data class GameUiState(val currentScrambledWord: String = "", val isGuessedWordWrong: Boolean = false, val score: Int = 0)
  ```
- Best practices ViewModels en composables: developer.android.com/develop/ui/compose/state-hoisting#screen-ui-state. (dia 10)

**Android → CMP: string resources**
- CMP vereist **positionele placeholders**: `<string name="score">Score: %1$d</string>`, `%1$s`. (dia 8) Android's `getString` (Java `String.format`) accepteert ook `%d`/`%s`; de CMP-formatter dwingt positionele indexering af op alle platforms. (notities dia 8)

**Navigation 3**
- Compose-first navigatie met meer controle over de back stack; Navigation 2 wordt snel uitgefaseerd. Let op: bestaande voorbeelden kunnen nog Navigation 2 gebruiken. Officiële docs: developer.android.com/guide/navigation/navigation-3. (dia 11)
- Principe *vaste startbestemming*: developer.android.com/guide/navigation/principles#fixed_start_destination. (notities dia 11, 13–16, 37)
- Nav-2-begrippen die je in bestaande code tegenkomt en mag vergeten: `NavController`/`rememberNavController`, `NavGraph`, `NavHost`. (dia 12)
- Kernbegrippen: (notities dia 12)
  - **`NavKey`** – unieke identifier van een scherm, vaak een `@Serializable` class/object; puur data, geen UI, alleen de parameters om het scherm te identificeren en de toestand te reconstrueren.
  - **`NavBackStack`** – observable Compose-lijst (`SnapshotStateList`) met NavKeys = navigatiegeschiedenis; navigeren met lijstoperaties `add`, `removeLastOrNull`, `clear`. Aanmaken met `rememberNavBackStack(...)`.
  - **`NavEntry` / `entry<T> { }`** – koppelt een key aan de UI (+ uniek id en optionele metadata).
  - **`entryProvider { }`** – lookup key → NavEntry; alle bestemmingen op één centrale plek.
  - **`SceneStrategy`** – bepaalt hoe entries getoond worden: `SinglePaneSceneStrategy` (standaard), `TwoPaneSceneStrategy` (tablets/foldables), `DialogSceneStrategy` (entry als dialog via metadata).
  - **`NavDisplay`** – composable die de back stack observeert, entries ophaalt, de scene kiest en terug afhandelt (`onBack`).
  - **`NavEntryDecorator`** – extra gedrag per entry: `rememberSaveableStateHolderNavEntryDecorator()` (rememberSaveable-state behouden), `rememberViewModelStoreNavEntryDecorator()` (ViewModel per entry, opgeruimd als het scherm van de stack gaat).
- Basisstappen: (dia 13–16)
  ```kotlin
  // 1) type-safe routes
  @Serializable data object ListRoute : NavKey
  @Serializable data class DetailRoute(val id: String) : NavKey
  // 2) back stack
  val backStack = rememberNavBackStack(ListRoute)
  // 3) NavDisplay die de back stack observeert
  NavDisplay(backStack = backStack, onBack = …)
  // 4) entries
  entry<ListRoute> { ListScreen("Welcome to Nav3") { … } }
  entry<DetailRoute> { key -> DetailScreen("Route id: ${key.id} ") }
  ```
- KMP-vorm (bron: kotlinlang.org/docs/multiplatform/compose-navigation-3.html; de definitie van `savedStateConfig` staat niet op de dia): (dia 17)
  ```kotlin
  @Serializable sealed interface Route : NavKey
  @Serializable data object ListRoute : Route
  @Serializable data class DetailRoute(val objectId: Int) : Route

  val backStack: NavBackStack<NavKey> = rememberNavBackStack(savedStateConfig, ListRoute)
  ```
- Volledige `NavDisplay`; schermen krijgen callbacks, niet de back stack: (dia 18)
  ```kotlin
  NavDisplay(
      backStack = backStack,
      onBack = { backStack.removeLastOrNull() },
      entryProvider = entryProvider {
          entry<ListRoute> {
              ListScreen(onDetail = { objectId -> backStack.add(DetailRoute(objectId)) })
          }
          entry<DetailRoute> {
              DetailScreen(objectId = it.objectId, onBack = { backStack.removeLastOrNull() })
          }
      }
  )
  ```
- `rememberNavBackStack(config, startRoute)` handelt polymorfe serialisatie van `@Serializable` data classes en data objects automatisch en platformonafhankelijk af. (dia 23)
- `NavDisplay` kan eenvoudig navigatie-animaties toevoegen. (dia 24)
- Talk: *Peeling back the Layers: A Navigation3 Deep Dive* (Marcello Galhardo & Ian Lake), youtube.com/watch?v=6g2mjgljn7M. (dia 25, 36)

**Workshop House Helper (KotlinConf)**
- Voorbeeldapp met schermen Onboarding Welcome/About/Done, Dashboard, LightDetail, CameraDetail, RenameDevice. (dia 26–27)
- **Task 9** – navigeren naar het hernoem-scherm: nieuwe key `@Serializable data class RenameDevice(val deviceId: DeviceId) : Route` (`Routes.kt`), entry toevoegen, bij dismiss de laatste entry van de back stack halen, navigatie-callback vanuit `CameraDetailsScreen` (`App.kt`): (dia 29–31)
  ```kotlin
  entry<CameraDetails> {
      CameraDetailsScreen(
          ...
          onNavigateToRename = { deviceId -> backStack.add(RenameDevice(deviceId)) },
      )
  }
  entry<RenameDevice> {
      RenameDeviceScreen(deviceId = it.deviceId, onDismiss = { backStack.removeLastOrNull() })
  }
  ```
- Scenes: developer.android.com/guide/navigation/navigation-3/scenes. (dia 32)
- **Task 10** – hernoem-scherm als dialog, volgens het dialog-recept (github.com/android/nav3-recipes → `dialog`): (dia 33–34)
  ```kotlin
  val dialogStrategy = remember { DialogSceneStrategy<Screen>() }
  NavDisplay(
      …,
      sceneStrategies = listOf(dialogStrategy),
      entryProvider = entryProvider {
          entry<RenameDevice>(metadata = DialogSceneStrategy.dialog()) { RenameDeviceScreen(...) }
      },
  )
  ```
  Inhoud in `Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.extraLarge) { Column(…) }`. (dia 35) Waarom: de kale Compose `Dialog` is transparant (achtergrond schijnt door), M3-dialogen hebben `extraLarge`-hoeken (28.dp) en `Surface` zet de juiste contentkleur (light/dark). (notities dia 35)
- Meer recepten: github.com/android/nav3-recipes; voor KMP/CMP: github.com/terrakok/nav3-recipes. (dia 37)

**Codelab Lunch Tray (Unit 4 pathway 2, *Add navigation*)**
- Veel voorbeelden zijn nog pre-Navigation 3. Challenge 1: Navigation 2 → 3. Challenge 2: Android/Gradle → KMP met Kotlin Toolchain. (dia 19)
- Gegeven opzet: `OrderViewModel` met `_uiState`/`asStateFlow()` en `data class OrderUiState`. (dia 20)
- `CenterAlignedTopAppBar` is niet meer nodig. (dia 21)
- Challenge 1, stappen: dependencies naar Navigation 3; enum met schermen → `@Serializable sealed interface LunchTrayRoute : NavKey { @Serializable data object Start : LunchTrayRoute … }`; NavController → back stack met de eerste route, optioneel met `SavedStateConfiguration`: `val backStack: NavBackStack<LunchTrayRoute> = rememberNavBackStack(Start)`. (dia 22)
- Challenge 2, leerpunten agentic migratie: (dia 23)
  - Begin met een nieuw KMP/Kotlin Toolchain-project en laat de AI-agent de code overzetten (voorkomt achtergebleven Gradle-artifacts).
  - Gebruik de door JetBrains onderhouden multiplatform-varianten onder `org.jetbrains.androidx.*`.
  - Material 3: `@OptIn(ExperimentalMaterial3Api::class)` vaak niet meer nodig.
  - String-placeholders positioneel: `<string name="subtotal">Subtotal: %1$s</string>`.
- Uitgevoerde KMP-aanpassingen Lunch Tray: (notities dia 37)
  - `%s` → `%1$s`; `R.string.x` → `Res.string.x` via `stringResource()`.
  - `java.text.NumberFormat` → eigen Kotlin-extensie `Double.formatPrice()`.
  - Nav3: `org.jetbrains.androidx.navigation3:navigation3-ui` + `lifecycle-viewmodel-navigation3`; `rememberNavBackStack` + `SavedStateConfiguration` met polymorfe `@Serializable` NavKeys.
  - Geen Android-resource-ids (`titleRes: Int`) in routes.
  - Alle schermen, ViewModels en modellen in `shared/src`; platformmodules (`androidApp`, `desktopApp`, `iosApp`, `webApp`) bevatten alleen opstart-wrappers naar de gedeelde `App()`.
  - Tests in `shared/test` met `kotlin.test` + `kotlinx-coroutines-test`.

**Huiswerk voor les 11** (dia 38–39): Android Basics with Compose t/m **Unit 4** – pathway 1 *Architecture components* (ViewModel, Unscramble; zie ook developer.android.com/topic/architecture en MAD Skills), pathway 2 *Navigation* (belangrijk voor proftaak: pas zelf Navigation 3 toe in de andere codelabs), pathway 3 *Adaptive* (optioneel). Bedenk per profgroep een vraag voor de gastles state management van Dave Leeds.

### Aanvulling (niet uit de slides)
- [Repo: pjfmast/HouseHelper-KTC @ 4003e3c] Uitwerking van de workshop (dia 26–35), door de docent gemigreerd naar Kotlin Toolchain + Koin. De repo-link staat niet in de slides. Code: `voorbeelden.md` → *Navigation 3 – basis*.
- [Repo: pjfmast/unscramble-ktc] KMP-versie van Unscramble (dia 9) met een explicit backing field (zie les 9). De repo-link staat niet in de slides.
- [Projectkeuze] We gebruiken de KMP-vorm van dia 17 (`sealed interface Route : NavKey` + `rememberNavBackStack(savedStateConfig, start)`), omdat de app ook op iOS draait. De `SavedStateConfiguration` vullen we zoals HouseHelper-KTC (`subclassesOfSealed<Route>()`); versieverschillen tussen bronnen: `voorbeelden.md` → *Navigation 3 – basis* → *Versies*.
- [Let op: bronnen verschillen] Type van de back stack: dia 17 `NavBackStack<NavKey>` (met config), dia 22 `NavBackStack<LunchTrayRoute>` (zonder config). Wij volgen dia 17, omdat we config gebruiken.
- [Projectregel] Ids zijn `Int` (`CLAUDE.md`); dat sluit aan op dia 17 (`objectId: Int`), niet op de `String`-id van de basisdia's 13–16.
- [Projectregel] Teksten via `stringResource(Res.string.x, …)` in `composeResources` (NL + EN), nooit hard (`CLAUDE.md`).
