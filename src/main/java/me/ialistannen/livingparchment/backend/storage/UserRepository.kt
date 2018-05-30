package me.ialistannen.livingparchment.backend.storage

import me.ialistannen.livingparchment.backend.server.auth.User

interface UserRepository {

    /**
     * Returns all users stored in the repository.
     */
    suspend fun getAllUsers(): List<User>

    /**
     * Fetches a user by his unique name.
     */
    suspend fun getUser(name: String): User?

    /**
     * Adds the given user. Will overwrite any existing user with the same name.
     */
    suspend fun addUser(user: User)

    /**
     * Deletes the given user.
     *
     * @return true if the user was deleted, false if it wasn't found
     */
    suspend fun deleteUser(name: String): Boolean
}