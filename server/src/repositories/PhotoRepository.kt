package repositories

import shared.models.Photo

interface PhotoRepository {
    suspend fun createPhoto(carId: Int, fileName: String): Photo

    suspend fun getPhotosByCarId(carId: Int): List<Photo>

    /** Returns the stored file name, or null if no photo exists with id [photoId]. */
    suspend fun getFileName(photoId: Int): String?

    suspend fun getFileNamesByCarId(carId: Int): List<String>
}
