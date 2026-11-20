import io.ktor.client.HttpClient
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import repositories.ExposedLocationRepository
import contracts.requests.LocationRequest
import tables.LocationTable
import kotlin.test.assertNull
import config.PhotoStorage
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.io.files.SystemTemporaryDirectory
import models.Location
import repositories.CarRepository
import repositories.LocationRepository
import repositories.PhotoRepository
import repositories.RentalTermsRepository
import repositories.ReservationRepository
import services.CarFilter
import services.CarService
import services.ValidationException
import services.validate
import contracts.models.BEVCar
import contracts.models.Car
import contracts.models.Photo
import contracts.models.RentalTerms
import contracts.models.Reservation
import contracts.models.ReservationStatus
import contracts.models.VehicleType
import contracts.requests.CarChangeDto
import contracts.requests.RentalTermsRequest
import contracts.responses.CarMapResponse
import io.ktor.client.request.bearerAuth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant
import kotlin.uuid.Uuid

// ==================== Test data ====================

private const val OWNER_ID = 1
private const val FREE_CAR_ID = 10
private const val RESERVED_CAR_ID = 11
private const val BARE_CAR_ID = 12 // no location and no rental terms
private const val PRICE_PER_DAY = 45.0
private const val LATITUDE = 51.5853
private const val LONGITUDE = 4.7946

private val periodStart = Instant.parse("2099-03-01T09:00:00Z")
private val periodEnd = Instant.parse("2099-03-01T17:00:00Z")

private fun testLocation(carId: Int) = Location(
    id = 1,
    carId = carId,
    latitude = LATITUDE,
    longitude = LONGITUDE,
    speed = null,
    timestamp = Instant.parse("2099-02-28T12:00:00Z")
)

private fun testCar(id: Int): Car = BEVCar(
    id = id,
    userId = OWNER_ID,
    licensePlate = "AB-12$id-C",
    brand = "Tesla",
    model = "Model 3",
    productionYear = 2022,
    trim = null,
    color = "Wit",
    seats = 5,
    doors = 4,
    vehicleType = VehicleType.PASSENGER_CAR,
    readyToDriveWeightKg = 1800,
    consumptionCombined = 15.0,
    co2EmissionCombined = 0.0,
    roadTaxPerQuarter = null,
    batteryCapacityKWh = 60.0
)

// ==================== Fakes ====================

private class FakeSearchCarRepository(vararg cars: Car) : CarRepository {
    private val cars = cars.toList()

    override suspend fun getAllCars(): List<Car> = cars

    override suspend fun getCarByLicensePlate(licensePlate: String): Car? = TODO()
    override suspend fun getCarById(carId: Int): Car? = TODO()
    override suspend fun getCarsByUserId(userId: Int): List<Car> = TODO()
    override suspend fun createCar(car: Car): Car = TODO()
    override suspend fun updateCar(carId: Int, change: CarChangeDto): Car? = TODO()
    override suspend fun deleteCar(carId: Int): Boolean = TODO()
}

/** Returns [reservedCarIds] for any period and remembers the period it was asked for. */
private class FakeSearchReservationRepository(private val reservedCarIds: Set<Int>) : ReservationRepository {
    var requestedPeriod: Pair<Instant, Instant>? = null

    override suspend fun getReservedCarIds(startTime: Instant, endTime: Instant): Set<Int> {
        requestedPeriod = startTime to endTime
        return reservedCarIds
    }

    override suspend fun createReservationIfAvailable(
        carId: Int, userId: Int, startTime: Instant, endTime: Instant, status: ReservationStatus, totalPrice: Double
    ): Reservation? = TODO()
    override suspend fun getReservationById(id: Int): Reservation? = TODO()
    override suspend fun updateStatus(id: Int, status: ReservationStatus): Reservation? = TODO()
    override suspend fun getReservationsByUserId(userId: Int): List<Reservation> = TODO()
}

/** Counts the bulk calls, so a test can check that the locations are not fetched per car. */
private class FakeSearchLocationRepository(vararg locations: Location) : LocationRepository {
    private val locations = locations.associateBy { it.carId }
    var bulkCalls = 0

    override suspend fun getLatestLocationsByCarId(): Map<Int, Location> {
        bulkCalls++
        return locations
    }

