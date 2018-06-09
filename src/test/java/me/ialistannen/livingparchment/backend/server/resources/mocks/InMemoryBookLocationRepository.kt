package me.ialistannen.livingparchment.backend.server.resources.mocks

import me.ialistannen.livingparchment.backend.storage.BookLocationRepository
import me.ialistannen.livingparchment.common.model.BookLocation
import java.util.*

class InMemoryBookLocationRepository : BookLocationRepository {

    private val locations = mutableListOf<BookLocation>()

    override suspend fun addLocation(bookLocation: BookLocation) {
        deleteLocation(bookLocation.uuid)
        locations.add(bookLocation)
    }

    override suspend fun deleteLocation(uuid: UUID): Boolean {
        return locations.removeIf { it.uuid == uuid }
    }

    override suspend fun getAllLocations(): List<BookLocation> {
        return ArrayList(locations)
    }

    override suspend fun getLocation(uuid: UUID): BookLocation? {
        return locations.firstOrNull { it.uuid == uuid }
    }
}