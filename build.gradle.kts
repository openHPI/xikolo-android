import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

buildscript {
    repositories {
        jcenter()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.KOTLIN}")
        classpath("io.realm:realm-gradle-plugin:7.0.8")
        classpath("com.google.gms:google-services:4.3.8")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.6.1")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.38.0"

    // Make sure to update this in `buildSrc/build.gradle.kts` as well
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
}

allprojects {
    repositories {
        jcenter()
        google()
        maven("https://jitpack.io")
        maven("https://clojars.org/repo/")
        flatDir {
            dirs("libs")
        }
    }
}

ktlint {
    version.set("0.40.0") // Make sure to update this in `buildSrc/build.gradle.kts` as well
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
