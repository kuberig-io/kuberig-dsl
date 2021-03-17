plugins {
    `java-gradle-plugin`
}

if (project.hasProperty("gradle.publish.key") && project.hasProperty("gradle.publish.secret")) {
    println("Gradle Plugin Portal credentials available, configuring Gradle Plugin Portal publishing...")
    plugins {
        id("com.gradle.plugin-publish") version "0.11.0"
    }
} else {
    println("Gradle Plugin Portal credentials NOT available, skipping Gradle Plugin Portal publishing.")
    println("gradle.publish.key: " + project.hasProperty("gradle.publish.key"))
    println("gradle.publish.secret: " + project.hasProperty("gradle.publish.secret"))
}

dependencies {
    val kotlinVersion = project.properties["kotlinVersion"]
    
    implementation(project(":kuberig-dsl-generator"))

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
}

gradlePlugin {
    plugins {
        create("kuberig-dsl-generator-gradle-plugin") {
            id = "io.kuberig.dsl.generator"
            displayName = "Kuberig Kotlin DSL generator plugin"
            description = "This plugin is used to generate a Kotlin DSL based on a Kubernetes or Openshift swagger API definition."
            implementationClass = "io.kuberig.dsl.generator.gradle.KubeRigDslGeneratorPlugin"
        }
    }
}

pluginBundle {
    website = project.properties["websiteUrl"]!! as String
    vcsUrl = project.properties["vcsUrl"]!! as String
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

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

gradlePlugin.testSourceSets(functionalTestSourceSet)
configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

// Add a task to run the functional tests
val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
    setForkEvery(1)
}

tasks.check {
    // Run the functional tests as part of `check`
    dependsOn(functionalTest)
}