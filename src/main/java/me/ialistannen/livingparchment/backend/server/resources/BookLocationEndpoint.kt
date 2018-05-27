package me.ialistannen.livingparchment.backend.server.resources

import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.storage.BookLocationRepository
import me.ialistannen.livingparchment.backend.util.logger
import me.ialistannen.livingparchment.common.api.response.BookLocationAddResponse
import me.ialistannen.livingparchment.common.api.response.BookLocationAddStatus
import me.ialistannen.livingparchment.common.api.response.BookLocationQueryResponse
import me.ialistannen.livingparchment.common.model.BookLocation
import javax.inject.Inject
import javax.validation.constraints.NotNull
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/locations")
@Produces(MediaType.APPLICATION_JSON)
class BookLocationEndpoint @Inject constructor(
        private val bookLocationRepository: BookLocationRepository
) {

    private val logger by logger()

    @GET
    fun getAllLocations(): BookLocationQueryResponse {
        return runBlocking {
            try {
                BookLocationQueryResponse(bookLocationRepository.getAllLocations())
            } catch (e: Exception) {
                logger.warn("Error getting all book locations", e)
                throw WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR)
            }
        }
    }

    @PUT
    fun addLocation(@NotNull bookLocation: BookLocation): BookLocationAddResponse {
        return runBlocking {
            try {
                bookLocationRepository.addLocation(bookLocation)
                BookLocationAddResponse(bookLocation.name, BookLocationAddStatus.ADDED)
            } catch (e: Exception) {
                logger.warn("Error adding book location", e)
                BookLocationAddResponse(
                        bookLocation.name,
                        BookLocationAddStatus.INTERNAL_ERROR
                )
            }
        }
    }
}