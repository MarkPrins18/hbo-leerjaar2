package models.rdw

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RdwFuelDto(
    @SerialName("kenteken") val licensePlate: String,
    @SerialName("brandstof_volgnummer") val fuelSequenceNumber: String? = null,
    @SerialName("brandstof_omschrijving") val fuelDescription: String? = null,
    @SerialName("brandstofverbruik_gecombineerd") val consumptionCombined: String? = null,
    @SerialName("co2_uitstoot_gecombineerd") val co2EmissionCombined: String? = null
)