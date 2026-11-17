# Ontwerpkeuzes

Bewuste keuzes. Alleen wijzigen op basis van instructies.

## Bi-fuel: eerste niet-elektrische RDW-rij
**Keuze:** Bij meerdere brandstofrijen nemen we de eerste niet-elektrische rij, niet de rij met het laagste volgnummer. Een benzine/LPG-auto kan dus als LPG worden opgeslagen.
**Waarom:** Bi-fuel komt weinig voor; niet de moeite waard om af te vangen.

## Uitzondering op "geen java.*": resources lezen
**Keuze:** `DatabaseSeeder.kt` leest seedfiles met `javaClass.getResource(...)`.
**Waarom:** De Kotlin stdlib heeft geen alternatief om classpath-resources te lezen.

## Overlap- en ritcontrole in de repository
**Keuze:** De overlapcontrole bij reserveren (`createReservationIfAvailable`) en de check "er loopt al een rit" (`startRideIfNoneInProgress`) staan in de repository, samen met de insert in één `suspendTransaction` met `forUpdate()`. Zo zijn check en insert atomair. `startRideIfNoneInProgress` vergrendelt eerst de auto en dan de reservering (dezelfde volgorde als bij reserveren), controleert of de reservering nog CONFIRMED is en of er met de auto nog een rit loopt op een willekeurige reservering, bijvoorbeeld van een vorige huurder die uitloopt. `finishRide` rondt in één transactie de rit af, slaat de locatie op en zet de reservering op COMPLETED.
**Waarom:** Niet terug verplaatsen naar de service, want dan komt de race condition terug. `ConcurrencyTest` bewaakt dit.

## Auto verwijderen alleen als hij niet in gebruik is
**Keuze:** `deleteCar` in de repository vergrendelt eerst de car-rij en de reserveringen van die auto met `forUpdate()` en controleert in dezelfde transactie of er een PENDING/CONFIRMED reservering is die nog niet voorbij is, of een rit IN_PROGRESS. Zo ja, dan geeft hij `false` en wist hij niets; de service maakt daar een 409 van. Afgeronde, geannuleerde en verlopen reserveringen en ritten worden wel mee verwijderd.
**Waarom:** Dezelfde locks als reserveren (car-rij) en rit starten (reserveringsrij), dus er kan tussen controle en verwijderen geen reservering of rit bijkomen. Anders zou de eigenaar data van huurders wissen.

## Locatie van een auto is zichtbaar voor elke ingelogde gebruiker
**Keuze:** `GET /cars/{carId}/maps` controleert niet op eigenaar of reservering; alleen inloggen is nodig.
**Waarom:** Teambesluit: `GET /cars/map` toont de posities van alle auto's al, dus een extra controle hier beschermt niets.
