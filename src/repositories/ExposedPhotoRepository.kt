package repositories

import kotlin.time.Instant
import models.Photo
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import tables.PhotoTable

class ExposedPhotoRepository : PhotoRepository {

    override suspend fun getPhotosForCar(carId: Int): List<Photo> = suspendTransaction {
        PhotoTable.selectAll()
            .where { PhotoTable.carId eq carId }
            .map { it.toPhoto() }
    }

    override suspend fun addPhoto(photo: Photo): Photo = suspendTransaction {
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        val newId = PhotoTable.insert {
            it[carId] = photo.carId
            it[url] = photo.url
            it[uploadedAt] = now.toEpochMilliseconds()
        }[PhotoTable.id]

        photo.copy(id = newId, uploadedAt = now)
    }

    override suspend fun deletePhoto(photoId: Int): Boolean = suspendTransaction {
        PhotoTable.deleteWhere { PhotoTable.id eq photoId } > 0
    }

    private fun ResultRow.toPhoto(): Photo = Photo(
        id = this[PhotoTable.id],
        carId = this[PhotoTable.carId],
        url = this[PhotoTable.url],
        uploadedAt = Instant.fromEpochMilliseconds(this[PhotoTable.uploadedAt])
    )
}
