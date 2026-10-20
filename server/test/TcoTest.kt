import clients.CostEstimator
import dto.CarChangeDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import models.*
import models.gemini.CarCostEstimate
import repositories.*
import responses.TcoResponse
import services.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

// ==================== Test data ====================

private const val OWNER_ID = 1
private const val OTHER_USER_ID = 2
private const val CAR_ID = 10
private const val MAX_ANNUAL_KM = 1_000_000.0
private const val TOLERANCE = 0.001

private fun iceCar(consumption: Double? = 10.0, roadTaxPerQuarter: Double? = 100.0): Car = ICECar(
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
    roadTaxPerQuarter = roadTaxPerQuarter,
    fuelType = FuelType.PETROL,
    tankCapacityL = 50.0,
    automaticTransmission = false
)

private fun bevCar(): Car = BEVCar(
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
    consumptionCombined = 15.0,
    co2EmissionCombined = 0.0,
    roadTaxPerQuarter = 50.0,
    batteryCapacityKWh = 60.0
)

// ==================== Fakes ====================

private class FakeTcoCarRepository(vararg cars: Car) : CarRepository {
    val cars = cars.associateBy { it.id }.toMutableMap()

    override suspend fun getCarById(carId: Int): Car? = cars[carId]

    override suspend fun updateCar(carId: Int, change: CarChangeDto): Car? {
        val car = cars[carId] as? ICECar ?: return null
        return car.copy(
            consumptionCombined = change.consumptionCombined ?: car.consumptionCombined,
            roadTaxPerQuarter = change.roadTaxPerQuarter ?: car.roadTaxPerQuarter
        ).also { cars[carId] = it }
    }

    override suspend fun getAllCars(): List<Car> = TODO()
    override suspend fun getCarByLicensePlate(licensePlate: String): Car? = TODO()
    override suspend fun createCar(car: Car): Car = TODO()
    override suspend fun deleteCar(carId: Int): Boolean = TODO()
}

