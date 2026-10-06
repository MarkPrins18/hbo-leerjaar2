package services

import models.Location
import requests.CarFilter

fun CarFilter.matches(location: Location?): Boolean =
    matchesDistance(location)
    // && matchesPrice(rentalOffer)

private fun CarFilter.matchesDistance(location: Location?): Boolean {
    if (maxDistanceKm == null || latitude == null || longitude == null) return true
    if (location == null) return false
    return distanceKm(latitude, longitude, location.latitude, location.longitude) <= maxDistanceKm
}

// TODO: enable once RentalOffer exists. Steps:
//  1. Add a `rentalOffer: RentalOffer?` parameter to matches() and uncomment `&& matchesPrice(rentalOffer)`.
//  2. In CarService.getCars(), fetch the RentalOffer per car and pass it to matches().
//  3. In CarService.getCars(), also skip the early return when maxPrice is set.
//  Adjust `rentalOffer.price` to the actual field name in RentalOffer.
//
// private fun CarFilter.matchesPrice(rentalOffer: RentalOffer?): Boolean {
//     if (maxPrice == null) return true
//     if (rentalOffer == null) return false
//     return rentalOffer.price <= maxPrice
// }
