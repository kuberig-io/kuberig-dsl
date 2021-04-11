plugins {
    id("io.kuberig.kotlin-conventions")
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish")
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