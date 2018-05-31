package me.ialistannen.livingparchment.backend.server.config

import io.dropwizard.Configuration
import io.dropwizard.bundles.assets.AssetsBundleConfiguration
import io.dropwizard.bundles.assets.AssetsConfiguration
import org.hibernate.validator.constraints.NotEmpty
import javax.validation.Valid
import javax.validation.constraints.NotNull

class LivingParchmentConfiguration : Configuration(), AssetsBundleConfiguration {

    @NotEmpty
    lateinit var dbUser: String

    @NotEmpty
    lateinit var dbConnectionString: String

    @NotEmpty
    lateinit var dbPassword: String

    @NotEmpty
    lateinit var jwtTokenSecret: String

    @NotEmpty
    lateinit var adminUserName: String

    @NotEmpty
    lateinit var adminPassword: String

    @NotEmpty
    lateinit var coverFolder: String

    @Valid
    @NotNull
    lateinit var assets: AssetsConfiguration

    override fun getAssetsConfiguration(): AssetsConfiguration {
        return assets
    }

    /**
     * Returns the jwt client secret used to authenticate users.
     */
    fun getJwtTokenSecret(): ByteArray {
        return jwtTokenSecret.toByteArray()
    }
}