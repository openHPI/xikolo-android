import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

buildscript {
    repositories {
        jcenter()
        google()
        maven("https://maven.fabric.io/public")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.6.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.KOTLIN}")
        classpath("com.google.gms:google-services:4.3.3")
        classpath("io.fabric.tools:gradle:1.31.2")
        classpath("io.realm:realm-gradle-plugin:6.1.0")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.28.0"
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
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
            "alpha", "beta", "rc", "cr", "m", "release", "preview", "b", "ea"
        ).any { qualifier ->
            val regex = "(?i).*[.-]$qualifier[.\\d-+]*".toRegex()
            regex.matches(candidate.version)
        }
    }
}
