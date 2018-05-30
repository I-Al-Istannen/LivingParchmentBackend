package me.ialistannen.livingparchment.backend.fetching

import me.ialistannen.livingparchment.backend.util.logger
import me.ialistannen.livingparchment.common.model.Book
import org.jsoup.nodes.Document
import java.util.*

abstract class BaseFetcher : BookFetcher {

    private val logger by logger()

    override suspend fun fetch(isbn: String): Book? {
        return try {
            val document = WebpageUtil.getPage(getQueryUrl(isbn)).await()
            val toProcess = preprocessQueryPage(document)

            toProcess
                    .mapNotNull {
                        try {
                            extractFromPage(it)
                        } catch (e: Exception) {
                            logger.info("Error fetching book ($isbn) '${e.localizedMessage}'")
                            null
                        }
                    }
                    .firstOrNull()
        } catch (e: Exception) {
            logger.info("Error fetching a book: '$isbn'", e)
            null
        }
    }

    protected abstract fun getQueryUrl(isbn: String): String

    /**
     * Performs some preprocessing with the query page.
     *
     * This method is suspending as you may need to perform additional webqueries to load the
     * detail pages, if the query didn't redirect you to the result page.
     *
     * @param document the query page
     * @return the document to pass to [extractFromPage]. If multiple could fit, return them all
     * and they will be used one after another until one succeeds
     */
    protected open suspend fun preprocessQueryPage(document: Document): Sequence<Document> {
        return sequenceOf(document)
    }

    protected open fun extractFromPage(document: Document): Book? {
        val title = extractTitle(document)
        val pageCount = catchErrors(document, 0, this::extractPageCount)
        val isbn = extractIsbn(document)
        val language = catchErrors(document, "N/A", this::extractLanguage)
        val publishInformation = catchErrors(
                document, "N/A" to Date(0), this::extractPublished
        )
        val authors = catchErrors(document, emptyList(), this::extractAuthors)
        val genre = catchErrors(document, emptyList(), this::extractGenre)
        val imageUrl: String? = catchErrors(document, null, this::extractBookImageUrl)

        return Book(
                title,
                pageCount,
                isbn,
                language,
                publisher = publishInformation.first,
                published = publishInformation.second,
                imageUrl = imageUrl,
                authors = authors,
                genre = genre,
                extra = addExtra(document)
        )
    }

    private fun <T> catchErrors(document: Document, default: T, action: (Document) -> T): T {
        return try {
            action.invoke(document)
        } catch (e: Exception) {
            logger.info("Error executing fetch", e)
            default
        }
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
     * Extracts the authors.
     */
    protected abstract fun extractAuthors(document: Document): List<String>

    /**
     * Extracts the genre.
     */
    protected abstract fun extractGenre(document: Document): List<String>

    /**
     * Extracts the image url for the book.
     */
    protected abstract fun extractBookImageUrl(document: Document): String?

    /**
     * Adds additional information.
     */
    protected open fun addExtra(document: Document): Map<String, Any> {
        return emptyMap()
    }
}