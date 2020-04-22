plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    buildToolsVersion(Versions.BUILD_TOOLS)
    compileSdkVersion(Versions.COMPILE_SDK)

    defaultConfig {
        minSdkVersion(Versions.MIN_SDK)
        targetSdkVersion(Versions.TARGET_SDK)
    }
    lintOptions {
        isAbortOnError = false
    }
    compileOptions {
        sourceCompatibility = Versions.JAVA
        targetCompatibility = Versions.JAVA
    }
    kotlinOptions {
        jvmTarget = Versions.KOTLIN_JVM
    }
}

dependencies {
    implementation(SharedDependencies.OK_HTTP)
    implementation(SharedDependencies.GSON)
}
