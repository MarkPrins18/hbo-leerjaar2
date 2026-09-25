package tables

import org.jetbrains.exposed.v1.core.Table

// Issue #12 - Bonuspuntensysteem

object BonusPointAwardTable : Table("bonus_point_award") {
    val id = integer("id").autoIncrement()
    val renterId = integer("renter_id") references RenterTable.id
    val tripId = integer("trip_id") references TripTable.id
    val points = integer("points")
    val awardedAt = long("awarded_at")

    override val primaryKey = PrimaryKey(id)
}

