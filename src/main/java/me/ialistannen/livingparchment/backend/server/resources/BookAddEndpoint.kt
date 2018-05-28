package me.ialistannen.livingparchment.backend.server.resources

import io.dropwizard.jersey.PATCH
import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.fetching.BookFetcher
import me.ialistannen.livingparchment.backend.storage.BookLocationRepository
import me.ialistannen.livingparchment.backend.storage.BookRepository
import me.ialistannen.livingparchment.backend.util.logger
import me.ialistannen.livingparchment.common.api.query.QueryType
import me.ialistannen.livingparchment.common.api.request.BookIsbnAddRequest
import me.ialistannen.livingparchment.common.api.response.BookAddResponse
import me.ialistannen.livingparchment.common.api.response.BookAddStatus
import me.ialistannen.livingparchment.common.api.response.BookPatchResponse
import me.ialistannen.livingparchment.common.api.response.BookPatchStatus
import me.ialistannen.livingparchment.common.model.Book
import me.ialistannen.livingparchment.common.serialization.fromJson
import org.hibernate.validator.constraints.NotEmpty
import javax.inject.Inject
import javax.validation.constraints.NotNull
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType

@Path("add")
@Produces(MediaType.APPLICATION_JSON)
class BookAddEndpoint @Inject constructor(
        private val bookRepository: BookRepository,
        private val bookFetcher: BookFetcher,
        private val locationRepository: BookLocationRepository
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

    @Path("patch")
    @PATCH
    fun patchBook(@NotEmpty @QueryParam("isbn") isbn: String, body: String): BookPatchResponse {
        return runBlocking {
            try {
                val book = (bookRepository.getBooksForQuery(QueryType.EXACT_MATCH, "isbn", isbn)
                        .firstOrNull()
                        ?: return@runBlocking BookPatchResponse(isbn, BookPatchStatus.NOT_FOUND, null))

                val patched = patchBook(book, body.fromJson())

                bookRepository.addBook(patched)

                BookPatchResponse(isbn, BookPatchStatus.PATCHED, patched)
            } catch (e: Exception) {
                logger.info("Error patching book", e)
                BookPatchResponse(isbn, BookPatchStatus.INTERNAL_ERROR, null)
            }
        }
    }

    private suspend fun patchBook(book: Book, patchData: Map<String, Any>): Book {
        val title = patchData["title"] as String? ?: book.title
        val locationName = patchData["location"] as String? ?: book.location?.name
        val pageCount = (patchData["pageCount"] as? Number?)?.toInt() ?: book.pageCount
        val publisher = patchData["publisher"] as String? ?: book.publisher
        val description = patchData["description"] ?: book.extra["description"]
        val genres = patchData["genre"] as List<*>? ?: book.genre
        val authors = patchData["authors"] as List<*>? ?: book.authors

        val location = locationRepository.getAllLocations().firstOrNull { it.name == locationName }

        val extra: MutableMap<String, Any> = book.extra.toMutableMap()
        extra["genre"] = genres
        extra["authors"] = authors

        if (description != null) {
            extra["description"] = description
            return book.copy(
                    title = title,
                    pageCount = pageCount,
                    publisher = publisher,
                    location = location,
                    extra = extra
            )
        }
        return book.copy(
                title = title,
                pageCount = pageCount,
                location = location,
                publisher = publisher,
                extra = extra
        )
    }

    private fun notFound(isbn: String) = BookAddResponse(isbn, BookAddStatus.NOT_FOUND)
}