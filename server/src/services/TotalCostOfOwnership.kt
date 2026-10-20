package services

import models.Car
import responses.TcoResponse
import kotlin.math.round

const val DEFAULT_ANNUAL_KM = 15_000.0
private const val QUARTERS_PER_YEAR = 4
private const val MONTHS_PER_YEAR = 12

fun calculateTco(car: Car, annualKm: Double): TcoResponse {
    val roadTaxPerQuarter = car.roadTaxPerQuarter
        ?: throw UnprocessableException("Geen wegenbelasting bekend voor auto ${car.id}")

    val annualFuelCost = calculateFuelCost(car, annualKm).estimatedCost
    val annualRoadTax = roadTaxPerQuarter * QUARTERS_PER_YEAR
    val annualTotal = annualFuelCost + annualRoadTax

    return TcoResponse(
        carId = car.id,
        annualKm = annualKm,
        annualFuelCost = annualFuelCost,
        annualRoadTax = roundToCents(annualRoadTax),
        annualTotal = roundToCents(annualTotal),
        monthlyTotal = roundToCents(annualTotal / MONTHS_PER_YEAR)
    )
}

private fun roundToCents(amount: Double): Double = round(amount * 100) / 100
