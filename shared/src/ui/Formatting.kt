package nl.carsharing.app.ui

import kotlin.math.roundToLong

/**
 * Formats [value] with exactly [decimals] decimals and the given decimal [separator];
 * a non-empty [grouping] is put between every three digits (1.190).
 */
fun formatDecimal(value: Double, decimals: Int, separator: String, grouping: String = ""): String {
    var factor = 1L
    repeat(decimals) { factor *= 10 }
    val scaled = (value * factor).roundToLong()
    val whole = (scaled / factor).toString()
    val grouped = if (grouping.isEmpty()) whole else whole.reversed().chunked(3).joinToString(grouping).reversed()
    if (decimals == 0) return grouped
    val fraction = (scaled % factor).toString().padStart(decimals, '0')
    return "$grouped$separator$fraction"
}
