import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    `kotlin-dsl`
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
}

repositories {
    mavenCentral()
}

ktlint {
    version.set("0.42.1")
    android.set(true)
    ignoreFailures.set(true)
    reporters {
        reporter(ReporterType.CHECKSTYLE)
    }
}
