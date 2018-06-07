package me.ialistannen.livingparchment.backend.fetching.amazon

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import me.ialistannen.livingparchment.backend.fetching.FetcherTest
import me.ialistannen.livingparchment.backend.fetching.Requestor
import me.ialistannen.livingparchment.backend.util.toLocalDate
import me.ialistannen.livingparchment.common.model.Book
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month
import java.util.*

internal class AmazonFetcherTest : FetcherTest() {

    override val fetcher: AmazonFetcher = AmazonFetcher(DummyRequestor())

    @Test
    fun `test full date format`() {
        val date = LocalDate.of(2017, Month.MARCH, 15)
        val normalizedString = "15. März 2017"

        val result = fetcher.normalizePartialGermanDateString("15. März 2017")
        assertEquals(normalizedString, result)

        assertEquals(date, fetcher.parseDate(normalizedString).toLocalDate())
    }

    @Test
    fun `test date format without day`() {
        val date = LocalDate.of(2017, Month.MARCH, 1)
        val normalizedString = "1. März 2017"

        val result = fetcher.normalizePartialGermanDateString("März 2017")
        assertEquals(normalizedString, result)

        assertEquals(date, fetcher.parseDate(normalizedString).toLocalDate())
    }

    @Test
    fun `test date format without day and month`() {
        val date = LocalDate.of(2017, Month.JANUARY, 1)
        val normalizedString = "1. Januar 2017"

        val result = fetcher.normalizePartialGermanDateString("2017")
        assertEquals(normalizedString, result)

        assertEquals(date, fetcher.parseDate(normalizedString).toLocalDate())
    }

    @Test
    fun `test date format without anything`() {
        val normalizedString = ""

        val result = fetcher.normalizePartialGermanDateString("")
        assertEquals(normalizedString, result)

        assertEquals(Date(0), fetcher.parseDate(normalizedString, Date(0)))
    }

    override fun Book?.isCorrectBook() {
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
        Assertions.assertEquals(
                128,
                pageCount,
                "Page count differs"
        )
        Assertions.assertEquals(
                "9783551556967",
                isbn,
                "isbn differs"
        )
        Assertions.assertEquals(
                "Deutsch",
                language,
                "language differs"
        )
        Assertions.assertEquals(
                Date(1490310000000),
                published,
                "published date differs"
        )
        Assertions.assertEquals(
                "https://images-na.ssl-images-amazon.com/images/I/51rZmvCQ2DL._SX332_BO1,204,203,200_.jpg",
                imageUrl,
                "image url differs"
        )
        Assertions.assertTrue(
                "Kindern und jungen Menschen zugute" in extra["description"].toString(),
                "description wrong (got ${extra["description"]}"
        )
    }

    private class DummyRequestor : Requestor() {
        override fun getPage(url: String): Deferred<Document> {
            return async {
                if ("field-keywords=9783551556967" in url || "field-keywords=3551556962" in url) {
                    parseResource("/fetching/amazon/example_html/QueryResultPage.html")
                } else {
                    parseResource("/fetching/amazon/example_html/DetailPage.html")
                }
            }
        }

        private fun parseResource(path: String) = Jsoup.parse(
                DummyRequestor::class.java.getResourceAsStream(path)
                        .bufferedReader()
                        .readText()
        )
    }
}