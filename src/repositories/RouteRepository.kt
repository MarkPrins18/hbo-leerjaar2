package repositories

import models.RouteResult

// Issue #10 - Route naar auto / externe routering

interface RouteRepository {
    /** Geeft null terug als de routeringsdienst geen route kon berekenen. */
    suspend fun calculateRoute(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double
    ): RouteResult?
}
