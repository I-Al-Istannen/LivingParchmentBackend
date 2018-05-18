package me.ialistannen.livingparchment.backend.fetching.goodreads

import me.ialistannen.livingparchment.backend.fetching.BookFetcher
import me.ialistannen.livingparchment.backend.fetching.getPage
import me.ialistannen.livingparchment.common.model.Book
import org.jsoup.nodes.Document
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class GoodReadFetcher : BookFetcher {

    override suspend fun fetch(isbn: String): Book? {
        val document = getPage(getQueryUrl(isbn)).await()

        return extractFromPage(document)
    }

    private fun getQueryUrl(isbn: String): String {
        return "https://www.goodreads.com/search?q=$isbn"
    }

    private fun extractFromPage(document: Document): Book? {
        val title = document.getElementById("bookTitle")
                .text()
        val pageCount = document.getElementsByAttributeValue("itemprop", "numberOfPages")
                .first()
                .text()
                .split(" ")[0]
                .toInt()
        val isbn = document.getElementsByAttributeValue("itemprop", "isbn")
                .first()
                .text()
        val language = document.getElementsByAttributeValue("itemprop", "inLanguage")
                .first()
                .text()
        val publishInformation = extractPublished(document)
        val authors = extractAuthors(document)
        val genre = extractGenre(document)

        return Book(
                title,
                pageCount,
                isbn,
                language,
                publisher = publishInformation.first,
                published = publishInformation.second,
                authors = authors,
                genre = genre
        )
    }

    private fun extractPublished(document: Document): Pair<String, Date> {
        val fullText = document.getElementById("details").text()
        val matchResult = EXTRACTION_REGEX.find(fullText) ?: return "" to Date(0)

        val publisher = matchResult.groupValues[2]
                .replace("\\(.+".toRegex(), "")
                .trim()
        val publishedString = matchResult.groupValues[1]
                .replace("(\\d+)\\w*".toRegex(), "$1")
                .trim()

        return try {
            publisher to DATE_FORMAT.parse(publishedString)
        } catch (e: ParseException) {
            publisher to Date(0)
        }
    }

    private fun extractAuthors(document: Document): List<String> {
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

    private fun extractGenre(document: Document): List<String> {
        return document.getElementsByClass("bookPageGenreLink")
                .filter { it.attr("href").startsWith("/genre") }
                .map { it.text().trim() }
    }
}

private val EXTRACTION_REGEX = """Published (.+)by (.+)""".toRegex()
private val DATE_FORMAT = SimpleDateFormat("MMMM dd yyyy", Locale.ENGLISH)