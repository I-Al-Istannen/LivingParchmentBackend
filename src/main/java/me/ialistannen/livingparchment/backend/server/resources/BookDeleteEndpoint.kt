package me.ialistannen.livingparchment.backend.server.resources

import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.storage.BookRepository
import me.ialistannen.livingparchment.backend.util.logger
import me.ialistannen.livingparchment.common.api.response.BookDeleteResponse
import me.ialistannen.livingparchment.common.api.response.BookDeleteStatus
import org.hibernate.validator.constraints.NotEmpty
import javax.inject.Inject
import javax.ws.rs.DELETE
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType

@Path("/delete")
@Produces(MediaType.APPLICATION_JSON)
class BookDeleteEndpoint @Inject constructor(
        private val bookRepository: BookRepository
) {

    private val logger by logger()

    @DELETE
    fun deleteBook(@QueryParam("isbn") @NotEmpty isbn: String): BookDeleteResponse {
        return runBlocking {
            try {
                if (bookRepository.removeBook(isbn)) {
                    BookDeleteResponse(isbn, BookDeleteStatus.DELETED)
                } else {
                    BookDeleteResponse(isbn, BookDeleteStatus.NOT_FOUND)
                }
            } catch (e: Exception) {
                logger.warn("Error deleting book for isbn '$isbn'", e)
                BookDeleteResponse(isbn, BookDeleteStatus.INTERNAL_ERROR)
            }
        }
    }
}