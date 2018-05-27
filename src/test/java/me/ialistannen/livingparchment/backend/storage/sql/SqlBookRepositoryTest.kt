package me.ialistannen.livingparchment.backend.storage.sql

import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.storage.sql.book.SqlBookRepository
import me.ialistannen.livingparchment.backend.storage.sql.location.SqlBookLocationRepository
import me.ialistannen.livingparchment.common.api.query.QueryType
import me.ialistannen.livingparchment.common.model.Book
import me.ialistannen.livingparchment.common.model.BookLocation
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

internal class SqlBookRepositoryTest : SqlTest() {

    companion object {

        private lateinit var bookRepository: SqlBookRepository

        private val bookOne: Book = Book(
                "Quidditch",
                20,
                "123456",
                "german",
                published = Date(
                        LocalDate.now()
                                .atStartOfDay()
                                .toInstant(ZoneOffset.ofHours(0))
                                .toEpochMilli()
                ),
                publisher = "Acme corporation",
                location = BookLocation("Test shelf 1", "N/A"),
                genre = listOf("fantasy", "rpg"),
                authors = listOf("Elise", "Coyote")
        )
        private val bookTwo: Book = Book(
                "Der kleine Mann",
                200,
                "12345678",
                "English",
                published = Date(
                        LocalDate.now()
                                .minusDays(2)
                                .atStartOfDay()
                                .toInstant(ZoneOffset.ofHours(0))
                                .toEpochMilli()
                ),
                publisher = "Prinzen ltd.",
                genre = listOf("biography", "music"),
                authors = listOf("Dagobart", "May")
        )

        @BeforeAll
        @JvmStatic
        fun setup() {
            SqlTest.setup()

            bookRepository = SqlBookRepository(jdbi)
            SqlBookLocationRepository(jdbi).apply {
                runBlocking {
                    bookOne.location?.let { addLocation(it) }
                    bookTwo.location?.let { addLocation(it) }
                }
            }

            runBlocking {
                bookRepository.addBook(bookOne)
                bookRepository.addBook(bookTwo)
            }
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            SqlTest.teardown()
        }
    }

    @Test
    fun addBook() {
        runBlocking {
            bookRepository.addBook(bookOne)

            val returned = bookRepository
                    .getBooksForQuery(QueryType.EXACT_MATCH, "isbn", "123456")

            assertEquals(bookOne, returned.firstOrNull())
        }
    }

    @Test
    fun `exact query on json field`() {
        runBlocking {
            bookRepository
                    .getBooksForQuery(QueryType.EXACT_MATCH, "authors", "Elise")
                    .validateReturnedOnlyBook()
        }
    }

    @Test
    fun `exact query on json field is case sensitive`() {
        runBlocking {
            val size = bookRepository
                    .getBooksForQuery(QueryType.EXACT_MATCH, "authors", "elise")
                    .size
            assertEquals(0, size)
        }
    }

    @Test
    fun `part query on json field`() {
        runBlocking {
            bookRepository
                    .getBooksForQuery(QueryType.PART, "authors", "Eli")
                    .validateReturnedOnlyBook()
        }
    }

    @Test
    fun `part query on json field is case insensitive`() {
        runBlocking {
            bookRepository
                    .getBooksForQuery(QueryType.PART, "authors", "eli")
                    .validateReturnedOnlyBook()
        }
    }

    @Test
    fun `regex query on json field`() {
        runBlocking {
            bookRepository
                    .getBooksForQuery(QueryType.REGEX_MATCH, "authors", "Eli.+")
                    .validateReturnedOnlyBook()
        }
    }

    @Test
    fun `regex query on json field is case insensitive`() {
        runBlocking {
            bookRepository
                    .getBooksForQuery(QueryType.REGEX_MATCH, "authors", "eli.+")
                    .validateReturnedOnlyBook()
        }
    }

    @Test
    fun `exact query on normal field`() {
        runBlocking {
            bookRepository
                    .getBooksForQuery(QueryType.EXACT_MATCH, "publisher", "Acme corporation")
                    .validateReturnedOnlyBook()
        }
    }

    @Test
    fun `exact query on normal field is case sensitive`() {
        runBlocking {
            val size = bookRepository
                    .getBooksForQuery(QueryType.EXACT_MATCH, "publisher", "acme corporation")
                    .size
            assertEquals(0, size)
        }
    }

    @Test
    fun `part query on normal field`() {
        runBlocking {
            bookRepository
                    .getBooksForQuery(QueryType.PART, "publisher", "Acme")
                    .validateReturnedOnlyBook()
        }
    }

    @Test
    fun `part query on normal field is case insensitive`() {
        runBlocking {
            bookRepository
                    .getBooksForQuery(QueryType.PART, "publisher", "acme")
                    .validateReturnedOnlyBook()
        }
    }

    @Test
    fun `regex query on normal field`() {
        runBlocking {
            bookRepository
                    .getBooksForQuery(QueryType.REGEX_MATCH, "publisher", "Acme.+")
                    .validateReturnedOnlyBook()
        }
    }

    @Test
    fun `regex query on normal field is case insensitive`() {
        runBlocking {
            bookRepository
                    .getBooksForQuery(QueryType.REGEX_MATCH, "publisher", "acme.+")
                    .validateReturnedOnlyBook()
        }
    }

    @Test
    fun `wrong exact query on json field doesn't match`() {
        runBlocking {
            bookRepository
                    .getBooksForQuery(QueryType.EXACT_MATCH, "authors", "acme")
                    .validateReturnedNoBook()
        }
    }

    @Test
    fun `wrong part query on json field doesn't match`() {
        runBlocking {
            bookRepository
                    .getBooksForQuery(QueryType.PART, "authors", "dsfwmo")
                    .validateReturnedNoBook()
        }
    }

    @Test
    fun `wrong regex query on json field doesn't match`() {
        runBlocking {
            bookRepository
                    .getBooksForQuery(QueryType.REGEX_MATCH, "authors", ".+HEY.+")
                    .validateReturnedNoBook()
        }
    }

    @Test
    fun `wrong exact query on normal field doesn't match`() {
        runBlocking {
            bookRepository
                    .getBooksForQuery(QueryType.EXACT_MATCH, "publisher", "acme")
                    .validateReturnedNoBook()
        }
    }

    @Test
    fun `wrong part query on normal field doesn't match`() {
        runBlocking {
            bookRepository
                    .getBooksForQuery(QueryType.PART, "publisher", "dsfwmo")
                    .validateReturnedNoBook()
        }
    }

    @Test
    fun `wrong regex query on normal field doesn't match`() {
        runBlocking {
            bookRepository
                    .getBooksForQuery(QueryType.REGEX_MATCH, "publisher", ".+HEY.+")
                    .validateReturnedNoBook()
        }
    }

    @Test
    fun `has our two books`() {
        runBlocking {
            val allBooks = bookRepository.getAllBooks()

            assertTrue(bookOne in allBooks, "Did not contain book one")
            assertTrue(bookTwo in allBooks, "Did not contain book two")
        }
    }

    private fun List<Book>.validateReturnedOnlyBook() {
        assertEquals(1, size)
        assertEquals(bookOne, firstOrNull())
    }

    private fun List<Book>.validateReturnedNoBook() {
        assertEquals(0, size)
    }
}