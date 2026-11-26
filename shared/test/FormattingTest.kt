import nl.carsharing.app.ui.formatDecimal
import nl.carsharing.app.ui.formatTrimmed
import kotlin.test.Test
import kotlin.test.assertEquals

// ==================== 1. Validation: formatDecimal() and formatTrimmed() ====================

class FormattingTest {
    @Test
    fun `negatief bedrag krijgt een minteken`() {
        assertEquals("-0,50", formatDecimal(-0.5, 2, ","))
    }

    @Test
    fun `negatief bedrag dat op nul afrondt krijgt geen minteken`() {
        assertEquals("0,00", formatDecimal(-0.004, 2, ","))
    }

    @Test
    fun `negatief bedrag met duizendtallen wordt gegroepeerd`() {
        assertEquals("-1.250,00", formatDecimal(-1250.0, 2, ",", "."))
    }

    @Test
    fun `afronden schuift door naar het hele getal`() {
        assertEquals("10,00", formatDecimal(9.996, 2, ","))
    }

    @Test
    fun `formatTrimmed laat nullen achter de komma weg`() {
        assertEquals("100", formatTrimmed(100.0, 2, ","))
        assertEquals("52,96", formatTrimmed(52.96, 2, ","))
    }
}
