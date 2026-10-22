package services

import clients.CostEstimator
import dto.CarChangeDto
import models.Car
import repositories.CarRepository
import responses.FuelCostResponse
import responses.TcoResponse

class CarCostService(
    private val costEstimator: CostEstimator,
    private val carRepository: CarRepository
) {
    suspend fun fillMissingCostData(carId: Int, userId: Int): Car {
        val car = carRepository.getOwnedCar(carId, userId)

        if (car.roadTaxPerQuarter != null && car.consumptionCombined != null) return car

        val estimate = callExternal("Gemini") { costEstimator.estimateCarCosts(car) }
        estimate.validate()

        val change = CarChangeDto(
            roadTaxPerQuarter = estimate.roadTaxPerQuarter.takeIf { car.roadTaxPerQuarter == null },
            consumptionCombined = estimate.consumptionCombined.takeIf { car.consumptionCombined == null }
        )
        return carRepository.updateCar(carId, change)
            ?: throw carNotFound(carId)
    }

    suspend fun getFuelCost(carId: Int, userId: Int, distanceKm: Double): FuelCostResponse {
        val car = carRepository.getOwnedCar(carId, userId)

        validateDistanceKm(distanceKm)

        val carWithConsumption = if (car.consumptionCombined == null) fillMissingCostData(carId, userId) else car
        return calculateFuelCost(carWithConsumption, distanceKm)
    }

    suspend fun getTco(carId: Int, userId: Int, annualKm: Double): TcoResponse {
        val car = carRepository.getOwnedCar(carId, userId)

        validateAnnualKm(annualKm)

        val completeCar =
            if (car.consumptionCombined == null || car.roadTaxPerQuarter == null) fillMissingCostData(carId, userId) else car
        return calculateTco(completeCar, annualKm)
    }
}
