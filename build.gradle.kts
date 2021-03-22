
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

fun isCiBuild(): Boolean {
    return System.getenv().getOrDefault("CI", "false") == "true"
}

fun isReleaseBuild(): Boolean {
    return isCiBuild() && System.getenv().containsKey("CI_COMMIT_TAG")
}

fun isCiJobTokenAvailable(): Boolean {
    return isCiBuild() && System.getenv().containsKey("CI_JOB_TOKEN")
}

fun determineVersion(): String {
    val env = System.getenv()

    return if (isCiBuild()) {
        if (isReleaseBuild()) {
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

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.slack.api:bolt:1.1.+")
        classpath("com.slack.api:bolt-servlet:1.1.+")
        classpath("com.slack.api:bolt-jetty:1.1.+")
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

if (project.hasProperty("gradle.publish.key") && project.hasProperty("gradle.publish.secret")) {
    println("Gradle Plugin Portal credentials available, configuring Gradle Plugin Portal publishing...")
} else {
    println("Gradle Plugin Portal credentials NOT available, skipping Gradle Plugin Portal publishing.")
    println("gradle.publish.key: " + project.hasProperty("gradle.publish.key"))
    println("gradle.publish.secret: " + project.hasProperty("gradle.publish.secret"))
    if (isReleaseBuild()) {
        throw GradleException("Gradle Plugin Portal credentials are required for a release build!")
    }
}

if (project.hasProperty("mavenCentralUsername") && project.hasProperty("mavenCentralPassword")) {
    println("Sonatype credentials available, configuring nexusPublishing...")
    nexusPublishing {
        repositories {
            sonatype {
                stagingProfileId.set("a75126268d08")

                username.set(project.properties["mavenCentralUsername"]!! as String)
                password.set(project.properties["mavenCentralPassword"]!! as String)
            }
        }
    }
} else {
    println("Sonatype credentials not available, skipping nexusPublishing plugin configuration.")
    println("mavenCentralUsername: " + project.hasProperty("mavenCentralUsername"))
    println("mavenCentralPassword: " + project.hasProperty("mavenCentralPassword"))
    if (isReleaseBuild()) {
        throw GradleException("Sonatype credentials are required for a release build!")
    }
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

    if (subProject.hasProperty("signing.keyId")
        && subProject.hasProperty("signing.password")
        && subProject.hasProperty("signing.secretKeyRingFile")) {
        println("Signing configuration available, configuring artifact signing...")

        apply {
            plugin("signing")
        }

        subProject.configure<SigningExtension> {
            subProject.extensions.getByType<PublishingExtension>().publications.all {
                sign(this)
            }
        }
    } else {
        println("Signing configuration not available, skipping artifact signing configuration.")
        println("signing.keyId: " + subProject.hasProperty("signing.keyId"))
        println("signing.password: " + subProject.hasProperty("signing.password"))
        println("signing.secretKeyRingFile: " + subProject.hasProperty("signing.secretKeyRingFile"))
        if (isReleaseBuild()) {
            throw GradleException("Signing configuration is required for a release build!")
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
}

tasks.register("deploy") {
    group = "publishing"

    if (isCiBuild()) {
        if (isReleaseBuild()) {
            println("Running RELEASE build, publish to GitLab, Sonatype and Gradle Plugin Portal.")
            // release build
            project.subprojects.forEach {
                if (isCiJobTokenAvailable()) {
                    dependsOn(it.tasks.getByName("publishAllPublicationsToGitLabRepository"))
                }
                dependsOn(it.tasks.getByName("publishAllPublicationsToLocalRepository"))
                dependsOn(it.tasks.getByName("publishToSonatype"))
            }

            dependsOn("closeSonatypeStagingRepository")
            //dependsOn(":kuberig-dsl-generator-gradle-plugin:publishPlugins")
        } else {
            // snapshot build
            println("Running SNAPSHOT build, only publishing to GitLab repository.")


            project.subprojects.forEach {
                if (isCiJobTokenAvailable()) {
                    dependsOn(it.tasks.getByName("publishAllPublicationsToGitLabRepository"))
                }
                it.tasks.getByName("publishAllPublicationsToLocalRepository")
            }
        }
    } else {
        project.subprojects.forEach { dependsOn(it.tasks.getByName("publishAllPublicationsToLocalRepository")) }
    }
}

tasks.register("generateSettings") {
    group = "other"
    description = "write gradle.properties file applying escapes where needed and write in correct encoding."

    val propsFile = File(System.getenv("GRADLE_USER_HOME"), "gradle.properties")

    val props = Properties()
    addPropIfAvailable(props, "GRADLE_PUBLISH_KEY", "gradle.publish.key")
    addPropIfAvailable(props, "GRADLE_PUBLISH_SECRET", "gradle.publish.secret")
    addPropIfAvailable(props, "SIGNING_KEY_ID", "signing.keyId")
    addPropIfAvailable(props, "SIGNING_PASSWORD", "signing.password")
    addPropIfAvailable(props, "PLAIN_M2_SIGNING_KEY", "signing.secretKeyRingFile")
    addPropIfAvailable(props, "SONATYPE_USERNAME", "mavenCentralUsername")
    addPropIfAvailable(props, "SONATYPE_PASSWORD", "mavenCentralPassword")

    propsFile.outputStream().use {
        props.store(it, null)
    }
}

fun addPropIfAvailable(props: Properties, envVarName: String, propertyKey: String) {
    val env = System.getenv()

    if (env.containsKey(envVarName)) {
        val rawValue = env[envVarName]!!
        val cleanedValue = if (rawValue.startsWith("\"") && rawValue.endsWith("\"")) {
            rawValue.substring(1, rawValue.length - 1)
        } else {
            rawValue
        }
        props.setProperty(propertyKey, cleanedValue)
    }
}
