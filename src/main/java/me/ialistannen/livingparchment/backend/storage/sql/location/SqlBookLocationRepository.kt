package me.ialistannen.livingparchment.backend.storage.sql.location

import me.ialistannen.livingparchment.backend.storage.BookLocationRepository
import me.ialistannen.livingparchment.backend.storage.sql.using
import me.ialistannen.livingparchment.common.model.BookLocation
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.util.*
import javax.inject.Inject

class SqlBookLocationRepository @Inject constructor(
        private val jdbi: Jdbi
) : BookLocationRepository {

    override suspend fun addLocation(bookLocation: BookLocation) {
        jdbi.using {
            createUpdate("""
                INSERT INTO BookLocations (id, name, description) VALUES (:id, :name, :description)
                ON CONFLICT (id) DO UPDATE SET name = :name, description = :description;
                """.trimIndent()
            )
                    .bind("id", bookLocation.uuid)
                    .bind("name", bookLocation.name)
                    .bind("description", bookLocation.description)
                    .execute()
        }
    }

    override suspend fun deleteLocation(uuid: UUID): Boolean {
        return jdbi.using {
            createUpdate("""
                DELETE FROM BookLocations
                WHERE id = :id
                ;""".trimIndent()
            )
                    .bind("id", uuid)
                    .execute() != 0
        }
    }

    override suspend fun getAllLocations(): List<BookLocation> {
        return jdbi.using {
            createQuery("SELECT * FROM BookLocations")
                    .map(BookLocationRowMapper())
                    .list()
        }
    }

    override suspend fun getLocation(uuid: UUID): BookLocation? {
        return jdbi.using {
            createQuery("SELECT * FROM BookLocations WHERE id = :id")
                    .bind("id", uuid)
                    .map(BookLocationRowMapper())
                    .firstOrNull()
        }
    }
}

/**
 * A simple [RowMapper] that returns a [BookLocation].
 */
class BookLocationRowMapper : RowMapper<BookLocation> {

    override fun map(rs: ResultSet, ctx: StatementContext): BookLocation {
        return BookLocation(
                uuid = rs.getObject("id") as UUID,
                name = rs.getString("name"),
                description = rs.getString("description")
        )
    }
}