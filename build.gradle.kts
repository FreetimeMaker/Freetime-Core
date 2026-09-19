import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension

plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
}

val releaseVersion = providers.gradleProperty("releaseVersion")
    .orElse(providers.environmentVariable("RELEASE_VERSION"))
    .orElse("0.0.0-local")

allprojects {
    group = "me.free-time"
    version = releaseVersion.get()
}

val artifactIds = mapOf(
    "Core" to "freetime-core",
    "Design" to "freetime-design",
    "Updater" to "freetime-updater",
    "Browser" to "freetime-browser",
    "Donations" to "freetime-donations"
)

subprojects {
    plugins.withId("com.android.library") {
        apply(plugin = "maven-publish")
        apply(plugin = "signing")

        afterEvaluate {
            extensions.configure<PublishingExtension> {
                publications {
                    create<MavenPublication>("release") {
                        from(components["release"])
                        artifactId = artifactIds.getValue(project.name)

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
                                    organization.set("Freetime Maker")
                                    organizationUrl.set("https://free-time.me")
                                }
                            }

                            scm {
                                connection.set("scm:git:https://github.com/FreetimeMaker/Freetime-Core.git")
                                developerConnection.set("scm:git:ssh://git@github.com/FreetimeMaker/Freetime-Core.git")
                                url.set("https://github.com/FreetimeMaker/Freetime-Core")
                            }
                        }
                    }
                }

                repositories {
                    maven {
                        name = "CentralBundle"
                        url = rootProject.layout.buildDirectory.dir("central-repository").get().asFile.toURI()
                    }
                }
            }

            extensions.configure<SigningExtension> {
                val signingKey = System.getenv("SIGNING_KEY")
                val signingPassword = System.getenv("SIGNING_PASSWORD")
                if (!signingKey.isNullOrBlank()) {
                    useInMemoryPgpKeys(signingKey, signingPassword)
                    sign(extensions.getByType<PublishingExtension>().publications["release"])
                }
            }
        }
    }
}
