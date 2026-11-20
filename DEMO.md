# Demo: inloggen, reserveren en een rit

Dit draaiboek hoort bij `main` (commit `68584f9`). Alle stappen, statuscodes en meldingen hieronder heb ik getest op een draaiende server met de seed-data.

## 0. Voorbereiding

1. Zorg dat poort 8080 vrij is. Start de server met `.\kotlin.bat run`.
2. Wacht op de regel `Responding at http://0.0.0.0:8080`.
3. Open `http://localhost:8080/swagger`. Swagger UI haalt zijn opmaak van internet (unpkg.com), dus zorg dat je internet hebt.

Bij elke start bouwt de server de database opnieuw op. In `application.yaml` staan `reset-schema: true` en `seed-data: true`. Daardoor krijg je bij elke demo dezelfde beginsituatie:

| Seed-gebruiker | Wachtwoord | Eigenaar van auto |
|---|---|---|
| eigenaar1@carshare.nl (id 1) | Welkom123! | 1, 4, 8 |
| eigenaar2@carshare.nl (id 2) | Welkom123! | 2, 5, 7 |
| eigenaar3@carshare.nl (id 3) | Welkom123! | 3, 6 |
| eigenaar4@carshare.nl (id 4) | Welkom123! | – |

Na een verse start bestaan er 6 reserveringen en 2 ritten. Jouw eerste nieuwe reservering krijgt dus id 7 en je eerste nieuwe rit id 3. Lees de id toch altijd af uit het antwoord.

## 1. Inloggen

`POST /users/login` → *Try it out*:

```json
{ "email": "eigenaar3@carshare.nl", "password": "Welkom123!" }
```

Je krijgt **200** met `{"token":"eyJhbGciOiJIUzI1NiIs..."}`.

Met een fout wachtwoord krijg je **401** `Onjuist e-mailadres of wachtwoord`.

**Uitleg (code):**
1. `UserRouting` roept `authService.login(request)` aan.
2. `AuthService` zoekt de gebruiker op e-mail (`trim().lowercase()`).
3. Het wachtwoord wordt gecontroleerd met `BCrypt.checkpw`. In de database staat alleen een hash, nooit het wachtwoord zelf.
4. `TokenService.createToken(user)` maakt een JWT met daarin:
   - `sub` = user-id
   - `email`
   - `exp` = nu + 60 minuten
5. Het token wordt ondertekend met HMAC-SHA256 en `jwt.secret`.

Bij een onbekend e-mailadres en bij een fout wachtwoord krijg je dezelfde melding. Zo kan een aanvaller niet afleiden welke e-mailadressen bestaan.

## 2. Authorize

1. Klik rechtsboven op **Authorize**.
2. Plak **alleen het token**, zonder het woord `Bearer`.
3. Klik op Authorize en daarna op Close.

Swagger zet zelf `Authorization: Bearer <token>` in de header. Dat is getest: de gegenereerde curl in Swagger toont `-H 'Authorization: Bearer eyJ...'`.

**Uitleg (code):** `requireAuthentication { }` in `Authentication.kt` controleert bij elke beveiligde route:
- de handtekening
- `iss` en `aud`
- of het token verlopen is (`exp`)
- of `sub` een getal is

Ontbreekt er iets of klopt er iets niet, dan krijg je **401** `Ontbrekende, ongeldige of verlopen token`. Klopt alles, dan geeft `call.authenticatedUserId()` het user-id uit `sub` terug.

## 3. Reservering plaatsen

`POST /reservations`:

```json
{ "carId": 1, "startTime": "2027-05-01T10:00:00Z", "endTime": "2027-05-01T12:00:00Z" }
```

Je krijgt **201**:

```json
{"id":7,"carId":1,"userId":3,"startTime":"2027-05-01T10:00:00Z","endTime":"2027-05-01T12:00:00Z","status":"CONFIRMED"}
```

**Laat zien:** je hebt geen `userId` meegestuurd, maar het antwoord bevat `"userId":3`. Die komt uit het token.

**Foutgevallen** (zelfde token):

| Wat je doet | Antwoord |
|---|---|
| Hetzelfde verzoek met 11:00–13:00 (overlapt) | **409** `Auto 1 is in dit tijdsblok al gereserveerd` |
| Starttijd in 2025 | **400** `Starttijd ligt in het verleden` |
| `"carId": 999` | **404** `Auto 999 niet gevonden` |
| `"userId": 1` extra in de body | **400** `Ongeldige body: ...` (onbekend veld wordt geweigerd) |
| Logout in Authorize, dan opnieuw proberen | **401** `Ontbrekende, ongeldige of verlopen token` |

