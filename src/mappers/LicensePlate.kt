package mappers

/**
 * Normalizes a license plate to the RDW notation: no dashes, uppercase.
 * For example "r-816-lf" becomes "R816LF".
 */
fun normalizeLicensePlate(licensePlate: String): String =
    licensePlate.replace("-", "").uppercase()