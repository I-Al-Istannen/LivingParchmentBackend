package me.ialistannen.livingparchment.backend.server.resources

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import io.dropwizard.testing.junit5.ResourceExtension
import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.fetching.BookFetcher
import me.ialistannen.livingparchment.backend.server.resources.mocks.InMemoryBookLocationRepository
import me.ialistannen.livingparchment.backend.server.resources.mocks.InMemoryBookRepository
import me.ialistannen.livingparchment.backend.util.toDate
import me.ialistannen.livingparchment.common.api.request.BookIsbnAddRequest
import me.ialistannen.livingparchment.common.api.response.BookAddResponse
import me.ialistannen.livingparchment.common.api.response.BookAddStatus
import me.ialistannen.livingparchment.common.api.response.BookPatchResponse
import me.ialistannen.livingparchment.common.api.response.BookPatchStatus
import me.ialistannen.livingparchment.common.model.Book
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.Month
import javax.ws.rs.client.Entity

@ExtendWith(DropwizardExtensionsSupport::class)
class BookAddEndpointTest {

    companion object : ResourceTest() {
        private val dummyBook = Book(
                "Magic beasts",
                20,
                "9783332313",
                "German",
                LocalDate.of(2018, Month.JANUARY, 1).toDate(),
                authors = listOf("JK", "Penny"),
                publisher = "Carlsen",
                imageUrl = BookAddEndpointTest::class.java
                        .getResource("/server/resources/example_image.jpg")
                        .toExternalForm(),
                genre = listOf("Fantady", "Myths")
        )

        private val coverPath: Path = Files.createTempDirectory("living")

        private val bookRepository = InMemoryBookRepository()
        private val locationRepository = InMemoryBookLocationRepository()

        override val endpoint: BookAddEndpoint = BookAddEndpoint(
                bookRepository,
                DummyBookFetcher(dummyBook),
                locationRepository,
                coverPath
        )

        override val extension: ResourceExtension = extension()

        override val path: String = "/add"

        private inline fun <reified T> makePatch(path: String, entity: Any,
                                                 vararg queryParams: Pair<String, String>): T {
            return super.makeCall(extension, path, queryParams.toList()) {
                method("PATCH", Entity.json(entity))
            }
        }

        @JvmStatic
        @AfterAll
        fun teardown() {
            coverPath.toFile().deleteRecursively()
        }
    }

    @AfterEach
    fun cleanup() {
        runBlocking {
            bookRepository.getAllBooks().forEach {
                bookRepository.removeBook(it)
            }
            locationRepository.getAllLocations().forEach {
                locationRepository.deleteLocation(it.uuid)
            }
        }
    }

    @Test
    fun `add book by isbn`() {
        runBlocking {
            val isbn = dummyBook.isbn

            val response: BookAddResponse = makeCall(extension, "$path/isbn") {
                put(Entity.json(BookIsbnAddRequest(isbn, null)))
            }

            assertEquals(BookAddStatus.ADDED, response.status)
            assertEquals(isbn, response.isbn)
            assertTrue(
                    Files.exists(coverPath.resolve("$isbn.jpg")),
                    "Cover not downloaded"
            )
            ensureBookAdded()
        }
    }

    @Test
    fun `add invalid book by isbn`() {
        runBlocking {
            val isbn = "rufsho"

            val response: BookAddResponse = makeCall(extension, "$path/isbn") {
                put(Entity.json(BookIsbnAddRequest(isbn, null)))
            }

            assertEquals(BookAddStatus.NOT_FOUND, response.status)
            assertEquals(isbn, response.isbn)
            assertFalse(
                    Files.exists(coverPath.resolve("$isbn.jpg")),
                    "Cover downloaded"
            )
            assertEquals(listOf<Book>(), bookRepository.getAllBooks())
        }
    }

    @Test
    fun `add whole book`() {
        runBlocking {
            val response: BookAddResponse = makeCall(extension, "$path/book") {
                put(Entity.json(dummyBook))
            }

            assertEquals(BookAddStatus.ADDED, response.status)
            assertEquals(dummyBook.isbn, response.isbn)
            assertTrue(
                    Files.exists(coverPath.resolve("${dummyBook.isbn}.jpg")),
                    "Cover not downloaded"
            )
            ensureBookAdded()
        }
    }

    @Test
    fun `patch book`() {
        runBlocking {
            bookRepository.addBook(dummyBook)

            val isbn = dummyBook.isbn
            val modifiedBook = dummyBook.copy(title = "Dummy title")

            val response: BookPatchResponse = makePatch(
                    "$path/patch",
                    modifiedBook,
                    "isbn" to isbn
            )

            assertEquals(BookPatchStatus.PATCHED, response.status)
            assertEquals(dummyBook.isbn, response.isbn)
            assertEquals(listOf(modifiedBook), bookRepository.getAllBooks())
        }
    }

    @Test
    fun `patch invalid book`() {
        runBlocking {
            bookRepository.addBook(dummyBook)

            val isbn = "fhjsifu"
            val modifiedBook = dummyBook.copy(title = "Dummy title")

            val response: BookPatchResponse = makePatch(
                    "$path/patch",
                    modifiedBook,
                    "isbn" to isbn
            )

            assertEquals(BookPatchStatus.NOT_FOUND, response.status)
            assertEquals(isbn, response.isbn)
            assertEquals(listOf(dummyBook), bookRepository.getAllBooks())
        }
    }

    private suspend fun ensureBookAdded() {
        assertEquals(listOf(dummyBook), bookRepository.getAllBooks())
    }

    private class DummyBookFetcher(private val book: Book) : BookFetcher {
        override suspend fun fetch(isbn: String): Book? = if (isbn == book.isbn) book else null
    }

}