package repositories

import models.BonusPointAward

// Issue #12 - Bonuspuntensysteem

interface BonusRepository {
    suspend fun getAwardsForRenter(renterId: Int): List<BonusPointAward>

    suspend fun getTotalPointsForRenter(renterId: Int): Int

    suspend fun awardPointsForTrip(tripId: Int, renterId: Int, points: Int): BonusPointAward
}
