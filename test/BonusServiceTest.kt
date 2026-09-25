import models.Trip
import models.calculateBonusPoints
import kotlin.test.*
import kotlin.time.Instant

// Issue #12 - Bonuspuntensysteem
//
// LET OP: deze test toetst de VOORLOPIGE aanname in models/BonusScoring.kt
// (1 punt per 10 km). Werk deze test bij zodra de echte regels uit issue #12
// bevestigd zijn.
class BonusServiceTest {

    private fun tripWithDistance(distanceKm: Double?): Trip = Trip(
        id = 1,
        reservationId = 1,
        startTime = Instant.fromEpochMilliseconds(0),
        endTime = Instant.fromEpochMilliseconds(0),
        startLatitude = 0.0,
        startLongitude = 0.0,
        endLatitude = 0.0,
        endLongitude = 0.0,
        distanceKm = distanceKm
    )

    @Test
    fun `geen afstand levert 0 punten op`() {
        assertEquals(0, calculateBonusPoints(tripWithDistance(null)))
    }

    @Test
    fun `0 km levert 0 punten op`() {
        assertEquals(0, calculateBonusPoints(tripWithDistance(0.0)))
    }

    @Test
    fun `25 km levert 2 punten op (afgerond naar beneden)`() {
        assertEquals(2, calculateBonusPoints(tripWithDistance(25.0)))
    }

    @Test
    fun `100 km levert 10 punten op`() {
        assertEquals(10, calculateBonusPoints(tripWithDistance(100.0)))
    }
}