**Volgorde in `ReservationService.createReservation`:**
1. Tijden controleren (400).
2. Bestaat de auto (404)?
3. Bestaat de huurder (404)?
4. Overlapt het tijdsblok (409)?
5. Opslaan als `CONFIRMED`.

Er is hier geen 403, omdat iedere ingelogde gebruiker mag reserveren.

**Tijden:** `Z` betekent UTC. Je mag ook de Nederlandse tijd met offset schrijven:
- `"2027-08-01T10:00:00+02:00"` wordt opgeslagen als `08:00:00Z` (getest).
- Een tijd zonder `Z` of offset geeft 400.
- Wintertijd is `+01:00`, zomertijd `+02:00`.

## 4. Rit starten

Een rit kan alleen starten tijdens de reservering, dus tussen `startTime` en `endTime`. Een reservering moet bij het aanmaken in de toekomst beginnen. Maak daarom een reservering die **over 1 minuut** begint, bijvoorbeeld op auto 6:

```json
{ "carId": 6, "startTime": "<over 1 minuut, bv. 2026-11-05T14:31:00+01:00>", "endTime": "<over 3 uur>" }
```

Je krijgt **201** met `id: 8` (als je stap 3 hebt gedaan).

| Stap | Antwoord |
|---|---|
| Meteen `POST /reservations/8/rides` | **409** `Reservering 8 is nu niet actief` |
| Inloggen als eigenaar2, Authorize met dat token, dan `POST /reservations/8/rides` | **403** `Geen toegang` |
| Terug naar het token van eigenaar3, na de starttijd `POST /reservations/8/rides` | **201** `{"id":3,"reservationId":8,...,"status":"IN_PROGRESS","bonusPoints":null}` |
| Nog een keer starten | **409** `Er loopt al een rit voor reservering 8` |

**Volgorde in `RideService.startRide`:**
1. Bestaat de reservering (404)?
2. Is het jouw reservering (403)?
3. Is de status `CONFIRMED` (409)?
4. Is de reservering nu actief (409)?
5. Loopt er al een rit (409)?

## 5. Rit afronden

`PATCH /rides/3/finish`:

```json
{ "distanceM": 25000, "avgAcceleration": 1.5, "avgDeceleration": 2.0 }
```

Je krijgt **200** met `"status":"COMPLETED"` en `"bonusPoints":25`.

| Stap | Antwoord |
|---|---|
| `"distanceM": -5` | **400** `Afstand en acceleraties mogen niet negatief zijn` |
| Als eigenaar2 | **403** `Geen toegang` |
| Nog een keer afronden | **409** `Rit 3 is al afgerond` |

**Bonuspunten (`BonusPoints.kt`):** een rit is rustig als `avgAcceleration < 2,78` én `avgDeceleration < 3,43` m/s². Een rustige rit levert 1 punt per volle kilometer op: 25 000 m geeft 25 punten. Anders levert de rit 0 punten op.

**Volgorde in `RideService.finishRide`:**
1. Body controleren (400).
2. Bestaat de rit (404)?
3. Bestaat de reservering (404)?
4. Is het jouw rit (403)?
5. Is de rit al afgerond (409)?

Let op: hier staat 400 vóór 404 en 403. Bij de andere endpoints komt de validatie later. Een beoordelaar kan vragen waarom dat zo is.

## 6. Reservering annuleren (optioneel)

`PATCH /reservations/7/cancel`:
- Als eigenaar2 krijg je **403**.
- Als eigenaar3 krijg je **200** met `"status":"CANCELLED"`.

## 7. Punten om te noemen

- **Geheim:** `dev-secret-change-me` is alleen voor ontwikkeling. In productie stel je `jwt.secret` in via de omgevingsvariabele `JWT_SECRET` (`"$JWT_SECRET:dev-secret-change-me"` in `application.yaml`).
- **Waarom het user-id uit het token komt:** een `userId` in de body kan iedereen invullen. Het token is ondertekend met het geheim. Pas je `sub` aan, dan klopt de handtekening niet meer en krijg je 401.
- **Token is leesbaar:** de inhoud van een JWT is alleen Base64-gecodeerd, niet versleuteld. Daarom staat er geen wachtwoord in.
- **Verloopt na 60 minuten** (`expiration-minutes: 60`). Daarna geeft de server 401 en moet je opnieuw inloggen.
- **Aandachtspunt:** `GET /users` staat nog open, zonder inloggen, met de comment `TODO: temporary endpoint for testing, remove before release`.

