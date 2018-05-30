package me.ialistannen.livingparchment.backend.server.auth

import java.security.Principal

data class User(private val name: String, val passwordHash: String,
                val role: String? = null
) : Principal {

    override fun getName(): String {
        return name
    }
}