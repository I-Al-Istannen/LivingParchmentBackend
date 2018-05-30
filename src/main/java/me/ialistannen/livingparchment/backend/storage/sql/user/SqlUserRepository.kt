package me.ialistannen.livingparchment.backend.storage.sql.user

import me.ialistannen.livingparchment.backend.server.auth.User
import me.ialistannen.livingparchment.backend.storage.UserRepository
import me.ialistannen.livingparchment.backend.storage.sql.using
import org.jdbi.v3.core.Jdbi
import javax.inject.Inject

class SqlUserRepository @Inject constructor(
        private val jdbi: Jdbi
) : UserRepository {

    override suspend fun getAllUsers(): List<User> {
        return jdbi.using {
            createQuery("SELECT * FROM Users")
                    .map(UserRowMapper())
                    .list()
        }
    }

    override suspend fun getUser(name: String): User? {
        return jdbi.using {
            createQuery("SELECT * FROM Users WHERE name = :name")
                    .bind("name", name)
                    .map(UserRowMapper())
                    .firstOrNull()
        }
    }

    override suspend fun addUser(user: User) {
        jdbi.using {
            deleteUser(user.name)

            createUpdate("INSERT INTO Users (name, password_hash)" +
                    " VALUES (:name, :passwordHash)")
                    .bind("name", user.name)
                    .bind("passwordHash", user.passwordHash)
                    .execute()
        }
    }

    override suspend fun deleteUser(name: String): Boolean {
        return jdbi.using {
            createUpdate("DELETE FROM Users WHERE name = :name")
                    .bind("name", name)
                    .execute() > 0
        }
    }
}