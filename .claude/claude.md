# Car Sharing API

Schoolproject autodeelplatform: Ktor-API (`server`) + Compose-app (`shared`, Android/iOS), gedeelde contracten in `contracts`.

## Werkwijze
- Praat Nederlands. Code, identifiers en KDoc Engels; foutmeldingen naar de client Nederlands.
- Grotere wijzigingen: eerst voorstel, pas na akkoord aanpassen. Bij twijfel: vragen.
- Pragmatisch en simpel.
- Kotlin/Ktor/Compose-aanpak, in deze volgorde:
  1. Bestaande code in deze repo gaat voor.
  2. De slides, alleen het bestand van de module waaraan je werkt: `server` → `.claude/informatie/slides-p1.md` (Ktor), `shared`/app → `.claude/informatie/slides-p2.md` (Compose). *Uit de slides* is de lesstof en heeft voorrang (zie de leeswijzer bovenaan `slides-p2.md`).
  3. Alleen als de slides het niet behandelen of een aanvulling aantoonbaar een beter resultaat geeft: *Aanvulling* in het slides-bestand en de voorbeelden in `voorbeelden.md`. Noem dan de bron en waarom je afwijkt van de slides.
  4. Niet gevonden → zoek in de repo's uit `voorbeelden.md` en vul het voorbeeld aan (met bron).
- Eisen school: `requirements-kort.md` (lees vóór een nieuwe feature); details/letterlijk: `requirements.md` (leidend).
- Weinig commentaar. `//check`, `//remove` en `//rename` zijn notities: laten staan.
- Na een wijziging: `./kotlin build` (Windows: `.\kotlin.bat build`), geen Gradle. Op Windows slaat dit iOS over (`iosApp` bouwt alleen op macOS).
- Werk altijd via subagents: de hoofdagent verdeelt en coördineert alleen. Bouwen, testen, mergen en de build-controle gaan ook naar subagents. Korte vragen zonder codewijziging mag je direct beantwoorden. Worktree-agents kunnen van een oudere commit starten: laat de integrator cherry-picken op de doelbranch.

## Regels
- Alleen Kotlin stdlib, geen `java.*`. Server: geen DI-library; dependencies via constructors vanuit `Routing.kt`. App (`shared`): Koin.
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
