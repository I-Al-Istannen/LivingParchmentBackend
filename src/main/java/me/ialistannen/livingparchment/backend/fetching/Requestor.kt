package me.ialistannen.livingparchment.backend.fetching

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import javax.inject.Inject

open class Requestor @Inject constructor() {
    /**
     * Fetches a web page
     *
     * @param url the url of the page
     * @return the fetched page
     */
    open fun getPage(url: String): Deferred<Document> {
        return async {
            Jsoup.connect(url)
                    .userAgent("LivingParchment")
                    .header("Accept-Language", "de,en-US;q=0.7,en;q=0.3") // german is nice
                    .get()
        }
    }
}
