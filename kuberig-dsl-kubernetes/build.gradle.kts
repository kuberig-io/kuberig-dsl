repositories {
    jcenter()
    maven("https://dl.bintray.com/teyckmans/rigeldev-oss-maven")
}

// optional
dependencies {
    val testImplementation by configurations
    val testRuntimeOnly by configurations
    
    testImplementation("eu.rigeldev.kuberig.dsl.kubernetes:kuberig-dsl-kubernetes-v1.14.1:0.0.8")
    
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.2")
}