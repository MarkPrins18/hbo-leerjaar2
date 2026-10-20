import clients.VehicleRegistry
import dto.CarChangeDto
import dto.CarDto
import kotlinx.coroutines.runBlocking
import mappers.normalizeLicensePlate
import models.BEVCar
import models.Car
import models.User
import models.VehicleType
import models.rdw.RdwFuelDto
import models.rdw.RdwVehicleDto
import repositories.CarRepository
import repositories.UserRepository
import services.CarImportService
import services.ConflictException
import services.ExternalServiceException
import services.ResourceNotFoundException
import services.UnprocessableException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

// ==================== Test data ====================

private const val USER_ID = 1
private const val UNKNOWN_USER_ID = 999
private const val LICENSE_PLATE = "AB123C"
private const val EXISTING_LICENSE_PLATE = "KS711F"

private fun rdwVehicle(): RdwVehicleDto = RdwVehicleDto(
    licensePlate = LICENSE_PLATE,
    brand = "TESLA",
    model = "MODEL 3",
    color = "WIT",
    seats = "5",
    doors = "4",
    vehicleType = "Personenauto",
    readyToDriveWeightKg = "1850",
    firstAdmissionDate = "20230115"
)

private fun rdwFuel(): RdwFuelDto = RdwFuelDto(
    licensePlate = LICENSE_PLATE,
    fuelSequenceNumber = "1",
    fuelDescription = "Elektriciteit",
    consumptionCombined = "15.5"
)

private fun importRequest(licensePlate: String = LICENSE_PLATE) =
    CarDto(licensePlate = licensePlate, batteryCapacityKWh = 75.0)

private fun existingCar(): Car = BEVCar(
    id = 1,
    userId = USER_ID,
    licensePlate = EXISTING_LICENSE_PLATE,
    brand = "BMW",
    model = "I4 M50",
    productionYear = 2022,
    trim = null,
    color = "GROEN",
    seats = 5,
    doors = 4,
    vehicleType = VehicleType.PASSENGER_CAR,
    readyToDriveWeightKg = 2290,
    consumptionCombined = null,
    co2EmissionCombined = null,
    roadTaxPerQuarter = null,
    batteryCapacityKWh = 80.0
)

// ==================== Fakes ====================

private class FakeImportCarRepository(vararg cars: Car) : CarRepository {
    val cars = cars.toMutableList()

    override suspend fun getCarByLicensePlate(licensePlate: String): Car? =
        cars.find { it.licensePlate == normalizeLicensePlate(licensePlate) }

    override suspend fun createCar(car: Car): Car = car.also { cars += it }

    override suspend fun getAllCars(): List<Car> = TODO()
    override suspend fun getCarById(carId: Int): Car? = TODO()
    override suspend fun updateCar(carId: Int, change: CarChangeDto): Car? = TODO()
    override suspend fun deleteCar(carId: Int): Boolean = TODO()
}

private class FakeImportUserRepository(private vararg val userIds: Int) : UserRepository {
    override suspend fun userExists(userId: Int): Boolean = userId in userIds

    override suspend fun findByEmail(email: String): User? = TODO()
    override suspend fun findAll(): List<User> = TODO()
    override suspend fun create(email: String, passwordHash: String): User = TODO()
}

/** Knows only [vehicle], or fails like an unreachable RDW API when [fail] is set. */
private class FakeVehicleRegistry(
    private val vehicle: RdwVehicleDto? = rdwVehicle(),
    private val fail: Boolean = false
) : VehicleRegistry {
    var calls = 0

    override suspend fun getVehicle(licensePlate: String): RdwVehicleDto? {
        calls++
        if (fail) throw IllegalStateException("RDW niet bereikbaar")
        return vehicle?.takeIf { it.licensePlate == normalizeLicensePlate(licensePlate) }
    }

    override suspend fun getFuel(licensePlate: String): List<RdwFuelDto> {
        calls++
        return listOf(rdwFuel())
    }
}

// ==================== 2. Service: CarImportService ====================

class CarImportServiceTest {
    private val carRepository = FakeImportCarRepository(existingCar())

    private fun service(vehicleRegistry: VehicleRegistry = FakeVehicleRegistry()) =
        CarImportService(vehicleRegistry, carRepository, FakeImportUserRepository(USER_ID))

    // ---------- importCar ----------

    @Test
    fun `auto wordt met de RDW-gegevens opgeslagen voor de ingelogde gebruiker`() = runBlocking {
        val car = service().importCar(importRequest(licensePlate = "ab-123-c"), USER_ID)

        val bev = assertIs<BEVCar>(car)
        assertEquals(USER_ID, bev.userId)
        assertEquals(LICENSE_PLATE, bev.licensePlate)
        assertEquals("TESLA", bev.brand)
        assertEquals(2023, bev.productionYear)
        assertEquals(15.5, bev.consumptionCombined)
        assertEquals(75.0, bev.batteryCapacityKWh)
        assertTrue(car in carRepository.cars)
    }

    @Test
    fun `onbekend kenteken bij de RDW geeft UnprocessableException`() = runBlocking {
        val service = service(FakeVehicleRegistry(vehicle = null))

        assertFailsWith<UnprocessableException> { service.importCar(importRequest(), USER_ID) }
        assertEquals(1, carRepository.cars.size)
    }

    @Test
    fun `RDW onbereikbaar geeft ExternalServiceException`() = runBlocking {
        val service = service(FakeVehicleRegistry(fail = true))

        assertFailsWith<ExternalServiceException> { service.importCar(importRequest(), USER_ID) }
        assertEquals(1, carRepository.cars.size)
    }

    @Test
    fun `bestaand kenteken geeft ConflictException zonder de RDW te vragen`() = runBlocking {
        val vehicleRegistry = FakeVehicleRegistry()

        assertFailsWith<ConflictException> {
            service(vehicleRegistry).importCar(importRequest(licensePlate = "ks-711-f"), USER_ID)
        }
        assertEquals(0, vehicleRegistry.calls)
    }

    @Test
    fun `onbekende gebruiker geeft ResourceNotFoundException zonder de RDW te vragen`() = runBlocking {
        val vehicleRegistry = FakeVehicleRegistry()

        assertFailsWith<ResourceNotFoundException> { service(vehicleRegistry).importCar(importRequest(), UNKNOWN_USER_ID) }
        assertEquals(0, vehicleRegistry.calls)
        assertEquals(1, carRepository.cars.size)
    }
}
