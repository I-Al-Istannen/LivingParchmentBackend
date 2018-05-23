package me.ialistannen.livingparchment.backend.server.health

import com.codahale.metrics.health.HealthCheck
import org.jdbi.v3.core.Jdbi
import javax.inject.Inject

class DatabaseHealthCheck @Inject constructor(
        private val jdbi: Jdbi
) : HealthCheck() {

    override fun check(): HealthCheck.Result {
        return try {
            var valid = false
            jdbi.useHandle<RuntimeException> {
                if (it.connection.isValid(10)) {
                    valid = true
                }
            }
            if (valid) {
                Result.healthy()
            } else {
                Result.unhealthy("Connection is invalid")
            }
        } catch (e: Exception) {
            Result.unhealthy(e)
        }
    }
}