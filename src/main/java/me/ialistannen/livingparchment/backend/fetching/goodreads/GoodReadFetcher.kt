package me.ialistannen.livingparchment.backend.fetching.goodreads

import me.ialistannen.livingparchment.backend.fetching.BaseFetcher
import org.jsoup.nodes.Document
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class GoodReadFetcher : BaseFetcher() {

    override fun getQueryUrl(isbn: String): String {
        return "https://www.goodreads.com/search?q=$isbn"
    }

    override fun extractTitle(document: Document): String = document
            .getElementById("bookTitle")
            .text()

    override fun extractPageCount(document: Document): Int = document
            .getElementsByAttributeValue("itemprop", "numberOfPages")
            .first()
            .text()
            .split(" ")[0]
            .toInt()

    override fun extractIsbn(document: Document): String = document
            .getElementsByAttributeValue("itemprop", "isbn")
            .first()
            .text()

    override fun extractLanguage(document: Document): String = document
            .getElementsByAttributeValue("itemprop", "inLanguage")
            .first()
            .text()

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
            publisher to DATE_FORMAT.parse(publishedString)
        } catch (e: ParseException) {
            publisher to Date(0)
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
}

private val EXTRACTION_REGEX = """Published (.+)by (.+)""".toRegex()
private val DATE_FORMAT = SimpleDateFormat("MMMM dd yyyy", Locale.ENGLISH)