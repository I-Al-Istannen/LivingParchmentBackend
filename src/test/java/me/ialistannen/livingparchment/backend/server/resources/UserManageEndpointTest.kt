package me.ialistannen.livingparchment.backend.server.resources

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import io.dropwizard.testing.junit5.ResourceExtension
import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.server.auth.User
import me.ialistannen.livingparchment.common.api.response.UserAddResponse
import me.ialistannen.livingparchment.common.api.response.UserAddStatus
import me.ialistannen.livingparchment.common.api.response.UserDeleteResponse
import me.ialistannen.livingparchment.common.api.response.UserDeleteStatus
import me.ialistannen.livingparchment.common.serialization.fromJson
import org.glassfish.jersey.client.ClientProperties
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mindrot.jbcrypt.BCrypt
import java.nio.charset.Charset
import java.util.concurrent.ThreadLocalRandom
import javax.ws.rs.client.Entity
import javax.ws.rs.client.Invocation
import javax.ws.rs.core.Form
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.Response

@ExtendWith(DropwizardExtensionsSupport::class)
internal class UserManageEndpointTest {

    companion object {
        private var endpoint: UserManageEndpoint
        private val userRepository = InMemoryUserRepository()

        private val testExtension: ResourceExtension

        init {
            endpoint = UserManageEndpoint(userRepository)

            testExtension = ResourceExtension.builder()
                    .addResource(endpoint)
                    .setClientConfigurator {
                        it.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true)
                    }
                    .build()
        }
    }

    @AfterEach
    fun cleanup() {
        runBlocking {
            for (user in userRepository.getAllUsers()) {
                userRepository.deleteUser(user.name)
            }
        }
    }

    @Test
    fun `test add normal user`() {
        checkAddUser("John Doe", "Test", null)
    }

    @Test
    fun `test add admin user`() {
        checkAddUser("John Doe", "Test", "ADMIN")
    }

    @Test
    fun `test add existing user`() {
        val name = "John Doe"
        val password = "Test"
        checkAddUser(name, password, null)
        checkAddUser(name, password, null, UserAddStatus.ALREADY_EXISTED)
    }

    @Test
    fun `test delete user`() {
        runBlocking {
            val name = "John Doe"
            userRepository.addUser(User(name, generateRandomPasswordHash()))

            val response = makeCall<UserDeleteResponse> {
                method("DELETE", form("username" to name))
            }

            assertEquals(UserDeleteStatus.DELETED, response.status)
        }
    }

    @Test
    fun `test delete non existent user`() {
        runBlocking {
            val name = "John Doe"

            val response = makeCall<UserDeleteResponse> {
                method("DELETE", form("username" to name))
            }

            assertEquals(UserDeleteStatus.NOT_FOUND, response.status)
        }
    }

    private fun checkAddUser(name: String, password: String, role: String?,
                             status: UserAddStatus = UserAddStatus.ADDED) {
        val response = makeCall<UserAddResponse> {
            if (role != null) {
                put(form(
                        "username" to name,
                        "password" to password,
                        "role" to role
                ))
            } else {
                put(form(
                        "username" to name,
                        "password" to password
                ))
            }
        }

        assertEquals(status, response.status)
        runBlocking {
            val user = userRepository.getUser(name)

            assertNotNull(user, "User not found")
            assertEquals(name, user!!.name)
            assertTrue(BCrypt.checkpw(password, user.passwordHash), "Password wrong")
            assertEquals(role, user.role)
        }
    }

    private fun form(vararg entries: Pair<String, String>): Entity<Form>? {
        return Entity.form(MultivaluedHashMap(entries.toMap()))
    }

    private inline fun <reified T> makeCall(action: Invocation.Builder.() -> Response): T {
        return testExtension.target("/admin/users")
                .request()
                .action()
                .readEntity(String::class.java)
                .fromJson()
    }

    private fun generateRandomPasswordHash(): String {
        val bytes = ByteArray(40)
        ThreadLocalRandom.current().nextBytes(bytes)
        val password = bytes.toString(Charset.defaultCharset())

        return BCrypt.hashpw(password, BCrypt.gensalt())
    }
}