package me.ialistannen.livingparchment.backend.di

import dagger.BindsInstance
import dagger.Component
import me.ialistannen.livingparchment.backend.server.ServerMain
import me.ialistannen.livingparchment.backend.server.database.ManagedJdbi
import javax.crypto.spec.SecretKeySpec

@ApplicationScope
@Component(modules = [
    BookRepositoryModule::class,
    BookLocationRepositoryModule::class,
    UserRepositoryModule::class,
    JdbiModule::class,
    FetchingModule::class
])
abstract class BackendMainComponent {

    @Component.Builder
    interface Builder {

        fun build(): BackendMainComponent

        @BindsInstance
        fun jdbi(jdbi: ManagedJdbi): Builder

        @BindsInstance
        fun jwtKey(key: SecretKeySpec): Builder
    }

    abstract fun inject(serverMain: ServerMain)
}