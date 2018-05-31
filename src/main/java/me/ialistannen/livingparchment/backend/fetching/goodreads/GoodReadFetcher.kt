package me.ialistannen.livingparchment.backend.fetching.goodreads

import me.ialistannen.livingparchment.backend.fetching.BaseFetcher
import me.ialistannen.livingparchment.backend.util.logger
import org.jsoup.nodes.Document
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class GoodReadFetcher : BaseFetcher() {

    private val logger by logger()

    override fun getQueryUrl(isbn: String): String {
        return "https://www.goodreads.com/search?q=$isbn"
    }

    override suspend fun preprocessQueryPage(document: Document): Sequence<Document> {
        if (document.getElementsByClass("authorName").isEmpty()) {
            return emptySequence()
        }
        return sequenceOf(document)
    }

    override fun extractTitle(document: Document): String = document
            .getElementById("bookTitle")
            .text()

    override fun extractPageCount(document: Document): Int = document
            .getElementsByAttributeValue("itemprop", "numberOfPages")
            .firstOrNull()
            ?.text()
            ?.split(" ")?.get(0)
            ?.toInt()
            ?: 0

    override fun extractIsbn(document: Document): String = document
            .getElementsByAttributeValue("itemprop", "isbn")
            .first()
            .text()

    override fun extractLanguage(document: Document): String = document
            .getElementsByAttributeValue("itemprop", "inLanguage")
            ?.first()
            ?.text()
            ?: "N/A"

    override fun extractBookImageUrl(document: Document): String? {
        return document.getElementById("coverImage")
                ?.absUrl("src")
    }

    override fun extractPublished(document: Document): Pair<String, Date> {
        val fullText = document.getElementById("details").text()
        val matchResult = EXTRACTION_REGEX.find(fullText) ?: return "" to Date(0)

        val publisher = matchResult.groupValues[2]
                .replace("\\(.+".toRegex(), "")
                .trim()
        val publishedString = matchResult.groupValues[1]
                .replace("(\\d+)\\w*".toRegex(), "$1")
                .trim()

        return try {
            publisher to parseDate(publishedString)
        } catch (e: ParseException) {
            publisher to Date(0)
        }
    }


    internal fun parseDate(input: String, default: Date = Date(0)): Date {
        return try {
            DATE_FORMAT.parse(normalizePartialEnglishDateString(input))
        } catch (e: Exception) {
            logger.warn("Can't parse date: '$input', message was: '${e.localizedMessage}'")
            default
        }
    }

    internal fun normalizePartialEnglishDateString(input: String): String {
        val parts = input.split(" ").filter { it.isNotBlank() }

        return when {
            parts.size == 3 -> input
            parts.size == 2 -> "${parts[0]} 01 ${parts[1]}"
            parts.size == 1 -> "January 01 ${parts.first()}"
            else -> ""
        }
    }

    override fun extractAuthors(document: Document): List<String> {
        return document.getElementsByClass("authorName")
                .map { it.text().trim() }
                .fold(arrayListOf()) { acc: MutableList<String>, s: String ->
                    if (s.startsWith("(") && acc.isNotEmpty()) {
                        acc[acc.lastIndex] += " $s"
                    } else {
                        acc.add(s)
                    }
                    acc
                }
    }

    override fun extractGenre(document: Document): List<String> {
        return document.getElementsByClass("bookPageGenreLink")
                .filter { it.attr("href").startsWith("/genre") }
                .map { it.text().trim() }
    }

    override fun addExtra(document: Document): Map<String, Any> {
        val descriptionElements = document.getElementById("description")
                ?.getElementsByTag("span")
        val description = descriptionElements
                ?.last()
                ?.text()
                ?: return emptyMap()
        return mapOf("description" to description)
    }
}

private val EXTRACTION_REGEX = """Published (.+)by (.+)""".toRegex()
private val DATE_FORMAT = SimpleDateFormat("MMMM dd yyyy", Locale.ENGLISH)