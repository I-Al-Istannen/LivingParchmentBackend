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
            fetcher.fetch("9783551556967").isQuidditchBook()
        }
    }

    @Test
    fun `finds basic information about known book with isbn 10`() {
        runBlocking {
            fetcher.fetch("3551556962").isQuidditchBook()
        }
    }

    private fun Book?.isQuidditchBook() {
        Assertions.assertNotNull(this)

        // Smart cast later on
        this as Book

        Assertions.assertTrue(
                "antastische Tierwesen und wo sie zu finden sind".toLowerCase()
                        in title.toLowerCase(),
                "title differs (got $title)"
        )
        Assertions.assertTrue(
                authors.any { "Rowling" in it } || authors.any { "Scamander" in it },
                "Rowling is not author (got $authors)"
        )
        Assertions.assertTrue(
                "carlsen" in publisher.toLowerCase(),
                "Publisher is not carlsen (got $publisher)"
        )
    }
}