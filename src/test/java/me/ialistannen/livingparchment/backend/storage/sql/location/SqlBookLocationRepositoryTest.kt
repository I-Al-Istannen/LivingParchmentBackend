package me.ialistannen.livingparchment.backend.storage.sql.location

import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.storage.sql.SqlTest
import me.ialistannen.livingparchment.common.model.BookLocation
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.*

internal class SqlBookLocationRepositoryTest : SqlTest() {
    companion object {

        private lateinit var bookLocationRepository: SqlBookLocationRepository
        private val bookLocation = BookLocation("Shelf 1", "The first shelf")

        @BeforeAll
        @JvmStatic
        fun setup() {
            SqlTest.setup()

            runBlocking {
                bookLocationRepository = SqlBookLocationRepository(jdbi)
            }
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            SqlTest.teardown()
        }
    }

    @Test
    fun `test adding location works`() {
        runBlocking {
            bookLocationRepository.addLocation(bookLocation)
            assertEquals(bookLocation, bookLocationRepository.getLocation(bookLocation.uuid))
        }
    }

    @Test
    fun `test removing location works`() {
        runBlocking {
            bookLocationRepository.addLocation(bookLocation)

            bookLocationRepository.deleteLocation(bookLocation.uuid)

            assertNull(bookLocationRepository.getLocation(bookLocation.uuid))
        }
    }

    @Test
    fun `test replacing works`() {
        runBlocking {
            bookLocationRepository.addLocation(bookLocation)
            val modified = bookLocation.copy(name = "Test")

            bookLocationRepository.addLocation(modified)

            assertEquals(modified, bookLocationRepository.getLocation(bookLocation.uuid))
        }
    }

    @Test
    fun `returns null for other uuid`() {
        runBlocking {
            assertNull(bookLocationRepository.getLocation(UUID.randomUUID()))
        }
    }
}