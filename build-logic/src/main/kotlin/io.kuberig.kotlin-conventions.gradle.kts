
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("maven-publish")
    id("org.jetbrains.kotlin.jvm")
    id("jacoco")
    id("org.jetbrains.dokka")
    id("signing")
}

group = "io.kuberig"

val vcsUrl by extra("https://github.com/kuberig-io/kuberig")
val webSiteUrl by extra("https://kuberig.io")

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
}

tasks.getByName("check").dependsOn(tasks.getByName("jacocoTestReport"))

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(project.sourceSets["main"].allSource)
}

val javadocJar by tasks.creating(Jar::class) {
    archiveClassifier.set("javadoc")
    val dokkaJavadoc = tasks.getByName<DokkaTask>("dokkaJavadoc")
    from(dokkaJavadoc.outputDirectory)
    dependsOn(dokkaJavadoc)
}

publishing {

    publications {
        create<MavenPublication>(project.name + "-maven") {
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)
        }
    }

    repositories {
        maven {
            name = "local"
            url = uri("$buildDir/repos/releases")
        }
    }

    if (isCiJobTokenAvailable()) {
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
}

tasks.withType<Jar> {
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version
        )
    }
}

signing {
    extensions.getByType<PublishingExtension>().publications.all {
        sign(this)
    }
}

plugins.withType<MavenPublishPlugin>().all {
    val publishing = extensions.getByType<PublishingExtension>()
    publishing.publications.withType<MavenPublication>().all {
        groupId = project.group as String
        artifactId = project.name
        version = project.version.toString()

        pom {
            name.set("${project.group}:${project.name}")
            description.set("Kuberig Gradle plugin for deploying to Kubernetes/OpenShift.")
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

fun isCiBuild(): Boolean {
    return System.getenv().getOrDefault("CI", "false") == "true"
}

fun isCiJobTokenAvailable(): Boolean {
    return isCiBuild() && System.getenv().containsKey("CI_JOB_TOKEN")
}