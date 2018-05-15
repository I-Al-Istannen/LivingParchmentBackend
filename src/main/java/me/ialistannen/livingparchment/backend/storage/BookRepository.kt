package me.ialistannen.livingparchment.backend.storage

import me.ialistannen.livingparchment.common.api.query.QueryType
import me.ialistannen.livingparchment.common.model.Book

interface BookRepository {

    /**
     * Adds a book.
     *
     * @param book the book to add
     */
    fun addBook(book: Book)

    /**
     * Removes the given book from the repository.
     *
     * @param book the book to remove
     */
    fun removeBook(book: Book)

    /**
     * Returns all books.
     */
    fun getAllBooks(): List<Book>

    /**
     * Searches books in the repository.
     *
     * @param type the [QueryType] to use
     * @param attribute the attribute to search
     * @param query the query
     */
    fun getBooksForQuery(type: QueryType, attribute: String, query: String): List<Book>
}