import fr.brouillard.oss.gradle.plugins.JGitverPluginExtension

plugins {
    id("fr.brouillard.oss.gradle.jgitver")
}

configure<JGitverPluginExtension> {
    strategy(fr.brouillard.oss.jgitver.Strategies.MAVEN)
}

if (isCiBuild()) {
    requireProperty("sonatypeUsername")
    requireProperty("sonatypePassword")
    requireProperty("gradle.publish.key")
    requireProperty("gradle.publish.secret")
    requireProperty("signing.keyId")
    requireProperty("signing.password")
    requireProperty("signing.secretKeyRingFile")
}

group = "io.kuberig"

tasks.register("deploy") {
    group = "publishing"

    if (isCiBuild()) {
        if (isReleaseBuild()) {
            println("Running RELEASE build, publish to GitLab, Sonatype and Gradle Plugin Portal.")
            // release build
            project.subprojects.forEach {
                if (it.plugins.hasPlugin("maven-publish")) {
                    if (isCiJobTokenAvailable()) {
                        dependsOn(it.tasks.getByName("publishAllPublicationsToGitLabRepository"))
                    }
                }
            }
            dependsOn("publishToMavenCentral")

            dependsOn("closeStagingRepo")
            //dependsOn(":kuberig-dsl-generator-gradle-plugin:publishPlugins")
        } else {
            // snapshot build
            println("Running SNAPSHOT build, only publishing to GitLab repository.")


            project.subprojects.forEach {
                if (it.plugins.hasPlugin("maven-publish")) {
                    if (isCiJobTokenAvailable()) {
                        dependsOn(it.tasks.getByName("publishAllPublicationsToGitLabRepository"))
                    }
                }
            }
        }
    } else {
        project.subprojects.forEach {
            if (it.plugins.hasPlugin("maven-publish")) {
                dependsOn(it.tasks.getByName("publishAllPublicationsToLocalRepository"))
            }
        }
    }
}

fun isCiBuild(): Boolean {
    return System.getenv().getOrDefault("CI", "false") == "true"
}

fun isReleaseBuild(): Boolean {
    return isCiBuild() && !project.version.toString().endsWith("SNAPSHOT")
}

fun isCiJobTokenAvailable(): Boolean {
    return isCiBuild() && System.getenv().containsKey("CI_JOB_TOKEN")
}

fun requireProperty(propertyName: String) {
    check(project.hasProperty(propertyName)) { "$propertyName property missing." }
}
