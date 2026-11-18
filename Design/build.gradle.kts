plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidLibrary {
        namespace = "com.freetime.design"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }
    jvm("desktop")
    jvmToolchain(17)

    sourceSets {
        commonMain.dependencies {
            api(compose.ui)
            api(compose.foundation)
            // Expressive APIs require a newer Material 3 than Compose's bundled default.
            api("org.jetbrains.compose.material3:material3:1.10.0-alpha05")
            implementation(compose.components.uiToolingPreview)
            api(libs.kyant.backdrop)
            api(libs.kyant.shapes)
        }
    }
}

val checkTemplates by tasks.registering {
    group = "verification"
    description = "Compiles all theme templates, previews and Freetime adapters for Android and desktop."
    dependsOn("compileAndroidMain", "compileKotlinDesktop")
}

tasks.named("check") {
    dependsOn(checkTemplates)
}
