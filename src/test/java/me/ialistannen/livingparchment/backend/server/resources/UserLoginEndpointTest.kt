package me.ialistannen.livingparchment.backend.server.resources

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import io.dropwizard.testing.junit5.ResourceExtension
import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.server.auth.User
import me.ialistannen.livingparchment.backend.storage.UserRepository
import me.ialistannen.livingparchment.common.api.response.LoginResponse
import me.ialistannen.livingparchment.common.api.response.LoginStatus
import org.jose4j.keys.HmacKey
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mindrot.jbcrypt.BCrypt
import java.util.concurrent.ThreadLocalRandom

@ExtendWith(DropwizardExtensionsSupport::class)
internal class UserLoginEndpointTest {

    companion object : ResourceTest() {
        private var userRepository: UserRepository = InMemoryUserRepository()
        override val endpoint: UserLoginEndpoint = createEndpoint()

        override val extension: ResourceExtension = extension()

        override val path: String = "/login"

        private fun createEndpoint(): UserLoginEndpoint {
            val keyBytes = ByteArray(255).apply {
                ThreadLocalRandom.current().nextBytes(this)
            }

            return UserLoginEndpoint(userRepository, HmacKey(keyBytes))
        }
    }

    @AfterEach
    fun cleanUp() {
        runBlocking {
            for (user in userRepository.getAllUsers()) {
                userRepository.deleteUser(user.name)
            }
        }
    }

    @Test
    fun `allow correct user to authenticate`() {
        addUser("John Doe", "12345")

        val response = makePost("John Doe", "12345")

        assertEquals(LoginStatus.AUTHENTICATED, response.status)
        assertNotNull(response.token)
    }

    @Test
    fun `deny wrong user to authenticate`() {
        addUser("John Doe", "12345")

        val response = makePost("Elvis Impersonator", "12345")

        assertEquals(LoginStatus.INVALID_CREDENTIALS, response.status)
        assertNull(response.token)
    }

    @Test
    fun `deny existing user with wrong password`() {
        addUser("John Doe", "123456")

        val response = makePost("John Doe", "--")

        assertEquals(LoginStatus.INVALID_CREDENTIALS, response.status)
        assertNull(response.token)
    }

    private fun makePost(name: String, password: String): LoginResponse {
        return makeCall {
            post(form("name" to name, "password" to password))
        }
    }

    private fun addUser(name: String, password: String) {
        runBlocking {
            userRepository.addUser(User(
                    name,
                    BCrypt.hashpw(password, BCrypt.gensalt())
            ))
        }
    }

}