---

# Voorbereiding assessment: tips en uitleg

Alles in dit deel heb ik op een draaiende server getest (main `68584f9`, seed-data). De antwoorden staan er letterlijk bij.

## Tip 1: de demo in Swagger

1. Start de server met `.\kotlin.bat run`. Er mag niets anders op poort 8080 draaien.
2. Open `http://localhost:8080/swagger`.
3. Log in met `POST /users/login`: `eigenaar3@carshare.nl` / `Welkom123!`.
4. Klik op **Authorize** en plak **alleen** het token. Swagger zet zelf `Bearer ` ervoor. De gegenereerde curl laat dat zien: `-H 'Authorization: Bearer eyJ...'`.
5. Doe een `POST /reservations` zonder `userId`. In het antwoord staat `"userId":3`. Dat is het bewijs dat de server het user-id uit het token haalt.
6. Log in als `eigenaar2`, klik op Authorize en zet dat token erin. Probeer daarna de rit of reservering van eigenaar3 te starten, af te ronden of te annuleren. Je krijgt dan **403** `Geen toegang`.

## Tip 2: de rit-demo (timing)

Er zijn twee regels uit de code die elkaar raken:

- `createReservation`: de starttijd moet in de toekomst liggen, anders **400** `Starttijd ligt in het verleden`.
- `startRide`: je kunt alleen een rit starten tussen `startTime` en `endTime`, anders **409** `Reservering X is nu niet actief`.

```kotlin
// RideService.startRide
val now = Clock.System.now()
if (now < reservation.startTime || now >= reservation.endTime) {
    throw ConflictException("Reservering $reservationId is nu niet actief")
}
```

Daarom maak je in de demo een reservering die **over 1 minuut** begint:
- Start je de rit meteen, dan krijg je 409.
- Na die minuut krijg je **201** met `IN_PROGRESS`.

Zo laat je meteen beide regels zien.

Tijden schrijf je met `Z` (UTC) of met een offset:
- `+01:00` in de wintertijd, `+02:00` in de zomertijd.
- `"2027-08-01T10:00:00+02:00"` wordt opgeslagen als `08:00:00Z`.
- Een tijd zonder `Z` of offset geeft 400.

## Uitleg 1: waarom het user-id uit het token komt en niet uit de body

**Het probleem met een `userId` in de body.** De body typt de gebruiker zelf. Stond daar `"userId": 1`, dan zou de server dat geloven. Iedere ingelogde gebruiker kon dan reserveringen plaatsen, ritten starten of annuleren uit naam van iemand anders. Dat staat ook in de projectregels: *"Neem nooit een user-id uit de request over"*.

**Waarom het token wél te vertrouwen is.** Het token bestaat uit drie delen: `header.payload.handtekening`.
- De payload bevat `"sub":"3"`, het user-id.
- De handtekening is HMAC-SHA256 over header en payload, berekend met `jwt.secret`. Alleen de server kent dat geheim.

```kotlin
// TokenService.createToken
JWT.create()
    .withIssuer(config.issuer)
    .withAudience(config.audience)
    .withSubject(user.id.toString())   // sub = user-id
    .withClaim(EMAIL_CLAIM, user.email)
    .withClaim(RegisteredClaims.EXPIRES_AT, expiresAt.epochSeconds)
    .sign(algorithm)                    // Algorithm.HMAC256(config.secret)
```

**Bewijs.** In een token heb ik `"sub":"7"` veranderd in `"sub":"1"`. Daarna klopte de handtekening niet meer:

```
verify origineel : subject=7
verify aangepast (sub 7 -> 1): SignatureVerificationException: The Token's Signature resulted invalid ...
```

**Hoe de route het user-id krijgt** (`Authentication.kt`):

```kotlin
fun ApplicationCall.authenticatedUserId(): Int =
    principal<JWTPrincipal>()?.subject?.toIntOrNull() ?: throw UnauthorizedException()
```

```kotlin
// ReservationRouting
val userId = call.authenticatedUserId()          // uit het token
val request = call.receive<ReservationRequest>()  // carId, startTime, endTime – geen userId
val reservation = reservationService.createReservation(userId, request)
```

