import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.3.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.KOTLIN}")
        classpath("io.realm:realm-gradle-plugin:7.0.8")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.7.1")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.39.0"

    // Make sure to update this in `buildSrc/build.gradle.kts` as well
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
}

allprojects {
    repositories {
        jcenter()
        mavenCentral()
        google()
    }
}

ktlint {
    version.set("0.42.1") // Make sure to update this in `buildSrc/build.gradle.kts` as well
    android.set(true)
    ignoreFailures.set(true)
    reporters {
        reporter(ReporterType.CHECKSTYLE)
    }
}

tasks.withType<DependencyUpdatesTask> {
    gradleReleaseChannel = "current"

    rejectVersionIf {
        listOf(
            "alpha", "beta", "rc", "cr", "m", "release", "preview", "b", "ea", "eap", "patch"
        ).any { qualifier ->
            val regex = "(?i).*[.-]$qualifier[.\\d-+]*".toRegex()
            regex.matches(candidate.version)
        }
    }
}