    override suspend fun getLatestLocationByCarId(carId: Int): Location? = TODO()
    override suspend fun addLocation(carId: Int, latitude: Double, longitude: Double, timestamp: Instant): Location = TODO()
}

/** Counts the bulk calls, so a test can check that the rental terms are not fetched per car. */
private class FakeSearchRentalTermsRepository(vararg rentalTerms: RentalTerms) : RentalTermsRepository {
    private val rentalTerms = rentalTerms.toList()
    var bulkCalls = 0

    override suspend fun getAllRentalTerms(): List<RentalTerms> {
        bulkCalls++
        return rentalTerms
    }

    override suspend fun carExists(carId: Int): Boolean = TODO()
    override suspend fun getRentalTermsByCarId(carId: Int): RentalTerms? = TODO()
    override suspend fun saveRentalTerms(carId: Int, request: RentalTermsRequest): RentalTerms = TODO()
}

private class UnusedSearchPhotoRepository : PhotoRepository {
    override suspend fun createPhoto(carId: Int, fileName: String): Photo = TODO()
    override suspend fun getPhotosByCarId(carId: Int): List<Photo> = TODO()
    override suspend fun getFileName(photoId: Int): String? = TODO()
    override suspend fun getFileNamesByCarId(carId: Int): List<String> = TODO()
}

// ==================== 1. Validation: CarFilter availability ====================

class CarAvailabilityFilterValidationTest {

    @Test
    fun `startTime en endTime samen is geldig`() {
        CarFilter(startTime = periodStart, endTime = periodEnd).validate()
    }

    @Test
    fun `alleen startTime geeft ValidationException`() {
        assertFailsWith<ValidationException> { CarFilter(startTime = periodStart).validate() }
    }

    @Test
    fun `alleen endTime geeft ValidationException`() {
        assertFailsWith<ValidationException> { CarFilter(endTime = periodEnd).validate() }
    }

    @Test
    fun `eind voor begin geeft ValidationException`() {
        assertFailsWith<ValidationException> { CarFilter(startTime = periodEnd, endTime = periodStart).validate() }
    }

    @Test
    fun `eind gelijk aan begin geeft ValidationException`() {
        assertFailsWith<ValidationException> { CarFilter(startTime = periodStart, endTime = periodStart).validate() }
    }
}

// ==================== 2. Service: CarService search ====================

class CarSearchServiceTest {
    private val reservationRepository = FakeSearchReservationRepository(setOf(RESERVED_CAR_ID))
    private val rentalTermsRepository = FakeSearchRentalTermsRepository(
        RentalTerms(id = 1, carId = FREE_CAR_ID, pricePerDay = PRICE_PER_DAY, pickupLocation = "Breda")
    )

    private val locationRepository = FakeSearchLocationRepository(testLocation(FREE_CAR_ID))

    private val service = CarService(
        FakeSearchCarRepository(testCar(FREE_CAR_ID), testCar(RESERVED_CAR_ID), testCar(BARE_CAR_ID)),
        locationRepository,
        UnusedSearchPhotoRepository(),
        PhotoStorage(Path(SystemTemporaryDirectory, "car-search-test-${Uuid.random()}")),
        rentalTermsRepository,
        reservationRepository
    )

    // ---------- getCarMap ----------

    @Test
    fun `kaart geeft per auto de laatste positie en de prijs per dag`() = runBlocking {
        val markers = service.getCarMap(CarFilter())

        val marker = markers.single { it.car.id == FREE_CAR_ID }
        assertEquals(LATITUDE, marker.latitude)
        assertEquals(LONGITUDE, marker.longitude)
        assertEquals(PRICE_PER_DAY, marker.pricePerDay)
        assertEquals(1, rentalTermsRepository.bulkCalls)
        assertEquals(1, locationRepository.bulkCalls)
    }

    @Test
    fun `auto zonder locatie en huurvoorwaarden staat op de kaart zonder positie en prijs`() = runBlocking {
        val marker = service.getCarMap(CarFilter()).single { it.car.id == BARE_CAR_ID }

        assertEquals(CarMapResponse(testCar(BARE_CAR_ID), null, null, null), marker)
    }