private class FakeTcoCostEstimator(
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

// ==================== 1. Validation: calculateTco() and validateAnnualKm() ====================

class TcoCalculationTest {
    @Test
    fun `standaard wordt met 15000 km per jaar gerekend`() {
        assertEquals(15_000.0, DEFAULT_ANNUAL_KM)
    }

    @Test
    fun `tco is verbruikskosten plus vier kwartalen wegenbelasting`() {
        val result = calculateTco(iceCar(consumption = 10.0, roadTaxPerQuarter = 100.0), 15_000.0)

        assertEquals(CAR_ID, result.carId)
        assertEquals(15_000.0, result.annualKm)
        assertEquals(1_500 * PETROL_PRICE_PER_L, result.annualFuelCost, TOLERANCE)
        assertEquals(400.0, result.annualRoadTax)
        assertEquals(1_500 * PETROL_PRICE_PER_L + 400, result.annualTotal, TOLERANCE)
    }

    @Test
    fun `verbruikskosten zijn dezelfde als bij fuel-cost`() {
        val car = bevCar()

        assertEquals(calculateFuelCost(car, 12_345.0).estimatedCost, calculateTco(car, 12_345.0).annualFuelCost)
    }

    @Test
    fun `maandbedrag is het jaarbedrag gedeeld door 12 afgerond op centen`() {
        val result = calculateTco(iceCar(roadTaxPerQuarter = 100.0), 0.0)

        assertEquals(33.33, result.monthlyTotal)
    }

    @Test
    fun `nul km kost alleen wegenbelasting`() {
        val result = calculateTco(iceCar(roadTaxPerQuarter = 100.0), 0.0)

        assertEquals(0.0, result.annualFuelCost)
        assertEquals(400.0, result.annualTotal)
    }

    @Test
    fun `auto zonder wegenbelasting geeft UnprocessableException`() {
        assertFailsWith<UnprocessableException> { calculateTco(iceCar(roadTaxPerQuarter = null), 15_000.0) }
    }

    @Test
    fun `auto zonder verbruik geeft UnprocessableException`() {
        assertFailsWith<UnprocessableException> { calculateTco(iceCar(consumption = null), 15_000.0) }
    }

    @Test
    fun `0 km en precies het maximum worden geaccepteerd`() {
        validateAnnualKm(0.0)
        validateAnnualKm(MAX_ANNUAL_KM)
    }

    @Test
    fun `negatief aantal km geeft ValidationException`() {
        assertFailsWith<ValidationException> { validateAnnualKm(-0.01) }
    }

    @Test
    fun `aantal km boven het maximum geeft ValidationException`() {
        assertFailsWith<ValidationException> { validateAnnualKm(MAX_ANNUAL_KM + 0.01) }
    }

    @Test
    fun `NaN en oneindig geven ValidationException`() {
        assertFailsWith<ValidationException> { validateAnnualKm(Double.NaN) }
        assertFailsWith<ValidationException> { validateAnnualKm(Double.POSITIVE_INFINITY) }
    }
}

// ==================== 2. Service: CarCostService ====================

class TcoServiceTest {
    private val costEstimator = FakeTcoCostEstimator()
    private val service = CarCostService(costEstimator, FakeTcoCarRepository(iceCar()))

    // ---------- getTco ----------

    @Test
    fun `eigenaar krijgt de tco`() = runBlocking {
        val result = service.getTco(CAR_ID, OWNER_ID, 15_000.0)

        assertEquals(calculateTco(iceCar(), 15_000.0), result)
        assertEquals(0, costEstimator.calls)
    }

    @Test
    fun `auto met alle kostendata wordt zelf teruggegeven zonder Gemini`() = runBlocking {
        val car = iceCar()
        val carRepository = FakeTcoCarRepository().apply { cars[CAR_ID] = car }
        val service = CarCostService(costEstimator, carRepository)

        val result = service.fillMissingCostData(CAR_ID, OWNER_ID)

        // Same object, not just an equal copy
        assertTrue(result === car)
        assertEquals(0, costEstimator.calls)
    }

    @Test
    fun `ontbrekende wegenbelasting wordt aangevuld door Gemini en opgeslagen`() = runBlocking {
        val carRepository = FakeTcoCarRepository(iceCar(roadTaxPerQuarter = null))
        val costEstimator = FakeTcoCostEstimator(CarCostEstimate(roadTaxPerQuarter = 100.0, consumptionCombined = 99.0))

        val result = CarCostService(costEstimator, carRepository).getTco(CAR_ID, OWNER_ID, 15_000.0)

        assertEquals(calculateTco(iceCar(roadTaxPerQuarter = 100.0), 15_000.0), result)
        assertEquals(100.0, carRepository.cars[CAR_ID]?.roadTaxPerQuarter)
        assertEquals(10.0, carRepository.cars[CAR_ID]?.consumptionCombined)
    }

    @Test
    fun `Gemini onbereikbaar geeft ExternalServiceException`() = runBlocking {
        val service = CarCostService(FakeTcoCostEstimator(fail = true), FakeTcoCarRepository(iceCar(roadTaxPerQuarter = null)))

        assertFailsWith<ExternalServiceException> { service.getTco(CAR_ID, OWNER_ID, 15_000.0) }
        Unit
    }

    @Test
    fun `Gemini zonder wegenbelasting geeft UnprocessableException`() = runBlocking {
        val costEstimator = FakeTcoCostEstimator(CarCostEstimate(roadTaxPerQuarter = null, consumptionCombined = null))
        val service = CarCostService(costEstimator, FakeTcoCarRepository(iceCar(roadTaxPerQuarter = null)))

        assertFailsWith<UnprocessableException> { service.getTco(CAR_ID, OWNER_ID, 15_000.0) }
        Unit
    }

    @Test
    fun `onbekende auto geeft ResourceNotFoundException`() = runBlocking {
        assertFailsWith<ResourceNotFoundException> { service.getTco(999, OWNER_ID, 15_000.0) }
        Unit
    }

    @Test
    fun `niet-eigenaar geeft ForbiddenException`() = runBlocking {
        assertFailsWith<ForbiddenException> { service.getTco(CAR_ID, OTHER_USER_ID, 15_000.0) }
        Unit
    }

    @Test
    fun `negatief aantal km geeft ValidationException`() = runBlocking {
        assertFailsWith<ValidationException> { service.getTco(CAR_ID, OWNER_ID, -1.0) }
        Unit
    }

    @Test
    fun `niet-eigenaar met ongeldig aantal km geeft ForbiddenException`() = runBlocking {
        assertFailsWith<ForbiddenException> { service.getTco(CAR_ID, OTHER_USER_ID, -1.0) }
        Unit
    }
}

// ==================== 3. E2E: tco route ====================

class TcoRoutesTest {
    // Seed: car 1 is a petrol car of user 1 with a consumption of 8.1 L/100 km and no road tax
    private val carId = 1
    private val roadTaxPerQuarter = 150.0

    private suspend fun HttpClient.getTco(carId: Int, annualKm: String? = null, token: String? = null): HttpResponse =
        get("/cars/$carId/tco" + (annualKm?.let { "?annualKm=$it" } ?: "")) {
            token?.let { bearerAuth(it) }
        }

    /** Fills in the road tax, so the test does not depend on Gemini. */
    private suspend fun HttpClient.setRoadTax(token: String) {
        patch("/cars/$carId") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(CarChangeDto(roadTaxPerQuarter = roadTaxPerQuarter))
        }
    }

    // ---------- GET /cars/{id}/tco ----------

    @Test
    fun `zonder annualKm wordt met 15000 km gerekend`() = e2eTest { client ->
        val token = client.login(OWNER_ID)
        client.setRoadTax(token)

        val response = client.getTco(carId, token = token)

        assertEquals(HttpStatusCode.OK, response.status)
        val result = response.body<TcoResponse>()
        assertEquals(15_000.0, result.annualKm)
        assertEquals(1_215 * PETROL_PRICE_PER_L, result.annualFuelCost, 0.01)
        assertEquals(600.0, result.annualRoadTax)
        assertEquals(1_215 * PETROL_PRICE_PER_L + 600, result.annualTotal, 0.01)
        assertEquals(result.annualTotal / 12, result.monthlyTotal, 0.01)
    }

    @Test
    fun `meegegeven annualKm wordt gebruikt`() = e2eTest { client ->
        val token = client.login(OWNER_ID)
        client.setRoadTax(token)

        val response = client.getTco(carId, "10000", token)

        assertEquals(HttpStatusCode.OK, response.status)
        val result = response.body<TcoResponse>()
        assertEquals(10_000.0, result.annualKm)
        assertEquals(810 * PETROL_PRICE_PER_L, result.annualFuelCost, 0.01)
    }

    @Test
    fun `zonder token geeft 401`() = e2eTest { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.getTco(carId).status)
    }

    @Test
    fun `niet-eigenaar geeft 403`() = e2eTest { client ->
        val token = client.login(OTHER_USER_ID)

        assertEquals(HttpStatusCode.Forbidden, client.getTco(carId, token = token).status)
    }

    @Test
    fun `onbekende auto geeft 404`() = e2eTest { client ->
        val token = client.login(OWNER_ID)

        assertEquals(HttpStatusCode.NotFound, client.getTco(999, token = token).status)
    }

    @Test
    fun `negatief aantal km geeft 400`() = e2eTest { client ->
        val token = client.login(OWNER_ID)

        assertEquals(HttpStatusCode.BadRequest, client.getTco(carId, "-5", token).status)
    }

    @Test
    fun `aantal km dat geen getal is geeft 400`() = e2eTest { client ->
        val token = client.login(OWNER_ID)

        assertEquals(HttpStatusCode.BadRequest, client.getTco(carId, "abc", token).status)
    }
}
