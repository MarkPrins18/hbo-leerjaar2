package clients

import models.rdw.RdwFuelDto
import models.rdw.RdwVehicleDto

interface VehicleRegistry {
    suspend fun getVehicle(licensePlate: String): RdwVehicleDto?
    suspend fun getFuel(licensePlate: String): List<RdwFuelDto>
}
