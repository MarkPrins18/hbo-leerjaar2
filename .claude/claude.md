# Car Sharing API

Schoolproject: Kotlin/Ktor Web API voor een autodeelplatform, gebouwd door een team van drie.

## Werkwijze
- Praat Nederlands. Code, identifiers en KDoc zijn Engels; foutmeldingen naar de client zijn Nederlands.
- Bij grotere wijzigingen: eerst voorstel, pas aanpassen na akkoord. Bij twijfel: vragen.
- Pragmatisch en simpel. Pas je aan de bestaande stijl en mappenstructuur aan.
- Weinig commentaar. `//check`, `//remove` en `//rename` zijn notities: laten staan.
- Na een wijziging: `./kotlin build` (Windows: `.\kotlin.bat build`). Geen Gradle.

## Regels
- Alleen Kotlin stdlib, geen `java.*`. Geen DI-library; dependencies via constructors vanuit `Routing.kt`.
- Route → Service → Repository. Routes kennen alleen services.
- Validatie in `services/CarValidation.kt`.
- Fouten: gooi exceptions (`services/Exception.kt`, Ktor's `BadRequestException`/`NotFoundException`), `StatusPages.kt` handelt ze af. Nooit zelf foutcodes responden.
- Repository-functies zijn `suspend` met `suspendTransaction`; ids zijn `Int`.

## Database
- H2-bestand (`MODE=MySQL`) via Exposed. Bij elke start wordt het schema gereset en opnieuw geseed.
- Nieuwe tabel: toevoegen aan `allTables` in `TableRegistry.kt`, seeddata in `resources/data/seed/` en `seedFilePaths` in `DatabaseSeeder.kt`.
- Nieuwe tabel die naar `car` verwijst: ook opnemen in `deleteCar`.

## Ontwerpkeuzes
Lees `.claude/decisions.md` voordat je iets wijzigt aan de RDW-import, mappers of datamodellen.