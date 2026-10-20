package clients

import models.Car
import models.gemini.CarCostEstimate

/** Estimates cost data that the RDW does not provide, implemented by [GeminiClient]. */
interface CostEstimator {
    suspend fun estimateCarCosts(car: Car): CarCostEstimate
}
