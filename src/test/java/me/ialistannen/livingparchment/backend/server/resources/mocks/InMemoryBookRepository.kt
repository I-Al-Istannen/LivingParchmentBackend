package me.ialistannen.livingparchment.backend.server.resources.mocks

import me.ialistannen.livingparchment.backend.storage.BookRepository
import me.ialistannen.livingparchment.common.api.query.QueryType
import me.ialistannen.livingparchment.common.model.Book

class InMemoryBookRepository : BookRepository {

    private val books: MutableList<Book> = mutableListOf()

    override suspend fun addBook(book: Book) {
        removeBook(book.isbn)
        books.add(book)
    }

    override suspend fun removeBook(book: Book): Boolean {
        return books.remove(book)
    }

    override suspend fun removeBook(isbn: String): Boolean {
        return books.removeIf { it.isbn == isbn }
    }

    override suspend fun getAllBooks(): List<Book> {
        return ArrayList(books)
    }

    override suspend fun getBooksForQuery(type: QueryType, attribute: String, query: String): List<Book> {
        // this is tested by the repo test
        if (type == QueryType.EXACT_MATCH && attribute == "isbn") {
            return books.filter { it.isbn == query }
        }
        return getAllBooks()
    }
}