package me.ialistannen.livingparchment.backend.server.resources

import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.storage.BookRepository
import me.ialistannen.livingparchment.backend.util.logger
import me.ialistannen.livingparchment.common.api.query.QueryType
import me.ialistannen.livingparchment.common.api.response.BookResponse
import org.hibernate.validator.constraints.NotEmpty
import javax.inject.Inject
import javax.validation.constraints.NotNull
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/query")
@Produces(MediaType.APPLICATION_JSON)
class BookQueryEndpoint @Inject constructor(
        private val bookRepository: BookRepository
) {

    private val logger by logger()

    @GET
    fun getBooks(@QueryParam("attributeName") @NotEmpty attributeName: String,
                 @QueryParam("queryType") @NotNull queryType: QueryType,
                 @QueryParam("queryString") @NotEmpty queryString: String
    ): BookResponse {
        return try {
            runBlocking {
                val books = bookRepository.getBooksForQuery(queryType, attributeName, queryString)

                BookResponse(books)
            }
        } catch (e: Exception) {
            logger.warn("Error fetching books", e)
            throw WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR)
        }
    }
}