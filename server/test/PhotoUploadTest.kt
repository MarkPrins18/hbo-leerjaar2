import config.PhotoStorage
import dto.CarChangeDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import models.BEVCar
import models.Car
import models.Photo
import models.VehicleType
import repositories.CarRepository
import repositories.PhotoRepository
import services.FileTooLargeException
import services.ForbiddenException
import services.PhotoService
import services.ResourceNotFoundException
import services.UnsupportedFileTypeException
import services.ValidationException
import services.validatePhoto
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.uuid.Uuid

// ==================== Test data ====================

private const val MAX_PHOTO_BYTES = 5 * 1024 * 1024
private const val OWNER_ID = 1
private const val OTHER_USER_ID = 2
private const val CAR_ID = 10

private val jpegHeader = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())

private fun jpegOfSize(size: Int): ByteArray = ByteArray(size).also { jpegHeader.copyInto(it) }

private fun testCar(id: Int = CAR_ID, ownerId: Int = OWNER_ID): Car = BEVCar(
    id = id,
    ownerId = ownerId,
    licensePlate = "AB-123-C",
    brand = "Tesla",
    model = "Model 3",
    productionYear = 2022,
    trim = null,
    color = "Wit",
    seats = 5,
    doors = 4,
    vehicleType = VehicleType.PASSENGER_CAR,
    readyToDriveWeightKg = 1800,
    consumptionCombined = 15.0,
    co2EmissionCombined = 0.0,
    batteryCapacityKWh = 60.0
)

// ==================== Fakes ====================

private class FakeCarRepository(vararg cars: Car) : CarRepository {
    private val cars = cars.associateBy { it.id }

    override suspend fun getCarById(carId: Int): Car? = cars[carId]

    override suspend fun getAllCars(): List<Car> = TODO()
    override suspend fun getCarByLicensePlate(licensePlate: String): Car? = TODO()
    override suspend fun createCar(car: Car): Car = TODO()
    override suspend fun updateCar(carId: Int, change: CarChangeDto): Car? = TODO()
    override suspend fun deleteCar(carId: Int): Boolean = TODO()
}

private class FakePhotoRepository(private val failOnCreate: Boolean = false) : PhotoRepository {
    private data class Row(val photo: Photo, val fileName: String)

    private val rows = mutableListOf<Row>()

    override suspend fun createPhoto(carId: Int, fileName: String): Photo {
        if (failOnCreate) throw IllegalStateException("Database niet bereikbaar")
        val id = rows.size + 1
        val photo = Photo(id, carId, "/photos/$id", Clock.System.now())
        rows += Row(photo, fileName)
        return photo
    }

    override suspend fun getPhotosByCarId(carId: Int): List<Photo> =
        rows.map { it.photo }.filter { it.carId == carId }

    override suspend fun getFileName(photoId: Int): String? =
        rows.find { it.photo.id == photoId }?.fileName

    override suspend fun getFileNamesByCarId(carId: Int): List<String> =
        rows.filter { it.photo.carId == carId }.map { it.fileName }
}

// ==================== 1. Validation: ByteArray.validatePhoto() ====================

class PhotoValidationTest {
    @Test
    fun `geldige jpeg wordt geaccepteerd`() {
        jpegHeader.validatePhoto()
    }

    @Test
    fun `jpeg van precies de maximale grootte wordt geaccepteerd`() {
        jpegOfSize(MAX_PHOTO_BYTES).validatePhoto()
    }

    @Test
    fun `lege foto geeft ValidationException`() {
        assertFailsWith<ValidationException> { ByteArray(0).validatePhoto() }
    }

    @Test
    fun `foto een byte boven de maximale grootte geeft FileTooLargeException`() {
        assertFailsWith<FileTooLargeException> { jpegOfSize(MAX_PHOTO_BYTES + 1).validatePhoto() }
    }

