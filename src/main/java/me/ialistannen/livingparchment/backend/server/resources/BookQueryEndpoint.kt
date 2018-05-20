package me.ialistannen.livingparchment.backend.server.resources

import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.storage.BookRepository
import me.ialistannen.livingparchment.common.api.query.QueryType
import me.ialistannen.livingparchment.common.api.response.BookResponse
import org.hibernate.validator.constraints.NotEmpty
import javax.inject.Inject
import javax.validation.constraints.NotNull
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType

@Path("/query")
@Produces(MediaType.APPLICATION_JSON)
class BookQueryEndpoint @Inject constructor(
        private val bookRepository: BookRepository
) {

    @GET
    fun getBooks(@QueryParam("attributeName") @NotEmpty attributeName: String,
                 @QueryParam("queryType") @NotNull queryType: QueryType,
                 @QueryParam("queryString") @NotEmpty queryString: String
    ): BookResponse {
        return runBlocking {
            val books = bookRepository.getBooksForQuery(queryType, attributeName, queryString)

            BookResponse(books)
        }
    }
}