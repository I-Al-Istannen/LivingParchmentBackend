package me.ialistannen.livingparchment.backend.storage.sql

import me.ialistannen.livingparchment.backend.storage.BookRepository
import me.ialistannen.livingparchment.backend.storage.StorageException
import me.ialistannen.livingparchment.backend.util.camelToSnakeCase
import me.ialistannen.livingparchment.common.api.query.QueryType
import me.ialistannen.livingparchment.common.model.Book
import me.ialistannen.livingparchment.common.serialization.toJsonTree
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.statement.Query
import org.jdbi.v3.core.statement.SqlStatement
import org.jdbi.v3.core.statement.Update
import javax.inject.Inject
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaType

class SqlBookRepository @Inject constructor(
        private val jdbi: Jdbi
) : BookRepository {

    private val fieldAttributes = setOf(
            "title", "published", "publisher", "language", "page_count", "isbn"
    )
    private val allowedJsonAttributes = setOf(
            "authors", "genre"
    )

    override suspend fun addBook(book: Book) {
        jdbi.using {
            // remove it if it already exists
            removeBook(book)

            createUpdate("""
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

    private fun <T> Update.bindProperty(property: KProperty<T>,
                                        transform: (T) -> Any? = { it }): Update {
        val transformed = transform.invoke(property.getter.call())
        return bindByType(
                property.name.camelToSnakeCase(),
                transformed,
                transformed?.javaClass ?: property.returnType.javaType
        )
    }

    private fun <T : SqlStatement<T>> SqlStatement<T>.bindJsonProperty(property: KProperty<*>): T {
        return bind(
                property.name.camelToSnakeCase(),
                property.getter.call().toJsonTree()
        )
    }

    override suspend fun removeBook(book: Book): Boolean {
        return removeBook(book.isbn)
    }

    override suspend fun removeBook(isbn: String): Boolean {
        return jdbi.using {
            createUpdate("DELETE FROM Books WHERE isbn = :isbn")
                    .bind("isbn", isbn)
                    .execute()
        } != 0
    }

    override suspend fun getAllBooks(): List<Book> {
        var result: List<Book> = emptyList()
        jdbi.using {
            result = createQuery("SELECT * FROM Books")
                    .map(BookRowMapper())
                    .list()
        }
        return result
    }

    override suspend fun getBooksForQuery(type: QueryType, attribute: String,
                                          query: String): List<Book> {
        if (type == QueryType.RETURN_ALL) {
            return getAllBooks()
        }

        if (attribute in fieldAttributes) {
            return getBooksForFieldQuery(type, attribute, query)
        }

        return getBooksForJsonQuery(type, attribute, query)
    }

    private suspend fun getBooksForFieldQuery(type: QueryType, attribute: String, query: String): List<Book> {
        if (attribute !in fieldAttributes) {
            throw StorageException("Key is not allowed. Valid are: '$fieldAttributes'")
        }

        val prefix = "SELECT * FROM Books"

        return performBookQuery(query) {
            when (type) {
                QueryType.EXACT_MATCH ->
                    createQuery("$prefix WHERE $attribute = :query")
                QueryType.PART ->
                    createQuery("$prefix WHERE $attribute ILIKE '%' || :query || '%'")
                QueryType.REGEX_MATCH ->
                    createQuery("$prefix WHERE $attribute ~* :query")
                else -> null
            }
        }
    }

    private suspend fun getBooksForJsonQuery(type: QueryType, attribute: String, query: String): List<Book> {
        if (attribute !in allowedJsonAttributes) {
            throw StorageException("Key is not allowed. Valid are: '$allowedJsonAttributes'")
        }

        val prefix = "SELECT * FROM Books"

        return performBookQuery(query) {
            when (type) {
                QueryType.EXACT_MATCH ->
                    createQuery("$prefix WHERE extra->'$attribute' ?? :query")
                QueryType.REGEX_MATCH ->
                    createQuery("$prefix WHERE extra->>'$attribute' ~* :query")
                QueryType.PART ->
                    createQuery("$prefix WHERE extra->>'$attribute' ILIKE '%' || :query || '%'")
                else -> null
            }
        }
    }

    private suspend fun performBookQuery(query: String,
                                         querySupplier: Handle.() -> Query?): List<Book> {

        var result = listOf<Book>()

        jdbi.using {
            querySupplier()?.let {
                result = it
                        .bind("query", query)
                        .map(BookRowMapper())
                        .list()
            }
        }

        return result
    }
}
