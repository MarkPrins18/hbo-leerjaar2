package services

data class CarFilter(
    val maxPrice: Double? = null,
    val maxDistanceKm: Double? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)