package me.ialistannen.livingparchment.backend.storage.sql

import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.common.api.query.QueryType
import me.ialistannen.livingparchment.common.model.Book
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

internal class SqlBookRepositoryTest {

    companion object {

        private lateinit var jdbi: Jdbi
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
            jdbi = Jdbi.create(
                    "jdbc:postgresql://localhost:5432/LivingParchmentTest",
                    "LivingParchment",
                    "123456"
            ).apply {
                installPlugin(SqlObjectPlugin())
                installPlugin(KotlinSqlObjectPlugin())
                installPlugin(KotlinPlugin())
                registerArgument(JsonNNodeArgumentFactory())
            }
            jdbi.useHandle<RuntimeException> {
                it.createUpdate("DROP TABLE Books").execute()
                DatabaseCreator().createTables(it)
            }
            bookRepository = SqlBookRepository(jdbi)

            runBlocking {
                bookRepository.addBook(bookOne)
                bookRepository.addBook(bookTwo)
            }
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

    private fun List<Book>.validateReturnedOnlyBook() {
        assertEquals(1, size)
        assertEquals(bookOne, firstOrNull())
    }

    private fun List<Book>.validateReturnedNoBook() {
        assertEquals(0, size)
    }
}