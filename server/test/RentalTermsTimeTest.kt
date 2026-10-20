import dto.RentalTermsRequest
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.datetime.LocalTime
import models.RentalTerms
import services.ValidationException
import services.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// ==================== Test data ====================

private const val SEED_CAR_ID = 1

// ==================== 1. Validation: RentalTermsRequest.validate() ====================

class RentalTermsTimeValidationTest {
    @Test
    fun `alleen een ophaalstarttijd zonder eindtijd geeft ValidationException`() {
        val request = RentalTermsRequest(
            pricePerDay = 50.0,
            pickupLocation = "Grote Markt 1, Breda",
            pickupTimeStart = LocalTime(8, 0)
        )

        assertFailsWith<ValidationException> { request.validate() }
    }
}

// ==================== 3. E2E: rental terms times ====================

class RentalTermsTimeRoutesTest {
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
