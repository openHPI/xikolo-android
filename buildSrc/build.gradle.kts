import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    `kotlin-dsl`
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
}

repositories {
    jcenter()
}

ktlint {
    version.set("0.40.0")
    android.set(true)
    ignoreFailures.set(true)
    reporters {
        reporter(ReporterType.CHECKSTYLE)
    }
}
