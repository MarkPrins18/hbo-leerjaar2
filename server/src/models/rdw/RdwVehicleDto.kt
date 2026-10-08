package models.rdw

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RdwVehicleDto(
    @SerialName("kenteken") val licensePlate: String,
    @SerialName("merk") val brand: String,
    @SerialName("handelsbenaming") val model: String,
    @SerialName("eerste_kleur") val color: String,
    @SerialName("aantal_zitplaatsen") val seats: String? = null,
    @SerialName("aantal_deuren") val doors: String? = null,
    @SerialName("voertuigsoort") val vehicleType: String,
    @SerialName("massa_rijklaar") val readyToDriveWeightKg: String? = null,
    @SerialName("datum_eerste_toelating") val firstAdmissionDate: String
)