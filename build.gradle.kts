import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
}

val releaseVersion = providers.gradleProperty("releaseVersion")
    .orElse(providers.environmentVariable("VERSION"))
    .orElse(libs.versions.freetime)

allprojects {
    group = if (System.getenv("JITPACK") == "true") {
        "com.github.FreetimeMaker.Freetime-Core"
    } else {
        "me.free-time"
    }
    version = releaseVersion.get()
}

subprojects {
    plugins.withId("com.android.library") {
        apply(plugin = "maven-publish")

        components.whenObjectAdded {
            if (name == "release" && extensions.getByType<PublishingExtension>().publications.findByName("release") == null) {
                val releaseComponent = this
                extensions.configure<PublishingExtension> {
                    publications {
                        create<MavenPublication>("release") {
                            from(releaseComponent)
                            // JitPack multi-module coordinates use the module name:
                            // com.github.FreetimeMaker.Freetime-Core:<Module>:<Tag>
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
