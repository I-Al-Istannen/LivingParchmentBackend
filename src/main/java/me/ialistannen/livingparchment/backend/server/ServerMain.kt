package me.ialistannen.livingparchment.backend.server

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.toastshaman.dropwizard.auth.jwt.JwtAuthFilter
import io.dropwizard.Application
import io.dropwizard.auth.AuthDynamicFeature
import io.dropwizard.auth.AuthValueFactoryProvider
import io.dropwizard.bundles.assets.ConfiguredAssetsBundle
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import me.ialistannen.livingparchment.backend.di.DaggerBackendMainComponent
import me.ialistannen.livingparchment.backend.server.auth.AdminSecurityHandler
import me.ialistannen.livingparchment.backend.server.auth.JwtAuthenticator
import me.ialistannen.livingparchment.backend.server.auth.JwtRoleAuthorizer
import me.ialistannen.livingparchment.backend.server.auth.User
import me.ialistannen.livingparchment.backend.server.config.LivingParchmentConfiguration
import me.ialistannen.livingparchment.backend.server.database.ManagedJdbi
import me.ialistannen.livingparchment.backend.server.health.DatabaseHealthCheck
import me.ialistannen.livingparchment.backend.server.resources.*
import me.ialistannen.livingparchment.backend.storage.sql.DatabaseCreator
import org.eclipse.jetty.util.component.AbstractLifeCycle
import org.eclipse.jetty.util.component.LifeCycle
import org.glassfish.jersey.server.filter.HttpMethodOverrideFilter
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import org.jose4j.keys.HmacKey
import javax.inject.Inject

class ServerMain : Application<LivingParchmentConfiguration>() {

    @Inject
    lateinit var bookEndpoint: BookAddEndpoint
    @Inject
    lateinit var bookQueryEndpoint: BookQueryEndpoint
    @Inject
    lateinit var bookDeleteEndpoint: BookDeleteEndpoint
    @Inject
    lateinit var bookLocationEndpoint: BookLocationEndpoint
    @Inject
    lateinit var userLoginEndpoint: UserLoginEndpoint
    @Inject
    lateinit var userManageEndpoint: UserManageEndpoint

    @Inject
    lateinit var jwtAuthenticator: JwtAuthenticator

    override fun getName(): String {
        return "living-parchment"
    }

    override fun run(configuration: LivingParchmentConfiguration, environment: Environment) {
        val managedJdbi = manageDatabase(configuration, environment)

        setupDependencyInjection(managedJdbi, configuration)

        registerEndpoints(environment)
        registerFilters(environment)
        registerAuthentication(configuration, environment)

        environment.lifecycle()
                .addLifeCycleListener(object : AbstractLifeCycle.AbstractLifeCycleListener() {
                    override fun lifeCycleStarted(event: LifeCycle) {
                        managedJdbi.getJdbi().useHandle<RuntimeException> {
                            DatabaseCreator().createTables(it)
                        }
                    }
                })
    }

    private fun manageDatabase(configuration: LivingParchmentConfiguration, environment: Environment): ManagedJdbi {
        val managedJdbi = ManagedJdbi(
                connectionString = configuration.dbConnectionString,
                password = configuration.dbPassword,
                user = configuration.dbUser
        )
        environment.lifecycle().manage(managedJdbi)
        environment.healthChecks()
                .register("database-health", DatabaseHealthCheck(managedJdbi.getJdbi()))
        return managedJdbi
    }

    private fun setupDependencyInjection(managedJdbi: ManagedJdbi, configuration: LivingParchmentConfiguration) {
        DaggerBackendMainComponent.builder()
                .jdbi(managedJdbi)
                .jwtKey(HmacKey(configuration.getJwtTokenSecret()))
                .build()
                .inject(this)
    }

    private fun registerEndpoints(environment: Environment) {
        environment.jersey().register(bookEndpoint)
        environment.jersey().register(bookQueryEndpoint)
        environment.jersey().register(bookDeleteEndpoint)
        environment.jersey().register(bookLocationEndpoint)
        environment.jersey().register(userLoginEndpoint)
        environment.jersey().register(userManageEndpoint)
        environment.jersey().register(AuthValueFactoryProvider.Binder<User>(User::class.java))
    }

    private fun registerFilters(environment: Environment) {
        environment.jersey().register(HttpMethodOverrideFilter())
    }

    private fun registerAuthentication(configuration: LivingParchmentConfiguration, environment: Environment) {
        val jwtConsumer = JwtConsumerBuilder()
                .setRequireSubject()
                .setAllowedClockSkewInSeconds(30)
                .setRequireExpirationTime()
                .setVerificationKey(HmacKey(configuration.getJwtTokenSecret()))
                .build()

        environment.jersey().register(AuthDynamicFeature(
                JwtAuthFilter.Builder<User>()
                        .setJwtConsumer(jwtConsumer)
                        .setAuthorizer(JwtRoleAuthorizer())
                        .setRealm("LivingParchment server")
                        .setPrefix("Bearer")
                        .setAuthenticator(jwtAuthenticator)
                        .buildAuthFilter()
        ))
        environment.jersey().register(RolesAllowedDynamicFeature::class.java)

        environment.admin().setSecurityHandler(
                AdminSecurityHandler(configuration.adminUserName, configuration.adminPassword)
        )
    }

    override fun initialize(bootstrap: Bootstrap<LivingParchmentConfiguration>) {
        bootstrap.objectMapper.registerModule(KotlinModule())

        bootstrap.addBundle(ConfiguredAssetsBundle())
    }
}

fun main(args: Array<String>) {
    ServerMain().run(
            "server",
            ServerMain::class.java.getResource("/living-parchment.yml")
                    .toString()
                    .removePrefix("file:")
    )
}