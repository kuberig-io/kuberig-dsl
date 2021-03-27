plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

val jacksonVersion by extra("2.12.2")

dependencies {
    implementation("com.konghq:unirest-java:2.3.14")
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.11.0.202103091610-r")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

gradlePlugin {
    plugins {
        create("maven-central") {
            id = "io.kuberig.maven-central"
            implementationClass = "manual.MavenCentralPublishPlugin"
        }
    }
}