package me.ialistannen.livingparchment.backend.fetching.goodreads

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import me.ialistannen.livingparchment.backend.fetching.FetcherTest
import me.ialistannen.livingparchment.backend.fetching.Requestor
import me.ialistannen.livingparchment.backend.util.toLocalDate
import me.ialistannen.livingparchment.common.model.Book
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month
import java.util.*

internal class GoodReadFetcherTest : FetcherTest() {

    override val fetcher: GoodReadFetcher = GoodReadFetcher(DummyRequestor())


    @Test
    fun `test full date format`() {
        val date = LocalDate.of(2017, Month.MARCH, 15)
        val normalizedString = "March 15 2017"

        val result = fetcher.normalizePartialEnglishDateString("March 15 2017")
        Assertions.assertEquals(normalizedString, result)

        Assertions.assertEquals(date, fetcher.parseDate(normalizedString).toLocalDate())
    }

    @Test
    fun `test date format without day`() {
        val date = LocalDate.of(2017, Month.MARCH, 1)
        val normalizedString = "March 01 2017"

        val result = fetcher.normalizePartialEnglishDateString("March 2017")
        Assertions.assertEquals(normalizedString, result)

        Assertions.assertEquals(date, fetcher.parseDate(normalizedString).toLocalDate())
    }

    @Test
    fun `test date format without day and month`() {
        val date = LocalDate.of(2017, Month.JANUARY, 1)
        val normalizedString = "January 01 2017"

        val result = fetcher.normalizePartialEnglishDateString("2017")
        Assertions.assertEquals(normalizedString, result)

        Assertions.assertEquals(date, fetcher.parseDate(normalizedString).toLocalDate())
    }

    @Test
    fun `test date format without anything`() {
        val normalizedString = ""

        val result = fetcher.normalizePartialEnglishDateString("")
        Assertions.assertEquals(normalizedString, result)

        Assertions.assertEquals(Date(0), fetcher.parseDate(normalizedString, Date(0)))
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
                "German",
                language,
                "language differs"
        )
        Assertions.assertEquals(
                Date(1490310000000),
                published,
                "published date differs"
        )
        Assertions.assertEquals(
                "https://images.gr-assets.com/books/1490271402l/34668075.jpg",
                imageUrl,
                "image url differs"
        )
        Assertions.assertTrue(
                "Entdecken Sie das faszinierende" in extra["description"].toString(),
                "description wrong (got ${extra["description"]}"
        )
    }

    private class DummyRequestor : Requestor() {
        override fun getPage(url: String): Deferred<Document> {
            return async {
                if ("9783551556967" in url || "3551556962" in url) {
                    parseResource("/fetching/goodreads/example_html/DetailPage.html")
                } else {
                    Jsoup.parse("")
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