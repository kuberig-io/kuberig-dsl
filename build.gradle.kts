
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun isCiBuild(): Boolean {
    return System.getenv().getOrDefault("CI", "false") == "true"
}

fun determineVersion(): String {
    val env = System.getenv()

    return if (isCiBuild()) {
        if (env.containsKey("CI_COMMIT_TAG")) {
            env["CI_COMMIT_TAG"]!!
        } else {
            env["CI_COMMIT_REF_SLUG"]!! + "-SNAPSHOT"
        }
    } else {
        if (project.version.toString() == "unspecified") {
            println("Defaulting to version 0.0.0")
            "0.0.0-SNAPSHOT"
        } else {
            project.version.toString()
        }
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm") apply false
    id("org.jetbrains.dokka") apply false
    id("io.github.gradle-nexus.publish-plugin")
}

val projectVersion = determineVersion()

group = "io.kuberig"
version = projectVersion

if (project.hasProperty("sonatypeUsername") && project.hasProperty("sonatypePassword")) {
    nexusPublishing {
        repositories {
            sonatype()
        }
    }
} else {
    println("Sonatype credentials not available, skipping nexusPublishing plugin configuration.")
}

subprojects {
    apply {
        plugin("maven-publish")
        plugin("java")
        plugin("idea")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("jacoco")
        plugin("org.jetbrains.dokka")
    }

    val subProject = this

    group = "io.kuberig"
    subProject.version = projectVersion

    repositories {
        mavenCentral()
        // dokka is not available on mavenCentral yet.
        jcenter {
            content {
                includeGroup("org.jetbrains.dokka")
                includeGroup("org.jetbrains") // dokka (transitive: jetbrains markdown)
                includeGroup("org.jetbrains.kotlinx") // dokka (transitive: kotlinx-html-jvm)
                includeGroup("com.soywiz.korlibs.korte") // dokka (transitive: korte-jvm)
            }
        }
    }

    dependencies {
        val implementation by configurations
        val testImplementation by configurations

        // Align versions of all Kotlin components
        implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

        // Use the Kotlin JDK 8 standard library.
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

        // Use the Kotlin test library.
        testImplementation("org.jetbrains.kotlin:kotlin-test")

        // Use the Kotlin JUnit integration.
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    }

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_1_8
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    tasks.named<Test>("test") {
        finalizedBy(tasks.getByName("jacocoTestReport")) // report is always generated after tests run
    }
    tasks.named<JacocoReport>("jacocoTestReport") {
        dependsOn(tasks.getByName("test")) // tests are required to run before generating the report
        reports {
            xml.isEnabled = true
            csv.isEnabled = false
        }

        if (isCiBuild()) {
            doLast {
                exec {
                    commandLine("bash <(curl -s https://codecov.io/bash)".split(" "))
                }
            }
        }
    }

    tasks.getByName("check").dependsOn(tasks.getByName("jacocoTestReport"))

    val sourceSets: SourceSetContainer by this
    val sourcesJar by tasks.creating(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    val javadocJar by tasks.creating(Jar::class) {
        archiveClassifier.set("javadoc")
        val dokkaJavadoc = subProject.tasks.getByName<DokkaTask>("dokkaJavadoc")
        from(dokkaJavadoc.outputDirectory)
        dependsOn(dokkaJavadoc)
    }

    configure<PublishingExtension> {

        publications {
            create<MavenPublication>(subProject.name + "-maven") {
                from(components["java"])
                artifact(sourcesJar)
                artifact(javadocJar)
            }
        }

        repositories {
            maven {
                name = "local"
                // change URLs to point to your repos, e.g. http://my.org/repo
                val releasesRepoUrl = uri("$buildDir/repos/releases")
                val snapshotsRepoUrl = uri("$buildDir/repos/snapshots")

                val urlToUse = if (projectVersion.endsWith("SNAPSHOT")) {
                        snapshotsRepoUrl
                } else {
                    releasesRepoUrl
                }

                url = urlToUse
            }
        }

        repositories {
            maven {
                url = uri("https://gitlab.com/api/v4/projects/24703950/packages/maven")
                name = "GitLab"
                credentials(HttpHeaderCredentials::class) {
                    name = "Job-Token"
                    value = System.getenv("CI_JOB_TOKEN")
                }
                authentication {
                    create<HttpHeaderAuthentication>("header")
                }
            }
        }
    }

    tasks.withType<Jar> {
        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version
            )
        }
    }

    if (subProject.hasProperty("signing.keyId") && subProject.hasProperty("signing.password") && subProject.hasProperty("signing.secretKeyRingFile")) {
        apply {
            plugin("signing")
        }

        subProject.configure<SigningExtension> {
            subProject.extensions.getByType<PublishingExtension>().publications.all {
                sign(this)
            }
        }
    }

    subProject.plugins.withType<MavenPublishPlugin>().all {
        val publishing = subProject.extensions.getByType<PublishingExtension>()
        publishing.publications.withType<MavenPublication>().all {
            groupId = subProject.group as String
            artifactId = subProject.name
            version = subProject.version as String

            val vcsUrl = project.properties["vcsUrl"]!! as String

            pom {
                name.set("${subProject.group}:${subProject.name}")
                description.set("Kuberig DSL generation.")
                url.set(vcsUrl)

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }

                developers {
                    developer {
                        id.set("teyckmans")
                        name.set("Tom Eyckmans")
                        email.set("teyckmans@gmail.com")
                    }
                }

                val sshConnection = vcsUrl.replace("https://", "ssh://") + ".git"

                scm {
                    connection.set("scm:git:$vcsUrl")
                    developerConnection.set("scm:git:$sshConnection")
                    url.set(vcsUrl)
                }
            }
        }
    }

    val deploy by subProject.tasks.registering

    afterEvaluate {
        val env = System.getenv()

        if (isCiBuild()) {
            if (env.containsKey("CI_COMMIT_TAG")) {
                // release build
                deploy.get().dependsOn(
                    subProject.tasks.getByName("publishAllPublicationsToGitLabRepository"),
                    subProject.tasks.getByName("publishToSonatype"),
                    subProject.tasks.getByName("closeAndReleaseSonatypeStagingRepository"),
                    subProject.tasks.getByName("publishPlugins")
                )
            } else {
                // snapshot build
                deploy.get().dependsOn(
                    subProject.tasks.getByName("publishAllPublicationsToGitLabRepository")
                )
            }
        }
    }
}


