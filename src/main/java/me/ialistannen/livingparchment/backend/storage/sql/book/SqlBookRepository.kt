package me.ialistannen.livingparchment.backend.storage.sql.book

import me.ialistannen.livingparchment.backend.storage.BookRepository
import me.ialistannen.livingparchment.backend.storage.StorageException
import me.ialistannen.livingparchment.backend.storage.sql.using
import me.ialistannen.livingparchment.backend.util.camelToSnakeCase
import me.ialistannen.livingparchment.common.api.query.QueryType
import me.ialistannen.livingparchment.common.model.Book
import me.ialistannen.livingparchment.common.serialization.toJsonTree
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.statement.Query
import org.jdbi.v3.core.statement.SqlStatement
import org.jdbi.v3.core.statement.Update
import java.sql.Types
import javax.inject.Inject
import kotlin.reflect.KProperty

class SqlBookRepository @Inject constructor(
        private val jdbi: Jdbi
) : BookRepository {

    private val fieldAttributes = setOf(
            "title", "published", "publisher", "language", "page_count", "isbn"
    )
    private val allowedJsonQueryAttributes = setOf(
            "authors", "genre", "location", "description"
    )

    override suspend fun addBook(book: Book) {
        jdbi.using {
            // remove it if it already exists
            removeBook(book)

            createUpdate("""
                INSERT INTO Books
                  (isbn, title, language, page_count, location, image_url, publisher, published, extra)
                VALUES
                   (:isbn, :title, :language, :page_count, :location::uuid, :image_url, :publisher, :published, :extra)
                """)
                    .bindProperty(book::isbn)
                    .bindProperty(book::title)
                    .bindProperty(book::language)
                    .bindProperty(book::pageCount)
                    .bindProperty(book::publisher)
                    .bindProperty(book::published)
                    .bindProperty(book::location) { it?.uuid }
                    .bindProperty(book::imageUrl)
                    .bindJsonProperty(book::extra)
                    .execute()
        }
    }

    private fun <T> Update.bindProperty(property: KProperty<T>,
                                        nullType: Int = Types.OTHER,
                                        transform: (T) -> Any? = { it }): Update {
        val transformed = transform.invoke(property.getter.call())
                ?: return bindNull(property.name.camelToSnakeCase(), nullType)

        return bindByType(
                property.name.camelToSnakeCase(),
                transformed,
                transformed.javaClass
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
            result = createQuery(selectPrefix)
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

        val prefix = selectPrefix

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
        if (attribute !in allowedJsonQueryAttributes) {
            throw StorageException("Key is not allowed. Valid are: '$allowedJsonQueryAttributes'")
        }

        val prefix = selectPrefix

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

    private val selectPrefix = """
        SELECT
        B.isbn,
        B.title,
        B.language,
        B.page_count,
        B.publisher,
        B.published,
        B.image_url,
        B.extra,
        L.id          as location_id,
        L.name        as location_name,
        L.description as location_description
        FROM Books as B
        LEFT JOIN BookLocations L on B.location = L.id
        """.trimIndent().replace('\n', ' ')
}
