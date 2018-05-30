package me.ialistannen.livingparchment.backend.server.resources

import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.storage.UserRepository
import me.ialistannen.livingparchment.common.api.response.LoginResponse
import me.ialistannen.livingparchment.common.api.response.LoginStatus
import org.hibernate.validator.constraints.NotEmpty
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.mindrot.jbcrypt.BCrypt
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.ws.rs.FormParam
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/login")
@Produces(MediaType.APPLICATION_JSON)
class UserLoginEndpoint @Inject constructor(
        private val userRepository: UserRepository,
        private val secretKey: SecretKeySpec
) {

    @POST
    fun login(@NotEmpty @FormParam("name") name: String,
              @NotEmpty @FormParam("password") password: String): LoginResponse {

        return runBlocking {
            val user = userRepository.getUser(name)
                    ?: return@runBlocking LoginResponse(null, LoginStatus.INVALID_CREDENTIALS)

            if (!BCrypt.checkpw(password, user.passwordHash)) {
                return@runBlocking LoginResponse(null, LoginStatus.INVALID_CREDENTIALS)
            }

            val jwtClaims = JwtClaims().apply {
                subject = name
                setExpirationTimeMinutesInTheFuture(Integer.MAX_VALUE.toFloat())
            }

            val signature = JsonWebSignature().apply {
                payload = jwtClaims.toJson()
                algorithmHeaderValue = AlgorithmIdentifiers.HMAC_SHA256
                key = secretKey
            }

            LoginResponse(signature.compactSerialization, LoginStatus.AUTHENTICATED)
        }
    }
}