import dto.CarFilter
import models.RentalTerms
import services.matches
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// ==================== Test data ====================

private const val MAX_PRICE = 50.0

private fun rentalTerms(pricePerDay: Double): RentalTerms = RentalTerms(
    id = 1,
    carId = 1,
    pricePerDay = pricePerDay,
    pickupLocation = "Grote Markt 1, Breda"
)

// ==================== 1. Validation: CarFilter.matches() ====================

class CarFilterValidationTest {
    private val filter = CarFilter(maxPrice = MAX_PRICE)

    @Test
    fun `auto met een prijs precies op het maximum valt binnen het filter`() {
        assertTrue(filter.matches(location = null, rentalTerms = rentalTerms(MAX_PRICE)))
    }

    @Test
    fun `auto zonder verhuurvoorwaarden valt af bij een maximale prijs`() {
        assertFalse(filter.matches(location = null, rentalTerms = null))
    }
}
