package responses

import kotlinx.serialization.Serializable

@Serializable
data class FuelCostResponse(
    val carId: Int,
    val category: String,
    val distanceKm: Double,
    val consumptionPer100Km: Double,
    val unit: String,
    val pricePerUnit: Double,
    val estimatedCost: Double
)
