package me.ialistannen.livingparchment.backend.server.health

import com.codahale.metrics.health.HealthCheck
import org.jdbi.v3.core.Jdbi
import javax.inject.Inject

class DatabaseHealthCheck @Inject constructor(
        private val jdbi: Jdbi
) : HealthCheck() {

    override fun check(): HealthCheck.Result {
        return try {
            jdbi.useHandle<RuntimeException> {
                it.connection
                        .prepareStatement("SELECT 1")
                        .execute()
            }
            Result.healthy()
        } catch (e: Exception) {
            Result.unhealthy(e)
        }
    }
}