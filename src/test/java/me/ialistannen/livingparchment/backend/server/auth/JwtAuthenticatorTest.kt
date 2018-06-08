package me.ialistannen.livingparchment.backend.server.auth

import me.ialistannen.livingparchment.backend.storage.UserRepository
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.consumer.JwtContext
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class JwtAuthenticatorTest {

    private val authenticator: JwtAuthenticator = JwtAuthenticator(DummyUserRepo())

    @Test
    fun `fetch valid user is present`() {
        assertNotNull(authenticator.authenticate(buildContext("John Doe")).orElse(null))
    }

    @Test
    fun `fetch invalid user is empty`() {
        assertNull(authenticator.authenticate(buildContext("Peter")).orElse(null))
    }

    private fun buildContext(subject: String) = JwtContext(
            JwtClaims().apply { this.subject = subject },
            emptyList()
    )

    private class DummyUserRepo : UserRepository {
        private val users: List<User> = listOf(
                User("John Doe", "Test"),
                User("Jane Doe", "Test2")
        )

        override suspend fun getAllUsers(): List<User> {
            return users
        }

        override suspend fun getUser(name: String): User? {
            return users.firstOrNull { it.name == name }
        }

        override suspend fun addUser(user: User) {

        }

        override suspend fun deleteUser(name: String): Boolean {
            return true
        }
    }
}