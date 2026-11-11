import shared.requests.CarChangeDto
import io.ktor.serialization.kotlinx.json.DefaultJson
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import models.*
import shared.models.*
import mappers.toResponse
import shared.responses.ErrorResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

// ==================== Test data ====================

private val iceCar = ICECar(
    id = 1,
    userId = 1,
    licensePlate = "KS711F",
    brand = "Volkswagen",
    model = "Golf",
    productionYear = 2018,
    trim = null,
    color = "Grijs",
    seats = 5,
    doors = 5,
    vehicleType = VehicleType.PASSENGER_CAR,
    readyToDriveWeightKg = 1300,
    consumptionCombined = 5.2,
    co2EmissionCombined = 120.0,
    roadTaxPerQuarter = 150.0,
    fuelType = FuelType.PETROL,
    tankCapacityL = 50.0,
    automaticTransmission = false
)

private val bevCar = BEVCar(
    id = 2,
    userId = 2,
    licensePlate = "R003BJ",
    brand = "Tesla",
    model = "Model 3",
    productionYear = 2022,
    trim = "Long Range",
    color = "Wit",
    seats = 5,
    doors = 4,
    vehicleType = VehicleType.PASSENGER_CAR,
    readyToDriveWeightKg = 1850,
    consumptionCombined = 15.0,
    co2EmissionCombined = 0.0,
    roadTaxPerQuarter = null,
    batteryCapacityKWh = 75.0
)

private val fcevCar = FCEVCar(
    id = 3,
    userId = 3,
    licensePlate = "XY-123-Z",
    brand = "Toyota",
    model = "Mirai",
    productionYear = 2021,
    trim = null,
    color = "Blauw",
    seats = 5,
    doors = 4,
    vehicleType = VehicleType.PASSENGER_CAR,
    readyToDriveWeightKg = 1900,
    consumptionCombined = 0.8,
    co2EmissionCombined = 0.0,
    roadTaxPerQuarter = 0.0,
    tankCapacityKgH2 = 5.6
)

private fun encodeCar(car: Car): JsonObject = DefaultJson.parseToJsonElement(DefaultJson.encodeToString<Car>(car)).jsonObject

private fun JsonObject.string(key: String): String = getValue(key).jsonPrimitive.content

// ==================== 1. Serialisatie: contract ====================

class ContractSerializationTest {

    @Test
    fun `ICECar heeft type ICECar in de JSON`() {
        val json = DefaultJson.encodeToString<Car>(iceCar)

        val obj = DefaultJson.parseToJsonElement(json).jsonObject
        assertEquals("ICECar", obj.string("type"))
        assertEquals(iceCar, DefaultJson.decodeFromString<Car>(json))
    }

    @Test
    fun `BEVCar en FCEVCar hebben hun eigen type`() {
        val bevJson = DefaultJson.encodeToString<Car>(bevCar)
        val fcevJson = DefaultJson.encodeToString<Car>(fcevCar)

        assertEquals("BEVCar", encodeCar(bevCar).string("type"))
        assertEquals("FCEVCar", encodeCar(fcevCar).string("type"))
        assertEquals(bevCar, DefaultJson.decodeFromString<Car>(bevJson))
        assertEquals(fcevCar, DefaultJson.decodeFromString<Car>(fcevJson))
    }

    @Test
    fun `Reservation zet Instant als ISO-string en status als naam`() {
        val reservation = Reservation(
            id = 1,
            carId = 1,
            userId = 2,
            startTime = Instant.parse("2026-01-01T10:00:00Z"),
            endTime = Instant.parse("2026-01-02T10:00:00Z"),
            status = ReservationStatus.PENDING,
            totalPrice = 125.5
        )

        val obj = DefaultJson.parseToJsonElement(DefaultJson.encodeToString(reservation)).jsonObject

        assertEquals("2026-01-01T10:00:00Z", obj.string("startTime"))
        assertEquals("PENDING", obj.string("status"))
    }

    @Test
    fun `Reservation zet totalPrice als getal`() {
        val reservation = Reservation(
            id = 1,
            carId = 1,
            userId = 2,
            startTime = Instant.parse("2026-01-01T10:00:00Z"),
            endTime = Instant.parse("2026-01-02T10:00:00Z"),
            status = ReservationStatus.CONFIRMED,
            totalPrice = 125.5
        )

        val json = DefaultJson.encodeToString(reservation)

        assertEquals("125.5", DefaultJson.parseToJsonElement(json).jsonObject.string("totalPrice"))
        assertEquals(reservation, DefaultJson.decodeFromString<Reservation>(json))
    }

    @Test
    fun `Ride zet RideStatus als naam`() {
        val ride = Ride(
            id = 1,
            reservationId = 1,
            startTime = Instant.parse("2026-01-01T10:00:00Z"),
            endTime = null,
            status = RideStatus.IN_PROGRESS,
            distanceM = null,
            avgAcceleration = null,
            avgDeceleration = null,
            bonusPoints = null
        )

        val obj = DefaultJson.parseToJsonElement(DefaultJson.encodeToString(ride)).jsonObject

        assertEquals("IN_PROGRESS", obj.string("status"))
    }

    @Test
    fun `RideStatus is zelfstandig serialiseerbaar als naam`() {
        val json = DefaultJson.encodeToString(RideStatus.COMPLETED)

        assertEquals("\"COMPLETED\"", json)
        assertEquals(RideStatus.COMPLETED, DefaultJson.decodeFromString<RideStatus>(json))
    }

    @Test
    fun `RentalTerms zet LocalTime als uur en minuten`() {
        val terms = RentalTerms(
            id = 1,
            carId = 1,
            pricePerDay = 50.0,
            pickupLocation = "Breda",
            pickupTimeStart = LocalTime(9, 0)
        )

        val obj = DefaultJson.parseToJsonElement(DefaultJson.encodeToString(terms)).jsonObject

        assertEquals("09:00", obj.string("pickupTimeStart"))
    }

    @Test
    fun `UserResponse bevat alleen id en email`() {
        val response = User(1, "a@b.nl", "hash").toResponse()

        val obj = DefaultJson.parseToJsonElement(DefaultJson.encodeToString(response)).jsonObject

        assertEquals(setOf("id", "email"), obj.keys)
    }

    @Test
    fun `ErrorResponse bevat status en message`() {
        val error = ErrorResponse(404, "Auto 1 niet gevonden")

        val json = DefaultJson.encodeToString(error)

        val obj = DefaultJson.parseToJsonElement(json).jsonObject
        assertEquals(setOf("status", "message"), obj.keys)
        assertEquals("404", obj.string("status"))
        assertEquals(error, DefaultJson.decodeFromString<ErrorResponse>(json))
    }

    @Test
    fun `CarChangeDto leest enums uit de JSON`() {
        val json = """{"fuelType":"DIESEL","vehicleType":"PASSENGER_CAR"}"""

        val dto = DefaultJson.decodeFromString<CarChangeDto>(json)

        assertEquals(FuelType.DIESEL, dto.fuelType)
        assertEquals(VehicleType.PASSENGER_CAR, dto.vehicleType)
    }
}
