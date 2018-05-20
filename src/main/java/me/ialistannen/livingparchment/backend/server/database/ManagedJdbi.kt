package me.ialistannen.livingparchment.backend.server.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.dropwizard.lifecycle.Managed
import me.ialistannen.livingparchment.backend.storage.sql.JsonNNodeArgumentFactory
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin

class ManagedJdbi(
        private val connectionString: String,
        private val user: String,
        private val password: String
) : Managed {

    private var hikariDataSource: HikariDataSource? = null
    private var jdbi: Jdbi

    init {
        jdbi = Jdbi.create {
            hikariDataSource!!.connection
        }.apply {
            installPlugin(SqlObjectPlugin())
            installPlugin(KotlinSqlObjectPlugin())
            installPlugin(KotlinPlugin())
            registerArgument(JsonNNodeArgumentFactory())
        }
    }

    override fun start() {
        val config = HikariConfig().apply {
            jdbcUrl = connectionString
            password = this@ManagedJdbi.password
            username = user
        }
        hikariDataSource = HikariDataSource(config)
    }

    override fun stop() {
        hikariDataSource?.close()
    }

    /**
     * Returns the jdbi instance.
     *
     * @return the [Jdbi] instance*
     */
    fun getJdbi(): Jdbi {
        return jdbi
    }
}