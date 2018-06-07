package me.ialistannen.livingparchment.backend.fetching

import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.common.model.Book
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal abstract class FetcherTest {

    abstract val fetcher: BookFetcher

    @Test
    fun `fails if book doesn't exist`() {
        runBlocking {
            Assertions.assertNull(fetcher.fetch("313142235325343"))
        }
    }

    @Test
    fun `finds basic information about known book with isbn 13`() {
        runBlocking {
            fetcher.fetch("9783551556967").isCorrectBook()
        }
    }

    @Test
    fun `finds basic information about known book with isbn 10`() {
        runBlocking {
            fetcher.fetch("3551556962").isCorrectBook()
        }
    }

    protected abstract fun Book?.isCorrectBook()
}