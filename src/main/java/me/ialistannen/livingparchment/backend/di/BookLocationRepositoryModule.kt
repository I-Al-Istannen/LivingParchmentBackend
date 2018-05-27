package me.ialistannen.livingparchment.backend.di

import dagger.Binds
import dagger.Module
import me.ialistannen.livingparchment.backend.storage.BookLocationRepository
import me.ialistannen.livingparchment.backend.storage.sql.location.SqlBookLocationRepository

@Module
abstract class BookLocationRepositoryModule {

    @ApplicationScope
    @Binds
    abstract fun bindBookLocationRepository(sqlBookLocationRepository: SqlBookLocationRepository): BookLocationRepository
}