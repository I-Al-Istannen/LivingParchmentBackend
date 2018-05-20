package me.ialistannen.livingparchment.backend.di

import dagger.Module
import dagger.Provides
import me.ialistannen.livingparchment.backend.server.database.ManagedJdbi
import org.jdbi.v3.core.Jdbi

@Module
open class JdbiModule {

    @ApplicationScope
    @Provides
    fun provideJdbi(managedJdbi: ManagedJdbi): Jdbi {
        return managedJdbi.getJdbi()
    }
}