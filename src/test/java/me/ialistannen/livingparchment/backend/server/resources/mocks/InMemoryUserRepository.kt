package me.ialistannen.livingparchment.backend.server.resources.mocks

import me.ialistannen.livingparchment.backend.server.auth.User
import me.ialistannen.livingparchment.backend.storage.UserRepository

internal class InMemoryUserRepository : UserRepository {

    private val users: MutableList<User> = mutableListOf()

    override suspend fun getAllUsers(): List<User> {
        return ArrayList(users) // to allow editing while iterating
    }

    override suspend fun getUser(name: String): User? {
        return users.firstOrNull { it.name == name }
    }

    override suspend fun addUser(user: User) {
        users.add(user)
    }

    override suspend fun deleteUser(name: String): Boolean {
        return users.removeIf { it.name == name }
    }
}