**Bewijs dat de body het niet kan overschrijven.** Stuur je toch `"userId": 1` mee, dan geeft de server **400** `Ongeldige body: ...`. `ReservationRequest` heeft geen veld `userId`, en een onbekend veld wordt geweigerd.

**Kort antwoord voor de beoordelaar:**
> "Het user-id komt uit het `sub` van de JWT. Dat token is ondertekend met een geheim dat alleen de server kent, dus de gebruiker kan het niet aanpassen zonder dat de handtekening ongeldig wordt. Een user-id in de body kan iedereen invullen; daarom bestaat dat veld niet meer in de request."

## Uitleg 2: de volgorde van de controles

Twee regels bepalen de volgorde:

1. **Je kunt pas iets over een object zeggen als het bestaat.** Om te controleren wie de eigenaar is, moet je het object eerst ophalen. Daarom komt 404 vóór 403.
2. **Wie geen toegang heeft, krijgt niets te weten over de inhoud of de status.** Daarom komt 403 vóór 409. Een 409-melding verraadt namelijk de status.

### Reserveren: 400 → 404 → 409

```kotlin
// ReservationService.createReservation
if (request.startTime <= Clock.System.now()) throw ValidationException("Starttijd ligt in het verleden")      // 400
if (request.endTime <= request.startTime) throw ValidationException("Eindtijd moet na de starttijd liggen")    // 400
if (!reservationRepository.carExists(request.carId)) throw ResourceNotFoundException("Auto ${request.carId} niet gevonden") // 404
if (!reservationRepository.userExists(userId)) throw ResourceNotFoundException("Gebruiker ${userId} niet gevonden")         // 404
if (reservationRepository.hasActiveOverlap(...)) throw ConflictException("Auto ${request.carId} is in dit tijdsblok al gereserveerd") // 409
```

| Volgorde | Waarom |
|---|---|
| 400 eerst | De tijden controleer je alleen met de request zelf, zonder database. Bij ongeldige invoer hoef je de database niet te raadplegen. |
| Geen 403 | Iedere ingelogde gebruiker mag reserveren. Je reserveert een auto, je past geen object van een ander aan. |
| 404 vóór 409 | Een overlap controleren voor een auto die niet bestaat, heeft geen betekenis. |

Getest: met een starttijd in het verleden én `carId: 999` krijg je **400** `Starttijd ligt in het verleden`, niet 404. De eerste controle die faalt, wint.

### Rit starten: 404 → 403 → 409

```kotlin
// RideService.startRide
val reservation = reservationRepository.getReservationById(reservationId)
    ?: throw ResourceNotFoundException("Reservering $reservationId niet gevonden")         // 404
if (reservation.userId != userId) throw ForbiddenException()                                 // 403
if (reservation.status != ReservationStatus.CONFIRMED) throw ConflictException("Reservering $reservationId is ${reservation.status}") // 409
if (now < reservation.startTime || now >= reservation.endTime) throw ConflictException("Reservering $reservationId is nu niet actief") // 409
if (rideRepository.hasRideInProgress(reservationId)) throw ConflictException("Er loopt al een rit voor reservering $reservationId")   // 409
```

**Bewijs voor regel 2.** Seed-reservering 3 hoort bij eigenaar3 en heeft status `CANCELLED`:

| Wie | Antwoord |
|---|---|
| eigenaar3 (eigenaar) | **409** `Reservering 3 is CANCELLED` |
| eigenaar2 (niet de eigenaar) | **403** `Geen toegang` |
| iemand, reservering 999 | **404** `Reservering 999 niet gevonden` |

Stond 409 vóór 403, dan zou eigenaar2 te zien krijgen dat de reservering van iemand anders geannuleerd is.

## Uitleg 3: rit afronden, waar 400 vóór 404 en 403 staat

```kotlin
// RideService.finishRide
if (request.distanceM < 0 || request.avgAcceleration < 0 || request.avgDeceleration < 0) {
    throw ValidationException("Afstand en acceleraties mogen niet negatief zijn")            // 400
}
val ride = rideRepository.getRideById(rideId) ?: throw ResourceNotFoundException("Rit $rideId niet gevonden")  // 404
val reservation = reservationRepository.getReservationById(ride.reservationId) ?: throw ResourceNotFoundException(...) // 404
if (reservation.userId != userId) throw ForbiddenException()                                  // 403
if (ride.status != RideStatus.IN_PROGRESS) throw ConflictException("Rit $rideId is al afgerond") // 409
```

