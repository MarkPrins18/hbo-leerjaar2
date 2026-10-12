package config

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray

/** Stores photo files on disk; the database only keeps the file name. */
class PhotoStorage(private val directory: Path) {

    fun save(fileName: String, bytes: ByteArray) {
        SystemFileSystem.createDirectories(directory)
        SystemFileSystem.sink(Path(directory, fileName)).buffered().use { it.write(bytes) }
    }

    fun read(fileName: String): ByteArray? {
        val path = Path(directory, fileName)
        if (!SystemFileSystem.exists(path)) return null
        return SystemFileSystem.source(path).buffered().use { it.readByteArray() }
    }

    fun delete(fileName: String) {
        SystemFileSystem.delete(Path(directory, fileName), mustExist = false)
    }

    fun clear() {
        if (!SystemFileSystem.exists(directory)) return
        SystemFileSystem.list(directory).forEach { SystemFileSystem.delete(it, mustExist = false) }
    }
}
