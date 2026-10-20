package clients

import models.rdw.RdwFuelDto
import models.rdw.RdwVehicleDto

/** Source of the official vehicle data for a license plate, implemented by [RdwClient]. */
interface VehicleRegistry {
    suspend fun getVehicle(licensePlate: String): RdwVehicleDto?
    suspend fun getFuel(licensePlate: String): List<RdwFuelDto>
}
