plugins { alias(libs.plugins.android.library) }
android {
    namespace = "me.free_time.browser"
    compileSdk = 37
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    api(project(":Core"))
    implementation(libs.androidx.core.ktx)
}
