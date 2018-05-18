package me.ialistannen.livingparchment.backend.fetching

import me.ialistannen.livingparchment.common.model.Book

interface BookFetcher {

    /**
     * Fetches a book by isbn, if possible.
     *
     * @return the fetched book
     */
    suspend fun fetch(isbn: String): Book?
}