import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.tasks.GenerateModuleMetadata

plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
}

val releaseVersion = providers.gradleProperty("releaseVersion")
    .orElse(providers.environmentVariable("VERSION"))
    .orElse(libs.versions.freetime)

val publicLibraryModules = listOf("Core", "Design", "Browser", "Donations", "FreetimeWarn", "Notifications")

tasks.register<Exec>("publishPublic") {
    group = "publishing"
    description = "Uploads release artifacts to Gemfury with public=true using the release version."
    dependsOn(publicLibraryModules.map { ":$it:publishAllPublicationsToPublicStagingRepository" })
    workingDir(rootDir)
    commandLine("python3", ".github/scripts/publish_public_maven.py", releaseVersion.get())
}

allprojects {
    group = "com.freetime"
    version = releaseVersion.get()
}

subprojects {
    plugins.withId("com.android.library") {
        apply(plugin = "maven-publish")

        extensions.configure<PublishingExtension> {
            repositories {
                maven {
                    name = "publicStaging"
                    url = rootProject.layout.buildDirectory.dir("public-maven").get().asFile.toURI()
                }
            }
        }

        // Gemfury's public upload API serves Maven artifacts, not Gradle .module files.
        // Publish POM dependency metadata without advertising an unavailable .module file.
        tasks.withType<GenerateModuleMetadata>().configureEach {
            enabled = false
        }

        components.whenObjectAdded {
            if (name == "release" && extensions.getByType<PublishingExtension>().publications.findByName("release") == null) {
                val releaseComponent = this
                extensions.configure<PublishingExtension> {
                    publications {
                        create<MavenPublication>("release") {
                            from(releaseComponent)
                            // Keep module names stable: com.freetime:<Module>:<Version>.
                            artifactId = project.name
                            pom {
                                name.set("Freetime Core - ${project.name}")
                                description.set("Reusable Android ${project.name} module from Freetime Core.")
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
    }
}
