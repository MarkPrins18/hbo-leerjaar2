package repositories

import models.Photo

interface PhotoRepository {
    suspend fun getPhotosForCar(carId: Int): List<Photo>

    /** Slaat een nieuwe foto op. Het `id`-veld van [photo] wordt genegeerd. */
    suspend fun addPhoto(photo: Photo): Photo

    /** Geeft true terug als er iets verwijderd is. */
    suspend fun deletePhoto(photoId: Int): Boolean
}
