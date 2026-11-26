package services

import clients.CostEstimator
import contracts.requests.CarChangeDto
import contracts.models.Car
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import repositories.CarRepository
import contracts.responses.FuelCostResponse
import contracts.responses.TcoResponse

class CarCostService(
    private val costEstimator: CostEstimator,
    private val carRepository: CarRepository
) {
    // shortcut: per process and lost on restart, so after a restart GETs ask Gemini once more per car
    private val askedCarIds = mutableSetOf<Int>()
    private val askedCarIdsMutex = Mutex()

    suspend fun fillMissingCostData(carId: Int, userId: Int): Car =
        fill(carRepository.getOwnedCar(carId, userId))

    suspend fun getFuelCost(carId: Int, userId: Int, distanceKm: Double): FuelCostResponse {
        val car = carRepository.getOwnedCar(carId, userId)

        validateDistanceKm(distanceKm)

        val carWithConsumption = if (car.consumptionCombined == null) fillOnce(car) else car
        return calculateFuelCost(carWithConsumption, distanceKm)
    }

    suspend fun getTco(carId: Int, userId: Int, annualKm: Double): TcoResponse {
        val car = carRepository.getOwnedCar(carId, userId)

        validateAnnualKm(annualKm)

        return calculateTco(fillOnce(car), annualKm)
    }

    /** GETs ask Gemini at most once per car; the POST fill endpoint always asks again. */
    private suspend fun fillOnce(car: Car): Car {
        if (askedCarIdsMutex.withLock { car.id in askedCarIds }) return car
        return fill(car)
    }

    private suspend fun fill(car: Car): Car {
        if (car.roadTaxPerQuarter != null && car.consumptionCombined != null) return car

        val estimate = callExternal("Gemini") { costEstimator.estimateCarCosts(car) }
        askedCarIdsMutex.withLock { askedCarIds.add(car.id) }
        estimate.validate()

        val change = CarChangeDto(
            roadTaxPerQuarter = estimate.roadTaxPerQuarter.takeIf { car.roadTaxPerQuarter == null },
            consumptionCombined = estimate.consumptionCombined.takeIf { car.consumptionCombined == null }
        )
        return carRepository.updateCar(car.id, change)
            ?: throw carNotFound(car.id)
    }
}
