package responses

import kotlinx.serialization.Serializable

@Serializable
data class TcoResponse(
    val carId: Int,
    val annualKm: Double,
    val annualFuelCost: Double,
    val annualRoadTax: Double,
    val annualTotal: Double,
    val monthlyTotal: Double
)
