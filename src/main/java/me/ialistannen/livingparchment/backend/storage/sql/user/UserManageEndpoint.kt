package me.ialistannen.livingparchment.backend.storage.sql.user

import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.storage.UserRepository
import org.hibernate.validator.constraints.NotEmpty
import javax.inject.Inject
import javax.ws.rs.FormParam
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
class UserManageEndpoint @Inject constructor(
        private val userRepository: UserRepository
) {

    @Path("add")
    @POST
    fun addUser(@NotEmpty @FormParam("name") name: String,
                @NotEmpty @FormParam("password") password: String) {
        runBlocking {
            if (userRepository.getUser(name) != null) {
                return@runBlocking
            }
        }
    }
}