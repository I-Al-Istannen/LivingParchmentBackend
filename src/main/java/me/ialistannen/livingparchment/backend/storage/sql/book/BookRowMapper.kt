package me.ialistannen.livingparchment.backend.storage.sql.book

import me.ialistannen.livingparchment.common.model.Book
import me.ialistannen.livingparchment.common.model.BookLocation
import me.ialistannen.livingparchment.common.serialization.fromJson
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.util.*

/**
 * A simple [RowMapper] to return a [Book].
 */
internal class BookRowMapper : RowMapper<Book> {

    override fun map(rs: ResultSet, ctx: StatementContext): Book {
        val isbn = rs.getString("isbn")
        val title = rs.getString("title")
        val pageCount = rs.getInt("page_count")
        val language = rs.getString("language")
        val publishedMillis = rs.getDate("published", UTC)
        val publisher = rs.getString("publisher")
        val imageUrl = rs.getString("image_url")
        val extra = rs.getString("extra").fromJson<Map<String, Any>>()
        val locationId = rs.getObject("location_id") as UUID?
        val locationName = rs.getString("location_name")
        val locationDescription = rs.getString("location_description")

        val location: BookLocation? = if (locationId == null) {
            null
        } else {
            BookLocation(locationName, locationDescription, locationId)
        }

        return Book(
                title = title,
                isbn = isbn,
                pageCount = pageCount,
                language = language,
                location = location,
                imageUrl = imageUrl,
                published = Date(publishedMillis.time),
                publisher = publisher,
                extra = extra
        )
    }
}

private val UTC: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))