**Wat dit in de praktijk doet** (getest):

| Verzoek | Antwoord |
|---|---|
| rit 999 (bestaat niet), `distanceM: -5` | **400** `Afstand en acceleraties mogen niet negatief zijn` |
| rit 999, geldige body | **404** `Rit 999 niet gevonden` |
| rit 1 (van eigenaar1) door eigenaar2, `distanceM: -5` | **400** |
| rit 1 door eigenaar2, geldige body | **403** `Geen toegang` |

**Waarom dit verdedigbaar is:** de body-controle heeft geen database nodig, net als de tijdcontrole bij reserveren. De 400 verraadt niets over de rit, want de melding gaat alleen over je eigen invoer.

**Waarom de beoordelaar ernaar kan vragen:** het is niet consistent met de rest van de API. Twee voorbeelden:
- Bij verhuurvoorwaarden (`RentalTermsService.setRentalTerms`) staat de validatie bewust ná de eigenaarcontrole: 404 → 403 → 400.
- `startRide` en `cancelReservation` beginnen met 404 en 403.

Iemand die geen toegang heeft tot rit 1 krijgt nu een 400 in plaats van een 403, zolang zijn body ongeldig is.

**Mogelijk antwoord:**
> "De validatie staat vooraan omdat die zonder database kan. Dat lekt geen informatie, maar het is wel anders dan bij de andere endpoints. Wil je het consistent maken, dan verplaats je de body-controle naar ná de eigenaarcontrole. Dan wordt de volgorde 404 → 403 → 400 → 409, net als bij verhuurvoorwaarden."

Kies zelf welk antwoord je geeft, en of je de code aanpast. Pas je de code aan, controleer dan ook de tests in `RideTest.kt`.

## Uitleg 4: het geheim (`jwt.secret`) en `JWT_SECRET`

In `application.yaml`:

```yaml
jwt:
  secret: "$JWT_SECRET:dev-secret-change-me"
```

Ktor leest dit als: *gebruik de omgevingsvariabele `JWT_SECRET`; bestaat die niet, gebruik dan `dev-secret-change-me`*. `JwtConfig.kt` leest de waarde met `config.property("jwt.secret")`, en `TokenService` gebruikt hem voor `Algorithm.HMAC256(config.secret)`.

**Waarom de standaardwaarde gevaarlijk is.** `dev-secret-change-me` staat in de repository, dus iedereen met toegang tot de code kent het. Met dat geheim kun je zelf een geldig token maken voor elke gebruiker, zonder wachtwoord.

Getest: ik heb buiten de server een token gemaakt met `"sub":"1"`, ondertekend met `dev-secret-change-me`. Daarmee kon ik reservering 4 van eigenaar1 annuleren:

```
PATCH /reservations/4/cancel  (zelfgemaakt token, geen login)
{"id":4,"carId":4,"userId":1,...,"status":"CANCELLED"} [200]
```

**Bewijs dat `JWT_SECRET` werkt.** Ik heb de server gestart met `JWT_SECRET=mijn-productie-geheim-123`:

```
oud token (gemaakt met dev-secret)  → 401 Ontbrekende, ongeldige of verlopen token
nieuw token (na opnieuw inloggen)   → werkt
ondertekend met mijn-productie-geheim-123: True
ondertekend met dev-secret-change-me    : False
```

**Instellen op Windows (PowerShell)**, in hetzelfde venster waarin je de server start:

```powershell
$env:JWT_SECRET = "<lange willekeurige tekst>"
.\kotlin.bat run
```

Dit heb ik niet op Windows getest; op Linux wel, met `JWT_SECRET=... java ...`. De variabele geldt alleen voor dat PowerShell-venster.

**Hoe lang moet het geheim zijn?** Voor HS256 schrijft de JWT-standaard (RFC 7518, §3.2) een sleutel van minimaal 256 bits voor, dus 32 bytes of meer, willekeurig gekozen. Zet het echte geheim nooit in de repository.

**Kort antwoord voor de beoordelaar:**
> "`dev-secret-change-me` is alleen de standaardwaarde voor ontwikkeling. Wie dat geheim kent, kan tokens maken voor elke gebruiker. In productie stel je het geheim in via de omgevingsvariabele `JWT_SECRET`; dan gebruikt `TokenService` die waarde om te ondertekenen en te controleren, en zijn tokens met het oude geheim ongeldig."
