package me.ialistannen.livingparchment.backend.fetching.amazon

import me.ialistannen.livingparchment.backend.fetching.BaseFetcher
import me.ialistannen.livingparchment.backend.fetching.FetchException
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.*

class AmazonFetcher : BaseFetcher() {

    override fun getQueryUrl(isbn: String): String {
        return "https://www.amazon.de/s/&field-keywords=$isbn"
    }

    override fun extractTitle(document: Document): String = document
            .getElementById("productTitle")
            .text()

    override fun extractPageCount(document: Document): Int {
        return detailSection(document,
                filter = { "seiten" in element.text().toLowerCase() },
                mapper = {
                    it.firstOrNull()
                            ?.mapData { it.replace("[^\\d]".toRegex(), "").toInt() }
                            ?: 0
                }
        )
    }

    override fun extractIsbn(document: Document): String {
        return detailSection(document,
                filter = { "isbn-13" in element.text().toLowerCase() },
                mapper = {
                    it.firstOrFail("No ISBN found for ${document.location()}")
                            .mapData {
                                it.replace("[^\\d]".toRegex(), "")
                            }
                })
    }

    override fun extractLanguage(document: Document): String {
        return detailSection(document,
                filter = { "sprache" in element.text().toLowerCase() },
                mapper = {
                    it.firstOrNull()
                            ?.mapData { it }
                            ?: "N/A"
                })
    }

    override fun extractPublished(document: Document): Pair<String, Date> {
        val publisher = detailSection(document,
                filter = { "verlag" in element.text().toLowerCase() },
                mapper = {
                    it.firstOrNull()
                            ?.mapData {
                                it.substringBefore(";")
                            }
                            ?: ""
                })
        val dateString = detailSection(document,
                filter = { "verlag" in element.text().toLowerCase() },
                mapper = {
                    it.firstOrNull()
                            ?.mapData {
                                Regex("\\((.+?)\\)").findAll(it)
                                        .lastOrNull()
                                        ?.groupValues
                                        ?.get(1)
                                        ?: ""
                            }
                })

        val date = if (dateString.isNullOrBlank()) {
            Date(0)
        } else {
            dateFormat.parse(dateString)
        }

        return publisher to date
    }

    override fun extractAuthors(document: Document): List<String> {
        return document.getElementsByClass("author")
                .mapNotNull {
                    val authorName = extractAuthorName(it) ?: return@mapNotNull null
                    val contribution = it.getElementsByClass("contribution")
                            ?.firstOrNull()
                            ?.text()
                            ?.replace("),", ")")
                            .orEmpty()

                    "$authorName $contribution"
                }
                .filter(String::isNotBlank)
    }

    private fun extractAuthorName(element: Element): String? {
        return element.getElementsByClass("contributorNameID")
                ?.text()
                ?: element.getElementsByTag("a")
                        .firstOrNull()
                        ?.text()
    }

    override fun extractGenre(document: Document): List<String> {
        return emptyList()
    }

    override fun addExtra(document: Document): Map<String, Any> {
        val description = document
                .getElementsByAttributeValue("data-feature-name", "bookDescription")
                .firstOrNull()
                ?.getElementsByTag("noscript")
                ?.firstOrNull()
                ?.text()
                ?: return emptyMap()

        return mapOf("description" to description)
    }

    /**
     * Performs an action on the detail section of the page.
     *
     * @param document the document
     * @param filter the filter for the [ListItem]s
     * @param mapper the mapping function
     *
     * @return the return value of the function
     */
    private fun <T> detailSection(document: Document,
                                  filter: ListItem.() -> Boolean,
                                  mapper: (List<ListItem>) -> T): T {
        val listItems = document.getElementById("detail_bullets_id").findListItem(filter)

        return mapper.invoke(listItems)
    }

    private fun Element.findListItem(listPredicate: (ListItem) -> Boolean): List<ListItem> {
        return getElementsByTag("li")
                .map { ListItem(it) }
                .filter { listPredicate.invoke(it) }
    }

    private data class ListItem(val element: Element) {

        fun <T> map(function: (Element) -> T): T {
            return function.invoke(element)
        }

        fun <T> mapData(function: (String) -> T): T {
            return map {
                function.invoke(it.textNodes().firstOrFail("Data not present").text().trim())
            }
        }
    }
}

private fun <T> Collection<T>.firstOrFail(message: String): T {
    if (isEmpty()) {
        throw FetchException(message)
    }
    return first()
}

private val dateFormat = SimpleDateFormat("dd. MMMM yyyy")