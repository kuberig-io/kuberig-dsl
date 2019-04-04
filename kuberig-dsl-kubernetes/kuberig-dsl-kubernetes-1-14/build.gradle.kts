buildscript {
    repositories {
        mavenCentral()
        maven("https://dl.bintray.com/teyckmans/rigeldev-oss-maven")
    }
    dependencies {
        classpath("eu.rigeldev.kuberig:kuberig-dsl-generator-gradle-plugin:0.0.8")
    }
}

apply(plugin = "eu.rigeldev.kuberig.dsl.generator")

repositories {
    mavenCentral()
    maven("https://dl.bintray.com/teyckmans/rigeldev-oss-maven")
}

// optional
dependencies {
    val testImplementation by configurations
    val testRuntimeOnly by configurations

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.2")
}