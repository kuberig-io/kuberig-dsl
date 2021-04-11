plugins {
    id("io.kuberig.kotlin-conventions")
}

dependencies {
    implementation(project(":kuberig-dsl:kuberig-dsl-base"))
    implementation("io.swagger:swagger-parser:1.0.41")

    implementation("org.slf4j:slf4j-api:1.7.26")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.2.3")
}
