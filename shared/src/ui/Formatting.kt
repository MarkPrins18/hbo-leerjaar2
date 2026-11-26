package nl.carsharing.app.ui

import androidx.compose.runtime.Composable
import kotlin.math.abs
import kotlin.math.roundToLong
import nl.carsharing.app.resources.Res
import nl.carsharing.app.resources.decimal_separator
import nl.carsharing.app.resources.grouping_separator
import nl.carsharing.app.resources.price_amount
import org.jetbrains.compose.resources.stringResource

/**
 * Formats [value] with exactly [decimals] decimals and the given decimal [separator];
 * a non-empty [grouping] is put between every three digits (1.190).
 */
fun formatDecimal(value: Double, decimals: Int, separator: String, grouping: String = ""): String {
    var factor = 1L
    repeat(decimals) { factor *= 10 }
    val scaled = (abs(value) * factor).roundToLong()
    val sign = if (value < 0 && scaled != 0L) "-" else ""
    val whole = (scaled / factor).toString()
    val grouped = if (grouping.isEmpty()) whole else whole.reversed().chunked(3).joinToString(grouping).reversed()
    if (decimals == 0) return "$sign$grouped"
    val fraction = (scaled % factor).toString().padStart(decimals, '0')
    return "$sign$grouped$separator$fraction"
}

/** Like [formatDecimal] with at most [maxDecimals] decimals and no trailing zeros: 36.0 → "36", 52.96 → "52,96". */
fun formatTrimmed(value: Double, maxDecimals: Int, separator: String): String {
    val text = formatDecimal(value, maxDecimals, separator)
    return if (maxDecimals == 0) text else text.trimEnd('0').removeSuffix(separator)
}

/** Amount in euros with the decimal and grouping separators of the current language (€ 1.190,00). */
@Composable
fun formatMoney(value: Double, decimals: Int = 2): String = stringResource(
    Res.string.price_amount,
    formatDecimal(value, decimals, stringResource(Res.string.decimal_separator), stringResource(Res.string.grouping_separator))
)
