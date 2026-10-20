package clients

import models.Car
import models.gemini.CarCostEstimate

interface CostEstimator {
    suspend fun estimateCarCosts(car: Car): CarCostEstimate
}
