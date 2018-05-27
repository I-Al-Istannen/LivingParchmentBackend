package me.ialistannen.livingparchment.backend.storage.sql

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin

abstract class SqlTest {
    companion object {

        lateinit var jdbi: Jdbi

        @JvmStatic
        fun setup() {
            jdbi = Jdbi.create(
                    "jdbc:postgresql://localhost:5432/LivingParchmentTest",
                    "LivingParchment",
                    "123456"
            ).apply {
                installPlugin(SqlObjectPlugin())
                installPlugin(KotlinSqlObjectPlugin())
                installPlugin(KotlinPlugin())
                registerArgument(JsonNNodeArgumentFactory())
            }
            jdbi.useHandle<RuntimeException> {
                it.createUpdate("DROP TABLE IF EXISTS Books").execute()
                it.createUpdate("DROP TABLE IF EXISTS BookLocations").execute()
                DatabaseCreator().createTables(it)
            }
        }

        @JvmStatic
        fun teardown() {
            jdbi.useHandle<RuntimeException> {
                it.createUpdate("DROP TABLE IF EXISTS Books").execute()
                it.createUpdate("DROP TABLE IF EXISTS BookLocations").execute()
            }
        }
    }
}