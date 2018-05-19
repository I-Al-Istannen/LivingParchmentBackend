package me.ialistannen.livingparchment.backend.storage.sql

import com.google.gson.JsonElement
import me.ialistannen.livingparchment.common.model.Book
import me.ialistannen.livingparchment.common.serialization.fromJson
import org.jdbi.v3.core.argument.Argument
import org.jdbi.v3.core.argument.ArgumentFactory
import org.jdbi.v3.core.config.ConfigRegistry
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import org.postgresql.util.PGobject
import java.lang.reflect.Type
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.*

class JsonNNodeArgumentFactory : ArgumentFactory {

    class JsonNodeArgument(private val value: PGobject) : Argument {

        override fun apply(position: Int, statement: PreparedStatement, ctx: StatementContext) {
            statement.setObject(position, value)
        }
    }

    override fun build(type: Type, value: Any, config: ConfigRegistry): Optional<Argument> {
        if (value !is JsonElement) {
            return Optional.empty()
        }

        val postgresObject = PGobject()
        postgresObject.type = "jsonb"
        postgresObject.value = value.toString()

        return Optional.of(JsonNodeArgument(postgresObject))
    }

}

/**
 * A simple [RowMapper] to return a [Book].
 */
class BookRowMapper : RowMapper<Book> {

    override fun map(rs: ResultSet, ctx: StatementContext): Book {
        val isbn = rs.getString("isbn")
        val title = rs.getString("title")
        val pageCount = rs.getInt("page_count")
        val language = rs.getString("language")
        val publishedMillis = rs.getDate("published", UTC)
        val publisher = rs.getString("publisher")
        val extra = rs.getString("extra").fromJson<Map<String, Any>>()

        return Book(
                title = title,
                isbn = isbn,
                pageCount = pageCount,
                language = language,
                published = Date(publishedMillis.time),
                publisher = publisher,
                extra = extra
        )
    }
}

private val UTC: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))