repositories {
    jcenter()
    maven("https://dl.bintray.com/teyckmans/rigeldev-oss-maven")
}

// optional
dependencies {
    val testImplementation by configurations
    val testRuntimeOnly by configurations

    val kubeRigVersion = "0.0.8"
    testImplementation("eu.rigeldev.kuberig:kuberig-dsl-base:$kubeRigVersion")
    testImplementation("eu.rigeldev.kuberig.dsl.kubernetes:kuberig-dsl-kubernetes-v1.14.1:$kubeRigVersion")

    val jacksonVersion = "2.9.8"
    testImplementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.2")
}