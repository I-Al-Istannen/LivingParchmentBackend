package me.ialistannen.livingparchment.backend.server.resources

import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.server.auth.User
import me.ialistannen.livingparchment.backend.storage.UserRepository
import me.ialistannen.livingparchment.backend.util.logger
import me.ialistannen.livingparchment.common.api.response.UserAddResponse
import me.ialistannen.livingparchment.common.api.response.UserAddStatus
import me.ialistannen.livingparchment.common.api.response.UserDeleteResponse
import me.ialistannen.livingparchment.common.api.response.UserDeleteStatus
import org.hibernate.validator.constraints.NotEmpty
import org.mindrot.jbcrypt.BCrypt
import javax.annotation.security.RolesAllowed
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/admin/users")
@Produces(MediaType.APPLICATION_JSON)
class UserManageEndpoint @Inject constructor(
        private val userRepository: UserRepository
) {

    private val logger by logger()

    @RolesAllowed("ADMIN")
    @DELETE
    fun deleteUser(@NotEmpty @FormParam("username") username: String): UserDeleteResponse {
        return runBlocking {
            try {
                if (userRepository.getUser(username) == null) {
                    return@runBlocking UserDeleteResponse(username, UserDeleteStatus.NOT_FOUND)
                }

                userRepository.deleteUser(username)

                UserDeleteResponse(username, UserDeleteStatus.DELETED)
            } catch (e: Exception) {
                logger.info("Error deleting a user", e)
                throw WebApplicationException(
                        Response.serverError().entity(e.localizedMessage).build()
                )
            }
        }
    }

    @RolesAllowed("ADMIN")
    @PUT
    fun addUser(@NotEmpty @FormParam("username") username: String,
                @NotEmpty @FormParam("password") password: String,
                @FormParam("role") role: String?): UserAddResponse {
        return runBlocking {
            try {
                if (userRepository.getUser(username) != null) {
                    return@runBlocking UserAddResponse(username, UserAddStatus.ALREADY_EXISTED)
                }

                val hash = BCrypt.hashpw(password, BCrypt.gensalt())
                userRepository.addUser(User(username, hash, role))

                UserAddResponse(username, UserAddStatus.ADDED)
            } catch (e: Exception) {
                logger.info("Error adding a user", e)
                throw WebApplicationException(
                        Response.serverError().entity(e.localizedMessage).build()
                )
            }
        }
    }
}