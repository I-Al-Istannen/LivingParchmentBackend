package me.ialistannen.livingparchment.backend.di

import dagger.Binds
import dagger.Module
import me.ialistannen.livingparchment.backend.storage.BookRepository
import me.ialistannen.livingparchment.backend.storage.sql.SqlBookRepository

@Module
abstract class BookRepositoryModule {

    @Binds
    abstract fun bookRepository(sqlBookRepository: SqlBookRepository): BookRepository
}