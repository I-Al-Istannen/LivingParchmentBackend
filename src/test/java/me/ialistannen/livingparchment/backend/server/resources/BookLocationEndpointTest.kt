package me.ialistannen.livingparchment.backend.server.resources

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.storage.BookLocationRepository
import me.ialistannen.livingparchment.common.api.response.*
import me.ialistannen.livingparchment.common.model.BookLocation
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*
import javax.ws.rs.client.Entity

@ExtendWith(DropwizardExtensionsSupport::class)
class BookLocationEndpointTest {

    companion object : ResourceTest() {
        private val locationRepository = InMemoryBookLocationRepository()

        override val endpoint: BookLocationEndpoint = BookLocationEndpoint(locationRepository)

        override val extension = extension()

        override val path: String = "/locations"
    }

    @AfterEach
    fun cleanup() {
        runBlocking {
            for (location in locationRepository.getAllLocations()) {
                locationRepository.deleteLocation(location.uuid)
            }
        }
    }

    @Test
    fun `test add location`() {
        val bookLocation = BookLocation("Hey", "You")
        val response = makeCall<BookLocationAddResponse> {
            put(Entity.json(bookLocation))
        }

        assertEquals(BookLocationAddStatus.ADDED, response.status)

        runBlocking {
            assertEquals(bookLocation, locationRepository.getLocation(bookLocation.uuid))
        }
    }

    @Test
    fun `test delete location`() {
        runBlocking {
            val bookLocation = BookLocation("Hey", "You")
            locationRepository.addLocation(bookLocation)

            val response = makeCall<BookLocationDeleteResponse> {
                method("DELETE", form("id" to bookLocation.uuid.toString()))
            }

            assertEquals(BookLocationDeleteStatus.DELETED, response.status)
            assertNull(locationRepository.getLocation(bookLocation.uuid))
        }
    }

    @Test
    fun `test get locations`() {
        runBlocking {
            val locations = listOf(
                    BookLocation("Hey", "You"),
                    BookLocation("Hey", "You there"),
                    BookLocation("Hey", "You!")
            )
            locations.forEach { locationRepository.addLocation(it) }

            val response = makeCall<BookLocationQueryResponse> {
                get()
            }

            assertEquals(locations, response.locations)
        }
    }

    @Test
    fun `test patch location`() {
        runBlocking {
            val location = BookLocation("Hello", "You")
            locationRepository.addLocation(location)

            val modifiedLocation = location.copy(
                    name = location.name + "!!",
                    description = location.description + "!!"
            )

            val response = makeCall<BookLocationPatchResponse> {
                method("PATCH", form(
                        "id" to location.uuid.toString(),
                        "name" to modifiedLocation.name,
                        "description" to modifiedLocation.description
                ))
            }

            assertEquals(BookLocationPatchStatus.PATCHED, response.status)
            assertEquals(modifiedLocation, response.newLocation)
            assertEquals(modifiedLocation, locationRepository.getLocation(location.uuid))
        }
    }

    @Test
    fun `test patch nonexistent location`() {
        runBlocking {
            val location = BookLocation("Hello", "You")

            val response = makeCall<BookLocationPatchResponse> {
                method("PATCH", form(
                        "id" to location.uuid.toString(),
                        "name" to "ds",
                        "description" to "ds"
                ))
            }

            assertEquals(BookLocationPatchStatus.NOT_FOUND, response.status)
            assertNull(response.newLocation)
            assertNull(locationRepository.getLocation(location.uuid))
        }
    }

    private class InMemoryBookLocationRepository : BookLocationRepository {

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
}