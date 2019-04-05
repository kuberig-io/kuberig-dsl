plugins {
    id("eu.rigeldev.kuberig.dsl.generator") version "0.0.8"
}

repositories {
    jcenter()
}

// optional
dependencies {
    val testImplementation by configurations
    val testRuntimeOnly by configurations

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.2")
}