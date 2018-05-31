package me.ialistannen.livingparchment.backend.server.resources

import io.dropwizard.jersey.PATCH
import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.storage.BookLocationRepository
import me.ialistannen.livingparchment.backend.util.logger
import me.ialistannen.livingparchment.common.api.response.*
import me.ialistannen.livingparchment.common.model.BookLocation
import org.hibernate.validator.constraints.NotEmpty
import java.util.*
import javax.annotation.security.PermitAll
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

    @PermitAll
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

    @PermitAll
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

    @PermitAll
    @PATCH
    fun patchLocation(@NotEmpty @FormParam("id") id: String,
                      @NotEmpty @FormParam("name") name: String,
                      @NotEmpty @FormParam("description") description: String): BookLocationPatchResponse {
        return runBlocking {
            try {
                val uuid = UUID.fromString(id)
                val bookLocation = bookLocationRepository.getLocation(uuid)
                        ?: return@runBlocking BookLocationPatchResponse(
                                null, BookLocationPatchStatus.NOT_FOUND
                        )

                val newLocation = bookLocation.copy(name = name, description = description)

                bookLocationRepository.addLocation(newLocation)

                BookLocationPatchResponse(newLocation, BookLocationPatchStatus.PATCHED)
            } catch (e: Exception) {
                logger.warn("Error patching a book", e)
                BookLocationPatchResponse(null, BookLocationPatchStatus.INTERNAL_ERROR)
            }
        }
    }

    @PermitAll
    @DELETE
    fun deleteLocation(@NotEmpty @FormParam("id") id: String): BookLocationDeleteResponse {
        return runBlocking {
            try {
                val uuid = UUID.fromString(id)
                bookLocationRepository.deleteLocation(uuid)
                BookLocationDeleteResponse(id, BookLocationDeleteStatus.DELETED)
            } catch (e: Exception) {
                logger.warn("Error deleting book location")
                BookLocationDeleteResponse(id, BookLocationDeleteStatus.INTERNAL_ERROR)
            }
        }
    }
}