import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.plugins.ide.idea.model.Module

plugins {
    java
    kotlin("jvm") version "1.3.20"
    idea
}

group = "eu.rigeldev.kuberig.kubernetes"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.fasterxml.jackson.core:jackson-core:2.9.8")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
    implementation("com.fasterxml.jackson.module:jackson-modules-java8:2.9.8")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.8")

    implementation(project(":kuberig-dsl-base"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.2")
    testImplementation("org.skyscreamer:jsonassert:1.5.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

sourceSets["main"].withConvention(KotlinSourceSet::class) {
    kotlin.srcDir("$buildDir/generated-src/main/kotlin")
}
idea.module {
    // Marks the already(!) added srcDir as "generated"
    generatedSourceDirs.add(project.file("$buildDir/generated-src/main/kotlin"))
}

tasks.getByName("compileKotlin", KotlinCompile::class) {
    dependsOn("generateDsl")
}

tasks {
    create("generateDsl", JavaExec::class) {
        group = "dsl-generation"
        main = "eu.rigeldev.kuberig.dsl.generator.DslCodeGeneratorKt"
        workingDir = project.rootProject.projectDir
        args = listOf(project.projectDir.absolutePath)
        classpath = project(":kuberig-dsl-generator").sourceSets["main"].runtimeClasspath
    }
}