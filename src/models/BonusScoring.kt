package models

// Issue #12 - Bonuspuntensysteem
//
// !!! OPEN PUNT - CONTROLEREN !!!
// De exacte spelregels voor het bonuspuntensysteem stonden niet in de broncode
// die ik onder ogen heb gekregen, en ik verzin ze niet zomaar (jouw instructie:
// geen speculatie). Onderstaande regel is daarom een expliciet gemarkeerde,
// VOORLOPIGE aanname die je moet controleren tegen de echte acceptatiecriteria
// van issue #12 voordat je dit als definitief beschouwt en inlevert:
//
//   Aanname: 1 bonuspunt per volledig afgelegde 10 km van een afgeronde rit.
//   Een rit zonder bekende afstand (distanceKm == null), of met afstand <= 0,
//   levert 0 punten op.
//
// Pas de berekening hieronder aan zodra je de echte regels uit issue #12 hebt,
// en pas dan test/BonusServiceTest.kt aan op de nieuwe verwachte waarden.

/** Bepaalt het aantal bonuspunten voor een afgeronde rit. */
fun calculateBonusPoints(trip: Trip): Int {
    val distance = trip.distanceKm ?: return 0
    if (distance <= 0.0) return 0
    return (distance / 10.0).toInt()
}

