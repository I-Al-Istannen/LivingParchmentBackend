package me.ialistannen.livingparchment.backend.fetching

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File

/**
 * Fetches a web page
 *
 * @param url the url of the page
 * @return the fetched page
 */
fun getPage(url: String): Deferred<Document> {
    return async {
        //                Jsoup.connect(url)
//                .userAgent("LivingParchment")
//                .get()

        Jsoup.parse(File("/tmp/test.html").readText())
    }
}