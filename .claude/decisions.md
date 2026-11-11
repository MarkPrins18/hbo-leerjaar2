# Ontwerpkeuzes

Bewuste keuzes. Alleen wijzigen op basis van instructies.

## Bi-fuel: eerste niet-elektrische RDW-rij
**Keuze:** Bij meerdere brandstofrijen nemen we de eerste niet-elektrische rij, niet de rij met het laagste volgnummer. Een benzine/LPG-auto kan dus als LPG worden opgeslagen.
**Waarom:** Bi-fuel komt weinig voor; niet de moeite waard om af te vangen.

## Uitzondering op "geen java.*": resources lezen
**Keuze:** `DatabaseSeeder.kt` leest seedfiles met `javaClass.getResource(...)`.
**Waarom:** De Kotlin stdlib heeft geen alternatief om classpath-resources te lezen.

## Overlap- en ritcontrole in de repository
**Keuze:** De overlapcontrole bij reserveren (`createReservationIfAvailable`) en de check "er loopt al een rit" (`startRideIfNoneInProgress`) staan in de repository, samen met de insert in één `suspendTransaction` met `forUpdate()`. Zo zijn check en insert atomair. `startRideIfNoneInProgress` controleert onder de lock ook of de reservering nog CONFIRMED is. `finishRide` rondt in één transactie de rit af, slaat de locatie op en zet de reservering op COMPLETED.
**Waarom:** Niet terug verplaatsen naar de service, want dan komt de race condition terug. `ConcurrencyTest` bewaakt dit.

