package me.ialistannen.livingparchment.backend.storage

import me.ialistannen.livingparchment.common.model.BookLocation
import java.util.*

interface BookLocationRepository {

    /**
     * Adds a location to the repository or modifies an existing one with the same UUID.
     *
     * @param bookLocation the new book location to add / to overwrite
     */
    suspend fun addLocation(bookLocation: BookLocation)

    /**
     * Removes a location from the repository.
     *
     * @param uuid the uuid of the [BookLocation]
     * @return true if it was deleted, false if it didn't exist
     */
    suspend fun deleteLocation(uuid: UUID): Boolean

    /**
     * Returns all book locations.
     */
    suspend fun getAllLocations(): List<BookLocation>

    /**
     * Returns the location for the given [UUID].
     */
    suspend fun getLocation(uuid: UUID): BookLocation?
}