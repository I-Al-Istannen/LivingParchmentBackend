package me.ialistannen.livingparchment.backend.di

import dagger.Component

@ApplicationScope
@Component(modules = [BookRepositoryModule::class, JdbiModule::class, FetchingModule::class])
abstract class BackendMainComponent