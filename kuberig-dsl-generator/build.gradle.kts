import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.20"
}

group = "eu.rigeldev.kuberig"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":kuberig-dsl-base"))
    implementation("io.swagger:swagger-parser:1.0.41")

    implementation("org.slf4j:slf4j-api:1.7.26")
    implementation("ch.qos.logback:logback-core:1.2.3")
    runtime("ch.qos.logback:logback-classic:1.2.3")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.2")
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