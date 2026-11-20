# Car Sharing API

Schoolproject autodeelplatform: Ktor-API (`server`) + Compose-app (`shared`, Android/iOS), gedeelde contracten in `contracts`.

## Werkwijze
- Praat Nederlands. Code, identifiers en KDoc Engels; foutmeldingen naar de client Nederlands.
- Grotere wijzigingen: eerst voorstel, pas na akkoord aanpassen. Bij twijfel: vragen.
- Pragmatisch en simpel.
- Kotlin/Ktor/Compose-aanpak: eerst `.claude/informatie/slides.md` en `voorbeelden.md` (lesvoorbeelden). Niet gevonden → zoek in de repo's uit `voorbeelden.md` en vul het voorbeeld aan. Bestaande code gaat voor.
- Eisen school: `requirements-kort.md` (lees vóór een nieuwe feature); details/letterlijk: `requirements.md` (leidend).
- Weinig commentaar. `//check`, `//remove` en `//rename` zijn notities: laten staan.
- Na een wijziging: `./kotlin build` (Windows: `.\kotlin.bat build`), geen Gradle. Op Windows slaat dit iOS over (`iosApp` bouwt alleen op macOS).
- `/brainstorming` start alleen de gebruiker.

## Regels
- Alleen Kotlin stdlib, geen `java.*`. Geen DI-library; dependencies via constructors vanuit `Routing.kt`.
- Route → Service → Repository. Routes kennen alleen services.
- Validatie in `services/CarValidation.kt`.
- Fouten: gooi alleen exceptions uit `services/Exception.kt` (niet Ktor's); `StatusPages.kt` handelt ze af, nooit zelf foutcodes responden.
- Repository-functies zijn `suspend` met `suspendTransaction`; ids zijn `Int`.
- Auth: `requireAuthentication { }` + `call.authenticatedUserId()` (`Authentication.kt`); nooit een user-id uit de request.
- Eigendomscontrole in de service: geef `userId` door, gooi `ForbiddenException` als het niet klopt.

## Modules
- `contracts`: alleen `@Serializable` klassen (`contracts.models`/`requests`/`responses`), geen logica, geen server-only velden.
- `shared`: Compose-UI (Android+iOS), zoals de demo's van de docent. Schermen: `ui/screens/<Naam>Screen.kt` met `@Preview`; theme `ui/theme/`; HTTP-client `api/`. Teksten in `composeResources/values/strings.xml` (NL) én `values-en/` (EN) via `stringResource(Res.string.x)`, nooit hard.
- `androidApp` / `iosApp`: alleen het startpunt dat `App()` toont.

## Lees eerst
- `.claude/decisions.md` vóór wijzigingen aan RDW-import, mappers of datamodellen.
- `.claude/testing.md` vóór tests schrijven.
- `.claude/database.md` vóór wijzigingen aan tabellen of seeddata.
