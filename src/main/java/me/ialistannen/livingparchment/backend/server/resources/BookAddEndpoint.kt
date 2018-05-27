package me.ialistannen.livingparchment.backend.server.resources

import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.fetching.BookFetcher
import me.ialistannen.livingparchment.backend.storage.BookRepository
import me.ialistannen.livingparchment.backend.util.logger
import me.ialistannen.livingparchment.common.api.request.BookIsbnAddRequest
import me.ialistannen.livingparchment.common.api.response.BookAddResponse
import me.ialistannen.livingparchment.common.api.response.BookAddStatus
import me.ialistannen.livingparchment.common.model.Book
import javax.inject.Inject
import javax.validation.constraints.NotNull
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("add")
@Produces(MediaType.APPLICATION_JSON)
class BookAddEndpoint @Inject constructor(
        private val bookRepository: BookRepository,
        private val bookFetcher: BookFetcher
) {

    private val logger by logger()

    @Path("isbn")
    @PUT
    fun addBookFromIsbn(@NotNull request: BookIsbnAddRequest): BookAddResponse {
        return runBlocking {
            try {
                val isbn = request.isbn
                var book = bookFetcher.fetch(isbn) ?: return@runBlocking notFound(isbn)

                request.location?.let { book = book.copy(location = request.location) }

                bookRepository.addBook(book)

                BookAddResponse(isbn, BookAddStatus.ADDED)
            } catch (e: Exception) {
                logger.info("Error adding book", e)
                BookAddResponse(request.isbn, BookAddStatus.INTERNAL_ERROR)
            }
        }
    }

    @Path("book")
    @PUT
    fun addBook(@NotNull book: Book): BookAddResponse {
        return runBlocking {
            try {
                bookRepository.addBook(book)

                BookAddResponse(book.isbn, BookAddStatus.ADDED)
            } catch (e: Exception) {
                logger.info("Error adding book", e)
                BookAddResponse(book.isbn, BookAddStatus.INTERNAL_ERROR)
            }
        }
    }

    private fun notFound(isbn: String) = BookAddResponse(isbn, BookAddStatus.NOT_FOUND)
}