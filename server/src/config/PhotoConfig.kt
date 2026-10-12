package config

import io.ktor.server.application.*
import kotlinx.io.files.Path

fun Application.photoStorage(): PhotoStorage =
    PhotoStorage(Path(environment.config.property("photos.directory").getString()))
