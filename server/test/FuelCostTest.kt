import clients.CostEstimator
import dto.CarChangeDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import models.*
import models.gemini.CarCostEstimate
import repositories.*
import responses.FuelCostResponse
import services.*
import kotlin.math.round
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// ==================== Test data ====================

private const val OWNER_ID = 1
private const val OTHER_USER_ID = 2
private const val CAR_ID = 10
private const val MAX_DISTANCE_KM = 1_000_000.0
private const val TOLERANCE = 0.001

private fun iceCar(fuelType: FuelType = FuelType.PETROL, consumption: Double? = 10.0): Car = ICECar(
    id = CAR_ID,
    userId = OWNER_ID,
    licensePlate = "AB-123-C",
    brand = "Volkswagen",
    model = "Golf",
    productionYear = 2021,
    trim = null,
    color = "Grijs",
    seats = 5,
    doors = 5,
    vehicleType = VehicleType.PASSENGER_CAR,
    readyToDriveWeightKg = 1250,
    consumptionCombined = consumption,
    co2EmissionCombined = 120.0,
    roadTaxPerQuarter = 100.0,
    fuelType = fuelType,
    tankCapacityL = 50.0,
    automaticTransmission = false
)

private fun bevCar(consumption: Double? = 15.0): Car = BEVCar(
    id = CAR_ID,
    userId = OWNER_ID,
    licensePlate = "AB-123-C",
    brand = "Tesla",
    model = "Model 3",
    productionYear = 2023,
    trim = null,
    color = "Wit",
    seats = 5,
    doors = 4,
    vehicleType = VehicleType.PASSENGER_CAR,
    readyToDriveWeightKg = 1850,
    consumptionCombined = consumption,
    co2EmissionCombined = 0.0,
    roadTaxPerQuarter = 50.0,
    batteryCapacityKWh = 75.0
)

private fun fcevCar(consumption: Double? = 0.8): Car = FCEVCar(
    id = CAR_ID,
    userId = OWNER_ID,
    licensePlate = "AB-123-C",
    brand = "Toyota",
    model = "Mirai",
    productionYear = 2017,
    trim = null,
    color = "Blauw",
    seats = 4,
    doors = 4,
    vehicleType = VehicleType.PASSENGER_CAR,
    readyToDriveWeightKg = 1925,
    consumptionCombined = consumption,
    co2EmissionCombined = 0.0,
    roadTaxPerQuarter = 50.0,
    tankCapacityKgH2 = 5.0
)

// ==================== Fakes ====================

private class FakeFuelCostCarRepository(vararg cars: Car) : CarRepository {
    val cars = cars.associateBy { it.id }.toMutableMap()

    override suspend fun getCarById(carId: Int): Car? = cars[carId]

    /** Only the consumption is filled in by the Gemini fallback. */
    override suspend fun updateCar(carId: Int, change: CarChangeDto): Car? {
        val car = cars[carId] as? BEVCar ?: return null
        return car.copy(consumptionCombined = change.consumptionCombined ?: car.consumptionCombined)
            .also { cars[carId] = it }
    }

    override suspend fun getAllCars(): List<Car> = TODO()
    override suspend fun getCarByLicensePlate(licensePlate: String): Car? = TODO()
    override suspend fun createCar(car: Car): Car = TODO()
    override suspend fun deleteCar(carId: Int): Boolean = TODO()
}

/** Returns [estimate], or fails like an unreachable Gemini API when [fail] is set. */
private class FakeFuelCostEstimator(
    private val estimate: CarCostEstimate? = null,
    private val fail: Boolean = false
) : CostEstimator {
    var calls = 0

    override suspend fun estimateCarCosts(car: Car): CarCostEstimate {
        calls++
        if (fail) throw IllegalStateException("Gemini niet bereikbaar")
        return estimate ?: TODO()
    }
}

// ==================== 1. Validation: calculateFuelCost() and validateDistanceKm() ====================

class FuelCostCalculationTest {
    @Test
    fun `benzineauto rekent met liters en de benzineprijs`() {
        val result = calculateFuelCost(iceCar(FuelType.PETROL, consumption = 10.0), 100.0)

        assertEquals("ICE", result.category)
        assertEquals("L", result.unit)
        assertEquals(PETROL_PRICE_PER_L, result.pricePerUnit)
        assertEquals(10 * PETROL_PRICE_PER_L, result.estimatedCost, TOLERANCE)
    }

    @Test
    fun `dieselauto rekent met de dieselprijs`() {
        val result = calculateFuelCost(iceCar(FuelType.DIESEL, consumption = 10.0), 100.0)

        assertEquals(10 * DIESEL_PRICE_PER_L, result.estimatedCost, TOLERANCE)
    }

