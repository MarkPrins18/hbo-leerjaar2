# Testaanpak

Hoe we in dit project testen. Basis voor nieuwe tests en voor de e2e-testskill.
Voorbeeld van alle drie de lagen: `server/test/PhotoUploadTest.kt`.

## Draaien
- `.\kotlin.bat test` (Linux/macOS: `./kotlin test`). Geen Gradle.
- Tests staan in `server/test/`, zonder package.
- Test-dependencies in `server/module.yaml`: alleen `ktor.server.testHost`. Gebruik `kotlin.test`; geen mock-library, geen `kotlinx-coroutines-test`.

## Drie lagen

| Laag | Wat | Hoe | Snelheid |
|---|---|---|---|
| 1. Validatie / pure functies | `services/CarValidation.kt`, berekeningen | Direct aanroepen, `assertEquals` / `assertFailsWith` | Zeer snel |
| 2. Service | Businesslogica, eigendomscontrole, rollback | Service met fakes via de constructor | Snel |
| 3. Route / e2e | Request-parsing, auth, statuscodes, JSON | `testApplication` met echte app en database | Traag |

Begin bij de laagste laag waar het gedrag zit. Test een regel niet opnieuw in een hogere laag; daar test je alleen dat de lagen goed samenwerken.

## Laag 1: pure functies
- Geen setup nodig. Zie `BonusPointsTest.kt` en `PhotoValidationTest`.
- Test altijd de grenzen: precies op de limiet, één erboven, leeg/nul.
- Controleer het type exception uit `services/Exception.kt`, niet de foutmelding.

## Laag 2: services met fakes
- Repositories zijn interfaces: schrijf een `Fake...Repository` in het testbestand die data in het geheugen houdt.
- Implementeer alleen de methodes die de test gebruikt; de rest is `TODO()`, zodat onverwacht gebruik direct faalt.
- Foutpaden simuleer je met een vlag in de fake, bijvoorbeeld `FakePhotoRepository(failOnCreate = true)`.
- `PhotoStorage` is een class: geef hem een unieke tijdelijke map (`Path(SystemTemporaryDirectory, "...-${Uuid.random()}")`) en ruim op in `@AfterTest`.
- `suspend`-functies: test-body in `runBlocking { }`. Eindigt de body op `assertFailsWith`, sluit dan af met `Unit` (JUnit verwacht een `void`-methode).
- Test per servicefunctie: succes, niet gevonden (`ResourceNotFoundException`), niet de eigenaar (`ForbiddenException`), ongeldige invoer, en dat er bij een fout geen bijwerkingen achterblijven.

## Laag 3: route / e2e
- Gebruik altijd `e2eTest { client -> ... }` uit `server/test/E2eSupport.kt`, nooit een kale `testApplication`.
- `e2eTest` start de hele app vanuit `application.yaml`, maar zet `database.url` op een unieke in-memory H2-database en `photos.directory` op een tijdelijke map. Zo blijft je lokale data in `server/resources/data/database/` onaangeraakt. Er is geen aparte test-config of aanpassing in `module.yaml` nodig.
- Elke test start een eigen app; de database is dus altijd gereset en geseed. Tests zijn onafhankelijk, maar trager dan laag 1 en 2.
- De `client` heeft JSON (ContentNegotiation) al ingesteld, dus `response.body<Photo>()` werkt direct.
- Auth: `val token = client.login(userId)` logt in als seed-gebruiker `eigenaar<id>@carshare.nl` (zie `resources/data/seed/users.sql`). Stuur het token mee met `bearerAuth(token)`.
- Seed-gebruiker 1 is eigenaar van auto 1, gebruiker 2 van auto 2, enzovoort. Elke seed-auto heeft één seedfoto. Gebruik een andere gebruiker dan de eigenaar om 403 te testen, en geen token om 401 te testen.
- Multipart (foto-upload):
  ```kotlin
  setBody(MultiPartFormDataContent(formData {
      append("photo", bytes, Headers.build {
          append(HttpHeaders.ContentDisposition, "filename=\"test.jpg\"")
      })
  }))
  ```
- Controleer per route: de statuscode, de JSON-body of headers bij succes, en dat fouten via `StatusPages.kt` de juiste code geven (400, 401, 403, 404, 413, 415).

## Conventies
- Testnamen in het Nederlands tussen backticks; ze beschrijven gedrag: `` `upload door niet-eigenaar geeft ForbiddenException` ``.
- Eén testbestand per functionaliteit, ingedeeld in blokken met een kop:
  1. `// ==================== Test data ====================`: constanten en helpers zoals `testCar()`
  2. `// ==================== Fakes ====================`
  3. `// ==================== 1. Validation: ... ====================`: een testclass per laag
  4. `// ==================== 2. Service: ... ====================`, met `// ---------- functie ----------` per servicefunctie
  5. `// ==================== 3. E2E: ... ====================`, met `// ---------- METHOD /pad ----------` per route
- Arrange / act / assert gescheiden door een witregel.
