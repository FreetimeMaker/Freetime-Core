pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Freetime Core KMP"
include(":Core", ":Design", ":Browser", ":Donations", ":FreetimeWarn", ":Notifications", ":Sample", ":Sample-android")
project(":Sample-android").projectDir = file("Sample/androidApp")
