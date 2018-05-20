package me.ialistannen.livingparchment.backend.server

import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.di.DaggerBackendMainComponent
import me.ialistannen.livingparchment.backend.fetching.amazon.AmazonFetcher
import me.ialistannen.livingparchment.backend.server.config.LivingParchmentConfiguration
import me.ialistannen.livingparchment.backend.server.database.ManagedJdbi
import me.ialistannen.livingparchment.backend.server.resources.BookAddEndpoint
import me.ialistannen.livingparchment.backend.storage.sql.DatabaseCreator
import org.eclipse.jetty.util.component.AbstractLifeCycle
import org.eclipse.jetty.util.component.LifeCycle
import javax.inject.Inject

class ServerMain : Application<LivingParchmentConfiguration>() {

    @Inject
    lateinit var bookEndpoint: BookAddEndpoint

    override fun getName(): String {
        return "living-parchment"
    }

    override fun run(configuration: LivingParchmentConfiguration, environment: Environment) {
        val managedJdbi = ManagedJdbi(
                connectionString = configuration.dbConnectionString,
                password = configuration.dbPassword,
                user = configuration.dbUser
        )
        environment.lifecycle().manage(managedJdbi)

        DaggerBackendMainComponent.builder()
                .jdbi(managedJdbi)
                .build()
                .inject(this)

        environment.jersey().register(bookEndpoint)

        environment.lifecycle()
                .addLifeCycleListener(object : AbstractLifeCycle.AbstractLifeCycleListener() {
                    override fun lifeCycleStarted(event: LifeCycle) {
                        managedJdbi.getJdbi().useHandle<RuntimeException> {
                            DatabaseCreator().createTables(it)
                        }
                    }
                })
    }

    override fun initialize(bootstrap: Bootstrap<LivingParchmentConfiguration>) {
        bootstrap.objectMapper.registerModule(KotlinModule())
    }
}

fun main(args: Array<String>) {

    runBlocking {
        println(AmazonFetcher().fetch("3866801920"))
    }

    System.exit(0)

    ServerMain().run(
            "server",
            ServerMain::class.java.getResource("/living-parchment.yml")
                    .toString()
                    .removePrefix("file:")
    )
}