import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.10.1"
}

dependencies {
    val kotlinVersion = project.properties["kotlinVersion"]
    
    implementation(project(":kuberig-dsl-generator"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
}

gradlePlugin {
    plugins {
        create("kuberig-dsl-generator-gradle-plugin") {
            id = "eu.rigeldev.kuberig.dsl.generator"
            displayName = "Kuberig Kotlin DSL generator plugin"
            description = "This plugin is used to generate a Kotlin DSL based on a Kubernetes or Openshift swagger API definition."
            implementationClass = "eu.rigeldev.kuberig.dsl.generator.gradle.KubeRigDslGeneratorPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/teyckmans/kuberig-dsl"
    vcsUrl = "https://github.com/teyckmans/kuberig-dsl"
    tags = listOf("kubernetes", "kotlin", "dsl", "generator", "openshift")
}

tasks.withType<ProcessResources> {
    filesMatching("kuberig.properties") {
        expand(
            Pair("kuberigVersion", project.version.toString())
        )
    }
}