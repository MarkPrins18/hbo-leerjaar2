package repositories

import mappers.toPhoto
import models.Photo
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import tables.PhotoTable
import kotlin.time.Clock

class ExposedPhotoRepository : PhotoRepository {

    override suspend fun createPhoto(carId: Int, fileName: String): Photo = suspendTransaction {
        val inserted = PhotoTable.insert {
            it[PhotoTable.carId] = carId
            it[PhotoTable.fileName] = fileName
            it[PhotoTable.uploadedAt] = Clock.System.now()
        }
        val id = inserted[PhotoTable.id]
        PhotoTable.selectAll().where { PhotoTable.id eq id }.single().toPhoto()
    }

    override suspend fun getPhotosByCarId(carId: Int): List<Photo> = suspendTransaction {
        PhotoTable.selectAll()
            .where { PhotoTable.carId eq carId }
            .orderBy(PhotoTable.id)
            .map { it.toPhoto() }
    }

    override suspend fun getFileName(photoId: Int): String? = suspendTransaction {
        PhotoTable.select(PhotoTable.fileName)
            .where { PhotoTable.id eq photoId }
            .singleOrNull()
            ?.get(PhotoTable.fileName)
    }

    override suspend fun getFileNamesByCarId(carId: Int): List<String> = suspendTransaction {
        PhotoTable.select(PhotoTable.fileName)
            .where { PhotoTable.carId eq carId }
            .map { it[PhotoTable.fileName] }
    }
}
