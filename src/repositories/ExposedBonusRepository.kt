package repositories

import kotlin.time.Instant
import models.BonusPointAward
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import tables.BonusPointAwardTable

// Issue #12 - Bonuspuntensysteem
class ExposedBonusRepository : BonusRepository {

    override suspend fun getAwardsForRenter(renterId: Int): List<BonusPointAward> = suspendTransaction {
        BonusPointAwardTable.selectAll()
            .where { BonusPointAwardTable.renterId eq renterId }
            .map { it.toAward() }
    }

    override suspend fun getTotalPointsForRenter(renterId: Int): Int = suspendTransaction {
        BonusPointAwardTable.selectAll()
            .where { BonusPointAwardTable.renterId eq renterId }
            .sumOf { it[BonusPointAwardTable.points] }
    }

    override suspend fun awardPointsForTrip(tripId: Int, renterId: Int, points: Int): BonusPointAward =
        suspendTransaction {
            val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
            val newId = BonusPointAwardTable.insert {
                it[BonusPointAwardTable.renterId] = renterId
                it[BonusPointAwardTable.tripId] = tripId
                it[BonusPointAwardTable.points] = points
                it[awardedAt] = now.toEpochMilliseconds()
            }[BonusPointAwardTable.id]

            BonusPointAward(id = newId, renterId = renterId, tripId = tripId, points = points, awardedAt = now)
        }

    private fun ResultRow.toAward(): BonusPointAward = BonusPointAward(
        id = this[BonusPointAwardTable.id],
        renterId = this[BonusPointAwardTable.renterId],
        tripId = this[BonusPointAwardTable.tripId],
        points = this[BonusPointAwardTable.points],
        awardedAt = Instant.fromEpochMilliseconds(this[BonusPointAwardTable.awardedAt])
    )
}