    @Test
    fun `lpg-auto rekent met de lpg-prijs`() {
        val result = calculateFuelCost(iceCar(FuelType.LPG, consumption = 10.0), 100.0)

        assertEquals(10 * LPG_PRICE_PER_L, result.estimatedCost, TOLERANCE)
    }

    @Test
    fun `elektrische auto rekent met kWh en de stroomprijs`() {
        val result = calculateFuelCost(bevCar(consumption = 15.0), 200.0)

        assertEquals("BEV", result.category)
        assertEquals("kWh", result.unit)
        assertEquals(30 * ELECTRICITY_PRICE_PER_KWH, result.estimatedCost, TOLERANCE)
    }

    @Test
    fun `waterstofauto rekent met kg en de waterstofprijs`() {
        val result = calculateFuelCost(fcevCar(consumption = 0.8), 500.0)

        assertEquals("FCEV", result.category)
        assertEquals("kg", result.unit)
        assertEquals(4 * HYDROGEN_PRICE_PER_KG, result.estimatedCost, TOLERANCE)
    }

    @Test
    fun `nul km kost niets`() {
        assertEquals(0.0, calculateFuelCost(iceCar(), 0.0).estimatedCost)
    }

    @Test
    fun `kosten worden afgerond op 2 decimalen`() {
        val cost = calculateFuelCost(iceCar(consumption = 7.3), 33.3).estimatedCost

        assertEquals(round(cost * 100) / 100, cost)
    }

    @Test
    fun `auto zonder verbruik geeft UnprocessableException`() {
        assertFailsWith<UnprocessableException> { calculateFuelCost(bevCar(consumption = null), 100.0) }
    }

    @Test
    fun `afstand van 0 en precies het maximum worden geaccepteerd`() {
        validateDistanceKm(0.0)
        validateDistanceKm(MAX_DISTANCE_KM)
    }

    @Test
    fun `negatieve afstand geeft ValidationException`() {
        assertFailsWith<ValidationException> { validateDistanceKm(-0.01) }
    }

    @Test
    fun `afstand boven het maximum geeft ValidationException`() {
        assertFailsWith<ValidationException> { validateDistanceKm(MAX_DISTANCE_KM + 0.01) }
    }

    @Test
    fun `NaN en oneindig geven ValidationException`() {
        assertFailsWith<ValidationException> { validateDistanceKm(Double.NaN) }
        assertFailsWith<ValidationException> { validateDistanceKm(Double.POSITIVE_INFINITY) }
    }
}

// ==================== 2. Service: CarCostService ====================

class FuelCostServiceTest {
    private fun service(vararg cars: Car, costEstimator: CostEstimator = FakeFuelCostEstimator()) =
        CarCostService(costEstimator, FakeFuelCostCarRepository(*cars))

    // ---------- getFuelCost ----------

    @Test
    fun `eigenaar krijgt de verbruikskosten`() = runBlocking {
        val costEstimator = FakeFuelCostEstimator()

        val result = service(iceCar(consumption = 10.0), costEstimator = costEstimator).getFuelCost(CAR_ID, OWNER_ID, 100.0)

        assertEquals(CAR_ID, result.carId)
        assertEquals(100.0, result.distanceKm)
        assertEquals(10 * PETROL_PRICE_PER_L, result.estimatedCost, TOLERANCE)
        assertEquals(0, costEstimator.calls)
    }

    @Test
    fun `ontbrekend verbruik wordt aangevuld door Gemini en opgeslagen`() = runBlocking {
        val carRepository = FakeFuelCostCarRepository(bevCar(consumption = null))
        val costEstimator = FakeFuelCostEstimator(CarCostEstimate(roadTaxPerQuarter = 99.0, consumptionCombined = 15.0))
        val service = CarCostService(costEstimator, carRepository)

        val result = service.getFuelCost(CAR_ID, OWNER_ID, 200.0)

        assertEquals(30 * ELECTRICITY_PRICE_PER_KWH, result.estimatedCost, TOLERANCE)
        assertEquals(15.0, carRepository.cars[CAR_ID]?.consumptionCombined)
        assertEquals(50.0, carRepository.cars[CAR_ID]?.roadTaxPerQuarter)
    }

    @Test
    fun `Gemini onbereikbaar geeft ExternalServiceException`() = runBlocking {
        val service = service(bevCar(consumption = null), costEstimator = FakeFuelCostEstimator(fail = true))

        assertFailsWith<ExternalServiceException> { service.getFuelCost(CAR_ID, OWNER_ID, 100.0) }
        Unit
    }

    @Test
    fun `verbruik buiten bereik van Gemini geeft ExternalServiceException`() = runBlocking {
        val costEstimator = FakeFuelCostEstimator(CarCostEstimate(roadTaxPerQuarter = null, consumptionCombined = -1.0))
        val carRepository = FakeFuelCostCarRepository(bevCar(consumption = null))

        assertFailsWith<ExternalServiceException> { CarCostService(costEstimator, carRepository).getFuelCost(CAR_ID, OWNER_ID, 100.0) }
        assertEquals(null, carRepository.cars[CAR_ID]?.consumptionCombined)
    }

