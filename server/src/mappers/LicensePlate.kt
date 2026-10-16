package mappers

/**
 * Normalizes a license plate to the RDW notation: no dashes or spaces, uppercase.
 * For example "r-816-lf" or "r 816 lf" becomes "R816LF".
 */
fun normalizeLicensePlate(licensePlate: String): String =
    licensePlate.filterNot { it == '-' || it.isWhitespace() }.uppercase()