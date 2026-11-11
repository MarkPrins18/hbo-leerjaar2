package services

import kotlin.math.ceil
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/** Every started day counts as a full day, with a minimum of one day. Rounded to cents. */
fun calculateTotalPrice(pricePerDay: Double, startTime: Instant, endTime: Instant): Double {
    val startedDays = ceil((endTime - startTime) / 1.days).toLong().coerceAtLeast(1)
    return (pricePerDay * startedDays * 100).roundToLong() / 100.0
}
