package me.ialistannen.livingparchment.backend.server.resources

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import io.dropwizard.testing.junit5.ResourceExtension
import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.server.auth.User
import me.ialistannen.livingparchment.backend.storage.UserRepository
import me.ialistannen.livingparchment.common.api.response.LoginResponse
import me.ialistannen.livingparchment.common.api.response.LoginStatus
import me.ialistannen.livingparchment.common.serialization.fromJson
import org.jose4j.keys.HmacKey
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mindrot.jbcrypt.BCrypt
import java.util.concurrent.ThreadLocalRandom
import javax.crypto.spec.SecretKeySpec
import javax.ws.rs.client.Entity
import javax.ws.rs.core.MultivaluedHashMap

@ExtendWith(DropwizardExtensionsSupport::class)
internal class UserLoginEndpointTest {

    companion object {
        private var secretKey: SecretKeySpec
        private var endpoint: UserLoginEndpoint
        private var userRepository: UserRepository = InMemoryUserRepository()

        private var testRule: ResourceExtension

        init {
            val keyBytes = ByteArray(255).apply {
                ThreadLocalRandom.current().nextBytes(this)
            }

            secretKey = HmacKey(keyBytes)
            endpoint = UserLoginEndpoint(userRepository, secretKey)

            testRule = ResourceExtension.builder()
                    .addResource(endpoint)
                    .build()
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
        return testRule.target("/login").request().post(
                Entity.form(MultivaluedHashMap(mapOf(
                        "name" to name,
                        "password" to password
                )))
        )
                .readEntity(String::class.java)
                .fromJson()
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