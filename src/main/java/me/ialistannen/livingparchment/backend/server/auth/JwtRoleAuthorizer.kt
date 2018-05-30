package me.ialistannen.livingparchment.backend.server.auth

import io.dropwizard.auth.Authorizer

class JwtRoleAuthorizer : Authorizer<User> {

    override fun authorize(principal: User, role: String): Boolean {
        return principal.role == role
    }
}