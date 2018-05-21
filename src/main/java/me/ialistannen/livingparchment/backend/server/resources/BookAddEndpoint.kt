package me.ialistannen.livingparchment.backend.server.resources

import com.codahale.metrics.annotation.Timed
import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.fetching.BookFetcher
import me.ialistannen.livingparchment.backend.storage.BookRepository
import me.ialistannen.livingparchment.backend.util.logger
import me.ialistannen.livingparchment.common.api.response.BookAddResponse
import me.ialistannen.livingparchment.common.api.response.BookAddStatus
import org.hibernate.validator.constraints.NotEmpty
import javax.inject.Inject
import javax.ws.rs.FormParam
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/add")
@Produces(MediaType.APPLICATION_JSON)
class BookAddEndpoint @Inject constructor(
        private val bookRepository: BookRepository,
        private val bookFetcher: BookFetcher
) {

    private val logger by logger()

    @PUT
    @Timed
    fun getHello(@NotEmpty @FormParam("isbn") isbn: String): BookAddResponse {
        return runBlocking {
            try {
                val book = bookFetcher.fetch(isbn) ?: return@runBlocking notFound(isbn)

                bookRepository.addBook(book)

                BookAddResponse(isbn, BookAddStatus.ADDED)
            } catch (e: Exception) {
                logger.info("Error adding book", e)
                BookAddResponse(isbn, BookAddStatus.INTERNAL_ERROR)
            }
        }
    }

    private fun notFound(isbn: String) = BookAddResponse(isbn, BookAddStatus.NOT_FOUND)
}