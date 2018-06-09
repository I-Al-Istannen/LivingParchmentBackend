package me.ialistannen.livingparchment.backend.di

import dagger.Module
import dagger.Provides
import me.ialistannen.livingparchment.backend.server.config.LivingParchmentConfiguration
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Named

@Module
class ConfigurationModule {

    @Named("coverFolder")
    @Provides
    fun provideCoverFolder(livingParchmentConfiguration: LivingParchmentConfiguration): Path {
        return Paths.get(livingParchmentConfiguration.coverFolder)
    }
}