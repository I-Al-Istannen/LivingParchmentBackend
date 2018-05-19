package me.ialistannen.livingparchment.backend.di

import dagger.Module
import dagger.Provides
import me.ialistannen.livingparchment.backend.storage.sql.JsonNNodeArgumentFactory
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin

@Module
open class JdbiModule {

    @ApplicationScope
    @Provides
    fun provideJdbi(): Jdbi {
        return Jdbi.create(
                "jdbc:postgresql://localhost:5432/LivingParchmentDb",
                "LivingParchment",
                "123456"
        ).apply {
            installPlugin(SqlObjectPlugin())
            installPlugin(KotlinSqlObjectPlugin())
            installPlugin(KotlinPlugin())
            registerArgument(JsonNNodeArgumentFactory())
        }
    }
}