package me.ialistannen.livingparchment.backend.storage.sql.user

import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.server.auth.User
import me.ialistannen.livingparchment.backend.storage.UserRepository
import me.ialistannen.livingparchment.backend.storage.sql.SqlTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mindrot.jbcrypt.BCrypt

internal class SqlUserRepositoryTest : SqlTest() {

    companion object {

        private lateinit var userRepository: UserRepository

        private val userOne = User(
                "John Doe",
                BCrypt.hashpw("hunter2", BCrypt.gensalt()),
                "ADMIN"
        )
        private val userTwo = User("Jane Doe", BCrypt.hashpw(
                "hunter3", BCrypt.gensalt()
        ))

        @JvmStatic
        @BeforeAll
        fun setup() {
            SqlTest.setup()
            userRepository = SqlUserRepository(jdbi)
        }

        @JvmStatic
        @AfterAll
        fun teardown() {
            SqlTest.teardown()
        }
    }

    @Test
    fun `adding user`() {
        runBlocking {
            userRepository.addUser(userOne)

            val fetched = userRepository.getUser(userOne.name)

            assertNotNull(fetched)
            assertEquals(userOne, fetched)
        }
    }

    @Test
    fun `deleting user`() {
        runBlocking {
            userRepository.addUser(userOne)
            userRepository.addUser(userTwo)

            userRepository.deleteUser(userTwo.name)

            assertNotNull(userRepository.getUser(userOne.name))
            assertNull(userRepository.getUser(userTwo.name))
        }
    }

    @Test
    fun `getting all users`() {
        runBlocking {
            userRepository.addUser(userOne)
            userRepository.addUser(userTwo)

            val users = userRepository.getAllUsers()

            assertTrue(userOne in users, "User 1 not in repo")
            assertTrue(userTwo in users, "User 2 not in repo")
        }
    }
}