    @Test
    fun `png geeft UnsupportedFileTypeException`() {
        val png = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)
        assertFailsWith<UnsupportedFileTypeException> { png.validatePhoto() }
    }

    @Test
    fun `bestand korter dan de jpeg-header geeft UnsupportedFileTypeException`() {
        assertFailsWith<UnsupportedFileTypeException> { jpegHeader.copyOf(2).validatePhoto() }
    }
}

// ==================== 2. Service: PhotoService ====================

class PhotoServiceTest {
    private val directory = Path(SystemTemporaryDirectory, "photo-test-${Uuid.random()}")
    private val storage = PhotoStorage(directory)
    private val photo = jpegOfSize(100)

    private fun service(photoRepository: PhotoRepository = FakePhotoRepository()) =
        PhotoService(photoRepository, FakeCarRepository(testCar()), storage)

    private fun storedFileCount(): Int =
        if (SystemFileSystem.exists(directory)) SystemFileSystem.list(directory).size else 0

    @AfterTest
    fun cleanUp() {
        storage.clear()
        SystemFileSystem.delete(directory, mustExist = false)
    }

    // ---------- uploadPhoto ----------

    @Test
    fun `eigenaar kan een foto uploaden`() = runBlocking {
        val photoRepository = FakePhotoRepository()

        val result = service(photoRepository).uploadPhoto(CAR_ID, OWNER_ID, photo)

        assertEquals(CAR_ID, result.carId)
        assertEquals(listOf(result), photoRepository.getPhotosByCarId(CAR_ID))
        assertEquals(1, storedFileCount())
    }

    @Test
    fun `upload voor onbekende auto geeft ResourceNotFoundException`() = runBlocking {
        assertFailsWith<ResourceNotFoundException> { service().uploadPhoto(999, OWNER_ID, photo) }
        assertEquals(0, storedFileCount())
    }

    @Test
    fun `upload door niet-eigenaar geeft ForbiddenException`() = runBlocking {
        assertFailsWith<ForbiddenException> { service().uploadPhoto(CAR_ID, OTHER_USER_ID, photo) }
        assertEquals(0, storedFileCount())
    }

    @Test
    fun `ongeldige foto wordt niet opgeslagen`() = runBlocking {
        assertFailsWith<ValidationException> { service().uploadPhoto(CAR_ID, OWNER_ID, ByteArray(0)) }
        assertEquals(0, storedFileCount())
    }

    @Test
    fun `bestand wordt verwijderd als opslaan in de database mislukt`() = runBlocking {
        val service = service(FakePhotoRepository(failOnCreate = true))

        assertFailsWith<IllegalStateException> { service.uploadPhoto(CAR_ID, OWNER_ID, photo) }
        assertEquals(0, storedFileCount())
    }

    // ---------- getPhotos ----------

    @Test
    fun `getPhotos geeft alle foto's van de auto`() = runBlocking {
        val service = service()
        val first = service.uploadPhoto(CAR_ID, OWNER_ID, photo)
        val second = service.uploadPhoto(CAR_ID, OWNER_ID, photo)

        assertEquals(listOf(first, second), service.getPhotos(CAR_ID))
    }

    @Test
    fun `getPhotos voor onbekende auto geeft ResourceNotFoundException`() = runBlocking {
        assertFailsWith<ResourceNotFoundException> { service().getPhotos(999) }
        Unit
    }

    // ---------- getPhotoData ----------

    @Test
    fun `getPhotoData geeft de geuploade bytes terug`() = runBlocking {
        val service = service()
        val uploaded = service.uploadPhoto(CAR_ID, OWNER_ID, photo)

        assertContentEquals(photo, service.getPhotoData(uploaded.id))
    }

    @Test
    fun `getPhotoData voor onbekende foto geeft ResourceNotFoundException`() = runBlocking {
        assertFailsWith<ResourceNotFoundException> { service().getPhotoData(999) }
        Unit
    }

    @Test
    fun `getPhotoData geeft ResourceNotFoundException als het bestand ontbreekt`() = runBlocking {
        val service = service()
        val uploaded = service.uploadPhoto(CAR_ID, OWNER_ID, photo)
        storage.clear()

        assertFailsWith<ResourceNotFoundException> { service.getPhotoData(uploaded.id) }
        Unit
    }
}

