package me.ialistannen.livingparchment.backend.server.resources

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import io.dropwizard.testing.junit5.ResourceExtension
import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.server.resources.mocks.InMemoryBookRepository
import me.ialistannen.livingparchment.backend.util.toDate
import me.ialistannen.livingparchment.common.api.response.BookDeleteResponse
import me.ialistannen.livingparchment.common.api.response.BookDeleteStatus
import me.ialistannen.livingparchment.common.model.Book
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate
import java.time.Month

@ExtendWith(DropwizardExtensionsSupport::class)
class BookDeleteEndpointTest {

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

        val bookRepository = InMemoryBookRepository()
        override val endpoint: BookDeleteEndpoint = BookDeleteEndpoint(bookRepository)

        override val extension: ResourceExtension = extension()
        override val path: String = "/delete"
    }

    @AfterEach
    fun cleanup() {
        runBlocking {
            bookRepository.getAllBooks().forEach { bookRepository.removeBook(it) }
        }
    }

    @Test
    fun `delete valid book`() {
        runBlocking {
            bookRepository.addBook(dummyBook)
            val isbn = dummyBook.isbn

            val response = makeCall<BookDeleteResponse>(listOf("isbn" to isbn)) {
                delete()
            }

            assertEquals(BookDeleteStatus.DELETED, response.status)
            assertEquals(isbn, response.isbn)
            assertEquals(emptyList<Book>(), bookRepository.getAllBooks())
        }
    }

    @Test
    fun `delete invalid book`() {
        runBlocking {
            bookRepository.addBook(dummyBook)
            val isbn = "dsds"

            val response = makeCall<BookDeleteResponse>(listOf("isbn" to isbn)) {
                delete()
            }

            assertEquals(BookDeleteStatus.NOT_FOUND, response.status)
            assertEquals(isbn, response.isbn)
            assertEquals(listOf(dummyBook), bookRepository.getAllBooks())
        }
    }

    @Test
    fun `delete without isbn`() {
        runBlocking {
            bookRepository.addBook(dummyBook)

            assertThrows<Exception> {
                makeCall<BookDeleteResponse> {
                    delete()
                }
            }

            assertEquals(listOf(dummyBook), bookRepository.getAllBooks())
        }
    }
}