import dto.RentalTermsRequest
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalTime
import models.RentalTerms
import repositories.RentalTermsRepository
import services.RentalTermsService
import services.ResourceNotFoundException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// ==================== Test data ====================

private const val SEED_CAR_ID = 1
private const val CAR_WITH_TERMS = 10
private const val CAR_WITHOUT_TERMS = 11

private val storedRentalTerms = RentalTerms(
    id = 1,
    carId = CAR_WITH_TERMS,
    pricePerDay = 45.0,
    pickupLocation = "Grote Markt 1, Breda"
)

// ==================== Fakes ====================

private class FakeStoredRentalTermsRepository(
    private val carIds: Set<Int>,
    vararg rentalTerms: RentalTerms
) : RentalTermsRepository {
    private val rentalTerms = rentalTerms.associateBy { it.carId }

    override suspend fun carExists(carId: Int): Boolean = carId in carIds
    override suspend fun getRentalTermsByCarId(carId: Int): RentalTerms? = rentalTerms[carId]

    override suspend fun saveRentalTerms(carId: Int, request: RentalTermsRequest): RentalTerms = TODO()
}

// ==================== 2. Service: RentalTermsService ====================

class RentalTermsServiceTest {
    private val service = RentalTermsService(
        FakeStoredRentalTermsRepository(setOf(CAR_WITH_TERMS, CAR_WITHOUT_TERMS), storedRentalTerms)
    )

    // ---------- getRentalTerms ----------

    @Test
    fun `getRentalTerms geeft de verhuurvoorwaarden van de auto`() = runBlocking {
        assertEquals(storedRentalTerms, service.getRentalTerms(CAR_WITH_TERMS))
    }

    @Test
    fun `getRentalTerms voor onbekende auto geeft ResourceNotFoundException`() = runBlocking {
        assertFailsWith<ResourceNotFoundException> { service.getRentalTerms(999) }
        Unit
    }

    @Test
    fun `getRentalTerms voor auto zonder verhuurvoorwaarden geeft ResourceNotFoundException`() = runBlocking {
        assertFailsWith<ResourceNotFoundException> { service.getRentalTerms(CAR_WITHOUT_TERMS) }
        Unit
    }
}

// ==================== 3. E2E: rental terms routes ====================

class RentalTermsRoutesTest {
    // ---------- GET /cars/{id}/rental-terms ----------

    @Test
    fun `ophaal- en inlevertijden komen terug zoals ze zijn opgeslagen`() = e2eTest { client ->
        val response = client.get("/cars/$SEED_CAR_ID/rental-terms")

        assertEquals(HttpStatusCode.OK, response.status)
        val rentalTerms = response.body<RentalTerms>()
        assertEquals(LocalTime(8, 0), rentalTerms.pickupTimeStart)
        assertEquals(LocalTime(18, 0), rentalTerms.pickupTimeEnd)
        assertEquals("Parkeergarage Kazerneplein, Gorinchem", rentalTerms.returnLocation)
        assertEquals(LocalTime(8, 0), rentalTerms.returnTimeStart)
        assertEquals(LocalTime(20, 0), rentalTerms.returnTimeEnd)
    }
}
