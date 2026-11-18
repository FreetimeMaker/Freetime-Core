import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.authentication.http.BasicAuthentication
import java.util.Properties

plugins {
    alias(libs.plugins.android.multiplatform.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
}

val releaseVersion = providers.gradleProperty("releaseVersion")
    .orElse(providers.environmentVariable("VERSION"))
    .orElse(libs.versions.freetime)

// Never use a deploy token from the tracked project gradle.properties.
val userFuryProperties = Properties().apply {
    val file = gradle.gradleUserHomeDir.resolve("gradle.properties")
    if (file.isFile) file.inputStream().use { load(it) }
}
val gemfuryUsername = providers.environmentVariable("FURY_USERNAME")
    .orElse(userFuryProperties.getProperty("furyUsername") ?: "freetimemaker")
val gemfuryToken = providers.environmentVariable("FURY_TOKEN")
    .orElse(userFuryProperties.getProperty("furyToken") ?: "")
val libraryModules = listOf("Core", "Design", "Browser", "Donations", "FreetimeWarn", "Notifications")

allprojects {
    group = "com.freetime"
    version = releaseVersion.get()
}

subprojects {
    if (name !in listOf("Sample", "Sample-android")) {
        plugins.withId("org.jetbrains.kotlin.multiplatform") {
            apply(plugin = "maven-publish")
            extensions.configure<PublishingExtension> {
                repositories {
                    maven {
                        name = "publicStaging"
                        url = rootProject.layout.buildDirectory.dir("public-maven").get().asFile.toURI()
                    }
                    maven {
                        name = "gemfury"
                        url = uri("https://repo.free-time.me/maven/")
                        authentication { create<BasicAuthentication>("basic") }
                        credentials {
                            username = gemfuryUsername.get()
                            password = gemfuryToken.get()
                        }
                    }

                }
                publications.withType<MavenPublication>().configureEach {
                    // Keep KMP's generated target coordinates and Gradle module metadata intact.
                    pom {
                        name.set("Freetime Core - ${project.name}")
                        description.set("Reusable Android and desktop Kotlin Multiplatform ${project.name} module.")
                        url.set("https://github.com/FreetimeMaker/Freetime-Core")
                        licenses {
                            license {
                                name.set("GNU General Public License v3.0")
                                url.set("https://www.gnu.org/licenses/gpl-3.0.html")
                                distribution.set("repo")
                            }
                        }
                        developers {
                            developer {
                                id.set("FreetimeMaker")
                                name.set("Freetime Maker")
                                url.set("https://github.com/FreetimeMaker")
                            }
                        }
                        scm {
                            connection.set("scm:git:https://github.com/FreetimeMaker/Freetime-Core.git")
                            url.set("https://github.com/FreetimeMaker/Freetime-Core")
                        }
                    }
                }
            }
        }
    }
}

val validateGemfuryCredentials = tasks.register("validateGemfuryCredentials") {
    doLast {
        check(gemfuryToken.get().isNotBlank()) {
            "Set FURY_TOKEN or furyToken in user ~/.gradle/gradle.properties. Project-local tokens are intentionally ignored."
        }
    }
}

subprojects {
    tasks.withType<org.gradle.api.publish.maven.tasks.PublishToMavenRepository>().configureEach {
        if (name.endsWith("ToGemfuryRepository")) dependsOn(validateGemfuryCredentials)
    }
}

tasks.register("publishGemfury") {
    group = "publishing"
    description = "Publishes all library KMP publications and metadata to Gemfury's Maven endpoint."
    dependsOn(libraryModules.map { ":$it:publishAllPublicationsToGemfuryRepository" })
}

tasks.register("publishPublic") {
    group = "publishing"
    description = "Rejects the legacy Android-only public upload path for KMP publications."
    doLast {
        throw GradleException(
            "The AAR-only public uploader cannot publish KMP metadata. " +
                "Use stagePublications for local validation, or publishKmp with repository " +
                "visibility configured, then verify_gemfury_release.py before tagging."
        )
    }
}

// This aggregate deliberately has no dependency on publish or a remote repository.
tasks.register("stagePublications") {
    group = "publishing"
    description = "Stages every library KMP publication locally without publishing credentials."
    dependsOn(libraryModules.map { ":$it:publishAllPublicationsToPublicStagingRepository" })
}

tasks.register("publishKmp") {
    group = "publishing"
    description = "Publishes native KMP publications; anonymous verification is required before release tagging."
    dependsOn("publishGemfury")
}
