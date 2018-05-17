package me.ialistannen.livingparchment.backend.storage

import me.ialistannen.livingparchment.common.api.query.QueryType
import me.ialistannen.livingparchment.common.model.Book

/**
 * Manages stored books. Any method may throw a [StorageException].
 */
interface BookRepository {

    /**
     * Adds a book.
     *
     * @param book the book to add
     * @throws StorageException if an error occurs
     */
    fun addBook(book: Book)

    /**
     * Removes the given book from the repository.
     *
     * @param book the book to remove
     * @throws StorageException if an error occurs
     */
    fun removeBook(book: Book)

    /**
     * Returns all books.
     *
     * @throws StorageException if an error occurs
     */
    fun getAllBooks(): List<Book>

    /**
     * Searches books in the repository.
     *
     * @param type the [QueryType] to use
     * @param attribute the attribute to search
     * @param query the query
     * @return all books matching the query
     * @throws StorageException if an error occurs
     */
    fun getBooksForQuery(type: QueryType, attribute: String, query: String): List<Book>
}