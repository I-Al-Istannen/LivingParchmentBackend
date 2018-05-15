package me.ialistannen.livingparchment.backend.storage.sql

import me.ialistannen.livingparchment.backend.storage.BookRepository
import me.ialistannen.livingparchment.common.api.query.QueryType
import me.ialistannen.livingparchment.common.model.Book
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.statement.PreparedBatch
import javax.inject.Inject
import kotlin.reflect.KProperty

class SqlBookRepository @Inject constructor(
        private val jdbi: Jdbi
) : BookRepository {

    init {
        createBookTables()
    }

    override fun addBook(book: Book) {
        jdbi.useHandle<RuntimeException> {
            removeBook(book)

            it.createUpdate("INSERT INTO Books (isbn) VALUES (:isbn)")
                    .bind("isbn", book.isbn)
                    .execute()

            val preparedBatch = it.prepareBatch(
                    "INSERT INTO BookAttributes (isbn, name, value) VALUES (?, ?, ?)"
            )
            preparedBatch.addPropertyWithIsbn(book, book::title)
            preparedBatch.addPropertyWithIsbn(book, book::language)
            preparedBatch.addPropertyWithIsbn(book, book::pageCount)
            preparedBatch.addListPropertyWithIsbn(book, book::genre)
            preparedBatch.addListPropertyWithIsbn(book, book::author)

            for ((name, value) in book.extra) {
                preparedBatch.add(book.isbn, name, value)
            }
            preparedBatch.execute()
        }
    }

    private fun PreparedBatch.addPropertyWithIsbn(book: Book, property: KProperty<*>) {
        add(book.isbn, property.name, property.getter.call().toString())
    }

    private fun PreparedBatch.addListPropertyWithIsbn(book: Book, property: KProperty<List<*>>) {
        for (value in property.getter.call()) {
            add(book.isbn, property.name, value.toString())
        }
    }

    override fun removeBook(book: Book) {
        jdbi.useHandle<RuntimeException> {
            it.createUpdate("DELETE FROM Books WHERE isbn = :isbn")
                    .bind("isbn", book.isbn)
                    .execute()
        }
    }

    override fun getAllBooks(): List<Book> {
        jdbi.useHandle<RuntimeException> {

        }
    }

    override fun getBooksForQuery(type: QueryType, attribute: String, query: String): List<Book> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Creates the necessary tables for the [SqlBookRepository].
     */
    private fun createBookTables() {
        jdbi.useHandle<RuntimeException> {
            it.createUpdate("""
            CREATE TABLE IF NOT EXISTS Books (
              isbn VARCHAR(13) PRIMARY KEY
            );""".trimIndent()
            ).execute()

            it.createUpdate("""
            CREATE TABLE IF NOT EXISTS BookAttributes (
              isbn VARCHAR(13) REFERENCES Books(isbn) ON DELETE CASCADE ON UPDATE CASCADE,
              name VARCHAR(40) NOT NULL,
              value TEXT NOT NULL
            );
            """.trimIndent()
            ).execute()
        }
    }
}
