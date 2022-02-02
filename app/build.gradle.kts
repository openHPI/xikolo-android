import com.android.build.gradle.api.ApplicationVariant
import java.io.FileInputStream
import java.util.Properties
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("realm-android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("org.jlleitschuh.gradle.ktlint")
}

android {
    buildToolsVersion = Versions.BUILD_TOOLS
    compileSdkVersion(Versions.COMPILE_SDK)

    defaultConfig {
        minSdkVersion(Versions.MIN_SDK)
        targetSdkVersion(Versions.TARGET_SDK)

        multiDexEnabled = true

        versionName = Versions.VERSION_NAME
        versionCode = Versions.VERSION_CODE

        resValue("integer", "xikolo_api_version", Versions.XIKOLO_API.toString())
        resValue("integer", "realm_schema_version", Versions.REALM_SCHEMA.toString())

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    flavorDimensions += listOf("brand")

    productFlavors {
        FLAVORS.forEach { (flavor, config) ->
            register(flavor) {
                applicationId = config.appId
                resConfigs(config.languages)
            }
        }
    }

    val signingFile = rootProject.file("signing.properties")
    val signingProperties = Properties()
    signingProperties.load(FileInputStream(signingFile))

    signingConfigs {
        signingProperties["RELEASE_STORE_FILE"]?.let { keystore ->
            val keystoreFile = rootProject.file(keystore)
            val keystorePassword = signingProperties["RELEASE_STORE_PASSWORD"].toString()

            FLAVORS.forEach { (flavor, _) ->
                register(flavor) {
                    storeFile = keystoreFile
                    storePassword = keystorePassword
                    keyAlias =
                        signingProperties["RELEASE_KEY_ALIAS_${flavor.toUpperCase()}"].toString()
                    keyPassword =
                        signingProperties["RELEASE_KEY_PASSWORD_${flavor.toUpperCase()}"].toString()
                }
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            isZipAlignEnabled = false
            isDebuggable = false

            signingProperties["RELEASE_STORE_FILE"]?.let {
                FLAVORS.forEach { (flavor, _) ->
                    productFlavors.getByName(flavor).signingConfig =
                        signingConfigs.getByName(flavor)
                }
            }
        }
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            isZipAlignEnabled = false
            isDebuggable = true

            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }
    }
    @Suppress("ObjectLiteralToLambda")
    applicationVariants.all(object : Action<ApplicationVariant> {
        override fun execute(variant: ApplicationVariant) {
            val flavor = variant.productFlavors.first()
            val config = FLAVORS[flavor.name] ?: error("flavor ${flavor.name} not found")
            var name = config.appName
            if (variant.buildType.name == "debug") {
                name = "$name.debug"
            }

            variant.resValue("string", "app_name", name)
            variant.resValue("string", "app_host", config.appHost)

            variant.resValue("color", "apptheme_primary", config.primaryColor)
            variant.resValue("color", "apptheme_secondary", config.secondaryColor)

            variant.resValue(
                "color", "apptheme_primary_dark", Color.darken(config.primaryColor)
            )
            variant.resValue(
                "color", "apptheme_secondary_dark", Color.darken(config.secondaryColor)
            )
            variant.resValue(
                "color", "apptheme_primary_light", Color.lighten(config.primaryColor)
            )
            variant.resValue(
                "color", "apptheme_secondary_light", Color.lighten(config.secondaryColor)
            )

            variant.resValue("string", "service_download", "$name Download Service")
        }
    })
    compileOptions {
        sourceCompatibility = Versions.JAVA
        targetCompatibility = Versions.JAVA
    }
    kotlinOptions {
        jvmTarget = Versions.KOTLIN_JVM
    }
    lint {
        abortOnError = false
        disable += setOf(
            "ContentDescription",
            "InflateParams",
            "Overdraw",
            "UnusedResources",
            "VectorPath",
            "VectorRaster"
        )
        htmlReport = false
        xmlReport = true
    }
}

ktlint {
    android.set(true)
    ignoreFailures.set(true)
    reporters {
        reporter(ReporterType.CHECKSTYLE)
    }
}

apply("dependencies.gradle.kts")
