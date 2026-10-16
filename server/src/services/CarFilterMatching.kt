package services

import models.Location
import models.RentalTerms
import dto.CarFilter

fun CarFilter.matches(location: Location?, rentalTerms: RentalTerms?): Boolean =
    matchesDistance(location) && matchesPrice(rentalTerms)

private fun CarFilter.matchesDistance(location: Location?): Boolean {
    if (maxDistanceKm == null || latitude == null || longitude == null) return true
    if (location == null) return false
    return distanceKm(latitude, longitude, location.latitude, location.longitude) <= maxDistanceKm
}

private fun CarFilter.matchesPrice(rentalTerms: RentalTerms?): Boolean {
    if (maxPrice == null) return true
    if (rentalTerms == null) return false
    return rentalTerms.pricePerDay <= maxPrice
}
