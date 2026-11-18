plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidLibrary {
        namespace = "com.freetime.warn"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        withHostTestBuilder {}.configure {}
    }
    jvm("desktop")
    jvmToolchain(17)
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(project(":Design"))
        }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}
