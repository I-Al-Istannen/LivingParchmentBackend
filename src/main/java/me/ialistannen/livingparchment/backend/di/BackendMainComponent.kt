package me.ialistannen.livingparchment.backend.di

import dagger.Component
import me.ialistannen.livingparchment.backend.storage.BookRepository

@ApplicationScope
@Component(modules = [BookRepositoryModule::class, JdbiModule::class])
abstract class BackendMainComponent {

    abstract fun getBookRepository(): BookRepository
}