    @Test
    fun `kaart laat gereserveerde auto weg bij een tijdvak`() = runBlocking {
        val markers = service.getCarMap(CarFilter(startTime = periodStart, endTime = periodEnd))

        assertEquals(listOf(FREE_CAR_ID, BARE_CAR_ID), markers.map { it.car.id })
    }

    @Test
    fun `kaart past de prijsfilter toe`() = runBlocking {
        val markers = service.getCarMap(CarFilter(maxPrice = PRICE_PER_DAY))

        assertEquals(listOf(FREE_CAR_ID), markers.map { it.car.id })
    }

    @Test
    fun `kaart met ongeldig tijdvak geeft ValidationException`() = runBlocking {
        assertFailsWith<ValidationException> { service.getCarMap(CarFilter(startTime = periodEnd, endTime = periodStart)) }
        Unit
    }

    // ---------- getCars ----------

    @Test
    fun `gereserveerde auto valt weg bij een tijdvak`() = runBlocking {
        val cars = service.getCars(CarFilter(startTime = periodStart, endTime = periodEnd))

        assertEquals(listOf(FREE_CAR_ID, BARE_CAR_ID), cars.map { it.id })
        assertEquals(periodStart to periodEnd, reservationRepository.requestedPeriod)
    }

    @Test
    fun `zonder tijdvak komen alle auto's terug zonder reserveringen op te vragen`() = runBlocking {
        val cars = service.getCars(CarFilter())

        assertEquals(listOf(FREE_CAR_ID, RESERVED_CAR_ID, BARE_CAR_ID), cars.map { it.id })
        assertEquals(null, reservationRepository.requestedPeriod)
    }

    @Test
    fun `afstandsfilter haalt alle locaties in een keer op`() = runBlocking {
        val cars = service.getCars(CarFilter(maxDistanceKm = 1.0, latitude = LATITUDE, longitude = LONGITUDE))

        assertEquals(listOf(FREE_CAR_ID), cars.map { it.id })
        assertEquals(1, locationRepository.bulkCalls)
    }

    @Test
    fun `ongeldig tijdvak geeft ValidationException`() = runBlocking {
        assertFailsWith<ValidationException> { service.getCars(CarFilter(startTime = periodStart)) }
        Unit
    }
}

// ==================== 3. E2E: car search routes ====================

class CarSearchRoutesTest {
    // Seed: reservering 4 (auto 4, CONFIRMED) valt op 2099-01-15,
    // reservering 3 (auto 3, CANCELLED) op 2026-10-01. Er zijn 8 seedauto's.
    private val seedCarCount = 8
    private val reservedSeedCarId = 4
    private val reservedDay = "startTime=2099-01-15T00:00:00Z&endTime=2099-01-16T00:00:00Z"
    private val cancelledDay = "startTime=2026-10-01T00:00:00Z&endTime=2026-10-02T00:00:00Z"
    // Seed: auto 3 staat in Utrecht en is van gebruiker 3
    private val utrechtSeedCarId = 3

