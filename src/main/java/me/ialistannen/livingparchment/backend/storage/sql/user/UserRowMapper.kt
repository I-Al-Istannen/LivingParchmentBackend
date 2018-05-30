package me.ialistannen.livingparchment.backend.storage.sql.user

import me.ialistannen.livingparchment.backend.server.auth.User
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

internal class UserRowMapper : RowMapper<User> {

    override fun map(rs: ResultSet, ctx: StatementContext?): User {
        val name: String = rs.getString("name")
        val passwordHash: String = rs.getString("password_hash")
        val role: String? = rs.getString("role")

        return User(name, passwordHash, role)
    }
}