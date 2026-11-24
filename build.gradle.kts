import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.GenerateMavenPom
import org.gradle.api.publish.maven.tasks.PublishToMavenLocal
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import javax.xml.parsers.DocumentBuilderFactory

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
    // JitPack exports VERSION=v4.0.1; Maven and the consolidator use bare SemVer.
    .map { it.removePrefix("v") }
val pythonExecutable = providers.gradleProperty("pythonExecutable").orElse("python3")
val libraryModules = listOf("Core", "Design", "Browser", "Donations", "FreetimeWarn", "Notifications")

allprojects {
    group = "com.freetime"
    version = releaseVersion.get()
}

val stageNativePublications = tasks.register("stageNativePublications") {
    group = "publishing"
    description = "Stages original KMP publications as private local inputs for consolidation."
    dependsOn(libraryModules.flatMap { module ->
        listOf("Android", "Desktop", "KotlinMultiplatform").map { publication ->
            ":$module:publish${publication}PublicationToNativeStagingRepository"
        }
    })
}

val prepareUnifiedPublications = tasks.register<Exec>("prepareUnifiedPublications") {
    group = "publishing"
    description = "Inlines platform variants into one Maven coordinate per library."
    dependsOn(stageNativePublications)
    workingDir(rootDir)
    commandLine(pythonExecutable.get(), ".github/scripts/consolidate_kmp_publications.py", releaseVersion.get())
    inputs.file(".github/scripts/consolidate_kmp_publications.py")
    inputs.dir(layout.buildDirectory.dir("native-maven"))
    inputs.property("releaseVersion", releaseVersion)
    outputs.dir(layout.buildDirectory.dir("unified-inputs"))
}

subprojects {
    if (name in libraryModules) {
        plugins.withId("org.jetbrains.kotlin.multiplatform") {
            apply(plugin = "maven-publish")
            extensions.configure<PublishingExtension> {
                repositories {
                    maven {
                        name = "nativeStaging"
                        url = rootProject.layout.buildDirectory.dir("native-maven").get().asFile.toURI()
                    }
                    maven {
                        name = "publicStaging"
                        url = rootProject.layout.buildDirectory.dir("public-maven").get().asFile.toURI()
                    }
                }
                publications.withType<MavenPublication>().configureEach {
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
                publications.create<MavenPublication>("unified") {
                    artifactId = project.name
                    val prefix = "${project.name}-${releaseVersion.get()}"
                    val directory = rootProject.layout.buildDirectory.dir(
                        "unified-inputs/com/freetime/${project.name}/${releaseVersion.get()}"
                    )
                    fun attach(suffix: String, fileExtension: String, artifactClassifier: String? = null) {
                        artifact(directory.map { it.file("$prefix$suffix.$fileExtension") }) {
                            extension = fileExtension
                            if (artifactClassifier != null) classifier = artifactClassifier
                            builtBy(prepareUnifiedPublications)
                        }
                    }
                    attach("", "jar")
                    attach("", "module")
                    attach("-sources", "jar", "sources")
                    attach("-android", "aar", "android")
                    attach("-android-sources", "jar", "android-sources")
                    attach("-desktop", "jar", "desktop")
                    attach("-desktop-sources", "jar", "desktop-sources")

                    val nativePom = rootProject.layout.buildDirectory.file(
                        "native-maven/com/freetime/${project.name}/${releaseVersion.get()}/$prefix.pom"
                    )
                    pom.withXml {
                        val factory = DocumentBuilderFactory.newInstance().apply {
                            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                            isNamespaceAware = true
                        }
                        val original = factory.newDocumentBuilder().parse(nativePom.get().asFile).documentElement
                        val destination = asElement()
                        // The attached .module file is a second classifier-less artifact, which makes
                        // Gradle fall back to packaging "pom"; the coordinate still publishes a jar.
                        val packaging = destination.getElementsByTagName("packaging")
                        if (packaging.length == 1) packaging.item(0).textContent = "jar"
                        for (index in 0 until original.childNodes.length) {
                            val node = original.childNodes.item(index)
                            if (node.localName in setOf("dependencies", "dependencyManagement")) {
                                destination.appendChild(destination.ownerDocument.importNode(node, true))
                            }
                        }
                        // Gradle must request our complete variant metadata instead of using only the POM.
                        destination.appendChild(destination.ownerDocument.createComment(
                            "do_not_remove: published-with-gradle-metadata"
                        ))
                    }
                }
            }
            tasks.withType<GenerateMavenPom>().configureEach {
                if (name == "generatePomFileForUnifiedPublication") dependsOn(prepareUnifiedPublications)
            }
            tasks.withType<GenerateModuleMetadata>().configureEach {
                // The unified publication attaches the validated, inlined metadata explicitly.
                if (name == "generateMetadataFileForUnifiedPublication") enabled = false
            }
            tasks.withType<PublishToMavenLocal>().configureEach {
                // Native root coordinates would collide with the unified publication in Maven local.
                if (name != "publishUnifiedPublicationToMavenLocal") enabled = false
            }
            tasks.withType<PublishToMavenRepository>().configureEach {
                val unified = name.startsWith("publishUnifiedPublication")
                if (name.endsWith("ToNativeStagingRepository")) {
                    if (unified) enabled = false
                } else if (!unified) {
                    // Native target publications are local inputs, never separate staged packages.
                    enabled = false
                }
            }
        }
    }
}

tasks.register("stagePublications") {
    group = "publishing"
    description = "Stages six complete single-coordinate KMP libraries locally without credentials."
    dependsOn(libraryModules.map { ":$it:publishUnifiedPublicationToPublicStagingRepository" })
}

tasks.register("publishPublic") {
    group = "publishing"
    description = "Rejects the legacy AAR-only uploader; it cannot publish KMP variant metadata."
    doLast {
        throw GradleException("Remote publishing is disabled. Use stagePublications to stage KMP .module metadata locally.")
    }
}
