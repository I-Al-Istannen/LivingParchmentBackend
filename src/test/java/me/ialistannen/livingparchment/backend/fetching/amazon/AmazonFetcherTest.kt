package me.ialistannen.livingparchment.backend.fetching.amazon

import me.ialistannen.livingparchment.backend.fetching.FetcherTest
import me.ialistannen.livingparchment.backend.util.toLocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month
import java.util.*

internal class AmazonFetcherTest : FetcherTest() {

    override val fetcher: AmazonFetcher = AmazonFetcher()

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
}