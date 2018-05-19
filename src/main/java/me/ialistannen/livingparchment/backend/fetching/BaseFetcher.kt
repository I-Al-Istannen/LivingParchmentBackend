package me.ialistannen.livingparchment.backend.fetching

import me.ialistannen.livingparchment.common.model.Book
import org.jsoup.nodes.Document
import java.util.*

abstract class BaseFetcher : BookFetcher {
    override suspend fun fetch(isbn: String): Book? {
        val document = getPage(getQueryUrl(isbn)).await()

        return extractFromPage(document)
    }

    protected abstract fun getQueryUrl(isbn: String): String

    protected open fun extractFromPage(document: Document): Book? {
        val title = extractTitle(document)
        val pageCount = extractPageCount(document)
        val isbn = extractIsbn(document)
        val language = extractLanguage(document)
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
                genre = genre,
                extra = addExtra(document)
        )
    }

    /**
     * Extracts the title.
     */
    protected abstract fun extractTitle(document: Document): String

    /**
     * Extracts the page count.
     */
    protected abstract fun extractPageCount(document: Document): Int

    /**
     * Extracts the ISBN.
     */
    protected abstract fun extractIsbn(document: Document): String

    /**
     * Extracts the language.
     */
    protected abstract fun extractLanguage(document: Document): String

    /**
     * Extracts publish information. Format is `<Publisher, Date published>`.
     */
    protected abstract fun extractPublished(document: Document): Pair<String, Date>

    /**
     * Extracts the authors
     */
    protected abstract fun extractAuthors(document: Document): List<String>

    /**
     * Extracts the genre
     */
    protected abstract fun extractGenre(document: Document): List<String>

    /**
     * Adds additional information.
     */
    protected open fun addExtra(document: Document): Map<String, Any> {
        return emptyMap()
    }
}