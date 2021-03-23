rootProject.name = "kuberig-dsl"

pluginManagement {
    val kotlinVersion : String by settings
    val dokkaVersion : String by settings

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.jetbrains.kotlin.jvm") {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
            }

            if (requested.id.id == "org.jetbrains.dokka") {
                useModule("org.jetbrains.dokka:dokka-gradle-plugin:$dokkaVersion")
            }

            if (requested.id.id == "io.github.gradle-nexus.publish-plugin") {
                useModule("io.github.gradle-nexus:publish-plugin:1.0.0")
            }
        }
    }

    repositories {
        gradlePluginPortal()
        mavenCentral()

        // dokka is not available on mavenCentral yet.
        jcenter {
            content {
                includeGroup("org.jetbrains.dokka")
                includeGroup("org.jetbrains") // dokka (transitive: jetbrains markdown)
            }
        }
    }
}

include ("kuberig-dsl-generator")
include ("kuberig-dsl-base")
include ("kuberig-dsl-generator-gradle-plugin")


buildCache {
    remote<HttpBuildCache> {
        url = uri("http://build-cache.rigel.dev:5071/cache/")
    }
}
