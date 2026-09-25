package tables

import org.jetbrains.exposed.v1.core.Table

/**
 * Minimale tabel voor huurders, gespiegeld aan OwnerTable.
 *
 * LET OP - open ontwerpvraag: Owner en Renter zijn hier twee losse tabellen,
 * omdat dit project (nog) geen gedeelde "persoon"/"gebruiker"-tabel heeft.
 * Of dit uiteindelijk 1 tabel moet worden (bijv. Person met een rol
 * OWNER/RENTER) is een teamkeuze die ik niet zomaar aanneem -- bevestig dit
 * met je team voordat je verdergaat.
 */
object RenterTable : Table("renter") {
    val id = integer("id").autoIncrement()

    override val primaryKey = PrimaryKey(id)
}

