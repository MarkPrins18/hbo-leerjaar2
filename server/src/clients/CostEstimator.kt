package clients

import shared.models.Car
import models.gemini.CarCostEstimate

interface CostEstimator {
    suspend fun estimateCarCosts(car: Car): CarCostEstimate
}
