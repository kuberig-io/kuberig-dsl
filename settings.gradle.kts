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

            if (requested.id.id == "de.marcphilipp.nexus-publish") {
                useModule("de.marcphilipp.gradle:nexus-publish-plugin:0.4.0")
            }

            if (requested.id.id == "io.codearte.nexus-staging") {
                useVersion("0.22.0")
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
