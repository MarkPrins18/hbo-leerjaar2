# Ontwerpkeuzes

Bewuste keuzes. Alleen wijzigen op basis van instructies.

## Bi-fuel: eerste niet-elektrische RDW-rij
**Keuze:** Bij meerdere brandstofrijen nemen we de eerste niet-elektrische rij, niet de rij met het laagste volgnummer. Een benzine/LPG-auto kan dus als LPG worden opgeslagen.
**Waarom:** Bi-fuel komt weinig voor; niet de moeite waard om af te vangen.

## Uitzondering op "geen java.*": resources lezen
**Keuze:** `DatabaseSeeder.kt` leest seedfiles met `javaClass.getResource(...)`.
**Waarom:** De Kotlin stdlib heeft geen alternatief om classpath-resources te lezen.

