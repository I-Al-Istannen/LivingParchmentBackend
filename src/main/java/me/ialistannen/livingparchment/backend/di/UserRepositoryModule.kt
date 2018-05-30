package me.ialistannen.livingparchment.backend.di

import dagger.Binds
import dagger.Module
import me.ialistannen.livingparchment.backend.storage.UserRepository
import me.ialistannen.livingparchment.backend.storage.sql.user.SqlUserRepository

@Module
abstract class UserRepositoryModule {

    @ApplicationScope
    @Binds
    abstract fun bindUSerRepository(sqlUserRepository: SqlUserRepository): UserRepository
}