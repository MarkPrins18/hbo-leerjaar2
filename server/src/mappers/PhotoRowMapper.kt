package mappers

import models.Photo
import org.jetbrains.exposed.v1.core.ResultRow
import tables.PhotoTable

fun ResultRow.toPhoto(): Photo = Photo(
    id = this[PhotoTable.id],
    carId = this[PhotoTable.carId],
    url = "/photos/${this[PhotoTable.id]}",
    uploadedAt = this[PhotoTable.uploadedAt]
)
