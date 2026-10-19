package services

import models.Photo
import repositories.CarRepository
import repositories.PhotoRepository
import config.PhotoStorage
import kotlin.uuid.Uuid

class PhotoService(
    private val photoRepository: PhotoRepository,
    private val carRepository: CarRepository,
    private val photoStorage: PhotoStorage
) {
    suspend fun uploadPhoto(carId: Int, userId: Int, data: ByteArray): Photo {
        val car = carRepository.getCarById(carId)
            ?: throw carNotFound(carId)

        if (car.userId != userId) throw ForbiddenException()

        data.validatePhoto()

        val fileName = "car${carId}_${Uuid.random()}.jpg"
        photoStorage.save(fileName, data)

        return try {
            photoRepository.createPhoto(carId, fileName)
        } catch (exception: Exception) {
            photoStorage.delete(fileName)
            throw exception
        }
    }

    suspend fun getPhotos(carId: Int): List<Photo> {
        carRepository.getCarById(carId) ?: throw carNotFound(carId)
        return photoRepository.getPhotosByCarId(carId)
    }

    suspend fun getPhotoData(photoId: Int): ByteArray =
        photoRepository.getFileName(photoId)?.let(photoStorage::read)
            ?: throw ResourceNotFoundException("Foto $photoId niet gevonden")

    private fun carNotFound(carId: Int) = ResourceNotFoundException("Auto $carId niet gevonden")
}
