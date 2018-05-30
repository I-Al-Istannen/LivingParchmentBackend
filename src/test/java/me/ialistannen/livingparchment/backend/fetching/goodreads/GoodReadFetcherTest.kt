package me.ialistannen.livingparchment.backend.fetching.goodreads

import me.ialistannen.livingparchment.backend.fetching.FetcherTest
import me.ialistannen.livingparchment.backend.util.toLocalDate
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month
import java.util.*

internal class GoodReadFetcherTest : FetcherTest() {
    override val fetcher: GoodReadFetcher = GoodReadFetcher()


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
}