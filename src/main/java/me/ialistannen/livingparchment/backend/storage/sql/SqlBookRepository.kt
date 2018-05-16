package me.ialistannen.livingparchment.backend.storage.sql

import me.ialistannen.livingparchment.backend.storage.BookRepository
import me.ialistannen.livingparchment.backend.util.camelToSnakeCase
import me.ialistannen.livingparchment.common.api.query.QueryType
import me.ialistannen.livingparchment.common.model.Book
import me.ialistannen.livingparchment.common.serialization.toJsonTree
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.statement.SqlStatement
import org.jdbi.v3.core.statement.Update
import javax.inject.Inject
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaType

class SqlBookRepository @Inject constructor(
        private val jdbi: Jdbi
) : BookRepository {

    init {
        createBookTables()
    }

    override fun addBook(book: Book) {
        jdbi.useHandle<RuntimeException> {
            removeBook(book)

            it.createUpdate("""
                INSERT INTO Books (isbn, title, language, page_count, publisher, published, extra)
                VALUES (:isbn, :title, :language, :page_count, :publisher, :published, :extra)
                """)
                    .bindProperty(book::isbn)
                    .bindProperty(book::title)
                    .bindProperty(book::language)
                    .bindProperty(book::pageCount)
                    .bindProperty(book::publisher)
                    .bindProperty(book::published)
                    .bindJsonProperty(book::extra)
                    .execute()
        }
    }

    private fun Update.bindProperty(property: KProperty<*>): Update {
        return bindByType(
                property.name.camelToSnakeCase(),
                property.getter.call(),
                property.returnType.javaType
        )
    }

    private fun <T : SqlStatement<T>> SqlStatement<T>.bindJsonProperty(property: KProperty<*>): T {
        return bind(
                property.name.camelToSnakeCase(),
                property.getter.call().toJsonTree()
        )
    }

    override fun removeBook(book: Book) {
        jdbi.useHandle<RuntimeException> {
            it.createUpdate("DELETE FROM Books WHERE isbn = :isbn")
                    .bind("isbn", book.isbn)
                    .execute()
        }
    }

    override fun getAllBooks(): List<Book> {
        return getBooksForQuery(QueryType.RETURN_ALL, "", "")
    }

    override fun getBooksForQuery(type: QueryType, attribute: String, query: String): List<Book> {
        return emptyList()
    }

    /**
     * Creates the necessary tables for the [SqlBookRepository].
     */
    private fun createBookTables() {
        jdbi.useHandle<RuntimeException> {
            it.createUpdate("""
            CREATE TABLE IF NOT EXISTS Books (
              isbn VARCHAR(13) PRIMARY KEY,
              title TEXT NOT NULL,
              language VARCHAR(20),
              page_count INTEGER,
              publisher TEXT,
              published DATE,
              EXTRA JSON
            );""".trimIndent()
            ).execute()
        }
    }
}
