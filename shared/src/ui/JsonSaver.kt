package nl.carsharing.app.ui

import androidx.compose.runtime.saveable.Saver
import kotlinx.serialization.json.Json

/** Saves a `@Serializable` value as a JSON string, so `rememberSaveable` keeps it across rotation. */
inline fun <reified T> jsonSaver(): Saver<T, String> = Saver(
    save = { Json.encodeToString(it) },
    restore = { Json.decodeFromString(it) }
)
