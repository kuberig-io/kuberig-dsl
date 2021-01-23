import com.jfrog.bintray.gradle.BintrayExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


buildscript {
    val kotlinVersion by extra("1.3.72")

    repositories {
        jcenter()
    }
    dependencies {
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

val projectVersion = if (project.version.toString() == "unspecified") {
    println("Defaulting to version 0.0.0")
    "0.0.0"
} else {
    project.version.toString()
}

subprojects {
    apply {
        plugin("maven-publish")
        plugin("java")
        plugin("idea")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("com.jfrog.bintray")
        plugin("jacoco")
    }

    val subProject = this

    group = "io.kuberig"
    subProject.version = projectVersion

    repositories {
        jcenter()
    }

    dependencies {
        val implementation by configurations
        val runtime by configurations
        val testImplementation by configurations
        val testRuntimeOnly by configurations

        implementation(kotlin("stdlib-jdk8"))

        // Use the Kotlin test library.
        testImplementation("org.jetbrains.kotlin:kotlin-test")

        // Use the Kotlin JUnit integration.
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    }

    configure<JavaPluginConvention> {
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
    val sourcesJar by tasks.registering(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    configure<PublishingExtension> {

        publications {
            register(subProject.name, MavenPublication::class) {
                from(components["java"])
                artifact(sourcesJar.get())
            }
        }

    }

    val bintrayApiKey : String by project
    val bintrayUser : String by project

    configure<BintrayExtension> {
        user = bintrayUser
        key = bintrayApiKey
        publish = true

        pkg(closureOf<BintrayExtension.PackageConfig> {
            repo = "rigeldev-oss-maven"
            name = "io-kuberig-" + subProject.name
            setLicenses("Apache-2.0")
            isPublicDownloadNumbers = true
            websiteUrl = project.properties["websiteUrl"]!! as String
            vcsUrl = project.properties["vcsUrl"]!! as String
        })

        setPublications(subProject.name)
    }

    tasks.withType<Jar> {
        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version
            )
        }
    }
}
