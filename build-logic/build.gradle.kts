plugins {
    `kotlin-dsl`
}

repositories {
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }

    mavenCentral()

    // dokka is not available on mavenCentral yet.
    jcenter {
        content {
            includeGroup("org.jetbrains.dokka")
            includeGroup("org.jetbrains") // dokka (transitive: jetbrains markdown)
        }
    }
}

val jacksonVersion by extra("2.12.2")

dependencies {
    implementation("com.konghq:unirest-java:2.3.14")
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.11.0.202103091610-r")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    implementation("gradle.plugin.fr.brouillard.oss.gradle:gradle-jgitver-plugin:0.10.0-rc03")
    implementation("fr.brouillard.oss:jgitver:0.12.0")


    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.32")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.4.30")

    implementation("com.gradle.publish:plugin-publish-plugin:0.13.0")
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

gradlePlugin {
    plugins {
        create("maven-central") {
            id = "io.kuberig.maven-central"
            implementationClass = "io.kuberig.publishing.MavenCentralPublishPlugin"
        }
    }
}