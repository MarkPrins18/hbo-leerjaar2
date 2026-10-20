package services

import models.*
import responses.FuelCostResponse
import kotlin.math.round

// Prices in EUR per unit. Fixed values: update them by hand when prices change.
const val PETROL_PRICE_PER_L = 2.22
const val DIESEL_PRICE_PER_L = 2.00
const val LPG_PRICE_PER_L = 0.85
const val ELECTRICITY_PRICE_PER_KWH = 0.45
const val HYDROGEN_PRICE_PER_KG = 18.00

fun calculateFuelCost(car: Car, distanceKm: Double): FuelCostResponse {
    val consumption = car.consumptionCombined
        ?: throw UnprocessableException("Geen verbruik bekend voor auto ${car.id}")

    val (category, unit, pricePerUnit) = when (car) {
        is ICECar -> Triple("ICE", "L", fuelPrice(car.fuelType))
        is BEVCar -> Triple("BEV", "kWh", ELECTRICITY_PRICE_PER_KWH)
        is FCEVCar -> Triple("FCEV", "kg", HYDROGEN_PRICE_PER_KG)
    }
    val cost = consumption / 100 * distanceKm * pricePerUnit

    return FuelCostResponse(
        carId = car.id,
        category = category,
        distanceKm = distanceKm,
        consumptionPer100Km = consumption,
        unit = unit,
        pricePerUnit = pricePerUnit,
        estimatedCost = round(cost * 100) / 100
    )
}

private fun fuelPrice(fuelType: FuelType): Double = when (fuelType) {
    FuelType.PETROL -> PETROL_PRICE_PER_L
    FuelType.DIESEL -> DIESEL_PRICE_PER_L
    FuelType.LPG -> LPG_PRICE_PER_L
}