    @Test
    fun `Gemini zonder verbruik geeft UnprocessableException`() = runBlocking {
        val costEstimator = FakeFuelCostEstimator(CarCostEstimate(roadTaxPerQuarter = null, consumptionCombined = null))
        val service = service(bevCar(consumption = null), costEstimator = costEstimator)

        assertFailsWith<UnprocessableException> { service.getFuelCost(CAR_ID, OWNER_ID, 100.0) }
        Unit
    }

    @Test
    fun `onbekende auto geeft ResourceNotFoundException`() = runBlocking {
        assertFailsWith<ResourceNotFoundException> { service(iceCar()).getFuelCost(999, OWNER_ID, 100.0) }
        Unit
    }

    @Test
    fun `niet-eigenaar geeft ForbiddenException`() = runBlocking {
        assertFailsWith<ForbiddenException> { service(iceCar()).getFuelCost(CAR_ID, OTHER_USER_ID, 100.0) }
        Unit
    }

    @Test
    fun `negatieve afstand geeft ValidationException`() = runBlocking {
        assertFailsWith<ValidationException> { service(iceCar()).getFuelCost(CAR_ID, OWNER_ID, -1.0) }
        Unit
    }
}

// ==================== 3. E2E: fuel cost route ====================

class FuelCostRoutesTest {
    // Seed: car 1 is a petrol car of user 1, car 7 a BEV of user 2, car 3 an FCEV of user 3
    private val iceCarId = 1
    private val bevCarId = 7
    private val fcevCarId = 3

    private suspend fun HttpClient.getFuelCost(carId: Int, distanceKm: String?, token: String? = null): HttpResponse =
        get("/cars/$carId/fuel-cost" + (distanceKm?.let { "?distanceKm=$it" } ?: "")) {
            token?.let { bearerAuth(it) }
        }

    // ---------- GET /cars/{id}/fuel-cost ----------

    @Test
    fun `eigenaar van benzineauto krijgt kosten op basis van liters`() = e2eTest { client ->
        val token = client.login(1)

        val response = client.getFuelCost(iceCarId, "100", token)

        assertEquals(HttpStatusCode.OK, response.status)
        val result = response.body<FuelCostResponse>()
        assertEquals("ICE", result.category)
        assertEquals(8.1, result.consumptionPer100Km)
        assertEquals(8.1 * PETROL_PRICE_PER_L, result.estimatedCost, 0.01)
    }

    @Test
    fun `eigenaar van elektrische auto krijgt kosten op basis van kWh`() = e2eTest { client ->
        val token = client.login(2)

        val response = client.getFuelCost(bevCarId, "100", token)

        assertEquals(HttpStatusCode.OK, response.status)
        val result = response.body<FuelCostResponse>()
        assertEquals("BEV", result.category)
        assertEquals(14.5 * ELECTRICITY_PRICE_PER_KWH, result.estimatedCost, 0.01)
    }

    @Test
    fun `eigenaar van waterstofauto krijgt kosten op basis van kg`() = e2eTest { client ->
        val token = client.login(3)

        val response = client.getFuelCost(fcevCarId, "100", token)

        assertEquals(HttpStatusCode.OK, response.status)
        val result = response.body<FuelCostResponse>()
        assertEquals("FCEV", result.category)
        assertEquals(0.76 * HYDROGEN_PRICE_PER_KG, result.estimatedCost, 0.01)
    }

    @Test
    fun `zonder token geeft 401`() = e2eTest { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.getFuelCost(iceCarId, "100").status)
    }

    @Test
    fun `niet-eigenaar geeft 403`() = e2eTest { client ->
        val token = client.login(OTHER_USER_ID)

        assertEquals(HttpStatusCode.Forbidden, client.getFuelCost(iceCarId, "100", token).status)
    }

    @Test
    fun `onbekende auto geeft 404`() = e2eTest { client ->
        val token = client.login(1)

        assertEquals(HttpStatusCode.NotFound, client.getFuelCost(999, "100", token).status)
    }

    @Test
    fun `negatieve afstand geeft 400`() = e2eTest { client ->
        val token = client.login(1)

        assertEquals(HttpStatusCode.BadRequest, client.getFuelCost(iceCarId, "-5", token).status)
    }

    @Test
    fun `afstand die geen getal is geeft 400`() = e2eTest { client ->
        val token = client.login(1)

        assertEquals(HttpStatusCode.BadRequest, client.getFuelCost(iceCarId, "abc", token).status)
    }

    @Test
    fun `ontbrekende afstand geeft 400`() = e2eTest { client ->
        val token = client.login(1)

        assertEquals(HttpStatusCode.BadRequest, client.getFuelCost(iceCarId, null, token).status)
    }
}
