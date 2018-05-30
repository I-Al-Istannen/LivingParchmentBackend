package me.ialistannen.livingparchment.backend.server.auth

import org.eclipse.jetty.security.AbstractLoginService
import org.eclipse.jetty.security.ConstraintMapping
import org.eclipse.jetty.security.ConstraintSecurityHandler
import org.eclipse.jetty.security.authentication.BasicAuthenticator
import org.eclipse.jetty.util.security.Constraint
import org.eclipse.jetty.util.security.Password

class AdminSecurityHandler(username: String, password: String) : ConstraintSecurityHandler() {

    init {
        val constraint = Constraint(Constraint.__BASIC_AUTH, ADMIN_ROLE)
        constraint.authenticate = true
        constraint.roles = arrayOf(ADMIN_ROLE)

        val constraintMapping = ConstraintMapping()
        constraintMapping.constraint = constraint
        constraintMapping.pathSpec = "/*"

        authenticator = BasicAuthenticator()
        addConstraintMapping(constraintMapping)

        loginService = AdminLoginService(username, password)
    }

    class AdminLoginService(private val username: String,
                            password: String
    ) : AbstractLoginService() {

        private val adminPrincipal = UserPrincipal(username, Password(password))

        override fun loadRoleInfo(user: UserPrincipal): Array<String> {
            if (user.name == username) {
                return arrayOf(ADMIN_ROLE)
            }
            return emptyArray()
        }

        override fun loadUserInfo(username: String): UserPrincipal? {
            if (this.username == username) {
                return adminPrincipal
            }
            return null
        }
    }
}

private const val ADMIN_ROLE: String = "ADMIN"