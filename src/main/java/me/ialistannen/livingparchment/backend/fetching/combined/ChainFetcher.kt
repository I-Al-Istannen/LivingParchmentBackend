package me.ialistannen.livingparchment.backend.fetching.combined

import me.ialistannen.livingparchment.backend.fetching.BookFetcher
import me.ialistannen.livingparchment.common.model.Book

/**
 * Chains multiple fetchers together.
 */
class ChainFetcher(vararg fetchers: BookFetcher) : BookFetcher {

    val fetchers: MutableList<BookFetcher> = fetchers.toMutableList()

    override suspend fun fetch(isbn: String): Book? {
        for (fetcher in fetchers) {
            val book = fetcher.fetch(isbn)

            if (book != null) {
                return book
            }
        }

        return null
    }

    override fun toString(): String {
        return "ChainFetcher(fetchers=$fetchers)"
    }
}

/**
 * Chains the two fetchers together..
 *
 * @receiver the fetcher to query first
 * @param bookFetcher the fetcher to use after this
 * @return a *new* [BookFetcher] that queries the receiver and then the parameter or the receiver
 * if the receiver was a [ChainFetcher]
 */
fun BookFetcher.then(bookFetcher: BookFetcher): BookFetcher {
    if (this is ChainFetcher) {
        fetchers.add(bookFetcher)
        return this
    }
    return ChainFetcher(this, bookFetcher)
}