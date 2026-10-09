import services.calculateBonusPoints
import kotlin.test.Test
import kotlin.test.assertEquals

class BonusPointsTest {
    @Test
    fun `rustige rit krijgt 1 punt per volle kilometer`() {
        assertEquals(12, calculateBonusPoints(12500.0, 1.8, 2.1))
    }

    @Test
    fun `net onder beide drempels is rustig`() {
        assertEquals(12, calculateBonusPoints(12500.0, 2.77, 3.42))
    }

    @Test
    fun `acceleratie precies op de drempel geeft 0 punten`() {
        assertEquals(0, calculateBonusPoints(12500.0, 2.78, 2.1))
    }

    @Test
    fun `deceleratie precies op de drempel geeft 0 punten`() {
        assertEquals(0, calculateBonusPoints(12500.0, 1.8, 3.43))
    }

    @Test
    fun `minder dan een volle kilometer geeft 0 punten`() {
        assertEquals(0, calculateBonusPoints(999.9, 1.8, 2.1))
    }
}