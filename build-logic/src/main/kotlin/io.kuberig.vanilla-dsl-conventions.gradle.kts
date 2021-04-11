plugins {
    id("io.kuberig.kotlin-conventions")
}

repositories {
    mavenCentral()

    if (project.hasProperty("stagingRepoUrl")) {
        val stagingRepoUrl = project.property("stagingRepoUrl") as String
        maven {
            url = uri(stagingRepoUrl)
        }
    }
}