// ==================== 3. E2E: photo routes ====================

class PhotoRoutesTest {
    private val seedCarId = 1
    private val seedOwnerId = 1
    private val photo = jpegOfSize(100)

    private suspend fun HttpClient.uploadPhoto(
        carId: Int,
        bytes: ByteArray,
        token: String? = null,
        fieldName: String = "photo"
    ): HttpResponse = post("/cars/$carId/photos") {
        token?.let { bearerAuth(it) }
        setBody(MultiPartFormDataContent(formData {
            append(fieldName, bytes, Headers.build {
                append(HttpHeaders.ContentDisposition, "filename=\"test.jpg\"")
            })
        }))
    }

    // ---------- POST /cars/{id}/photos ----------

    @Test
    fun `eigenaar uploadt foto en kan hem daarna ophalen`() = e2eTest { client ->
        val token = client.login(seedOwnerId)

        val response = client.uploadPhoto(seedCarId, photo, token)

        assertEquals(HttpStatusCode.Created, response.status)
        val uploaded = response.body<Photo>()
        assertEquals(seedCarId, uploaded.carId)

        val download = client.get(uploaded.url)
        assertEquals(HttpStatusCode.OK, download.status)
        assertEquals(ContentType.Image.JPEG, download.contentType())
        assertContentEquals(photo, download.readRawBytes())
    }

    @Test
    fun `upload zonder token geeft 401`() = e2eTest { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.uploadPhoto(seedCarId, photo).status)
    }

    @Test
    fun `upload door niet-eigenaar geeft 403`() = e2eTest { client ->
        val token = client.login(OTHER_USER_ID)

        assertEquals(HttpStatusCode.Forbidden, client.uploadPhoto(seedCarId, photo, token).status)
    }

    @Test
    fun `upload voor onbekende auto geeft 404`() = e2eTest { client ->
        val token = client.login(seedOwnerId)

        assertEquals(HttpStatusCode.NotFound, client.uploadPhoto(999, photo, token).status)
    }

    @Test
    fun `upload zonder veld photo geeft 400`() = e2eTest { client ->
        val token = client.login(seedOwnerId)

        assertEquals(HttpStatusCode.BadRequest, client.uploadPhoto(seedCarId, photo, token, fieldName = "file").status)
    }

    @Test
    fun `upload van png geeft 415`() = e2eTest { client ->
        val token = client.login(seedOwnerId)
        val png = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)

        assertEquals(HttpStatusCode.UnsupportedMediaType, client.uploadPhoto(seedCarId, png, token).status)
    }

    @Test
    fun `upload boven de maximale grootte geeft 413`() = e2eTest { client ->
        val token = client.login(seedOwnerId)

        val response = client.uploadPhoto(seedCarId, jpegOfSize(MAX_PHOTO_BYTES + 1), token)

        assertEquals(HttpStatusCode.PayloadTooLarge, response.status)
    }

    // ---------- GET /cars/{id}/photos ----------

    @Test
    fun `foto's van een auto ophalen kan zonder token`() = e2eTest { client ->
        val response = client.get("/cars/$seedCarId/photos")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(listOf(seedCarId), response.body<List<Photo>>().map { it.carId })
    }

    @Test
    fun `foto's van onbekende auto geeft 404`() = e2eTest { client ->
        assertEquals(HttpStatusCode.NotFound, client.get("/cars/999/photos").status)
    }

    // ---------- GET /photos/{id} ----------

    @Test
    fun `seedfoto is te downloaden`() = e2eTest { client ->
        val response = client.get("/photos/1")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.readRawBytes().isNotEmpty())
    }

    @Test
    fun `onbekende foto geeft 404`() = e2eTest { client ->
        assertEquals(HttpStatusCode.NotFound, client.get("/photos/999").status)
    }

    @Test
    fun `ongeldig foto-id geeft 400`() = e2eTest { client ->
        assertEquals(HttpStatusCode.BadRequest, client.get("/photos/abc").status)
    }
}
