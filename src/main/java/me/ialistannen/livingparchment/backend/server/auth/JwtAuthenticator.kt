package me.ialistannen.livingparchment.backend.server.auth

import io.dropwizard.auth.Authenticator
import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.storage.UserRepository
import org.jose4j.jwt.consumer.JwtContext
import java.util.*
import javax.inject.Inject

class JwtAuthenticator @Inject constructor(
        private val userRepository: UserRepository
) : Authenticator<JwtContext, User> {

    override fun authenticate(credentials: JwtContext): Optional<User> {
        return runBlocking {
            val userName = credentials.jwtClaims.subject
            val user = userRepository.getUser(userName)

            Optional.ofNullable(user)
        }
    }
}