    private suspend fun HttpClient.putLocation(carId: Int, token: String): HttpResponse =
        put("/cars/$carId/location") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(LocationRequest(LATITUDE, LONGITUDE))
        }

    // ---------- GET /cars ----------

    @Test
    fun `auto met een actieve reservering in het tijdvak valt weg`() = e2eTest { client ->
        val response = client.get("/cars?$reservedDay")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals((1..seedCarCount).filter { it != reservedSeedCarId }, response.body<List<Car>>().map { it.id }.sorted())
    }

    @Test
    fun `geannuleerde reservering blokkeert de auto niet`() = e2eTest { client ->
        val response = client.get("/cars?$cancelledDay")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals((1..seedCarCount).toList(), response.body<List<Car>>().map { it.id }.sorted())
    }

    @Test
    fun `tijdvak wordt gecombineerd met de andere filters`() = e2eTest { client ->
        // maxPrice=45 alleen geeft auto 4 en 6
        val response = client.get("/cars?maxPrice=45&$reservedDay")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(listOf(6), response.body<List<Car>>().map { it.id })
    }

    @Test
    fun `afstandsfilter gebruikt de nieuwste locatie na het verplaatsen van een auto`() = e2eTest { client ->
        val token = client.login(utrechtSeedCarId)
        client.putLocation(utrechtSeedCarId, token)

        val response = client.get("/cars?maxDistanceKm=5&latitude=$LATITUDE&longitude=$LONGITUDE")

        // Seed: alleen auto 4 staat binnen 5 km van Breda; auto 3 staat in Utrecht tot hij wordt verplaatst
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(listOf(utrechtSeedCarId, 4), response.body<List<Car>>().map { it.id }.sorted())
    }

    @Test
    fun `tijdstip dat geen ISO-8601 is geeft 400`() = e2eTest { client ->
        val response = client.get("/cars?startTime=morgen&endTime=2099-01-15T00:00:00Z")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    // ---------- GET /cars/map ----------

    @Test
    fun `kaart geeft alle auto's met laatste positie en prijs`() = e2eTest { client ->
        val token = client.login(2)

        val response = client.get("/cars/map") { bearerAuth(token) }

        // Seed: laatste locatie van auto 1 is 51.643610, 4.860440; auto 1 kost 89.00, auto 8 heeft geen huurvoorwaarden
        assertEquals(HttpStatusCode.OK, response.status)
        val markers = response.body<List<CarMapResponse>>()
        assertEquals((1..seedCarCount).toList(), markers.map { it.car.id }.sorted())
        val car1 = markers.single { it.car.id == 1 }
        assertEquals(51.643610, car1.latitude)
        assertEquals(4.860440, car1.longitude)
        assertEquals(89.0, car1.pricePerDay)
        assertEquals(null, markers.single { it.car.id == 8 }.pricePerDay)
    }

    @Test
    fun `kaart toont de nieuwe positie na het verplaatsen van een auto`() = e2eTest { client ->
        val token = client.login(utrechtSeedCarId)
        client.putLocation(utrechtSeedCarId, token)

        val response = client.get("/cars/map") { bearerAuth(token) }

        assertEquals(HttpStatusCode.OK, response.status)
        val marker = response.body<List<CarMapResponse>>().single { it.car.id == utrechtSeedCarId }
        assertEquals(LATITUDE, marker.latitude)
        assertEquals(LONGITUDE, marker.longitude)
    }

    @Test
    fun `kaart toont een auto zonder locatie zonder positie`() = e2eTest { client ->
        val token = client.login(2)
        suspendTransaction { LocationTable.deleteWhere { LocationTable.carId eq 8 } }

        val response = client.get("/cars/map") { bearerAuth(token) }

        assertEquals(HttpStatusCode.OK, response.status)
        val marker = response.body<List<CarMapResponse>>().single { it.car.id == 8 }
        assertNull(marker.latitude)
        assertNull(marker.longitude)
    }

    @Test
    fun `kaart met tijdvak laat de gereserveerde auto weg`() = e2eTest { client ->
        val token = client.login(2)

        val response = client.get("/cars/map?$reservedDay") { bearerAuth(token) }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals((1..seedCarCount).filter { it != reservedSeedCarId }, response.body<List<CarMapResponse>>().map { it.car.id }.sorted())
    }

    @Test
    fun `kaart werkt zonder token en botst niet met cars id`() = e2eTest { client ->
        val response = client.get("/cars/map")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals((1..seedCarCount).toList(), response.body<List<CarMapResponse>>().map { it.car.id }.sorted())
    }

    @Test
    fun `kaart met eind voor begin geeft 400`() = e2eTest { client ->
        val token = client.login(2)

        val response = client.get("/cars/map?startTime=2099-01-16T00:00:00Z&endTime=2099-01-15T00:00:00Z") { bearerAuth(token) }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    // ---------- LocationRepository.getLatestLocationsByCarId ----------

    @Test
    fun `bulkquery geeft per auto dezelfde laatste locatie als de query per auto`() = e2eTest { client ->
        client.login(1)
        val repository = ExposedLocationRepository()
        val timestamp = Instant.parse("2099-01-01T12:00:00Z")
        repository.addLocation(1, 52.0, 5.0, timestamp)
        val newest = repository.addLocation(1, LATITUDE, LONGITUDE, timestamp)

        val latest = repository.getLatestLocationsByCarId()

        // Gelijk tijdstip: de rij met het hoogste id wint
        assertEquals(newest, latest[1])
        assertEquals((1..seedCarCount).toSet(), latest.keys)
        for ((carId, location) in latest) assertEquals(repository.getLatestLocationByCarId(carId), location)
    }
}
