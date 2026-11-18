plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidLibrary {
        namespace = "com.freetime.sample.shared"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }
    jvm("desktop")
    jvmToolchain(17)
    sourceSets {
        commonMain.dependencies {
            implementation(project(":Core"))
            implementation(project(":Design"))
            implementation(project(":Browser"))
            implementation(project(":Donations"))
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
        }
        getByName("desktopMain").dependencies { implementation(compose.desktop.currentOs) }
    }
}

compose.desktop {
    application { mainClass = "com.freetime.sample.MainKt" }
}
