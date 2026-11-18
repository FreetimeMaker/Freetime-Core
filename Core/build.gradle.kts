plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.multiplatform.library)
}

kotlin {
    androidLibrary {
        namespace = "com.freetime.core"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        testNamespace = "com.freetime.core.test"
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {}.configure {
            applicationId = "com.freetime.core.test"
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }
    jvm("desktop")
    jvmToolchain(17)
    sourceSets {
        commonTest.dependencies { implementation(kotlin("test")) }
        getByName("androidDeviceTest").dependencies {
            implementation(libs.androidx.test.runner)
            implementation(libs.androidx.test.ext.junit)
        }
    }
}
