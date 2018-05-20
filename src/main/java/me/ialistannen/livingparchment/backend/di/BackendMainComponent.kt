package me.ialistannen.livingparchment.backend.di

import dagger.BindsInstance
import dagger.Component
import me.ialistannen.livingparchment.backend.server.ServerMain
import me.ialistannen.livingparchment.backend.server.database.ManagedJdbi

@ApplicationScope
@Component(modules = [BookRepositoryModule::class, JdbiModule::class, FetchingModule::class])
abstract class BackendMainComponent {

    @Component.Builder
    interface Builder {

        fun build(): BackendMainComponent

        @BindsInstance
        fun jdbi(jdbi: ManagedJdbi): Builder
    }

    abstract fun inject(serverMain: ServerMain)
}