plugins {
    id("io.kuberig.gradle-plugin-conventions")
}

dependencies {
    val kotlinVersion = project.properties["kotlinVersion"]
    
    implementation(project(":kuberig-dsl:kuberig-dsl-generator"))

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
}

val pluginDescription = "This plugin is used to generate a Kotlin DSL based on a Kubernetes or Openshift swagger API definition."

gradlePlugin {
    plugins {
        create("kuberig-dsl-generator-gradle-plugin") {
            id = "io.kuberig.dsl.generator"
            displayName = "Kuberig Kotlin DSL generator plugin"
            description = pluginDescription
            implementationClass = "io.kuberig.dsl.generator.gradle.KubeRigDslGeneratorPlugin"
        }
    }
}

pluginBundle {
    website = extra["webSiteUrl"]!! as String
    vcsUrl = extra["vcsUrl"]!! as String
    tags = listOf("kubernetes", "kotlin", "dsl", "generator", "openshift")
}

tasks.named<ProcessResources>("processResources") {
    doFirst {
        println("ProcessResources using version: " + project.version.toString())
        check(project.version.toString() != "unspecified")
    }
    filesMatching("io.kuberig.dsl.generator.properties") {
        expand(
            Pair("kuberigDslVersion", project.version.toString())
        )
    }
}

plugins.withType<MavenPublishPlugin>().all {
    val publishing = extensions.getByType<PublishingExtension>()
    publishing.publications.withType<MavenPublication>().all {
        pom {
            description.set(pluginDescription)
        }
    }
}